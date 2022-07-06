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

package io.ballerina.stdlib.xmldata.utils;

import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A util class for the XmlData package's native implementation.
 *
 * @since 1.1.0
 */
public class XmlDataUtils {

    private static final String ERROR = "Error";
    private static final String NAME = "Name";
    private static final String ATTRIBUTE_PREFIX = "attribute_";
    private static final String VALUE = "value";

    public static BError getError(String message) {
        return ErrorCreator.createError(ModuleUtils.getModule(), ERROR, StringUtils.fromString(message),
                null, null);
    }

    @SuppressWarnings("unchecked")
    public static Object getModifiedRecord(BMap<BString, Object> input, BTypedesc type) {
        Type describingType = type.getDescribingType();
        Object value = input.get(input.getKeys()[0]);
        if (value instanceof BArray) {
            BArray objectArray = (BArray) value;
            Type elementType = TypeUtils.getReferredType(((ArrayType) objectArray.getType()).getElementType());
            if (elementType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                BMap<BString, Object> jsonMap = ValueCreator.createMapValue(Constants.JSON_MAP_TYPE);
                for (Map.Entry<BString, Object> entry :input.entrySet()) {
                    List<BMap<BString, Object>> records = new ArrayList<>();
                    BArray arrayValue = (BArray) entry.getValue();
                    for (int i = 0; i < arrayValue.getLength(); i++) {
                        BMap<BString, Object> record = addFields(((BMap<BString, Object>) arrayValue.get(i)),
                                elementType);
                        records.add(processParentAnnotation(elementType, record));
                    }
                    jsonMap.put(entry.getKey(), ValueCreator.createArrayValue(records.toArray(),
                            TypeCreator.createArrayType(elementType)));
                }
                return jsonMap;
            }
        }
        if (describingType.getTag() == TypeTags.RECORD_TYPE_TAG &&
                describingType.getFlags() != Constants.DEFAULT_TYPE_FLAG) {
            BArray jsonArray = ValueCreator.createArrayValue(PredefinedTypes.TYPE_JSON_ARRAY);
            BMap<BString, Object> recordField =  addFields(input, type.getDescribingType());
            BMap<BString, Object> processedRecord = processParentAnnotation(type.getDescribingType(), recordField);
            BString rootTagName = processedRecord.getKeys()[0];
            jsonArray.append(processedRecord.get(rootTagName));
            jsonArray.append(rootTagName);
            return jsonArray;
        }
        return input;
    }

    @SuppressWarnings("unchecked")
    private static BMap<BString, Object> addFields(BMap<BString, Object> input, Type type) {
        BMap<BString, Object> record = ValueCreator.createMapValue(Constants.JSON_MAP_TYPE);
        Map<String, Field> fields = ((RecordType) type).getFields();
        BMap<BString, Object> annotations = ((RecordType) type).getAnnotations();
        for (Map.Entry<BString, Object> entry: input.entrySet()) {
            BString key = entry.getKey();
            Object value = entry.getValue();
            if (fields.containsKey(key.getValue())) {
                Type childType = fields.get(key.toString()).getFieldType();
                childType = getTypeFromUnionType(childType, value);
                if (childType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                    processRecord(key, annotations, record, value, childType);
                } else if (childType.getTag() == TypeTags.TYPE_REFERENCED_TYPE_TAG) {
                    Type referredType = TypeUtils.getReferredType(childType);
                    record.put(key, addFields(((BMap<BString, Object>) value), referredType));
                } else if (childType.getTag() == TypeTags.ARRAY_TAG) {
                    processArray(childType, annotations, record, entry);
                } else {
                    addPrimitiveValue(key, annotations, record, value);
                }
            } else {
                record.put(key, value);
            }
        }
        return record;
    }

    @SuppressWarnings("unchecked")
    private static void processRecord(BString key, BMap<BString, Object> annotations,
                                      BMap<BString, Object> record, Object value, Type childType) {
        BMap<BString, Object>  annotationRecord = ValueCreator.createMapValue(Constants.JSON_MAP_TYPE);
        BMap<BString, Object> annotation = ((RecordType) childType).getAnnotations();
        if (annotations.size() > 0) {
            processSubRecordAnnotation(annotations, annotationRecord);
        }
        BMap<BString, Object> subRecord = addFields(((BMap<BString, Object>) value), childType);
        if (annotation.size() > 0) {
            processSubRecordAnnotation(annotation, subRecord);
        }
        record.put(key, subRecord);
        if (annotationRecord.size() > 0) {
            record.put(annotationRecord.getKeys()[0],
                    annotationRecord.get(annotationRecord.getKeys()[0]));
        }
    }

    @SuppressWarnings("unchecked")
    private static void addPrimitiveValue(BString key, BMap<BString, Object> annotations,
                                          BMap<BString, Object> record, Object value) {
        BString annotationKey =
                StringUtils.fromString((Constants.FIELD + key).replace(":", "\\:"));
        if (annotations.containsKey(annotationKey)) {
            BMap<BString, Object> annotationValue = (BMap<BString, Object>) annotations.get(annotationKey);
            BString keyValue = processFieldAnnotation(annotationValue, key.getValue());
            record.put(keyValue, value);
        } else {
            record.put(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    private static void processArray(Type childType, BMap<BString, Object> annotations,
                                     BMap<BString, Object> record, Map.Entry<BString, Object> entry) {
        Type elementType = TypeUtils.getReferredType(((ArrayType) childType).getElementType());
        BMap<BString, Object>  annotationRecord = ValueCreator.createMapValue(Constants.JSON_MAP_TYPE);
        if (annotations.size() > 0) {
            processSubRecordAnnotation(annotations, annotationRecord);
        }
        BArray arrayValue = (BArray) entry.getValue();
        if (elementType.getTag() == TypeTags.RECORD_TYPE_TAG) {
            List<BMap<BString, Object>> records = new ArrayList<>();
            for (int i = 0; i < arrayValue.getLength(); i++) {
                BMap<BString, Object> subRecord = addFields(((BMap<BString, Object>) arrayValue.get(i)),
                        elementType);
                subRecord = processParentAnnotation(elementType, subRecord);
                records.add((BMap<BString, Object>) subRecord.get(subRecord.getKeys()[0]));
            }
            record.put(entry.getKey(), ValueCreator.createArrayValue(records.toArray(),
                    TypeCreator.createArrayType(Constants.JSON_ARRAY_TYPE)));
        } else {
            List<Object> records = new ArrayList<>();
            for (int i = 0; i < arrayValue.getLength(); i++) {
                records.add(arrayValue.get(i));
            }
            record.put(entry.getKey(), ValueCreator.createArrayValue(records.toArray(),
                    TypeCreator.createArrayType(Constants.JSON_ARRAY_TYPE)));
        }
        if (annotationRecord.size() > 0) {
            record.put(annotationRecord.getKeys()[0],
                    annotationRecord.get(annotationRecord.getKeys()[0]));
        }
    }

    public static Type getTypeFromUnionType(Type childType, Object value) {
        if (childType instanceof UnionType) {
            UnionType bUnionType = ((UnionType) childType);
            for (Type memberType : bUnionType.getMemberTypes()) {
                if (value.getClass().getName().toUpperCase(Locale.ROOT).contains(
                        memberType.getName().toUpperCase(Locale.ROOT))) {
                    childType = memberType;
                }
            }
        }
        return childType;
    }

    private static BMap<BString, Object> processParentAnnotation(Type type, BMap<BString, Object> record) {
        BMap<BString, Object> parentRecord = ValueCreator.createMapValue(Constants.JSON_MAP_TYPE);
        BMap<BString, Object> namespaces = ValueCreator.createMapValue(Constants.JSON_MAP_TYPE);
        BMap<BString, Object> annotations = ((RecordType) type).getAnnotations();
        BString rootName = processAnnotation(annotations, type.getName(), namespaces);
        if (namespaces.size() > 0) {
            for (Map.Entry<BString, Object> namespace : namespaces.entrySet()) {
                record.put(namespace.getKey(), namespace.getValue());
            }
        }
        parentRecord.put(rootName, record);
        return parentRecord;
    }

    @SuppressWarnings("unchecked")
    private static BString processFieldAnnotation(BMap<BString, Object> annotation, String key) {
        StringBuilder keyBuilder = new StringBuilder(key);
        for (BString value : annotation.getKeys()) {
            String stringValue = value.getValue();
            if (stringValue.endsWith(NAME)) {
                BMap<BString, Object> names = (BMap<BString, Object>) annotation.get(value);
                String name = names.get(StringUtils.fromString(VALUE)).toString();
                if (keyBuilder.toString().contains(":")) {
                    keyBuilder = new StringBuilder(keyBuilder.substring(0, keyBuilder.indexOf(":") + 1) + name);
                } else if (keyBuilder.toString().contains(ATTRIBUTE_PREFIX)) {
                    keyBuilder = new StringBuilder(keyBuilder.substring(0, keyBuilder.indexOf("_") + 1) + name);
                } else {
                    keyBuilder = new StringBuilder(((BMap<BString, Object>) annotation.get(value)).
                            get(StringUtils.fromString(VALUE)).toString());
                }
            }
            if (stringValue.endsWith(Constants.NAME_SPACE)) {
                BMap<BString, Object> namespaceAnnotation = (BMap<BString, Object>) annotation.get(value);
                if (keyBuilder.toString().contains(ATTRIBUTE_PREFIX)) {
                    keyBuilder = new StringBuilder(keyBuilder.substring(0, keyBuilder.indexOf("_") + 1) +
                            namespaceAnnotation.get(StringUtils.
                            fromString(Constants.PREFIX)) + ":" + keyBuilder.substring(keyBuilder.indexOf("_") + 1));
                } else {
                    keyBuilder.insert(0, namespaceAnnotation.get(
                            StringUtils.fromString(Constants.PREFIX)) + ":");
                }
            }
            if (stringValue.endsWith(Constants.ATTRIBUTE)) {
                keyBuilder.insert(0, ATTRIBUTE_PREFIX);
            }
        }
        return StringUtils.fromString(keyBuilder.toString());
    }

    @SuppressWarnings("unchecked")
    private static BString processAnnotation(BMap<BString, Object> annotation, String key,
                                        BMap<BString, Object> namespaces) {
        StringBuilder keyBuilder = new StringBuilder(key);
        for (BString value : annotation.getKeys()) {
            if (!value.getValue().contains(Constants.FIELD)) {
                String stringValue = value.getValue();
                if (stringValue.endsWith(NAME)) {
                    String nameValue = ((BMap<BString, Object>) annotation.get(value)).
                            get(StringUtils.fromString(VALUE)).toString();
                    if (keyBuilder.toString().contains(":")) {
                        keyBuilder = new StringBuilder(keyBuilder.substring(0,
                                keyBuilder.indexOf(":") + 1) + nameValue);
                    } else {
                        keyBuilder = new StringBuilder(nameValue);
                    }
                }
                if (stringValue.endsWith(Constants.NAME_SPACE)) {
                    BMap<BString, Object> namespaceAnnotation = (BMap<BString, Object>) annotation.get(value);
                    BString uri = (BString) namespaceAnnotation.get(StringUtils.fromString(Constants.URI));
                    BString prefix = (BString) namespaceAnnotation.get(StringUtils.fromString(Constants.PREFIX));
                    if (prefix ==  null) {
                        namespaces.put(StringUtils.fromString(ATTRIBUTE_PREFIX + "xmlns"), uri);
                    } else {
                        namespaces.put(StringUtils.fromString(ATTRIBUTE_PREFIX + "xmlns:" + prefix), uri);
                        keyBuilder.insert(0, namespaceAnnotation.get(
                                StringUtils.fromString(Constants.PREFIX)) + ":");
                    }
                }
            }
        }
        return StringUtils.fromString(keyBuilder.toString());
    }

    @SuppressWarnings("unchecked")
    private static void processSubRecordAnnotation(BMap<BString, Object> annotation, BMap<BString, Object>  subRecord) {
        BString[] keys = annotation.getKeys();
        for (BString value :keys) {
            if (value.getValue().endsWith(Constants.NAME_SPACE)) {
                BMap<BString, Object> namespaceAnnotation = (BMap<BString, Object>) annotation.get(value);
                BString uri = (BString) namespaceAnnotation.get(StringUtils.fromString(Constants.URI));
                BString prefix = (BString) namespaceAnnotation.get(StringUtils.fromString(Constants.PREFIX));
                if (prefix == null) {
                    subRecord.put(StringUtils.fromString(ATTRIBUTE_PREFIX + "xmlns"), uri);
                } else {
                    subRecord.put(StringUtils.fromString(ATTRIBUTE_PREFIX + "xmlns:" + prefix), uri);
                }
            }
        }
    }
}
