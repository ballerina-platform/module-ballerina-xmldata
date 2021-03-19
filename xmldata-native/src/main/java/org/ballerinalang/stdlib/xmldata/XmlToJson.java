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

package org.ballerinalang.stdlib.xmldata;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
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
import org.ballerinalang.stdlib.xmldata.utils.Constants;
import org.ballerinalang.stdlib.xmldata.utils.XmlDataUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private static final ArrayType JSON_ARRAY_TYPE = TypeCreator.createArrayType(PredefinedTypes.TYPE_JSON);
    public static final int NS_PREFIX_BEGIN_INDEX = BXmlItem.XMLNS_URL_PREFIX.length();

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
            return convertToJSON(xml, attributePrefix, preserveNamespaces);
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
    public static Object convertToJSON(BXml xml, String attributePrefix, boolean preserveNamespaces) {

        if (xml instanceof BXmlItem) {
            return convertElement((BXmlItem) xml, attributePrefix, preserveNamespaces);
        } else if (xml instanceof BXmlSequence) {
            BXmlSequence xmlSequence = (BXmlSequence) xml;
            if (xmlSequence.isEmpty()) {
                return newJsonList();
            }
            Object seq = convertBXmlSequence(xmlSequence, attributePrefix, preserveNamespaces);
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
                                         boolean preserveNamespaces) {
        BMap<BString, Object> rootNode = newJsonMap();
        LinkedHashMap<String, String> attributeMap = collectAttributesAndNamespaces(xmlItem, preserveNamespaces);
        String keyValue = getElementKey(xmlItem, preserveNamespaces);
        Object children = convertBXmlSequence((BXmlSequence) xmlItem.getChildrenSeq(),
                attributePrefix, preserveNamespaces);

        if (children == null) {
            if (attributeMap.isEmpty()) {
                putAsBStrings(rootNode, keyValue, "");
            } else {
                BMap<BString, Object> attrMap = newJsonMap();
                addAttributes(attrMap, attributePrefix, attributeMap);

                putAsBStrings(rootNode, keyValue, attrMap);
            }
            return rootNode;
        }

        putAsBStrings(rootNode, keyValue, children);
        addAttributes(rootNode, attributePrefix, attributeMap);
        return rootNode;
    }

    private static void addAttributes(BMap<BString, Object> rootNode, String attributePrefix,
                                      LinkedHashMap<String, String> attributeMap) {
        for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
            putAsBStrings(rootNode, attributePrefix + entry.getKey(), entry.getValue());
        }
    }

    private static void putAsBStrings(BMap<BString, Object> map, String key, String value) {
        map.put(fromString(key), fromString(value));
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
                                              boolean preserveNamespaces) {
        List<BXml> sequence = xmlSequence.getChildrenList();
        if (sequence.isEmpty()) {
            return null;
        }

        switch (calculateMatchingJsonTypeForSequence(sequence)) {
            case SAME_KEY:
                return convertSequenceWithSameNamedElements(attributePrefix, preserveNamespaces, sequence);
            case ELEMENT_ONLY:
                return convertSequenceWithOnlyElements(attributePrefix, preserveNamespaces, sequence);
            default:
                return convertHeterogeneousSequence(attributePrefix, preserveNamespaces, sequence);
        }
    }

    private static Object convertHeterogeneousSequence(String attributePrefix, boolean preserveNamespaces,
                                                       List<BXml> sequence) {
        if (sequence.size() == 1) {
            return convertToJSON((BXml) sequence.get(0), attributePrefix, preserveNamespaces);
        }

        ArrayList<Object> list = new ArrayList<>();
        for (BXml bxml : sequence) {
            if (isCommentOrPi(bxml)) {
                continue;
            }
            list.add(convertToJSON((BXml) bxml, attributePrefix, preserveNamespaces));
        }

        if (list.isEmpty()) {
            return null;
        }
        return newJsonListFrom(list);
    }

    private static Object convertSequenceWithOnlyElements(String attributePrefix, boolean preserveNamespaces,
                                                          List<BXml> sequence) {
        BMap<BString, Object> elementObj = newJsonMap();
        for (BXml bxml : sequence) {
            // Skip comments and PI items.
            if (isCommentOrPi(bxml)) {
                continue;
            }
            String elementName = getElementKey((BXmlItem) bxml, preserveNamespaces);
            Object elemAsJson = convertElement((BXmlItem) bxml, attributePrefix, preserveNamespaces);
            if (elemAsJson instanceof BMap) {
                @SuppressWarnings("unchecked")
                BMap<BString, Object> mapVal = (BMap<BString, Object>) elemAsJson;
                if (mapVal.size() == 1) {
                    Object val = mapVal.get(fromString(elementName));
                    if (val != null) {
                        putAsBStrings(elementObj, elementName, val);
                        continue;
                    }
                }
            }
            putAsBStrings(elementObj, elementName, elemAsJson);
        }
        return elementObj;
    }

    private static boolean isCommentOrPi(BXml bxml) {
        return bxml.getNodeType() == XmlNodeType.COMMENT || bxml.getNodeType() == XmlNodeType.PI;
    }

    private static Object convertSequenceWithSameNamedElements(String attributePrefix, boolean preserveNamespaces,
                                                               List<BXml> sequence) {
        String elementName = null;
        for (BXml bxml : sequence) {
            if (bxml.getNodeType() == XmlNodeType.ELEMENT) {
                elementName = bxml.getElementName();
                break;
            }
        }
        BMap<BString, Object> listWrapper = newJsonMap();
        BArray arrayValue = convertChildrenToJsonList(sequence, attributePrefix, preserveNamespaces);
        putAsBStrings(listWrapper, elementName, arrayValue);
        return listWrapper;
    }

    private static BArray convertChildrenToJsonList(List<BXml> sequence, String prefix,
                                                    boolean preserveNamespaces) {
        List<Object> list = new ArrayList<>();
        for (BXml child : sequence) {
            if (isCommentOrPi(child)) {
                continue;
            }
            if (child.getAttributesMap().isEmpty()) {
                list.add(convertToJSON((BXml) child.children(), prefix, preserveNamespaces));
            } else {
                list.add(convertElement((BXmlItem) child, prefix, preserveNamespaces));
            }
        }
        return newJsonListFrom(list);
    }

    private static SequenceConvertibility calculateMatchingJsonTypeForSequence(List<BXml> sequence) {
        Iterator<BXml> iterator = sequence.iterator();
        BXml next = iterator.next();
        if (next.getNodeType() == XmlNodeType.TEXT) {
            return SequenceConvertibility.LIST;
        }
        // Scan until first convertible item is found.
        while (iterator.hasNext() && (isCommentOrPi(next))) {
            next = iterator.next();
            if (next.getNodeType() == XmlNodeType.TEXT) {
                return SequenceConvertibility.LIST;
            }
        }

        String firstElementName = next.getElementName();
        int i = 0;
        boolean sameElementName = true;
        for (; iterator.hasNext(); i++) {
            BXml val = iterator.next();
            if (val.getNodeType() == XmlNodeType.ELEMENT) {
                if (!((BXmlItem) val).getElementName().equals(firstElementName)) {
                    sameElementName = false;
                }
                if (!val.getAttributesMap().isEmpty()) {
                    return SequenceConvertibility.LIST;
                }
            } else if (val.getNodeType() == XmlNodeType.TEXT) {
                return SequenceConvertibility.LIST;
            } else {
                i--; // we don't want `i` to count up for comments and PI items
            }
        }
        return (sameElementName && i > 0) ? SequenceConvertibility.SAME_KEY : SequenceConvertibility.ELEMENT_ONLY;
    }

    private static BArray newJsonList() {
        return ValueCreator.createArrayValue(JSON_ARRAY_TYPE);
    }

    public static BArray newJsonListFrom(List<Object> items) {
        return ValueCreator.createArrayValue(items.toArray(), TypeCreator.createArrayType(PredefinedTypes.TYPE_JSON));
    }

    private static BMap<BString, Object> newJsonMap() {
        return ValueCreator.createMapValue(JSON_MAP_TYPE);
    }

    /**
     * Extract attributes and namespaces from the XML element.
     *
     * @param element XML element to extract attributes and namespaces
     * @param preserveNamespaces should namespace attribute be preserved
     */
    private static LinkedHashMap<String, String> collectAttributesAndNamespaces(BXmlItem element,
                                                                                boolean preserveNamespaces) {
        LinkedHashMap<String, String> attributeMap = new LinkedHashMap<>();
        BMap<BString, BString> attributesMap = element.getAttributesMap();
        Map<String, String> nsPrefixMap = getNamespacePrefixes(attributesMap);

        for (Map.Entry<BString, BString> entry : attributesMap.entrySet()) {
            if (preserveNamespaces) {
                if (isNamespacePrefixEntry(entry)) {
                    addNamespacePrefixAttribute(attributeMap, entry);
                } else {
                    addAttributePreservingNamespace(attributeMap, nsPrefixMap, entry);
                }
            } else {
                addAttributeDiscardingNamespace(attributeMap, entry);
            }
        }
        return attributeMap;
    }

    private static void addNamespacePrefixAttribute(LinkedHashMap<String, String> attributeMap,
                                                    Map.Entry<BString, BString> entry) {
        String key = entry.getKey().getValue();
        String value = entry.getValue().getValue();
        String prefix = key.substring(NS_PREFIX_BEGIN_INDEX);
        if (prefix.equals(XMLNS)) {
            attributeMap.put(prefix, value);
        } else {
            attributeMap.put(XMLNS + ":" + prefix, value);
        }
    }

    private static void addAttributeDiscardingNamespace(LinkedHashMap<String, String> attributeMap,
                                                        Map.Entry<BString, BString> entry) {
        String key = entry.getKey().getValue();
        String value = entry.getValue().getValue();
        int nsEndIndex = key.lastIndexOf('}');
        if (nsEndIndex > 0) {
            String localName = key.substring(nsEndIndex + 1);
            if (localName.equals(XMLNS)) {
                return;
            }
            attributeMap.put(localName, value);
        } else {
            attributeMap.put(key, value);
        }
    }

    private static void addAttributePreservingNamespace(LinkedHashMap<String, String> attributeMap,
                                                        Map<String, String> nsPrefixMap,
                                                        Map.Entry<BString, BString> entry) {
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
                attributeMap.put(nsPrefix + ":" + local, value);
            }
        } else {
            attributeMap.put(key, value);
        }
    }

    private static Map<String, String> getNamespacePrefixes(BMap<BString, BString> xmlAttributeMap) {
        HashMap<String, String> nsPrefixMap = new HashMap<>();
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
        return entry.getKey().getValue().startsWith(BXmlItem.XMLNS_URL_PREFIX);
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

    private enum SequenceConvertibility {
        SAME_KEY, ELEMENT_ONLY, LIST
    }

    private XmlToJson() {
    }
}
