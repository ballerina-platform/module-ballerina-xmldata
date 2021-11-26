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
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
                    null, "", COLON);
        } catch (Exception e) {
            return XmlDataUtils.getError(e.getMessage());
        }
    }

    public static Object toJson(BXml xml, BMap<?, ?> options, Type type) {
        try {
            String attributePrefix = ((BString) options.get(StringUtils.fromString(Constants.OPTIONS_ATTRIBUTE_PREFIX)))
                    .getValue();
            boolean preserveNamespaces = ((Boolean) options.get(StringUtils.fromString(Constants.OPTIONS_PRESERVE_NS)));
            return convertToJSON(xml, attributePrefix, preserveNamespaces, new AttributeManager(),
                    type, "", UNDERSCORE);
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
                                       AttributeManager attributeManager, Type type, String uniqueKey,
                                       String namespaceDelimiter) {
        if (xml instanceof BXmlItem) {
            return convertElement((BXmlItem) xml, attributePrefix, preserveNamespaces, attributeManager, type,
                    uniqueKey, namespaceDelimiter);
        } else if (xml instanceof BXmlSequence) {
            BXmlSequence xmlSequence = (BXmlSequence) xml;
            if (xmlSequence.isEmpty()) {
                return StringUtils.fromString(EMPTY_STRING);
            }
            Object seq = convertBXmlSequence(xmlSequence, attributePrefix, preserveNamespaces, attributeManager, type,
                    uniqueKey, namespaceDelimiter);
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
                                         boolean preserveNamespaces, AttributeManager attributeManager, Type type,
                                         String uniqueKey, String namespaceDelimiter) {
        BMap<BString, Object> childrenData = newJsonMap();
        if (preserveNamespaces) {
            processAttributes(xmlItem, attributePrefix, childrenData, attributeManager, type, uniqueKey,
                    namespaceDelimiter);
        }
        String uKeyValue = getUniqueKey(xmlItem, preserveNamespaces, uniqueKey);
        String keyValue = getElementKey(xmlItem, preserveNamespaces);
        Object children = convertBXmlSequence(xmlItem.getChildrenSeq(), attributePrefix,
                preserveNamespaces, attributeManager, type, uKeyValue, namespaceDelimiter);
        BMap<BString, Object> rootNode = newJsonMap();
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
                putAsFieldTypes(childrenData, CONTENT, children.toString().trim(), type, uKeyValue);
                return childrenData;
            }
        } else {
            if (children instanceof BMap) {
                putAsBStrings(rootNode, keyValue, children);
            } else if (children == null) {
                putAsFieldTypes(rootNode, keyValue, EMPTY_STRING, type, uKeyValue);
            } else if (children instanceof BString) {
                putAsFieldTypes(rootNode, keyValue, children.toString().trim(), type, uKeyValue);
            }
        }
        return rootNode;
    }

    private static void processAttributes(BXmlItem xmlItem, String attributePrefix,
                                      BMap<BString, Object> mapData, AttributeManager attributeManager, Type type,
                                          String uniqueKey, String namespaceDelimiter) {
        LinkedHashMap<String, String> tempAttributeMap =  new LinkedHashMap<>();
        if (attributeManager.getMap().isEmpty()) {
            tempAttributeMap = collectAttributesAndNamespaces(xmlItem, namespaceDelimiter);
            attributeManager.initializeMap(tempAttributeMap);
        } else {
            LinkedHashMap<String, String> newAttributeMap = collectAttributesAndNamespaces(xmlItem, namespaceDelimiter);
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
                                      LinkedHashMap<String, String> attributeMap, Type type, String uniqueKey) {
        for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
            putAsFieldTypes(rootNode, attributePrefix + entry.getKey(), entry.getValue(), type, uniqueKey);
        }
    }

    private static void putAsFieldTypes(BMap<BString, Object> map, String key, String value, Type type,
                                        String uniqueKey) {
        if (type != null) {
            String[] keys = {};
            if (!uniqueKey.equals("")) {
                keys = uniqueKey.split("\\.");
            }
            String valueType;

            for (String k : keys) {
                if (type instanceof RecordType) {
                    if (((RecordType) type).getFields().get(k) != null) {
                        type = ((RecordType) type).getFields().get(k).getFieldType();
                    }
                } else if (type instanceof ArrayType) {
                    type = ((ArrayType) type).getElementType();
                }
            }
            if (type instanceof ArrayType) {
                valueType = ((ArrayType) type).getElementType().getName();
            } else {
                valueType = type.getName();
            }
            switch (valueType) {
                case "int":
                    map.put(fromString(key), Integer.parseInt(value));
                    break;
                case "float":
                    map.put(fromString(key), Double.parseDouble(value));
                    break;
                case "boolean":
                    map.put(fromString(key), Boolean.parseBoolean(value));
                    break;
                default:
                    map.put(fromString(key), fromString(value));
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
                                              boolean preserveNamespaces, AttributeManager attributeManager,
                                              Type type, String uniqueKey, String namespaceDelimiter) {
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
                uniqueKey, namespaceDelimiter);
    }

    private static Object convertHeterogeneousSequence(String attributePrefix, boolean preserveNamespaces,
                                                       List<BXml> sequence, AttributeManager attributeManager,
                                                       Type type, String uniqueKey, String namespaceDelimiter) {
        if (sequence.size() == 1) {
            return convertToJSON(sequence.get(0), attributePrefix, preserveNamespaces, attributeManager, type,
                    uniqueKey, namespaceDelimiter);
        }
        BMap<BString, Object> mapJson = newJsonMap();
        for (BXml bxml : sequence) {
            if (isCommentOrPi(bxml)) {
            } else if (bxml.getNodeType() == XmlNodeType.TEXT) {
                mapJson.put(fromString(CONTENT), fromString(bxml.toString().trim()));
            } else {
                BString elementName = fromString(getElementKey((BXmlItem) bxml, preserveNamespaces));
                Object result = convertToJSON(bxml, attributePrefix, preserveNamespaces, attributeManager,
                        type, uniqueKey, namespaceDelimiter);
                result = validateResult(result, elementName);
                Object value = mapJson.get(elementName);
                if (value == null) {
                    mapJson.put(elementName, result);
                } else if (value instanceof BArray) {
                    ((BArray) value).append(result);
                    mapJson.put(elementName, value);
                } else {
                    BArray jsonList = newJsonList();
                    jsonList.append(value);
                    jsonList.append(result);
                    mapJson.put(elementName, jsonList);
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

    private static BMap<BString, Object> newJsonMap() {
        return ValueCreator.createMapValue(JSON_MAP_TYPE);
    }

    /**
     * Extract attributes and namespaces from the XML element.
     *
     * @param element XML element to extract attributes and namespaces
     */
    private static LinkedHashMap<String, String> collectAttributesAndNamespaces(BXmlItem element,
                                                                                String namespaceDelimiter) {
        LinkedHashMap<String, String> attributeMap = new LinkedHashMap<>();
        BMap<BString, BString> attributesMap = element.getAttributesMap();
        Map<String, String> nsPrefixMap = getNamespacePrefixes(attributesMap);

        for (Map.Entry<BString, BString> entry : attributesMap.entrySet()) {
            if (isNamespacePrefixEntry(entry)) {
                addNamespacePrefixAttribute(attributeMap, entry, namespaceDelimiter);
            } else {
                addAttributePreservingNamespace(attributeMap, nsPrefixMap, entry, namespaceDelimiter);
            }
        }
        return attributeMap;
    }

    private static void addNamespacePrefixAttribute(LinkedHashMap<String, String> attributeMap,
                                                    Map.Entry<BString, BString> entry, String namespaceDelimiter) {
        String key = entry.getKey().getValue();
        String value = entry.getValue().getValue();
        String prefix = key.substring(NS_PREFIX_BEGIN_INDEX);
        if (prefix.equals(XMLNS)) {
            attributeMap.put(prefix, value);
        } else {
            attributeMap.put(XMLNS + namespaceDelimiter + prefix, value);
        }
    }

    private static void addAttributePreservingNamespace(LinkedHashMap<String, String> attributeMap,
                                                        Map<String, String> nsPrefixMap,
                                                        Map.Entry<BString, BString> entry, String namespaceDelimiter) {
        String key = entry.getKey().getValue();
        String value = entry.getValue().getValue();
        int nsEndIndex = key.lastIndexOf('}');
        if (nsEndIndex > 0) {
            String ns = key.substring(1, nsEndIndex);
            String local = key.substring(nsEndIndex + 1);
            String nsPrefix = nsPrefixMap.get(ns);
            // `!nsPrefix.equals("xmlns")` because attributes does not belong to default namespace.
            if (nsPrefix == null) {
                attributeMap.put(local, value);
            } else if (nsPrefix.equals(XMLNS)) {
                attributeMap.put(XMLNS, value);
            } else {
                attributeMap.put(nsPrefix + namespaceDelimiter + local, value);
            }
        } else {
            attributeMap.put(key, value);
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

    private static String getUniqueKey(BXmlItem xmlItem, boolean preserveNamespaces, String uniqueKey) {
        StringBuilder elementKey = new StringBuilder();
        StringBuilder key = new StringBuilder();
        if (!uniqueKey.equals("")) {
            key.append(uniqueKey).append(".");
        }
        QName qName = xmlItem.getQName();
        if (preserveNamespaces) {
            String prefix = qName.getPrefix();
            if (prefix != null && !prefix.isEmpty()) {
                elementKey.append(prefix).append(':');
            }
        }
        elementKey.append(qName.getLocalPart());
        return key.append(elementKey).toString();
    }

    private XmlToJson() {
    }
}
