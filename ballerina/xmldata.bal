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

# Represents a record type to provide configurations for the JSON to XML
# conversion.
#
# + attributePrefix - The attribute prefix to use in the XML representation
# + arrayEntryTag - The XML tag to add an element from a JSON array
public type JsonOptions record {
    string attributePrefix = "@";
    string arrayEntryTag = "root";
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
# + options - The `xmldata:xmldata:JsonOptions` record for JSON to XML conversion properties
# + return - XML representation of the given JSON if the JSON is
# successfully converted or else an `xmldata:Error`
public isolated function fromJson(json jsonValue, JsonOptions options = {}) returns xml?|Error {

    if !isLeafNode(jsonValue) {
        return getElement("root", check traverseNode(jsonValue), check getAttributesMap(jsonValue));
    } else {
        map<json>|error jMap = jsonValue.ensureType();
        if jMap is map<json> {
            if jMap.length() == 0 {
                return xml ``;
            }
            return getElement(jMap.keys()[0], check traverseNode(jMap.toArray()[0]));
        }
    }
    return error Error("failed to parse xml");
}

isolated function traverseNode(json jNode) returns xml|Error {
    xml xNode = xml ``;
    if jNode is map<json> {
        foreach [string, json] [k, v] in jNode.entries() {
            if !k.startsWith("@") {
                xml node = check getElement(k, check traverseNode(v), check getAttributesMap(v));
                xNode += node;
            }
        }
    } else if jNode is json[] {
        foreach var i in jNode {
            xml item = check getElement("item", check traverseNode(i), check getAttributesMap(i));
            xNode += item;
        }
    } else {
        xNode = xml:createText(jNode.toString());
    }
    return xNode;
}

isolated function isLeafNode(json node) returns boolean {
    map<json>|error jMap = node.ensureType();
    if jMap is map<json> {
        if jMap.length() > 1 {
            return false;
        } else if jMap.length() == 1 {
            if jMap.toArray()[0] is map<json> || jMap.toArray()[0] is json[] {
                return false;
            }
        }
    }
    if node is json[] {
        return false;
    }
    return true;
}

isolated function getElement(string name, xml children, map<string> attributes = {}) returns xml|Error {
    xml:Element element;
    string namespaceUrl = XMLNS_NAMESPACE_URI;
    int? index = name.indexOf(":");
    if index is int {
        string prefix = name.substring(0, index);
        string elementName = name.substring(index + 1, name.length());
        element = xml:createElement(string `{${namespaceUrl}}${elementName}`);
        map<string> atMap = element.getAttributes();
        atMap[string `{http://www.w3.org/2000/xmlns/}${prefix}`] = namespaceUrl;
        _ = atMap.remove("{http://www.w3.org/2000/xmlns/}xmlns");
    } else {
        if (!name.startsWith("@")) {
            element = xml:createElement(name, attributes, children);
        } else {
            return error Error("attribute cannot be an object or array");
        }
    }
    map<string> attr = element.getAttributes();
    foreach [string, string] [k, v] in attributes.entries() {
        if !k.includes(":") {
            attr[k.substring(1)] = v;
        }
    }
    return element;
}

isolated function getAttributesMap(json jTree) returns map<string>|Error {
    map<string> attributes = {};
    map<json>|error attr = jTree.ensureType();
    if attr is map<json> {
        foreach [string, json] [k, v] in attr.entries() {
            if k.startsWith("@") {
                if v is map<json> || v is json[] {
                    return error Error("attribute cannot be an object or array");
                }
                attributes[k] = v.toString();
            }
        }
    }
    return attributes;
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
