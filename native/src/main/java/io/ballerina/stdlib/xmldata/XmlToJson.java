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
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.types.XmlNodeType;
import io.ballerina.runtime.api.utils.JsonUtils;
import io.ballerina.runtime.api.utils.StringUtils;
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
import java.util.LinkedHashMap;
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

    private static final Type JSON_MAP_TYPE =
            TypeCreator.createMapType(TypeConstants.MAP_TNAME, PredefinedTypes.TYPE_JSON, new Module(null, null, null));
    private static final String XMLNS = "xmlns";
    private static final String DOUBLE_QUOTES = "\"";
    private static final String CONTENT = "#content";
    private static final String EMPTY_STRING = "";
    private static final ArrayType JSON_ARRAY_TYPE = TypeCreator.createArrayType(PredefinedTypes.TYPE_JSON);
    private static final ArrayType DECIMAL_ARRAY_TYPE = TypeCreator.createArrayType(PredefinedTypes.TYPE_DECIMAL);
    private static final ArrayType BOOLEAN_ARRAY_TYPE = TypeCreator.createArrayType(PredefinedTypes.TYPE_BOOLEAN);
    private static final ArrayType FLOAT_ARRAY_TYPE = TypeCreator.createArrayType(PredefinedTypes.TYPE_FLOAT);
    private static final ArrayType LONG_ARRAY_TYPE = TypeCreator.createArrayType(PredefinedTypes.TYPE_INT);
    private static final ArrayType STRING_ARRAY_TYPE = TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING);
    public static final int NS_PREFIX_BEGIN_INDEX = BXmlItem.XMLNS_NS_URI_PREFIX.length();
    private static final String COLON = ":";
    private static final String UNDERSCORE = "_";

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

    public static Object toJson(BXml xml, boolean preserveNamespaces, Type type) {
        try {
            return convertToJSON(xml, UNDERSCORE, preserveNamespaces, type, null);
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
                                       BMap<BString, BString> parentAttributeMap) {
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
                return newJsonList();
            }
            return seq;
        } else if (xml.getNodeType().equals(XmlNodeType.TEXT)) {
            return JsonUtils.parse(DOUBLE_QUOTES + xml.stringValue(null).replace(DOUBLE_QUOTES,
                    "\\\"") + DOUBLE_QUOTES);
        } else {
            return newJsonMap();
        }
    }

    /**
     * Converts given xml object to the corresponding json.
     *
     * @param xmlItem XML element to traverse
     * @param attributePrefix Prefix to use in attributes
     * @param preserveNamespaces preserve the namespaces when converting
     * @return ObjectNode Json object node corresponding to the given xml element
     */
    private static Object convertElement(BXmlItem xmlItem, String attributePrefix,
                                         boolean preserveNamespaces, Type type,
                                         BMap<BString, BString> parentAttributeMap) {
        BMap<BString, Object> childrenData = newJsonMap();
        BMap<BString, BString> attributeMap = xmlItem.getAttributesMap();
        String keyValue = getElementKey(xmlItem, preserveNamespaces);
        processAttributes(attributeMap, attributePrefix, childrenData, getFieldType(keyValue, type),
                parentAttributeMap, xmlItem.getQName().getPrefix(), preserveNamespaces);
        Object children = convertBXmlSequence(xmlItem.getChildrenSeq(), attributePrefix, preserveNamespaces,
                getFieldType(keyValue, type),  attributeMap);
        BMap<BString, Object> rootNode = newJsonMap();
        if (type != null) {
            Type fieldType = getFieldType(type, uKeyValue);
            if ((fieldType != null ? fieldType.getTag() : 0) == 32 && !(children instanceof BArray)) {
                children = convertToArray(fieldType, children);
            }
        }
        if (childrenData.size() > 0) {
            if (children instanceof BMap) {
                BMap<BString, Object> data = (BMap<BString, Object>) children;
                for (Map.Entry<BString, Object> entry: childrenData.entrySet()) {
                    data.put(entry.getKey(), entry.getValue());
                }
                putAsBStrings(rootNode, keyValue, data);
            } else if (children == null) {
                putAsBStrings(rootNode, keyValue, childrenData);
            } else if (children instanceof BString) {
                putAsFieldTypes(childrenData, CONTENT, children.toString().trim(), getFieldType(keyValue, type));
                putAsBStrings(rootNode, keyValue, childrenData);
                return rootNode;
            }
        } else {
            if (children instanceof BMap) {
                putAsBStrings(rootNode, keyValue, children);
            } else if (children == null) {
                putAsFieldTypes(rootNode, keyValue, EMPTY_STRING, getFieldType(keyValue, type));
            } else if (children instanceof BString) {
                putAsFieldTypes(rootNode, keyValue, children.toString().trim(), getFieldType(keyValue, type));
            }
        }
        return rootNode;
    }

    private static Type getFieldType(String fieldName, Type type) {
        if (type instanceof RecordType) {
            if (((RecordType) type).getFields().get(fieldName) != null) {
                type = ((RecordType) type).getFields().get(fieldName).getFieldType();
            }
        } else if (type instanceof ArrayType) {
            Type filedType = ((ArrayType) type).getElementType();
            if (filedType instanceof RecordType) {
                Map<String, Field> fileds = ((RecordType) filedType).getFields();
                if (fileds.get(fieldName) != null) {
                    return fileds.get(fieldName).getFieldType();
                }
            }
            return filedType;
        }
        return type;
    }

    private static void processAttributes(BMap<BString, BString> attributeMap, String attributePrefix,
                                          BMap<BString, Object> mapData, Type type,
                                          BMap<BString, BString> parentAttributeMap, String prefix,
                                          boolean preserveNamespaces) {
        Map<String, String> nsPrefixMap = getNamespacePrefixes(attributeMap);
        if (prefix != null && preserveNamespaces && parentAttributeMap != null) {
            for (Map.Entry<BString, BString> entry : attributeMap.entrySet()) {
                BString value = entry.getValue();
                if (!isNamespacePrefixEntry(entry) ||
                        isBelongingToElement(parentAttributeMap, entry.getKey(), value)) {
                    String key = attributePrefix + getKey(entry, nsPrefixMap, preserveNamespaces);
                    putAsFieldTypes(mapData, key, value.getValue(), type);
                }
            }
        } else {
            for (Map.Entry<BString, BString> entry : attributeMap.entrySet()) {
                String key = getKey(entry, nsPrefixMap, preserveNamespaces);
                if (key != null) {
                    putAsFieldTypes(mapData, attributePrefix + key, entry.getValue().getValue(), type);
                }
            }
        }
    }

    private static boolean isBelongingToElement(BMap<BString, BString> parentAttributeMap, BString key,
                                                       BString value) {
        return !(parentAttributeMap.containsKey(key) &&
                parentAttributeMap.get(key).getValue().equals(value.getValue()));
    }

    private static void putAsFieldTypes(BMap<BString, Object> map, String key, String value, Type type) {
    private static void putAsFieldTypes(BMap<BString, Object> map, String key, String value, Type type,
                                        List<String> uniqueKey) throws Exception {
        if (type != null) {
            String valueType;
            if (type instanceof ArrayType) {
                Type fieldType = ((ArrayType) type).getElementType();
                if (fieldType instanceof RecordType) {
                    if (((RecordType) fieldType).getFields().get(key) != null) {
                        type = ((RecordType) fieldType).getFields().get(key).getFieldType();
                        valueType = type.getName();
                    } else {
                        valueType = fieldType.getName();
                    }
                } else {
                    valueType = ((ArrayType) type).getElementType().getName();
                }
            } else {
                valueType = type.getName();
            }
            switch (valueType) {
                case "int":
            Type fieldType = getFieldType(type, uniqueKey);
            uniqueKey.remove(key);
            if (fieldType instanceof UnionType || fieldType.getTag() == TypeTags.UNION_TAG) {
                UnionType bUnionType = (UnionType) fieldType;
                for (Type memberType : bUnionType.getMemberTypes()) {
                    try {
                        convertToRecordType(map, memberType, key, value);
                        break;
                    } catch (Exception e) {
                        continue;
                    }
                }
            } else {
                convertToRecordType(map, fieldType, key, value);
            }
        } else {
            map.put(fromString(key), fromString(value));
        }
    }

    public static Type getFieldType(Type type, List<String> uniqueKey) throws Exception {
        if (type.toString().contains("record {| anydata...; |}")) {
            return type;
        }
        Map<String, Field> fields = getFields(type);
        int i = 0;
        String key;
        while (i < uniqueKey.size() - 1) {
            key = uniqueKey.get(i);
            isFieldNull(fields, type, key);
            if (fields.containsKey(key)) {
                Type fieldType = fields.get(key).getFieldType();
                if (fieldType instanceof UnionType || fieldType.getTag() == TypeTags.UNION_TAG) {
                    UnionType bUnionType = (UnionType) fieldType;
                    for (Type memberType : bUnionType.getMemberTypes()) {
                        fields = getFields(memberType);
                        if (fields != null && fields.get(key) != null) {
                            break;
                        }
                    }
                } else {
                    fields = getFields(fields.get(key).getFieldType());
                }
            } else {
                return getType(type, key);
            }
            i++;
        }
        key = uniqueKey.get(i);
        isFieldNull(fields, type, key);
        if (fields.containsKey(key)) {
            return fields.get(key).getFieldType();
        } else {
            return getType(type, key);
        }
    }

    private static Type getType(Type type, String key) throws Exception {
        if (type instanceof RecordType) {
            if (!((RecordType) type).isSealed()) {
                return type;
            } else {
                throw new Exception("field '" + key + "' cannot be added to the closed record '" + type + "'");
            }
        } else {
            throw new Exception("field '" + key + "' cannot be added to the record '" + type + "'");
        }
    }

    private static void isFieldNull(Map<String, Field> fields, Type type, String key) throws Exception {
        if (fields == null && type.isPureType()) {
            throw new Exception("field '" + key + "' cannot be added to the closed record '" + type + "'");
        }
    }

    private static Map<String, Field> getFields(Type type) {
        Map<String, Field> fields = null;
        if (type instanceof RecordType) {
            fields = ((RecordType) type).getFields();
        } else if (type instanceof ArrayType) {
            fields = ((RecordType) ((ArrayType) type).getElementType()).getFields();
        }
        return fields;
    }

    private static void putAsBStrings(BMap<BString, Object> map, String key, Object value) {
        map.put(fromString(key), value);
    }

    private static void convertToRecordType(BMap<BString, Object> map, Type valueType, String key, String value)
            throws Exception {
        try {
            switch (valueType.getTag()) {
                case 1:
                    map.put(fromString(key), Long.parseLong(value));
                    break;
                case "float":
                    map.put(fromString(key), Double.parseDouble(value));
                    break;
                case "boolean":
                    map.put(fromString(key), Boolean.parseBoolean(value));
                    break;
                case 32:
                    BArray array = convertToArray(valueType, value);
                    map.put(fromString(key), array);
                    break;
                case 5:
                default:
                    map.put(fromString(key), fromString(value));
                    break;
            }
        } else {
            map.put(fromString(key), fromString(value));
        }
    }

    private static void putAsBStrings(BMap<BString, Object> map, String key, Object value) {
        map.put(fromString(key), value);
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
                                              BMap<BString, BString> parentAttributeMap) {
        List<BXml> sequence = xmlSequence.getChildrenList();
        List<BXml> newSequence = new ArrayList<>();
        for (BXml value: sequence) {
            String textValue = value.getTextValue();
            if (textValue.isEmpty() || !textValue.trim().isEmpty()) {
                newSequence.add(value);
            }
        }
        if (newSequence.isEmpty()) {
            return null;
        }
        return convertHeterogeneousSequence(attributePrefix, preserveNamespaces, newSequence, type,
                parentAttributeMap);
    }

    private static Object convertHeterogeneousSequence(String attributePrefix, boolean preserveNamespaces,
                                                       List<BXml> sequence, Type type,
                                                       BMap<BString, BString> parentAttributeMap) {
        if (sequence.size() == 1) {
            return convertToJSON(sequence.get(0), attributePrefix, preserveNamespaces, type, parentAttributeMap);
        }
        BMap<BString, Object> mapJson = newJsonMap();
        for (BXml bxml : sequence) {
            if (isCommentOrPi(bxml)) {
                continue;
            } else if (bxml.getNodeType() == XmlNodeType.TEXT) {
                if (mapJson.containsKey(fromString(CONTENT))) {
                    if (mapJson.get(fromString(CONTENT)) instanceof BString) {
                        BArray jsonList = newJsonList();
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
                Object result = convertToJSON(bxml, attributePrefix, preserveNamespaces,
                        type, parentAttributeMap);
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
                        arr = ValueCreator.createArrayValue(LONG_ARRAY_TYPE);
                    } else if (value instanceof Boolean) {
                        arr = ValueCreator.createArrayValue(BOOLEAN_ARRAY_TYPE);
                    } else if (value instanceof Double) {
                        arr = ValueCreator.createArrayValue(FLOAT_ARRAY_TYPE);
                    } else if (value.getClass().getCanonicalName().contains("DecimalValue")) {
                        arr = ValueCreator.createArrayValue(DECIMAL_ARRAY_TYPE);
                    } else if (value instanceof BString) {
                        arr = ValueCreator.createArrayValue(STRING_ARRAY_TYPE);
                    } else {
                        arr = newJsonList();
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

    private static BArray newJsonList() {
        return ValueCreator.createArrayValue(JSON_ARRAY_TYPE);
    }

    private static BMap<BString, Object> getMap() {
        return ValueCreator.createMapValue(TypeCreator.createMapType(JSON_MAP_TYPE));
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
    private static String getElementKey(BXmlItem xmlItem, boolean preserveNamespaces) {
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

    private static List<String> getUniqueKey(BXmlItem xmlItem, boolean preserveNamespaces, List<String> uniqueKey) {
        StringBuilder elementKey = new StringBuilder();
        QName qName = xmlItem.getQName();
        if (preserveNamespaces) {
            String prefix = qName.getPrefix();
            if (prefix != null && !prefix.isEmpty()) {
                elementKey.append(prefix).append(':');
            }
        }
        elementKey.append(qName.getLocalPart());
        uniqueKey.add(elementKey.toString());
        return uniqueKey;
    }

    private XmlToJson() {
    }
}
