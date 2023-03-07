// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/jballerina.java;

const string XMLNS_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";
const string CONTENT = "#content";
const string ATTRIBUTE_PREFIX = "attribute_";
const string XMLNS = "xmlns";

# Defines the name of the XML element.
#
# + value - The name of the XML element
public type NameConfig record {|
    string value;
|};

# The annotation is used to specify the new name of the existing record name or field name according to the XML format.
# In the XML-record conversion, this annotation can be used to override the default XML element name using the
# `xmldata:toXML` API and validate the overridden XML element name with record field using the `xmldata:fromXml` API.
public annotation NameConfig Name on type, record field;

# Defines the namespace of the XML element
#
# + prefix - The value of the prefix of the namespace
# + uri - The value of the URI of the namespace
public type NamespaceConfig record {|
    string prefix?;
    string uri;
|};

# The annotation is used to specify the namespace's prefix and URI of the XML element.
# In the XML-record conversion, this annotation can be used to add XML namespace using the `xmldata:toXML` API and
# validate the the XML namespace with record in the `xmldata:fromXml` API.
public annotation NamespaceConfig Namespace on type;

# The annotation is used to denote the field that is considered an attribute.
# In the XML-record conversion, this annotation can be used to add XML attribute using the `xmldata:toXML` API and
# validate the XML attribute with record fields in the `xmldata:fromXml` API.
public annotation Attribute on record field;

# Converts a `Map` or `Record` representation to its XML representation.
# Additionally, when converting from a record, the `xmldata:Namespace`, `xmldata:Name`, and `xmldata:Attribute`
# annotations can be used to add `namespaces`, `name of elements', and `attributes` to XML representation.
#
# + mapValue - The `Map` or `Record` representation source to be converted to XML
# + return - XML representation of the given source if the source is
# successfully converted or else an `xmldata:Error`
public isolated function toXml(map<anydata> mapValue) returns xml|Error {
    if mapValue is map<xml>|map<xml[]> {
        return convertMapXml(mapValue);
    }
    JsonOptions jsonOption = {attributePrefix: ATTRIBUTE_PREFIX, arrayEntryTag : ""};
    typedesc<(map<anydata>)> inputType = typeof mapValue;
    json|json[]|record{} jsonValue = check getModifiedRecord(mapValue, inputType);
    if jsonValue is json[] {
        jsonOption.rootTag = jsonValue[1].toString();
        return <xml>check fromJson(jsonValue[0], jsonOption);
    }
    return <xml>check fromJson(jsonValue.toJson(), jsonOption);
}

isolated function convertMapXml(map<xml>|map<xml[]> mapValue) returns xml {
    xml xNode = xml ``;
    foreach [string, xml|xml[]] entry in mapValue.entries() {
        xml|xml[] values = entry[1];
        xml childNode = xml ``;
        if values is xml[] {
            foreach xml value in values {
                childNode += value;
            }
            xNode += xml:createElement(entry[0], {}, childNode);
        } else {
            xNode += xml:createElement(entry[0], {}, values);
        }
    }
    return xml:createElement("root", {}, xNode);
}

isolated function getModifiedRecord(map<anydata> mapValue, typedesc<(map<anydata>|json)> inputType)
returns json|json[]|record{}|Error = @java:Method {
    'class: "io.ballerina.stdlib.xmldata.utils.XmlDataUtils"
} external;

# Provides configurations for converting JSON to XML.
#
# + attributePrefix - The prefix of JSON elements' key which is to be treated as an attribute in the XML representation
# + arrayEntryTag - The name of the XML elements that represent a converted JSON array entry
# + rootTag- The name of the root element of the XML that will be created. If its value is (), and the converted XML
#            is not in the valid format, it will create a root tag as `root`
public type JsonOptions record {|
    string attributePrefix = "@";
    string arrayEntryTag = "item";
    string? rootTag = ();
|};

# Converts a JSON object to an XML representation.
# ```ballerina
# json data = {
#     name: "John",
#     age: 30
# };
# xml? xmlValue = check xmldata:fromJson(data);
# ```
#
# + jsonValue - The JSON source to be converted to XML
# + options - The `xmldata:JsonOptions` record for JSON to XML conversion properties
# + return - XML representation of the given JSON if the JSON is
# successfully converted or else an `xmldata:Error`. The `()` value is not returned
public isolated function fromJson(json jsonValue, JsonOptions options = {}) returns xml?|Error {
    string? rootTag = options.rootTag;
    map<string> allNamespaces = {};
    if !isSingleNode(jsonValue) {
        addNamespaces(allNamespaces, check getNamespacesMap(jsonValue, options, {}));
        return getElement(rootTag is string ? rootTag : "root",
                          check traverseNode(jsonValue, allNamespaces, {}, options), allNamespaces, options,
                          check getAttributesMap(jsonValue, options, allNamespaces));
    } else {
        map<json>|error jMap = jsonValue.ensureType();
        if jMap is map<json> {
            if jMap.length() == 0 {
                return xml ``;
            }
            json value = jMap.toArray()[0];
            addNamespaces(allNamespaces, check getNamespacesMap(value, options, {}));
            if value is json[] {
                return getElement(rootTag is string ? rootTag : "root",
                                  check traverseNode(value, allNamespaces, {}, options, jMap.keys()[0]),
                                  allNamespaces, options, check getAttributesMap(value, options, allNamespaces));
            } else {
                string key = jMap.keys()[0];
                if key == CONTENT {
                    return xml:createText(value.toString());
                }
                xml output = check getElement(jMap.keys()[0], check traverseNode(value, allNamespaces, {}, options),
                                            allNamespaces, options,
                                            check getAttributesMap(value, options, allNamespaces));
                if rootTag is string {
                    return xml:createElement(rootTag.toString(), {}, output);
                }
                return output;
            }
        }
        if jsonValue !is null {
            return xml:createText(jsonValue.toString());
        } else {
            return xml ``;
        }
    }
}

isolated function traverseNode(json jNode, map<string> allNamespaces, map<string> parentNamespaces, JsonOptions options,
                                string? key = ()) returns xml|Error {
    map<string> namespacesOfElem = {};
    string attributePrefix = options.attributePrefix;
    xml xNode = xml ``;
    if jNode is map<json> {
        foreach [string, json] [k, value] in jNode.entries() {
            string jsonKey = k.trim();
            if !jsonKey.startsWith(attributePrefix) {
                if jsonKey == CONTENT {
                    xNode += xml:createText(value.toString());
                } else {
                    namespacesOfElem = check getNamespacesMap(value, options, parentNamespaces);
                    addNamespaces(allNamespaces, namespacesOfElem);
                    if value is json[] {
                        xml node = check traverseNode(value, allNamespaces, namespacesOfElem, options, jsonKey);
                        xNode += node;
                    } else {
                        xml node =
                        check getElement(jsonKey, check traverseNode(value, allNamespaces, namespacesOfElem, options),
                                        allNamespaces, options,
                                        check getAttributesMap(value, options, allNamespaces, parentNamespaces));
                        xNode += node;
                    }
                }
            }
        }
    } else if jNode is json[] {
        foreach var i in jNode {
            string arrayEntryTagKey = "";
            if (key is string) {
                arrayEntryTagKey = key;
            } else if options.arrayEntryTag != "" {
                arrayEntryTagKey = options.arrayEntryTag;
            }
            namespacesOfElem = check getNamespacesMap(i, options, parentNamespaces);
            addNamespaces(allNamespaces, namespacesOfElem);
            xml item;
            if options.arrayEntryTag == "" {
                item = check getElement(arrayEntryTagKey,
                                        check traverseNode(i, allNamespaces, namespacesOfElem, options, key),
                                        allNamespaces, options,
                                        check getAttributesMap(i, options, allNamespaces, parentNamespaces));
            } else {
                item = check getElement(arrayEntryTagKey,
                                        check traverseNode(i, allNamespaces, namespacesOfElem, options),
                                        allNamespaces, options,
                                        check getAttributesMap(i, options, allNamespaces, parentNamespaces));
            }
            xNode += item;
        }
    } else {
        xNode = xml:createText(jNode.toString());
    }
    return xNode;
}

isolated function isSingleNode(json node) returns boolean {
    map<anydata>|error jMap = node.ensureType();
    if jMap is map<anydata> && jMap.length() > 1 {
        return false;
    }
    if node is json[] {
        return false;
    }
    return true;
}

isolated function getElement(string name, xml children, map<string> namespaces, JsonOptions options,
                            map<string> attributes = {}) returns xml|Error {
    string attributePrefix = options.attributePrefix;
    xml:Element element;
    int? index = name.indexOf(":");
    if index is int {
        string prefix = name.substring(0, index);
        string elementName = name.substring(index + 1, name.length());
        string namespaceUrl = attributes[string `{${XMLNS_NAMESPACE_URI}}${prefix}`].toString();
        if namespaceUrl == "" {
            namespaceUrl = namespaces[string `{${XMLNS_NAMESPACE_URI}}${prefix}`].toString();
            if namespaceUrl != "" {
                attributes[string `{${XMLNS_NAMESPACE_URI}}${prefix}`] = namespaceUrl;
            }
        }
        if namespaceUrl == "" {
            element = xml:createElement(elementName, attributes, children);
        } else {
            element = xml:createElement(string `{${namespaceUrl}}${elementName}`, attributes, children);
        }
    } else {
        if !name.startsWith(attributePrefix) {
            map<string> newAttributes = attributes;
            if newAttributes.hasKey(string `{${XMLNS_NAMESPACE_URI}}`) {
                string value = newAttributes.get(string `{${XMLNS_NAMESPACE_URI}}`);
                _ = newAttributes.remove(string `{${XMLNS_NAMESPACE_URI}}`);
                newAttributes[XMLNS] = value;
            }
            element = xml:createElement(name, newAttributes, children);
        } else {
            return error Error("attribute cannot be an object or array");
        }
    }
    return element;
}

isolated function getAttributesMap(json jTree, JsonOptions options, map<string> namespaces,
                                    map<string> parentNamespaces = {}) returns map<string>|Error {
    map<string> attributes = parentNamespaces.clone();
    map<json>|error attr = jTree.ensureType();
    string attributePrefix = options.attributePrefix;
    if attr is map<json> {
        foreach [string, json] [k, v] in attr.entries() {
            if k.startsWith(attributePrefix) {
                if v is map<json> || v is json[] {
                    return error Error("attribute cannot be an object or array");
                }
                int? index = k.indexOf(":");
                if index is int {
                    string suffix = k.substring(index + 1);
                    if k.startsWith(attributePrefix + XMLNS) {
                        attributes[string `{${XMLNS_NAMESPACE_URI}}${suffix}`] = v.toString();
                    } else {
                        int startIndex = getStartIndex(attributePrefix, k);
                        string prefix = k.substring(startIndex, index);
                        string namespaceUrl = namespaces.get(string `{${XMLNS_NAMESPACE_URI}}${prefix}`);
                        attributes[string `{${namespaceUrl}}${suffix}`] = v.toString();
                    }
                } else {
                    if k == attributePrefix + XMLNS {
                        attributes[XMLNS] = v.toString();
                    } else {
                        int startIndex = getStartIndex(attributePrefix, k);
                        attributes[k.substring(startIndex)] = v.toString();
                    }
                }
            }
        }
    }
    return attributes;
}

isolated function getStartIndex(string attributePrefix, string key) returns int {
    int startIndex = 1;
    if (attributePrefix is ATTRIBUTE_PREFIX) {
        int? location = key.indexOf("_");
        if (location is int) {
            startIndex = location + 1;
        }
    }
    return startIndex;
}

isolated function getNamespacesMap(json jTree, JsonOptions options, map<string> parentNamespaces = {})
                            returns map<string>|Error {
    map<string> namespaces = parentNamespaces.clone();
    map<json>|error attr = jTree.ensureType();
    string attributePrefix = options.attributePrefix;
    if attr is map<json> {
        foreach [string, json] [k, v] in attr.entries() {
            if k.startsWith(attributePrefix) {
                if v is map<json> || v is json[] {
                    return error Error("attribute cannot be an object or array.");
                }
                if k.startsWith(attributePrefix + XMLNS) {
                    int? index = k.indexOf(":");
                    if index is int {
                        string prefix = k.substring(index + 1);
                        namespaces[string `{${XMLNS_NAMESPACE_URI}}${prefix}`] = v.toString();
                    } else {
                        namespaces[string `{${XMLNS_NAMESPACE_URI}}`] = v.toString();
                    }
                }
            }
        }
    }
    return namespaces;
}

isolated function addNamespaces(map<string> allNamespaces, map<string> namespaces) {
    foreach [string, string] namespace in namespaces.entries() {
        allNamespaces[namespace[0]] = namespace[1];
    }
}

# Provides configurations for converting XML to JSON.
#
# + attributePrefix - Attribute prefix used in the XML
# + preserveNamespaces - Instructs whether to preserve the namespaces of the XML when converting
public type XmlOptions record {|
    string attributePrefix = "@";
    boolean preserveNamespaces = true;
|};

# Converts an XML object to its JSON representation.
# ```ballerina
# xml xmlValue = xml `<!-- outer comment -->` + xml `<name>supun</name>`;
# json jsonValue = check xmldata:toJson(xmlValue);
# ```
#
# + xmlValue - The XML source to be converted to JSON
# + options - The `xmldata:XmlOptions` record consisting of the configurations for the conversion
# + return - The JSON representation of the given XML on success, else returns an `xmldata:Error`
public isolated function toJson(xml xmlValue, XmlOptions options = {}) returns json|Error = @java:Method {
    'class: "io.ballerina.stdlib.xmldata.XmlToJson"
} external;

# Converts an XML to its Record representation.
# ```ballerina
# type Person record {
#     string name;
# };
# xml xmlValue = xml `<!-- outer comment -->` + xml `<name>Alex</name>`;
# Person|xmldata:Error person = xmldata:toRecord(xmlValue);
# ```
#
# + xmlValue - The XML source to be converted to a Record
# + preserveNamespaces - Instructs whether to preserve the namespaces of the XML when converting
# + returnType - The `typedesc` of the record that should be returned as a result.
#                The optional value fields are not allowed in the record type.
# + return - The Record representation of the given XML on success, else returns an `xmldata:Error`
# # Deprecated
# This function is going away in a future release. Use `fromXml` instead.
@deprecated
public isolated function toRecord(xml xmlValue, boolean preserveNamespaces = true, typedesc<record {}> returnType = <>)
returns returnType|Error = @java:Method {
    'class: "io.ballerina.stdlib.xmldata.XmlToRecord"
} external;

# Converts an XML to its `Map` or `Record` representation.
# Additionally, when converting to a record, XML `namespaces`, `name of elements` and `attributes` can be validated
# through `xmldata:Namespace`, `xmldata:Name` and ``xmldata:Attribute` annotations.
#
# + xmlValue - The XML source to be converted to a given target type. If the XML elements have a prefix,
#              the mapping field names of the record must also have the same prefix.
# + returnType - The `typedesc` of the returned value. this should be either `map` or `record` type.
# + return - The given target type representation of the given XML on success,
#            else returns an `xmldata:Error`
public isolated function fromXml(xml xmlValue, typedesc<map<anydata>> returnType = <>)
returns returnType|Error = @java:Method {
    'class: "io.ballerina.stdlib.xmldata.MapFromXml"
} external;
