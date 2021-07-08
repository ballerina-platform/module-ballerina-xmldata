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
# xml|Error xmlValue = xmldata:fromJson(data);
# ```
#
# + jsonValue - The JSON source
# + options - The `xmldata:JsonOptions` record for JSON to XML conversion properties
# + return - XML representation of the given JSON if the JSON is
#            successfully converted or else an `error`
public isolated function fromJson(json? jsonValue, JsonOptions options = {}) returns xml|Error = @java:Method {
    'class: "io.ballerina.stdlib.xmldata.JsonToXml"
} external;

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
# json|Error jsonValue = toJson(xmlValue);
# ```
#
# + xmlValue - The XML source to be converted to JSON
# + options - The `XmlOptions` record consisting of the configurations for the conversion
# + return - The JSON representation of the given XML on success, else returns an `error`
public isolated function toJson(xml xmlValue, XmlOptions options = {}) returns json|Error = @java:Method {
    'class: "io.ballerina.stdlib.xmldata.XmlToJson"
} external;
