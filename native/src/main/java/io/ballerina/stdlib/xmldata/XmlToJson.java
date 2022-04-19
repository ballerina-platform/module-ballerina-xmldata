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
    private static final ArrayType MAP_ARRAY_TYPE = TypeCreator.createArrayType(PredefinedTypes.TYPE_MAP);
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
            return convertToJSON(xml, attributePrefix, preserveNamespaces, new AttributeManager(),
                    null, new ArrayList<String>());
        } catch (Exception e) {
            return XmlDataUtils.getError(e.getMessage());
        }
    }

    public static Object toJson(BXml xml, boolean preserveNamespaces, Type type) {
        try {
            return convertToJSON(xml, UNDERSCORE, preserveNamespaces, new AttributeManager(), type,
                    new ArrayList<String>());
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
    public static Object convertToJSON(BXml xml, String attributePrefix, boolean preserveNamespaces,
                                       AttributeManager attributeManager, Type type,
                                       List<String> uniqueKey) throws Exception {
        if (xml instanceof BXmlItem) {
            return convertElement((BXmlItem) xml, attributePrefix, preserveNamespaces, attributeManager, type,
                    uniqueKey);
        } else if (xml instanceof BXmlSequence) {
            BXmlSequence xmlSequence = (BXmlSequence) xml;
            if (xmlSequence.isEmpty()) {
                return StringUtils.fromString(EMPTY_STRING);
            }
            Object seq = convertBXmlSequence(xmlSequence, attributePrefix, preserveNamespaces, attributeManager, type,
                    uniqueKey);
            if (seq == null) {
                return newJsonList(type);
            }
            return seq;
        } else if (xml.getNodeType().equals(XmlNodeType.TEXT)) {
            return JsonUtils.parse(DOUBLE_QUOTES + xml.stringValue(null).replace(DOUBLE_QUOTES,
                    "\\\"") + DOUBLE_QUOTES);
        } else {
            return getMap(type);
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
                                         boolean preserveNamespaces, AttributeManager attributeManager, Type type,
                                         List<String> uniqueKey) throws Exception {
        BMap<BString, Object> childrenData = getMap(type);
        List<String> uKeyValue = getUniqueKey(xmlItem, preserveNamespaces, uniqueKey);
        processAttributes(xmlItem, attributePrefix, childrenData, attributeManager, type, uniqueKey,
                preserveNamespaces);
        String keyValue = getElementKey(xmlItem, preserveNamespaces);
        Object children = convertBXmlSequence(xmlItem.getChildrenSeq(), attributePrefix,
                preserveNamespaces, attributeManager, type, uKeyValue);
        BMap<BString, Object> rootNode = getMap(type);
        if (childrenData.size() > 0) {
            if (children instanceof BMap) {
                BMap<BString, Object> data = (BMap<BString, Object>) children;
                for (Map.Entry<BString, Object> entry: childrenData.entrySet()) {
                    data.put(entry.getKey(), entry.getValue());
                }
                putAsBStrings(rootNode, keyValue, data);
                uniqueKey.remove(uniqueKey.size() - 1);
            } else if (children == null) {
                putAsBStrings(rootNode, keyValue, childrenData);
                uniqueKey.remove(uniqueKey.size() - 1);
            } else if (children instanceof BString) {
                uniqueKey.add(CONTENT);
                putAsFieldTypes(childrenData, CONTENT, children.toString().trim(), type, uKeyValue);
                uniqueKey.remove(CONTENT);
                putAsBStrings(rootNode, keyValue, childrenData);
                uniqueKey.remove(uniqueKey.size() - 1);
                return rootNode;
            }
        } else {
            if (children instanceof BMap) {
                putAsBStrings(rootNode, keyValue, children);
                uniqueKey.remove(uniqueKey.size() - 1);
            } else if (children == null) {
                putAsFieldTypes(rootNode, keyValue, EMPTY_STRING, type, uKeyValue);
            } else if (children instanceof BString) {
                putAsFieldTypes(rootNode, keyValue, children.toString().trim(), type, uKeyValue);
            }
        }
        return rootNode;
    }

    private static void processAttributes(BXmlItem xmlItem, String attributePrefix, BMap<BString, Object> mapData,
                                          AttributeManager attributeManager, Type type, List<String> uniqueKey,
                                          boolean preserveNamespaces) throws Exception {
        LinkedHashMap<String, String> tempAttributeMap =  new LinkedHashMap<>();
        if (attributeManager.getMap().isEmpty()) {
            tempAttributeMap = collectAttributesAndNamespaces(xmlItem, preserveNamespaces);
            attributeManager.initializeMap(tempAttributeMap);
        } else {
            LinkedHashMap<String, String> newAttributeMap = collectAttributesAndNamespaces(xmlItem, preserveNamespaces);
            LinkedHashMap<String, String> attributeMap = attributeManager.getMap();
            for (Map.Entry<String, String> entrySet : newAttributeMap.entrySet()) {
                String key = entrySet.getKey();
                String value = entrySet.getValue();
                if (attributeMap.get(key) == null || !attributeMap.get(key).equals(value)) {
                    attributeManager.setValue(key, value);
                    tempAttributeMap.put(key, value);
                }
            }
        }
        addAttributes(mapData, attributePrefix, tempAttributeMap, type, uniqueKey);
    }

    private static void addAttributes(BMap<BString, Object> rootNode, String attributePrefix,
                                      LinkedHashMap<String, String> attributeMap, Type type,
                                      List<String> uniqueKey) throws Exception {
        for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
            String key = attributePrefix + entry.getKey();
            uniqueKey.add(key);
            putAsFieldTypes(rootNode, key, entry.getValue(), type, uniqueKey);
        }
    }

    private static void putAsFieldTypes(BMap<BString, Object> map, String key, String value, Type type,
                                        List<String> uniqueKey) throws Exception {
        if (type != null) {
            Type fieldType = getFieldType(type, uniqueKey);
            uniqueKey.remove(key);
            if (fieldType instanceof UnionType || fieldType.getTag() == TypeTags.UNION_TAG) {
                UnionType bUnionType = (UnionType) fieldType;
                for (Type memberType : bUnionType.getMemberTypes()) {
                    try {
                        if (convertToRecordType(map, memberType.toString(), key, value)) {
                            break;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            } else {
                convertToRecordType(map, fieldType.toString(), key, value);
            }
        } else {
            map.put(fromString(key), fromString(value));
        }
    }

    public static Type getFieldType(Type type, List<String> uniqueKey) {
        if (type.toString().contains("record {| anydata...; |}")) {
            return type;
        }
        Map<String, Field> fields = getFields(type);
        int i = 0;
        while (i < uniqueKey.size() - 1) {
            String key = uniqueKey.get(i);
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
            i++;
        }
        if (fields.containsKey(uniqueKey.get(i))) {
            return fields.get(uniqueKey.get(i)).getFieldType();
        }
        return type;
    }

    public static Map<String, Field> getFields(Type type) {
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

    private static boolean convertToRecordType(BMap<BString, Object> map, String valueType, String key, String value)
            throws Exception {
        try {
            switch (valueType) {
                case "int":
                case "int?":
                case "int[]":
                case "int[]?":
                    map.put(fromString(key), Long.parseLong(value));
                    return true;
                case "float":
                case "float?":
                case "float[]":
                case "float[]?":
                    map.put(fromString(key), Double.parseDouble(value));
                    return true;
                case "decimal":
                case "decimal?":
                case "decimal[]":
                case "decimal[]?":
                    map.put(fromString(key), ValueCreator.createDecimalValue(
                            BigDecimal.valueOf(Double.parseDouble(value))));
                    return true;
                case "boolean":
                case "boolean?":
                case "boolean[]":
                case "boolean[]?":
                    map.put(fromString(key), Boolean.parseBoolean(value));
                    return true;
                case "string":
                case "string?":
                case "string[]":
                case "string[]?":
                default:
                    map.put(fromString(key), fromString(value));
                    return true;
            }
        } catch (NumberFormatException e) {
            throw new Exception("Error occurred when converting value:" + value + " to " + valueType);
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
                                              boolean preserveNamespaces, AttributeManager attributeManager,
                                              Type type, List<String> uniqueKey) throws Exception {
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
        return convertHeterogeneousSequence(attributePrefix, preserveNamespaces, newSequence, attributeManager, type,
                                            uniqueKey);
    }

    private static Object convertHeterogeneousSequence(String attributePrefix, boolean preserveNamespaces,
                                                       List<BXml> sequence, AttributeManager attributeManager,
                                                       Type type, List<String> uniqueKey) throws Exception {
        if (sequence.size() == 1) {
            return convertToJSON(sequence.get(0), attributePrefix, preserveNamespaces, attributeManager, type,
                                 uniqueKey);
        }
        BMap<BString, Object> mapJson = getMap(type);
        for (BXml bxml : sequence) {
            if (isCommentOrPi(bxml)) {
                continue;
            } else if (bxml.getNodeType() == XmlNodeType.TEXT) {
                if (mapJson.containsKey(fromString(CONTENT))) {
                    if (mapJson.get(fromString(CONTENT)) instanceof BString) {
                        BArray jsonList = newJsonList(type);
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
                Object result = convertToJSON(bxml, attributePrefix, preserveNamespaces, attributeManager,
                        type, uniqueKey);
                result = validateResult(result, elementName);
                Object value = mapJson.get(elementName);
                if (value == null) {
                    mapJson.put(elementName, result);
                } else if (value instanceof BArray) {
                    ((BArray) value).append(result);
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
                        arr = newJsonList(type);
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

    private static BArray newJsonList(Type type) {
        if (type == null) {
            return ValueCreator.createArrayValue(JSON_ARRAY_TYPE);
        }
        return ValueCreator.createArrayValue(MAP_ARRAY_TYPE);
    }

    private static BMap<BString, Object> getMap(Type type) {
        if (type == null) {
            return ValueCreator.createMapValue(TypeCreator.createMapType(JSON_MAP_TYPE));
        }
        return ValueCreator.createRecordValue((RecordType) type);
    }

    /**
     * Extract attributes and namespaces from the XML element.
     *
     * @param element XML element to extract attributes and namespaces
     */
    private static LinkedHashMap<String, String> collectAttributesAndNamespaces(BXmlItem element,
                                                                                boolean preserveNamespaces) {
        LinkedHashMap<String, String> attributeMap = new LinkedHashMap<>();
        BMap<BString, BString> attributesMap = element.getAttributesMap();
        Map<String, String> nsPrefixMap = getNamespacePrefixes(attributesMap);

        for (Map.Entry<BString, BString> entry : attributesMap.entrySet()) {
            if (preserveNamespaces) {
                if (isNamespacePrefixEntry(entry)) {
                    addNamespacePrefixAttribute(attributeMap, entry.getKey().getValue(),
                            entry.getValue().getValue());
                } else {
                    addAttributePreservingNamespace(attributeMap, nsPrefixMap, entry.getKey().getValue(),
                            entry.getValue().getValue());
                }
            } else {
                preserveNonNamespacesAttributes(attributeMap, nsPrefixMap, entry.getKey().getValue(),
                        entry.getValue().getValue());
            }
        }
        return attributeMap;
    }

    private static void preserveNonNamespacesAttributes(LinkedHashMap<String, String> attributeMap,
                                                        Map<String, String> nsPrefixMap,
                                                        String attributeKey, String attributeValue) {
        // The namespace-related key will contain the pattern as `{link}suffix`
        if (!Pattern.matches("\\{.*\\}.*", attributeKey)) {
            addAttributePreservingNamespace(attributeMap, nsPrefixMap, attributeKey, attributeValue);
        }
    }

    private static void addNamespacePrefixAttribute(LinkedHashMap<String, String> attributeMap,
                                                    String attributeKey, String attributeValue) {
        String prefix = attributeKey.substring(NS_PREFIX_BEGIN_INDEX);
        if (prefix.equals(XMLNS)) {
            attributeMap.put(prefix, attributeValue);
        } else {
            attributeMap.put(XMLNS + COLON + prefix, attributeValue);
        }
    }

    private static void addAttributePreservingNamespace(LinkedHashMap<String, String> attributeMap,
                                                        Map<String, String> nsPrefixMap,
                                                        String attributeKey, String attributeValue) {
        int nsEndIndex = attributeKey.lastIndexOf('}');
        if (nsEndIndex > 0) {
            String ns = attributeKey.substring(1, nsEndIndex);
            String local = attributeKey.substring(nsEndIndex + 1);
            String nsPrefix = nsPrefixMap.get(ns);
            // `!nsPrefix.equals("xmlns")` because attributes does not belong to default namespace.
            if (nsPrefix == null) {
                attributeMap.put(local, attributeValue);
            } else if (nsPrefix.equals(XMLNS)) {
                attributeMap.put(XMLNS, attributeValue);
            } else {
                attributeMap.put(nsPrefix + COLON + local, attributeValue);
            }
        } else {
            attributeMap.put(attributeKey, attributeValue);
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
