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
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.utils.JsonUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlItem;
import io.ballerina.runtime.api.values.BXmlSequence;
import io.ballerina.stdlib.xmldata.utils.Constants;
import io.ballerina.stdlib.xmldata.utils.XmlDataUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import static io.ballerina.runtime.api.utils.StringUtils.fromString;

/**
 * This class work as a bridge with ballerina and a Java implementation of ballerina/xmldata modules.
 *
 * @since 1.1.0
 */
public class XmlToJson {

    private static final ArrayType STRING_ARRAY_TYPE = TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING);
    private static final ArrayType DECIMAL_ARRAY_TYPE = TypeCreator.createArrayType(PredefinedTypes.TYPE_DECIMAL);
    private static final ArrayType BOOLEAN_ARRAY_TYPE = TypeCreator.createArrayType(PredefinedTypes.TYPE_BOOLEAN);
    private static final ArrayType FLOAT_ARRAY_TYPE = TypeCreator.createArrayType(PredefinedTypes.TYPE_FLOAT);
    private static final ArrayType INT_ARRAY_TYPE = TypeCreator.createArrayType(PredefinedTypes.TYPE_INT);
    private static final String XMLNS = "xmlns";
    private static final String DOUBLE_QUOTES = "\"";
    private static final String CONTENT = "#content";
    private static final String EMPTY_STRING = "";
    public static final int NS_PREFIX_BEGIN_INDEX = BXmlItem.XMLNS_NS_URI_PREFIX.length();
    private static final String COLON = ":";

    /**
     * Converts an XML to the corresponding JSON representation.
     *
     * @param xml    XML record object
     * @param options option details
     * @return JSON object that construct from XML
     */
    public static Object toJson(BXml xml, BMap<?, ?> options) {
        try {
            String attributePrefix = ((BString) options.get(StringUtils.fromString(Constants.OPTIONS_ATTRIBUTE_PREFIX)))
                    .getValue();
            boolean preserveNamespaces = ((Boolean) options.get(StringUtils.fromString(Constants.OPTIONS_PRESERVE_NS)));
            return convertToJSON(xml, attributePrefix, preserveNamespaces, null, null);
        } catch (Exception e) {
            return XmlDataUtils.getError(e.getMessage());
        }
    }

    public static Object toJson(BXml xml, boolean preserveNamespaces, String attributePrefix, Type type) {
        try {
            return convertToJSON(xml, attributePrefix, preserveNamespaces, type, null);
        } catch (Exception e) {
            return XmlDataUtils.getError(e.getMessage());
        }
    }

    /**
     * Converts given xml object to the corresponding JSON value.
     *
     * @param xml                XML object to get the corresponding json
     * @param attributePrefix    Prefix to use in attributes
     * @param preserveNamespaces preserve the namespaces when converting
     * @return JSON representation of the given xml object
     */
    public static Object convertToJSON(BXml xml, String attributePrefix, boolean preserveNamespaces, Type type,
                                       BMap<BString, BString> parentAttributeMap) throws Exception {
        if (type instanceof MapType) {
            MapType mapType = (MapType) type;
            if (mapType.getConstrainedType().getTag() == TypeTags.XML_TAG) {
                BMap<BString, Object> map = createMapValue(type);
                map.put(fromString(CONTENT), xml);
                return map;
            }
        }
        if (xml instanceof BXmlItem) {
            return convertElement((BXmlItem) xml, attributePrefix, preserveNamespaces, type, parentAttributeMap);
        } else if (xml instanceof BXmlSequence) {
            BXmlSequence xmlSequence = (BXmlSequence) xml;
            if (xmlSequence.isEmpty()) {
                return StringUtils.fromString(EMPTY_STRING);
            }
            Object seq = convertBXmlSequence(xmlSequence, attributePrefix, preserveNamespaces, type,
                    parentAttributeMap);
            if (seq == null) {
                return createNewJsonList();
            }
            return seq;
        } else if (xml.getNodeType().equals(XmlNodeType.TEXT)) {
            return convertValue(xml, type);
        } else {
            return createMapValue(type);
        }
    }

    private static Object convertValue(BXml xml, Type type) throws Exception {
        if (type != null) {
            if (type.getTag() == TypeTags.ARRAY_TAG) {
                return convertToArray(type, xml);
            } else if (type.getTag() == TypeTags.MAP_TAG) {
                switch (((MapType) type).getConstrainedType().getTag()) {
                    case TypeTags.INT_TAG:
                        return Long.parseLong(xml.toString());
                    case TypeTags.BOOLEAN_TAG:
                        return Boolean.parseBoolean(xml.toString());
                    case TypeTags.DECIMAL_TAG:
                        return ValueCreator.createDecimalValue(BigDecimal.valueOf(Double.parseDouble(xml.toString())));
                    case TypeTags.FLOAT_TAG:
                        return Double.parseDouble(xml.toString());
                }
            }
        }
        return JsonUtils.parse(DOUBLE_QUOTES + xml.stringValue(null).replace(DOUBLE_QUOTES,
                "\\\"") + DOUBLE_QUOTES);
    }

    /**
     * Converts given xml object to the corresponding json.
     *
     * @param xmlItem XML element to traverse
     * @param attributePrefix Prefix to use in attributes
     * @param preserveNamespaces preserve the namespaces when converting
     * @return ObjectNode Json object node corresponding to the given xml element
     */
    @SuppressWarnings("unchecked")
    private static Object convertElement(BXmlItem xmlItem, String attributePrefix,
                                         boolean preserveNamespaces, Type type,
                                         BMap<BString, BString> parentAttributeMap) throws Exception {
        BMap<BString, Object> childrenData = createMapValue(type);
        BMap<BString, BString> attributeMap = xmlItem.getAttributesMap();
        String keyValue = getElementKey(xmlItem, preserveNamespaces);
        Type fieldType = getFieldType(keyValue, type);
        processAttributeWithAnnotation(xmlItem, attributePrefix, preserveNamespaces, childrenData, fieldType,
                attributeMap, parentAttributeMap);
        Object children = convertBXmlSequence(xmlItem.getChildrenSeq(), attributePrefix, preserveNamespaces,
                fieldType,  attributeMap);
        BMap<BString, Object> rootNode = createMapValue(type);
        if (type != null && fieldType instanceof ArrayType && children instanceof BMap &&
                TypeUtils.getReferredType(((ArrayType) fieldType).getElementType()) instanceof RecordType) {
            for (Map.Entry<BString, Object> entry: childrenData.entrySet()) {
                ((BMap<BString, Object>) children).put(entry.getKey(), entry.getValue());
            }
            children = convertToArray(fieldType, children);
        }
        return insertDataToMap(childrenData, children, rootNode,  keyValue, fieldType);
    }

    private static void processAttributeWithAnnotation(BXmlItem xmlItem, String attributePrefix,
                                                        boolean preserveNamespaces, BMap<BString, Object> childrenData,
                                                        Type fieldType, BMap<BString, BString> attributeMap,
                                                        BMap<BString, BString> parentAttributeMap) throws Exception {
        if (!attributePrefix.equals(Constants.SKIP_ATTRIBUTE)) {
            BMap<BString, Object> annotations = null;
            if (attributePrefix.equals(Constants.ADD_IF_HAS_ANNOTATION))  {
                Type annotationType = fieldType;
                if (fieldType.getTag() == TypeTags.ARRAY_TAG) {
                    annotationType = TypeUtils.getReferredType(((ArrayType) fieldType).getElementType());
                } else if (fieldType.getTag() == TypeTags.TYPE_REFERENCED_TYPE_TAG) {
                    annotationType = TypeUtils.getReferredType(fieldType);;
                } else if (fieldType instanceof UnionType) {
                    UnionType bUnionType = (UnionType) fieldType;
                    for (Type memberType : bUnionType.getMemberTypes()) {
                        Type referMemberType = TypeUtils.getReferredType(memberType);
                        if (referMemberType.getTag() == TypeTags.RECORD_TYPE_TAG)  {
                            annotationType = referMemberType;
                        }
                    }
                }
                if (annotationType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                    annotations = ((RecordType) annotationType).getAnnotations();
                }
            }
            processAttributes(attributeMap, attributePrefix, childrenData, fieldType,
                    parentAttributeMap, xmlItem.getQName().getPrefix(), preserveNamespaces, annotations);
        }
    }

    @SuppressWarnings("unchecked")
    private static BMap<BString, Object> insertDataToMap(BMap<BString, Object> childrenData, Object children,
                                                         BMap<BString, Object> rootNode, String keyValue,
                                                         Type fieldType) throws Exception {
        if (childrenData.size() > 0) {
            if (children instanceof BMap) {
                BMap<BString, Object> data = (BMap<BString, Object>) children;
                for (Map.Entry<BString, Object> entry: childrenData.entrySet()) {
                    data.put(entry.getKey(), entry.getValue());
                }
                put(rootNode, keyValue, data);
            }  else if (children == null) {
                put(rootNode, keyValue, childrenData);
            } else if (children instanceof BArray) {
                put(rootNode, keyValue, children);
            } else if (children instanceof BString) {
                putAsFieldTypes(childrenData, CONTENT, children.toString().trim(), fieldType);
                put(rootNode, keyValue, childrenData);
                return rootNode;
            } else {
                put(rootNode, keyValue, children);
            }
        } else {
            if (children instanceof BMap) {
                put(rootNode, keyValue, children);
            } else if (children == null) {
                putAsFieldTypes(rootNode, keyValue, EMPTY_STRING, fieldType);
            } else if (children instanceof BArray) {
                put(rootNode, keyValue, children);
            } else if (children instanceof BString) {
                putAsFieldTypes(rootNode, keyValue, children.toString().trim(), fieldType);
            } else {
                put(rootNode, keyValue, children);
            }
        }
        return rootNode;
    }

    private static Type getFieldType(String fieldName, Type type) {
        if (type != null) {
            if (type.getTag() == TypeTags.RECORD_TYPE_TAG) {
                return getRecordFieldType(type, fieldName);
            } else if (type.getTag() == TypeTags.ARRAY_TAG) {
                Type fieldType = TypeUtils.getReferredType(((ArrayType) type).getElementType());
                if (fieldType instanceof RecordType) {
                    return getRecordFieldType(fieldType, fieldName);
                }
                return fieldType;
            } else if (type.getTag() == TypeTags.TYPE_REFERENCED_TYPE_TAG) {
                Type referredType = TypeUtils.getReferredType(type);
                if (referredType.getTag() == TypeTags.RECORD_TYPE_TAG) {
                    return getRecordFieldType(referredType, fieldName);
                }
            } else if (type.getTag() == TypeTags.MAP_TAG) {
                Type valueType = ((MapType) type).getConstrainedType();
                if (valueType.getTag() == TypeTags.TABLE_TAG) {
                    return ((TableType) valueType).getConstrainedType();
                }
                return valueType;
            }
        }
        return type;
    }

    private static Type getRecordFieldType(Type type, String fieldName) {
        RecordType recordType = (RecordType) type;
        Map<String, Field> fields = recordType.getFields();
        if (fields.get(fieldName) != null) {
            return fields.get(fieldName).getFieldType();
        }
        return type;
    }

    private static void processAttributes(BMap<BString, BString> attributeMap, String attributePrefix,
                                          BMap<BString, Object> mapData, Type type,
                                          BMap<BString, BString> parentAttributeMap, String prefix,
                                          boolean preserveNamespaces, BMap<BString, Object> annotations)
            throws Exception {
        Map<String, String> nsPrefixMap = getNamespacePrefixes(attributeMap);
        if (prefix != null && preserveNamespaces && parentAttributeMap != null) {
            for (Map.Entry<BString, BString> entry : attributeMap.entrySet()) {
                BString value = entry.getValue();
                if (!isNamespacePrefixEntry(entry) ||
                        !isBelongingToElement(parentAttributeMap, entry.getKey(), value)) {
                    String key = getAttributeKey(attributePrefix, getKey(entry, nsPrefixMap, preserveNamespaces));
                    checkAnnotationAndAddAttributes(annotations, mapData, getFieldType(key, type), key,
                            value.getValue(), attributePrefix);
                }
            }
        } else {
            for (Map.Entry<BString, BString> entry : attributeMap.entrySet()) {
                String key = getKey(entry, nsPrefixMap, preserveNamespaces);
                if (key != null) {
                    key = getAttributeKey(attributePrefix, key);
                    checkAnnotationAndAddAttributes(annotations, mapData, getFieldType(key, type), key,
                            entry.getValue().getValue(), attributePrefix);
                }
            }
        }
    }

    private static String getAttributeKey(String attributePrefix , String key) {
        if (attributePrefix.equals(Constants.ADD_IF_HAS_ANNOTATION)) {
            return key;
        } else {
            return attributePrefix + key;
        }
    }

    @SuppressWarnings("unchecked")
    private static void checkAnnotationAndAddAttributes(BMap<BString, Object> annotations,
                                                        BMap<BString, Object> mapData, Type type, String key,
                                                        String value, String attributePrefix) throws Exception {
        if (!attributePrefix.equals(Constants.ADD_IF_HAS_ANNOTATION)) {
            putAsFieldTypes(mapData, key, value, type);
        } else if (annotations.size() > 0) {
            BString annotationKey = StringUtils.fromString((Constants.FIELD + key).replace(":",
                    "\\:"));
            for (BString annotationsKey : annotations.getKeys()) {
                if (annotationsKey.getValue().contains(Constants.FIELD) &&
                        annotationsKey.getValue().equals(annotationKey.getValue())) {
                    BMap<BString, Object> annotationsForField = (BMap<BString, Object>) annotations.get(annotationsKey);
                    for (BString annotationForField : annotationsForField.getKeys()) {
                        if (annotationForField.getValue().endsWith(Constants.ATTRIBUTE)) {
                            putAsFieldTypes(mapData, key, value, getFieldType(key, type));
                            break;
                        }
                    }
                } else if (!annotationsKey.getValue().contains(Constants.FIELD) &&
                        annotationsKey.getValue().endsWith(Constants.NAME_SPACE)) {
                    BMap<BString, Object> namespaceAnnotation = (BMap<BString, Object>) annotations.get(annotationsKey);
                    BString prefix = (BString) namespaceAnnotation.get(StringUtils.fromString(Constants.PREFIX));
                    if ((prefix == null && key.equals("xmlns")) || (prefix != null && prefix.getValue().equals(key))) {
                        break;
                    }
                }
            }
        }
    }

    private static boolean isBelongingToElement(BMap<BString, BString> parentAttributeMap, BString key,
                                                       BString value) {
        return parentAttributeMap.containsKey(key) && parentAttributeMap.get(key).getValue().equals(value.getValue());
    }

    private static void putAsFieldTypes(BMap<BString, Object> map, String key, String value, Type type)
            throws Exception {
        if (type != null) {
            if (type instanceof ArrayType) {
                Type fieldType = TypeUtils.getReferredType(((ArrayType) type).getElementType());
                if (fieldType instanceof RecordType) {
                    if (((RecordType) fieldType).getFields().get(key) != null) {
                        type = ((RecordType) fieldType).getFields().get(key).getFieldType();
                    }
                }
            }
            if (type instanceof UnionType) {
                UnionType bUnionType = (UnionType) type;
                boolean isSuccessfullyCast = false;
                for (Type memberType : bUnionType.getMemberTypes()) {
                    try {
                        convertToRecordType(map, memberType, key, value);
                        isSuccessfullyCast = true;
                        break;
                    } catch (NumberFormatException e) {
                        // Ignored
                    }
                }
                if (!isSuccessfullyCast) {
                    throw new Exception("Couldn't convert value: " + value + " to " + bUnionType);
                }
            } else {
                convertToRecordType(map, type, key, value);
            }
        } else {
            map.put(fromString(key), fromString(value));
        }
    }

    private static void put(BMap<BString, Object> map, String key, Object value) {
        map.put(fromString(key), value);
    }

    private static void convertToRecordType(BMap<BString, Object> map, Type valueType, String key, String value)
            throws Exception {
        try {
            switch (valueType.getTag()) {
                case TypeTags.INT_TAG:
                    map.put(fromString(key), Long.parseLong(value));
                    break;
                case TypeTags.FLOAT_TAG:
                    map.put(fromString(key), Double.parseDouble(value));
                    break;
                case TypeTags.DECIMAL_TAG:
                    map.put(fromString(key), ValueCreator.createDecimalValue(
                            BigDecimal.valueOf(Double.parseDouble(value))));
                    break;
                case TypeTags.BOOLEAN_TAG:
                    map.put(fromString(key), Boolean.parseBoolean(value));
                    break;
                case TypeTags.ARRAY_TAG:
                    BArray array = convertToArray(valueType, value);
                    map.put(fromString(key), array);
                    break;
                case TypeTags.STRING_TAG:
                default:
                    map.put(fromString(key), fromString(value));
                    break;
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Error occurred when converting value: " + value + " to " + valueType);
        } catch (Exception e) {
            throw new Exception("Error occurred when converting value. " + e.getMessage());
        }
    }

    private static BArray convertToArray(Type valueType, Object value) throws Exception {
        Type elementType = TypeUtils.getReferredType(((ArrayType) valueType).getElementType());
        BArray arr;
        String valueString = value.toString();
        try {
            switch (elementType.getTag()) {
                case TypeTags.INT_TAG:
                    arr = ValueCreator.createArrayValue(INT_ARRAY_TYPE);
                    if (!valueString.isEmpty()) {
                        arr.append(Long.parseLong(valueString));
                    }
                    return arr;
                case TypeTags.FLOAT_TAG:
                    arr = ValueCreator.createArrayValue(FLOAT_ARRAY_TYPE);
                    if (!valueString.isEmpty()) {
                        arr.append(Double.parseDouble(valueString));
                    }
                    return arr;
                case TypeTags.DECIMAL_TAG:
                    arr = ValueCreator.createArrayValue(DECIMAL_ARRAY_TYPE);
                    if (!valueString.isEmpty()) {
                        arr.append(ValueCreator.createDecimalValue(
                                BigDecimal.valueOf(Double.parseDouble(valueString))));
                    }
                    return arr;
                case TypeTags.BOOLEAN_TAG:
                    arr = ValueCreator.createArrayValue(BOOLEAN_ARRAY_TYPE);
                    if (!valueString.isEmpty()) {
                        arr.append(Boolean.parseBoolean(valueString));
                    }
                    return arr;
                case TypeTags.STRING_TAG:
                    arr = ValueCreator.createArrayValue(STRING_ARRAY_TYPE);
                    if (!valueString.isEmpty()) {
                        arr.append(fromString(valueString));
                    }
                    return arr;
                default:
                    arr = createNewJsonList();
                    if (!valueString.isEmpty()) {
                        arr.append(value);
                    }
                    return arr;
            }
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Error occurred when converting value:" + value + " to " + valueType);
        } catch (Exception e) {
            throw new Exception("Error occurred when converting value:" + e.getMessage());
        }
    }

    /**
     * Converts given xml sequence to the corresponding json.
     *
     * @param xmlSequence XML sequence to traverse
     * @param attributePrefix Prefix to use in attributes
     * @param preserveNamespaces preserve the namespaces when converting
     * @return JsonNode Json node corresponding to the given xml sequence
     */
    private static Object convertBXmlSequence(BXmlSequence xmlSequence, String attributePrefix,
                                              boolean preserveNamespaces, Type type,
                                              BMap<BString, BString> parentAttributeMap) throws Exception {
        List<BXml> sequence = xmlSequence.getChildrenList();
        List<BXml> newSequence = new ArrayList<>();
        for (BXml value: sequence) {
            String textValue = value.toString();
            if (textValue.isEmpty() || !textValue.trim().isEmpty()) {
                newSequence.add(value);
            }
        }
        if (newSequence.isEmpty()) {
            return null;
        }
        if (type != null && type.getTag() == TypeTags.XML_TAG) {
            if (newSequence.size() == 1) {
                return newSequence.get(0);
            }
            return xmlSequence.elements();
        }
        return convertHeterogeneousSequence(attributePrefix, preserveNamespaces, newSequence, type,
                parentAttributeMap);
    }

    private static Object convertHeterogeneousSequence(String attributePrefix, boolean preserveNamespaces,
                                                       List<BXml> sequence, Type type,
                                                       BMap<BString, BString> parentAttributeMap) throws Exception {
        if (sequence.size() == 1) {
            return convertToJSON(sequence.get(0), attributePrefix, preserveNamespaces, type, parentAttributeMap);
        }
        BMap<BString, Object> mapJson = createMapValue(type);
        for (BXml bxml : sequence) {
            if (isCommentOrPi(bxml)) {
                continue;
            } else if (bxml.getNodeType() == XmlNodeType.TEXT) {
                if (mapJson.containsKey(fromString(CONTENT))) {
                    if (mapJson.get(fromString(CONTENT)) instanceof BString) {
                        BArray jsonList = createNewJsonList();
                        jsonList.append(mapJson.get(fromString(CONTENT)));
                        jsonList.append(fromString(bxml.toString().trim()));
                        mapJson.put(fromString(CONTENT), jsonList);
                    } else {
                        BArray jsonList = mapJson.getArrayValue(fromString(CONTENT));
                        jsonList.append(fromString(bxml.toString().trim()));
                        mapJson.put(fromString(CONTENT), jsonList);
                    }
                } else {
                    mapJson.put(fromString(CONTENT), fromString(bxml.toString().trim()));
                }
            } else {
                BString elementName = fromString(getElementKey((BXmlItem) bxml, preserveNamespaces));
                Object result = convertToJSON(bxml, attributePrefix, preserveNamespaces, type, parentAttributeMap);
                result = validateResult(result, elementName);
                Object value = mapJson.get(elementName);
                if (value == null) {
                    mapJson.put(elementName, result);
                } else if (value instanceof BArray) {
                    if (result instanceof BArray) {
                        BArray array = (BArray) result;
                        if (!array.isEmpty()) {
                            ((BArray) value).append(array.get(0));
                        }
                    } else {
                        ((BArray) value).append(result);
                    }
                    mapJson.put(elementName, value);
                } else {
                    BArray arr;
                    if (value instanceof Long) {
                        arr = ValueCreator.createArrayValue(INT_ARRAY_TYPE);
                    } else if (value instanceof Boolean) {
                        arr = ValueCreator.createArrayValue(BOOLEAN_ARRAY_TYPE);
                    } else if (value instanceof Double) {
                        arr = ValueCreator.createArrayValue(FLOAT_ARRAY_TYPE);
                    } else if (value.getClass().getCanonicalName().contains("DecimalValue")) {
                        arr = ValueCreator.createArrayValue(DECIMAL_ARRAY_TYPE);
                    } else if (value instanceof BString) {
                        arr = ValueCreator.createArrayValue(STRING_ARRAY_TYPE);
                    } else {
                        arr = createNewJsonList();
                    }
                    arr.append(value);
                    arr.append(result);
                    mapJson.put(elementName, arr);
                }
            }
        }
        return mapJson;
    }

    private static Object validateResult(Object result, BString elementName) {
        Object validateResult;
        if (result == null) {
            validateResult = fromString(EMPTY_STRING);
        } else if (result instanceof BMap && ((BMap<?, ?>) result).get(elementName) != null) {
            validateResult = ((BMap<?, ?>) result).get(elementName);
        } else {
            validateResult = result;
        }
        return validateResult;
    }

    private static boolean isCommentOrPi(BXml bxml) {
        return bxml.getNodeType() == XmlNodeType.COMMENT || bxml.getNodeType() == XmlNodeType.PI;
    }

    private static BArray createNewJsonList() {
        return ValueCreator.createArrayValue(Constants.JSON_ARRAY_TYPE);
    }

    private static BMap<BString, Object> createMapValue(Type type) {
        if (type != null) {
            if (type.getTag() == TypeTags.MAP_TAG) {
                return ValueCreator.createMapValue((MapType) type);
            }
        }
        return ValueCreator.createMapValue(Constants.JSON_MAP_TYPE);
    }

    /**
     * Extract attributes and namespaces from the XML element.
     */
    private static String getKey(Map.Entry<BString, BString> entry, Map<String, String> nsPrefixMap,
                                 boolean preserveNamespaces) {
        if (preserveNamespaces) {
            if (isNamespacePrefixEntry(entry)) {
                return getNamespacePrefixAttribute(entry.getKey().getValue());
            } else {
                return getAttributePreservingNamespace(nsPrefixMap, entry.getKey().getValue());
            }
        } else {
            if (isNonNamespaceAttribute(entry.getKey().getValue())) {
                return getAttributePreservingNamespace(nsPrefixMap, entry.getKey().getValue());
            }
        }
        return null;
    }

    private static Boolean isNonNamespaceAttribute(String attributeKey) {
        // The namespace-related key will contain the pattern as `{link}suffix`
        return !Pattern.matches("\\{.*\\}.*", attributeKey);
    }

    private static String getNamespacePrefixAttribute(String attributeKey) {
        String prefix = attributeKey.substring(NS_PREFIX_BEGIN_INDEX);
        if (prefix.equals(XMLNS)) {
            return prefix;
        } else {
            return XMLNS + COLON + prefix;
        }
    }

    private static String getAttributePreservingNamespace(Map<String, String> nsPrefixMap, String attributeKey) {
        int nsEndIndex = attributeKey.lastIndexOf('}');
        if (nsEndIndex > 0) {
            String ns = attributeKey.substring(1, nsEndIndex);
            String local = attributeKey.substring(nsEndIndex + 1);
            String nsPrefix = nsPrefixMap.get(ns);
            // `!nsPrefix.equals("xmlns")` because attributes does not belong to default namespace.
            if (nsPrefix == null) {
                return local;
            } else if (nsPrefix.equals(XMLNS)) {
                return XMLNS;
            } else {
                return nsPrefix + COLON + local;
            }
        } else {
            return attributeKey;
        }
    }

    private static ConcurrentHashMap<String, String> getNamespacePrefixes(BMap<BString,
            BString> xmlAttributeMap) {
        ConcurrentHashMap<String, String> nsPrefixMap = new ConcurrentHashMap<>();
        for (Map.Entry<BString, BString> entry : xmlAttributeMap.entrySet()) {
            if (isNamespacePrefixEntry(entry)) {
                String prefix = entry.getKey().getValue().substring(NS_PREFIX_BEGIN_INDEX);
                String ns = entry.getValue().getValue();
                nsPrefixMap.put(ns, prefix);
            }
        }
        return nsPrefixMap;
    }

    private static boolean isNamespacePrefixEntry(Map.Entry<BString, BString> entry) {
        return entry.getKey().getValue().startsWith(BXmlItem.XMLNS_NS_URI_PREFIX);
    }

    /**
     * Extract the key from the element with namespace information.
     *
     * @param xmlItem XML element for which the key needs to be generated
     * @param preserveNamespaces Whether namespace info included in the key or not
     * @return String Element key with the namespace information
     */
    public static String getElementKey(BXmlItem xmlItem, boolean preserveNamespaces) {
        // Construct the element key based on the namespaces
        StringBuilder elementKey = new StringBuilder();
        QName qName = xmlItem.getQName();
        if (preserveNamespaces) {
            String prefix = qName.getPrefix();
            if (prefix != null && !prefix.isEmpty()) {
                elementKey.append(prefix).append(':');
            }
        }
        elementKey.append(qName.getLocalPart());
        return elementKey.toString();
    }

    private XmlToJson() {
    }
}
