/*
 *  Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.stdlib.xmldata.json;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.JsonUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BListInitialValueEntry;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.util.HashMap;
import java.util.Iterator;

import static io.ballerina.stdlib.xmldata.json.JsonParser.StateMachine.FIRST_ARRAY_ELEMENT_READY_STATE;
import static io.ballerina.stdlib.xmldata.json.JsonParser.StateMachine.FIRST_FIELD_READY_STATE;

/**
 * Create objects for partially parsed json.
 *
 * @since 3.0.0
 */
public class JsonCreator {

    // convert json[] to output array
    static BArray finalizeArray(JsonParser.StateMachine sm, Type arrType, BArray currArr)
            throws JsonParser.JsonParserException {
        int arrTypeTag = arrType.getTag();
        BListInitialValueEntry[] initialValues = new BListInitialValueEntry[currArr.size()];
        for (int i = 0; i < currArr.size(); i++) {
            Object curElement = currArr.get(i);
            Type currElmType = TypeUtils.getType(curElement);
            if (currElmType.getTag() == TypeTags.ARRAY_TAG) {
                if (arrTypeTag == TypeTags.ARRAY_TAG) {
                    curElement = finalizeArray(sm, ((ArrayType) arrType).getElementType(), (BArray) curElement);
                } else if (arrTypeTag == TypeTags.TUPLE_TAG) {
                    curElement = finalizeArray(sm, ((TupleType) arrType).getTupleTypes().get(i), (BArray) curElement);
                } else {
                    throw new JsonParser.JsonParserException("invalid type in field " + getCurrentFieldPath(sm));
                }
            } else {
                if (arrTypeTag == TypeTags.ARRAY_TAG) {
                    curElement = JsonCreator.convertJSON(sm, curElement, ((ArrayType) arrType).getElementType());
                } else if (arrTypeTag == TypeTags.TUPLE_TAG) {
                    curElement = JsonCreator.convertJSON(sm, curElement, ((TupleType) arrType).getTupleTypes().get(i));
                } else {
                    throw new JsonParser.JsonParserException("invalid type in field " + getCurrentFieldPath(sm));
                }
            }

            initialValues[i] = ValueCreator.createListInitialValueEntry(curElement);
        }

        if (arrTypeTag == TypeTags.ARRAY_TAG) {
            return ValueCreator.createArrayValue((ArrayType) arrType, initialValues);
        } else if (arrTypeTag == TypeTags.TUPLE_TAG) {
            return ValueCreator.createTupleValue((TupleType) arrType, initialValues);
        } else {
            throw new JsonParser.JsonParserException("invalid type in field " + getCurrentFieldPath(sm));
        }
    }

    static JsonParser.StateMachine.State initRootObject(JsonParser.StateMachine sm)
            throws JsonParser.JsonParserException {
        if (sm.rootRecord == null) {
            throw new JsonParser.JsonParserException("expected record type for input type");
        }
        sm.currentJsonNode = ValueCreator.createRecordValue(sm.rootRecord);
        return FIRST_FIELD_READY_STATE;
    }

    static JsonParser.StateMachine.State initRootArray(JsonParser.StateMachine sm)
            throws JsonParser.JsonParserException {
        if (sm.rootArray == null) {
            throw new JsonParser.JsonParserException("expected array type for input type");
        }
        sm.currentJsonNode = ValueCreator.createArrayValue(sm.definedJsonArrayType);
        return FIRST_ARRAY_ELEMENT_READY_STATE;
    }

    static JsonParser.StateMachine.State initNewObject(JsonParser.StateMachine sm)
            throws JsonParser.JsonParserException {
        Type currentType = TypeUtils.getReferredType(sm.currentField.getFieldType());
        if (sm.currentJsonNode != null) {
            sm.nodesStack.push(sm.currentJsonNode);
        }
        if (currentType.getTag() == TypeTags.JSON_TAG) {
            sm.currentJsonNode = ValueCreator.createMapValue();
            sm.fieldHierarchy.push(new HashMap<>());
            sm.restType.push(sm.definedJsonType);
            sm.jsonFieldMode = true;
        } else if (currentType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            RecordType recordType = (RecordType) currentType;
            sm.fieldHierarchy.push(recordType.getFields());
            sm.restType.push(recordType.getRestFieldType());
            sm.currentJsonNode = ValueCreator.createRecordValue(recordType);
        } else {
            throw new JsonParser.JsonParserException("invalid type in field " + getCurrentFieldPath(sm));
        }
        return FIRST_FIELD_READY_STATE;
    }

    static JsonParser.StateMachine.State initNewArray(JsonParser.StateMachine sm) {
        if (sm.currentJsonNode != null) {
            sm.nodesStack.push(sm.currentJsonNode);
        }

        sm.currentJsonNode = ValueCreator.createArrayValue(sm.definedJsonArrayType);

        return FIRST_ARRAY_ELEMENT_READY_STATE;
    }

    static void setValueToJsonType(JsonParser.StateMachine sm, JsonParser.StateMachine.ValueType type, Object value,
                                   Type currentType) throws JsonParser.JsonParserException {
        Object convertedVal;
        try {
            convertedVal = type.equals(JsonParser.StateMachine.ValueType.ARRAY_ELEMENT) ?
                    value : convertJSON(sm, value, currentType);
        } catch (BError e) {
            if (sm.currentField != null) {
                throw new JsonParser.JsonParserException("incompatible value '" + value + "' for type '" +
                        sm.currentField.getFieldType() + "' in field '" + getCurrentFieldPath(sm) + "'");
            }
            // ignore this element in projection
            return;
        }
        switch (type) {
            case ARRAY_ELEMENT:
                ((BArray) sm.currentJsonNode).append(convertedVal);
                break;
            case FIELD:
                ((BMap<BString, Object>) sm.currentJsonNode).put(
                        StringUtils.fromString(sm.fieldNames.pop()), convertedVal);
                break;
            default:
                sm.currentJsonNode = convertedVal;
                break;
        }
    }

    static Object convertJSON(JsonParser.StateMachine sm, Object value, Type type)
            throws JsonParser.JsonParserException {
        // all types are currently allowed for readonly type fields
        if (type.getTag() == TypeTags.READONLY_TAG) {
            return value;
        }
        try {
            return JsonUtils.convertJSON(value, type);
        } catch (Exception e) {
            throw new JsonParser.JsonParserException("incompatible value '" + value + "' for type '" +
                    type + "' in field '" + getCurrentFieldPath(sm) + "'");
        }
    }

    private static String getCurrentFieldPath(JsonParser.StateMachine sm) {
        Iterator<String> itr = sm.fieldNames.descendingIterator();

        StringBuilder result = new StringBuilder(itr.hasNext() ? itr.next() : "");
        while (itr.hasNext()) {
            result.append(".").append(itr.next());
        }
        return result.toString();
    }

    static boolean isNegativeZero(String str) {
        return '-' == str.charAt(0) && 0 == Double.parseDouble(str);
    }

}
