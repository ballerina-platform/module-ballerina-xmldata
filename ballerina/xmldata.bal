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

# Represents a record type to provide configurations for the JSON to XML
# conversion.
#
# + attributePrefix - The attribute prefix to use in the XML representation
# + arrayEntryTag - The XML tag to add an element from a JSON array
public type JsonOptions record {
    string attributePrefix = "@";
    string arrayEntryTag = "item";
};

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
# successfully converted or else an `xmldata:Error`
public isolated function fromJson(json jsonValue, JsonOptions options = {}) returns xml?|Error {
    map<string> allNamespaces = {};
    if !isSingleNode(jsonValue) {
        addNamespaces(allNamespaces, check getNamespacesMap(jsonValue, {}, options));
        return getElement("root", check traverseNode(jsonValue, allNamespaces, {}, options), allNamespaces,
                            check getAttributesMap(jsonValue, allNamespaces, options = options));
    } else {
        map<json>|error jMap = jsonValue.ensureType();
        if jMap is map<json> {
            if jMap.length() == 0 {
                return xml ``;
            }
            json value = jMap.toArray()[0];
            addNamespaces(allNamespaces, check getNamespacesMap(value, {}, options));
            if value is json[] {
                return getElement("root", check traverseNode(value, allNamespaces, {}, options, jMap.keys()[0]),
                                allNamespaces, check getAttributesMap(value, allNamespaces, options = options));
            } else {
                string key = jMap.keys()[0];
                if key == CONTENT {
                    return xml:createText(value.toString());
                }
                return getElement(jMap.keys()[0], check traverseNode(value, allNamespaces, {}, options), allNamespaces,
                                check getAttributesMap(value, allNamespaces, options = options));
            }
        }
        if jsonValue !is null {
            return xml:createText(jsonValue.toString());
        } else {
            return xml ``;
        }
    }
}

isolated function traverseNode(json jNode, map<string> allNamespaces, map<string> parentNamespaces,
                               JsonOptions options = {}, string? key = ()) returns xml|Error {
    string arrayEntryTag = options.arrayEntryTag == "" ? "item" : options.arrayEntryTag;
    string attributePrefix = options.attributePrefix == "" ? "@" : options.attributePrefix;
    map<string> namespacesOfElem = {};
    xml xNode = xml ``;
    if jNode is map<json> {
        foreach [string, json] [k, v] in jNode.entries() {
            if !k.startsWith(attributePrefix) {
                if k == CONTENT {
                    xNode += xml:createText(v.toString());
                } else {
                    namespacesOfElem = check getNamespacesMap(v, parentNamespaces, options);
                    addNamespaces(allNamespaces, namespacesOfElem);
                    if v is json[] {
                        xml node = check traverseNode(v, allNamespaces, namespacesOfElem, options, k);
                        xNode += node;
                    } else {
                        xml node = check getElement(k, check traverseNode(v, allNamespaces, namespacesOfElem, options),
                                                    allNamespaces,
                                                    check getAttributesMap(v, allNamespaces, parentNamespaces,
                                                            options = options));
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
            } else {
                arrayEntryTagKey = arrayEntryTag;
            }
            namespacesOfElem = check getNamespacesMap(i, parentNamespaces, options);
            addNamespaces(allNamespaces, namespacesOfElem);
            xml item = check getElement(arrayEntryTagKey, check traverseNode(i, allNamespaces, namespacesOfElem),
                                        allNamespaces,
                                        check getAttributesMap(i, allNamespaces, parentNamespaces, options = options));
            xNode += item;
        }
    } else {
        xNode = xml:createText(jNode.toString());
    }
    return xNode;
}

isolated function isSingleNode(json node) returns boolean {
    map<json>|error jMap = node.ensureType();
    if jMap is map<json> && jMap.length() > 1 {
        return false;
    }
    if node is json[] {
        return false;
    }
    return true;
}

isolated function getElement(string name, xml children, map<string> namespaces, map<string> attributes = {},
                            JsonOptions options = {}) returns xml|Error {
    string attributePrefix = options.attributePrefix == "" ? "@" : options.attributePrefix;
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
            element = xml:createElement(name, attributes, children);
        } else {
            return error Error("attribute cannot be an object or array");
        }
    }
    return element;
}

isolated function getAttributesMap(json jTree, map<string> namespaces, map<string> parentNamespaces = {},
                                    JsonOptions options = {}) returns map<string>|Error {
    string attributePrefix = options.attributePrefix == "" ? "@" : options.attributePrefix;
    map<string> attributes = parentNamespaces.clone();
    map<json>|error attr = jTree.ensureType();
    if attr is map<json> {
        foreach [string, json] [k, v] in attr.entries() {
            if k.startsWith(attributePrefix) {
                if v is map<json> || v is json[] {
                    return error Error("attribute cannot be an object or array");
                }
                int? index = k.indexOf(":");
                if index is int {
                    string suffix = k.substring(index + 1);
                    if k.startsWith(attributePrefix + "xmlns") {
                        attributes[string `{${XMLNS_NAMESPACE_URI}}${suffix}`] = v.toString();
                    } else {
                        string prefix = k.substring(1, index);
                        string namespaceUrl = namespaces.get(string `{${XMLNS_NAMESPACE_URI}}${prefix}`);
                        attributes[string `{${namespaceUrl}}${suffix}`] = v.toString();
                    }
                } else {
                    attributes[k.substring(1)] = v.toString();
                }
            }
        }
    }
    return attributes;
}

isolated function getNamespacesMap(json jTree, map<string> parentNamespaces = {},
                                    JsonOptions options = {}) returns map<string>|Error {
    string attributePrefix = options.attributePrefix == "" ? "@" : options.attributePrefix;
    map<string> namespaces = parentNamespaces.clone();
    map<json>|error attr = jTree.ensureType();
    if attr is map<json> {
        foreach [string, json] [k, v] in attr.entries() {
            if k.startsWith(attributePrefix) {
                if v is map<json> || v is json[] {
                    return error Error("attribute cannot be an object or array");
                }
                if k.startsWith(attributePrefix + "xmlns") {
                    string prefix = k.substring(<int>k.indexOf(":") + 1);
                    namespaces[string `{${XMLNS_NAMESPACE_URI}}${prefix}`] = v.toString();
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
public type XmlOptions record {
    string attributePrefix = "@";
    boolean preserveNamespaces = true;
};

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
# + returnType - The `typedesc` of the record that should be returned as a result
# + return - The Record representation of the given XML on success, else returns an `xmldata:Error`
public isolated function toRecord(xml xmlValue, boolean preserveNamespaces = true, typedesc<record {}> returnType = <>)
returns returnType|Error = @java:Method {
    'class: "io.ballerina.stdlib.xmldata.XmlToRecord"
} external;
