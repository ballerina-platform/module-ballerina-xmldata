/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.stdlib.xmldata;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.utils.XmlUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BRefValue;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.api.values.BXmlItem;
import io.ballerina.runtime.api.values.BXmlQName;
import org.ballerinalang.stdlib.xmldata.utils.Constants;
import org.ballerinalang.stdlib.xmldata.utils.XmlDataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * Common utility methods used for JSON manipulation.
 *
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public class JsonToXml {

    private static final String XSI_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String XSI_PREFIX = "xsi";
    private static final String NIL = "nil";

    /**
     * Converts a JSON to the corresponding XML representation.
     *
     * @param json    JSON record object
     * @param options option details
     * @return XML object that construct from JSON
     */
    public static Object fromJson(Object json, BMap<BString, BString> options) {
        try {
            String attributePrefix = (options.get(StringUtils.fromString(Constants.OPTIONS_ATTRIBUTE_PREFIX))).
                    getValue();
            String arrayEntryTag = (options.get(StringUtils.fromString(Constants.OPTIONS_ARRAY_ENTRY_TAG))).
                    getValue();
            return convertToXML(json, attributePrefix, arrayEntryTag);
        } catch (Exception e) {
            return XmlDataUtils.getTimeError(e.getMessage());
        }
    }

    /**
     * Converts given JSON object to the corresponding XML.
     *
     * @param json            JSON object to get the corresponding XML
     * @param attributePrefix String prefix used for attributes
     * @param arrayEntryTag   String used as the tag in the arrays
     * @return BXml XML representation of the given JSON object
     */
    public static BXml convertToXML(Object json, String attributePrefix, String arrayEntryTag) {
        if (json == null) {
            return ValueCreator.createXmlSequence();
        }

        List<BXml> xmlElemList = traverseTree(json, attributePrefix, arrayEntryTag);
        if (xmlElemList.size() == 1) {
            return xmlElemList.get(0);
        } else {
            ArrayList<BXml> seq = new ArrayList<>(xmlElemList);
            return ValueCreator.createXmlSequence(seq);
        }
    }

    // Private methods

    /**
     * Traverse a JSON root node and produces the corresponding XML items.
     *
     * @param json            to be traversed
     * @param attributePrefix String prefix used for attributes
     * @param arrayEntryTag   String used as the tag in the arrays
     * @return List of XML items generated during the traversal.
     */
    private static List<BXml> traverseTree(Object json, String attributePrefix, String arrayEntryTag) {
        List<BXml> xmlArray = new ArrayList<>();
        if (!(json instanceof BRefValue)) {
            BXml xml = (BXml) XmlUtils.parse(json.toString());
            xmlArray.add(xml);
        } else {
            traverseJsonNode(json, null, null, xmlArray, attributePrefix, arrayEntryTag);
        }
        return xmlArray;
    }

    /**
     * Traverse a JSON node ad produces the corresponding xml items.
     *
     * @param json               to be traversed
     * @param nodeName           name of the current traversing node
     * @param parentElement      parent element of the current node
     * @param xmlElemList List of XML items generated
     * @param attributePrefix    String prefix used for attributes
     * @param arrayEntryTag      String used as the tag in the arrays
     * @return List of XML items generated during the traversal.
     */
    @SuppressWarnings("rawtypes")
    private static BXmlItem traverseJsonNode(Object json, String nodeName, BXmlItem parentElement,
                                             List<BXml> xmlElemList, String attributePrefix,
                                             String arrayEntryTag) {
        BXmlItem currentRoot = null;
        if (nodeName != null) {
            // Extract attributes and set to the immediate parent.
            if (nodeName.startsWith(attributePrefix)) {
                if (json instanceof BRefValue) {
                    throw XmlDataUtils.getTimeError("attribute cannot be an object or array");
                }
                if (parentElement != null) {
                    String attributeKey = nodeName.substring(1);
                    parentElement.setAttribute(attributeKey, null, null, json.toString());
                }
                return parentElement;
            }

            // Validate whether the tag name is an XML supported qualified name, according to the XML recommendation.
            XmlUtils.validateXmlName(nodeName);

            BXmlQName tagName = ValueCreator.createXmlQName(StringUtils.fromString(nodeName));
            currentRoot = (BXmlItem) ValueCreator.createXmlValue(tagName, (BString) null);
        }

        if (json == null) {
            currentRoot.setAttribute(NIL, XSI_NAMESPACE, XSI_PREFIX, "true");
        } else {
            BMap<BString, Object> map;

            Type type = TypeUtils.getType(json);
            switch (type.getTag()) {

                case TypeTags.MAP_TAG:
                    if (((MapType) type).getConstrainedType().getTag() != TypeTags.JSON_TAG) {
                        throw XmlDataUtils.getTimeError("error in converting map<non-json> to xml");
                    }
                    map = (BMap<BString, Object>) json;
                    for (Entry<BString, Object> entry : map.entrySet()) {
                        currentRoot = traverseJsonNode(entry.getValue(), entry.getKey().getValue(),
                                currentRoot, xmlElemList, attributePrefix, arrayEntryTag);
                        if (nodeName == null) { // Outermost object
                            xmlElemList.add(currentRoot);
                            currentRoot = null;
                        }
                    }
                    break;
                case TypeTags.JSON_TAG:
                    map = (BMap) json;
                    for (Entry<BString, Object> entry : map.entrySet()) {
                        currentRoot = traverseJsonNode(entry.getValue(), entry.getKey().getValue(), currentRoot,
                                xmlElemList, attributePrefix, arrayEntryTag);
                        if (nodeName == null) { // Outermost object
                            xmlElemList.add(currentRoot);
                            currentRoot = null;
                        }
                    }
                    break;
                case TypeTags.ARRAY_TAG:
                    BArray array = (BArray) json;
                    for (int i = 0; i < array.size(); i++) {
                        currentRoot = traverseJsonNode(array.getRefValue(i), arrayEntryTag, currentRoot,
                                xmlElemList, attributePrefix, arrayEntryTag);
                        if (nodeName == null) { // Outermost array
                            xmlElemList.add(currentRoot);
                            currentRoot = null;
                        }
                    }
                    break;
                case TypeTags.INT_TAG:
                case TypeTags.FLOAT_TAG:
                case TypeTags.DECIMAL_TAG:
                case TypeTags.STRING_TAG:
                case TypeTags.BOOLEAN_TAG:
                    if (currentRoot == null) {
                        throw XmlDataUtils.getTimeError("error in converting json to xml");
                    }

                    BXml text = ValueCreator.createXmlText(json.toString());
                    addChildElem(currentRoot, text);
                    break;
                default:
                    throw XmlDataUtils.getTimeError("error in converting json to xml");
            }
        }

        // Set the current constructed root the parent element
        if (parentElement != null) {
            addChildElem(parentElement, currentRoot);
            currentRoot = parentElement;
        }
        return currentRoot;
    }

    private static void addChildElem(BXmlItem currentRoot, BXml child) {
        currentRoot.getChildrenSeq().getChildrenList().add(child);
    }
}
