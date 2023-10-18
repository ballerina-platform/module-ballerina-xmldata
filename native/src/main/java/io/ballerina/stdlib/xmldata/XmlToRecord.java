/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.ValueUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.stdlib.xmldata.utils.Constants;
import io.ballerina.stdlib.xmldata.utils.XmlDataUtils;

import java.util.Map;

import static io.ballerina.stdlib.xmldata.XmlToJson.toJson;

/**
 * This class converts an XML to a Ballerina record type.
 *
 * @since 2.0.2
 */
public final class XmlToRecord {

    private XmlToRecord() {}

    public static Object toRecord(BXml xml, boolean preserveNamespaces, BTypedesc type) {
        return toRecord(xml, preserveNamespaces, Constants.UNDERSCORE, type);
    }

    public static Object toRecord(BXml xml, boolean preserveNamespaces, String attributePrefix, BTypedesc type) {
        try {
            Object jsonObject = convertToJson(xml, preserveNamespaces, attributePrefix, type);
            if (jsonObject instanceof BError) {
                return XmlDataUtils.getError("XML type mismatch with record type: " +
                        ((BError) jsonObject).getErrorMessage());
            }
            return getJsonObject(type, jsonObject);
        } catch (Exception e) {
            return XmlDataUtils.getError("Failed to convert xml to record type: " + e.getMessage());
        }
    }

    private static Object getJsonObject(BTypedesc type, Object jsonObject) {
        try {
            return ValueUtils.convert(jsonObject, type.getDescribingType());
        } catch (BError bError) {
            return XmlDataUtils.getError("XML type mismatch with record type: " +
                    ((Map) bError.getDetails()).get(StringUtils.fromString("message")).toString());
        }
    }

    public static Object convertToJson(BXml xml, boolean preserveNamespaces, String attributePrefix, BTypedesc type)
            throws Exception {
        Type describingType = type.getDescribingType();
        validateRecordType(describingType);
        return toJson(xml, preserveNamespaces, attributePrefix, describingType);
    }

    private static void validateRecordType(Type type) throws Exception {
        if (type instanceof RecordType) {
            Map<String, Field> fields = ((RecordType) type).getFields();
            for (Map.Entry<String, Field> entry : fields.entrySet()) {
                Field field = entry.getValue();
                Type fieldType = field.getFieldType();
                if (field.getFieldType().isNilable()) {
                    throw new Exception("The record field: " + field.getFieldName() +
                            " does not support the optional value type: " + fieldType);
                }
            }
        } else if (type instanceof ArrayType) {
            Type fieldType = ((ArrayType) type).getElementType();
            if (fieldType instanceof RecordType) {
                validateRecordType(fieldType);
            }
        }
    }
}
