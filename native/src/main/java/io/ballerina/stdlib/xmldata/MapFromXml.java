/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.stdlib.xmldata;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.utils.ValueUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlSequence;
import io.ballerina.stdlib.xmldata.utils.Constants;
import io.ballerina.stdlib.xmldata.utils.XmlDataUtils;

import java.util.List;
import java.util.Map;

/**
 * This class converts an XML to a Ballerina record type.
 *
 * @since 2.3.0
 */
public final class MapFromXml {

    private MapFromXml() {}

    @SuppressWarnings("unchecked")
    public static Object fromXml(BXml xml, BTypedesc type) {
        Type describingType = type.getDescribingType();
        if (describingType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            Object output;
            try {
                if (describingType.getFlags() != Constants.DEFAULT_TYPE_FLAG) {
                    String recordName = getXmlNameFromRecordAnnotation((RecordType) describingType,
                            describingType.getName());
                    String elementName = getKey(xml);
                    if (!recordName.equals(elementName)) {
                        return XmlDataUtils.getError("The record type name: " + recordName +
                                " mismatch with given XML name: " + elementName);
                    }
                    output = XmlToRecord.convertToJson(xml, true, Constants.ADD_IF_HAS_ANNOTATION,
                            type);
                } else {
                    output =  XmlToRecord.convertToJson(xml, true, Constants.SKIP_ATTRIBUTE, type);
                }
                if (output instanceof BError) {
                    return XmlDataUtils.getError("XML type mismatch with record type: " +
                            ((BError) output).getErrorMessage());
                }
                BMap<BString, Object> record = (BMap<BString, Object>) output;
                output = describingType.getFlags() == Constants.DEFAULT_TYPE_FLAG ? output
                        : record.get(record.getKeys()[0]);
                return getOutput(type, output);
            } catch (Exception e) {
                return XmlDataUtils.getError("Failed to convert xml to record type: " + e.getMessage());
            }
        } else {
            try {
                Type valueType = ((MapType) describingType).getConstrainedType();
                isValidXmlWithOutputType(xml, valueType);
                Object output = XmlToJson.toJson(xml, false, Constants.SKIP_ATTRIBUTE,
                        type.getDescribingType());
                if (valueType.getTag() == TypeTags.TABLE_TAG) {
                    TableType tableType = (TableType) valueType;
                    BMap<BString, Object> tableMap = ValueCreator.createMapValue(TypeCreator.createMapType(tableType));
                    BMap<BString, Object> resultMap = (BMap<BString, Object>) output;
                    for (Map.Entry<BString, Object> entry : resultMap.entrySet()) {
                        BTable tableValue = ValueCreator.createTableValue(tableType);
                        Type tableValueType = TypeUtils.getReferredType(((TableType) valueType).getConstrainedType());
                        if (tableValueType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                            tableValue.put(ValueUtils.convert(entry.getValue(), tableValueType));
                        } else {
                            tableValue.put(entry.getValue());
                        }
                        tableMap.put(entry.getKey(), tableValue);
                    }
                    return tableMap;
                }
                return output;
            } catch (Exception e) {
                return XmlDataUtils.getError(e.getMessage());
            }
        }
    }

    private static Object getOutput(BTypedesc type, Object output) {
        try {
            return ValueUtils.convert(output, type.getDescribingType());
        } catch (BError bError) {
            return XmlDataUtils.getError("XML type mismatch with record type: " +
                    ((Map) bError.getDetails()).get(StringUtils.fromString("message")).
                            toString());
        }
    }

    private static String getKey(BXml xml) {
        String elementKey = xml.elements().getElementName();
        int startIndex = 0;
        if (elementKey.contains("}")) {
            startIndex = elementKey.indexOf("}") + 1;
        }
        return elementKey.substring(startIndex);
    }

    private static void isValidXmlWithOutputType(BXml xml, Type type) throws Exception {
        int typeTag = type.getTag();
        if (typeTag == TypeTags.ARRAY_TAG) {
            typeTag = ((ArrayType) type).getElementType().getTag();
        }
        if (isPrimitiveType(typeTag)) {
            BXml elements = xml.elements().children();
            if (elements instanceof BXmlSequence) {
                if (isNotValidXml(((BXmlSequence) elements).getChildrenList())) {
                    throw new Exception("Failed to convert the xml:" + xml.elements().children() + " to " +
                            type  + " type.");
                }
            }
        }
    }

    private static boolean isPrimitiveType(int typeTag) {
        return typeTag == TypeTags.STRING_TAG || typeTag == TypeTags.BOOLEAN_TAG || typeTag == TypeTags.INT_TAG ||
                typeTag == TypeTags.FLOAT_TAG || typeTag == TypeTags.DECIMAL_TAG;
    }

    private static boolean isNotValidXml(List<BXml> sequence) {
        // Valid XML format: <KEY>VALUE</KEY>
        return (sequence.size() == 1 && !sequence.get(0).elements().children().isEmpty()) || sequence.size() > 1;
    }

    @SuppressWarnings("unchecked")
    public static String getXmlNameFromRecordAnnotation(RecordType record, String recordName) {
        BMap<BString, Object> annotations = record.getAnnotations();
        for (BString annotationsKey : annotations.getKeys()) {
            String key = annotationsKey.getValue();
            if (!key.contains(Constants.FIELD) && key.endsWith(Constants.NAME)) {
                return ((BMap<BString, Object>) annotations.get(annotationsKey)).get(Constants.VALUE).toString();
            }
        }
        return recordName;
    }
}
