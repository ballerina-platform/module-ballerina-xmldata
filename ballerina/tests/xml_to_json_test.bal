// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/test;

@test:Config {
    groups: ["toJson"]
}
isolated function testFromXML() {
    var x1 = xml `<!-- outer comment -->`;
    var x2 = xml `<name>supun</name>`;
    xml x3 = x1 + x2;
    json|Error j = toJson(x3);
    if (j is json) {
        test:assertEquals(j.toJsonString(), "{\"name\":\"supun\"}", msg = "testFromXML result incorrect");
    } else {
        test:assertFail("testFromXML result is not json");
    }
}

@test:Config {
    groups: ["toJson"]
}
isolated function testFromXML2() {
    json|Error j = toJson(xml `foo`);
    if (j is json) {
        test:assertEquals(j.toJsonString(), "foo", msg = "testFromXML result incorrect");
    } else {
        test:assertFail("testFromXML2 result is not json");
    }
}

xml e = xml `<Invoice xmlns="example.com" attr="attr-val" xmlns:ns="ns.com" ns:attr="ns-attr-val">
        <PurchesedItems>
            <PLine><ItemCode>223345</ItemCode><Count>10</Count></PLine>
            <PLine><ItemCode>223300</ItemCode><Count>7</Count></PLine>
            <PLine><ItemCode discount="22%">200777</ItemCode><Count>7</Count></PLine>
        </PurchesedItems>
        <Address xmlns="">
            <StreetAddress>20, Palm grove, Colombo 3</StreetAddress>
            <City>Colombo</City>
            <Zip>00300</Zip>
            <Country>LK</Country>
        </Address>
    </Invoice>`;

@test:Config {
    groups: ["toJson"]
}
function testComplexXMLElementToJson() returns Error? {
    json j = check toJson(e);
    test:assertEquals(j.toJsonString(), "{\"Invoice\":[\"\\n        \", " +
                        "{\"PurchesedItems\":" +
                        "[\"\\n            \", " +
                        "{\"PLine\":[{\"ItemCode\":\"223345\", \"@xmlns\":\"example.com\"}, " +
                        "{\"Count\":\"10\", \"@xmlns\":\"example.com\"}], \"@xmlns\":\"example.com\"}, " +
                        "\"\\n            \", " +
                        "{\"PLine\":[{\"ItemCode\":\"223300\", \"@xmlns\":\"example.com\"}, " +
                        "{\"Count\":\"7\", \"@xmlns\":\"example.com\"}], \"@xmlns\":\"example.com\"}, " +
                        "\"\\n            \", {\"PLine\":[{\"ItemCode\":\"200777\", \"@xmlns\":\"example.com\", " +
                        "\"@discount\":\"22%\"}, " +
                        "{\"Count\":\"7\", \"@xmlns\":\"example.com\"}], \"@xmlns\":\"example.com\"}, " +
                        "\"\\n        \"], \"@xmlns\":\"example.com\"}, \"\\n        \", " +
                        "{\"Address\":[\"\\n            \", " +
                        "{\"StreetAddress\":\"20, Palm grove, Colombo 3\"}, \"\\n            \", " +
                        "{\"City\":\"Colombo\"}, \"\\n            \", {\"Zip\":\"00300\"}, \"\\n            \", " +
                        "{\"Country\":\"LK\"}, \"\\n        \"], \"@xmlns\":\"\"}, \"\\n    \"], " +
                        "\"@xmlns\":\"example.com\", \"@xmlns:ns\":\"ns.com\", \"@attr\":\"attr-val\", " +
                        "\"@ns:attr\":\"ns-attr-val\"}", msg = "testComplexXMLElementToJson result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testComplexXMLElementToJsonNoPreserveNS() returns Error? {
    json j = check toJson(e, { preserveNamespaces: false });
    test:assertEquals(j.toJsonString(), "{\"Invoice\":[\"\\n        \", " +
                        "{\"PurchesedItems\":[\"\\n            \", " +
                        "{\"PLine\":[{\"ItemCode\":\"223345\"}, {\"Count\":\"10\"}]}, " +
                        "\"\\n            \", {\"PLine\":[{\"ItemCode\":\"223300\"}, {\"Count\":\"7\"}]}, " +
                        "\"\\n            \", {\"PLine\":[{\"ItemCode\":\"200777\", \"@discount\":\"22%\"}, " +
                        "{\"Count\":\"7\"}]}, \"\\n        \"]}, \"\\n        \", {\"Address\":[\"\\n            \", " +
                        "{\"StreetAddress\":\"20, Palm grove, Colombo 3\"}, \"\\n            \", " +
                        "{\"City\":\"Colombo\"}, \"\\n            \", {\"Zip\":\"00300\"}, \"\\n            \", " +
                        "{\"Country\":\"LK\"}, \"\\n        \"]}, " +
                        "\"\\n    \"], \"@ns\":\"ns.com\", \"@attr\":\"ns-attr-val\"}",
                        msg = "testFromXML result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
isolated function testUsingConvertedJsonValue() returns Error|error? {
    json j = check toJson(xml `<Element><A>BCD</A><A>ZZZ</A></Element>`);
    json[] ar = <json[]>(check j.Element.A);
    test:assertEquals((<string> ar[0]) + ":" + (<string> ar[1]), "BCD:ZZZ", msg = "testFromXML result incorrect");
}

type PInfo record {
    string name;
    string age;
    string gender;
};

@test:Config {
    groups: ["toJson"]
}
isolated function testXmlToJsonToPInfo() returns Error|error? {
    json j = check toJson(
        xml `<PInfo><name>Jane</name><age>33</age><gender>not-specified</gender></PInfo>`);
    json k =  check j.PInfo;
    PInfo p = check k.cloneWithType(PInfo);
    test:assertEquals(p.toString(), "{\"name\":\"Jane\",\"age\":\"33\",\"gender\":\"not-specified\"}",
    msg = "testXmlToJsonToPInfo result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
isolated function testXMLWithEmptyChildren() returns Error? {
    xml x = xml `<foo><bar>2</bar><car></car></foo>`;
    json j = check toJson(x);
    test:assertEquals(j.toJsonString(), "{\"foo\":{\"bar\":\"2\", \"car\":\"\"}}",
    msg = "testXMLWithEmptyChildren result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
isolated function testXMLToJosnArray() returns Error? {
    xml x = xml `<Root><A/><B/><C/></Root>`;
    json j = check toJson(x);
    json expected = {"Root": {"A":"", "B":"", "C":""}};
    test:assertEquals(j, expected, msg = "testXMLToJosnArray result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
isolated function testXMLSameKeyToJosnArray() returns Error? {
    xml x = xml `<Root><A>A</A><A>B</A><A>C</A></Root>`;
    json j = check toJson(x);
    json expected = {"Root": {"A":["A", "B", "C"]}};
    test:assertEquals(j, expected, msg = "testXMLSameKeyToJosnArray result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
isolated function testXMLSameKeyWithAttrToJsonArray() returns Error? {
    xml x = xml `<Root><A attr="hello">A</A><A attr="name">B</A><A>C</A></Root>`;
    json j = check toJson(x);
    json expected = {"Root": [{"A":"A", "@attr":"hello"}, {"A":"B", "@attr":"name"}, {"A": "C"}]};
    test:assertEquals(j, expected, msg = "testXMLSameKeyWithAttrToJsonArray result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
isolated function testXMLElementWithMultipleAttributesAndNamespaces() returns Error? {
    xml x = xml `<Root xmlns:ns="ns.com" ns:x="y" x="z"/>`;
    json j = check toJson(x);
    json expected = {"Root": {"@xmlns:ns":"ns.com", "@ns:x":"y", "@x":"z"}};
    test:assertEquals(j, expected, msg = "testXMLElementWithMultipleAttributesAndNamespaces result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testSequenceOfSameElementNamedItems() {
    test:assertEquals(convertChildrenToJson("<root><hello>1</hello><hello>2</hello></root>"),
    "{\"hello\":[\"1\", \"2\"]}", msg = "testSequenceOfSameElementNamedItems result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testSequenceOfDifferentElementNamedItems() {
    test:assertEquals(convertChildrenToJson("<root><hello-0>1</hello-0><hello-1>2</hello-1></root>"),
    "{\"hello-0\":\"1\", \"hello-1\":\"2\"}",
    msg = "testSequenceOfDifferentElementNamedItems result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testElementWithDifferentNamedChildrenElementItems() {
    test:assertEquals(convertToJson("<root><hello-0>1</hello-0><hello-1>2</hello-1></root>"),
    "{\"root\":{\"hello-0\":\"1\", \"hello-1\":\"2\"}}",
    msg = "testElementWithDifferentNamedChildrenElementItems result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testElementWithSameNamedChildrenElementItems() {
    test:assertEquals(convertToJson("<root><hello>1</hello><hello>2</hello></root>"),
    "{\"root\":{\"hello\":[\"1\", \"2\"]}}",
    msg = "testElementWithSameNamedChildrenElementItems result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testElementWithSameNamedChildrenElementItemsWithNonConvertible() {
    test:assertEquals(convertToJson("<root><hello>1</hello><!--cmnt--><hello>2</hello></root>"),
    "{\"root\":{\"hello\":[\"1\", \"2\"]}}",
    msg = "testElementWithSameNamedChildrenElementItemsWithNonConvertible result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testElementWithSameNamedChildrenElementItemsWithNonConvertibleBegin() {
    test:assertEquals(convertToJson("<root><!--cmnt--><hello>1</hello><hello>2</hello></root>"),
    "{\"root\":{\"hello\":[\"1\", \"2\"]}}",
    msg = "testElementWithSameNamedChildrenElementItemsWithNonConvertibleBegin result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testElementWithSameNamedChildrenElementItemsWithNonConvertibleEnd() {
    test:assertEquals(convertToJson("<root><hello>1</hello><hello>2</hello><!--cmnt--></root>"),
    "{\"root\":{\"hello\":[\"1\", \"2\"]}}",
    msg = "testElementWithSameNamedChildrenElementItemsWithNonConvertibleEnd result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testElementWithSameNamedEmptyChildren() {
    test:assertEquals(convertToJson("<root><hello attr0=\"hello\"></hello><hello></hello></root>"),
    "{\"root\":{\"hello\":[{\"hello\":{\"@attr0\":\"hello\"}}, []]}}",
    msg = "testElementWithSameNamedEmptyChildren result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testComplexXMLtoJson() {
    test:assertEquals(convertToJson(
    "<Invoice xmlns=\"example.com\" attr=\"attr-val\" xmlns:ns=\"ns.com\" ns:attr=\"ns-attr-val\">\n" +
                            "        <PurchesedItems>\n" +
                            "            <PLine><ItemCode>223345</ItemCode><Count>10</Count></PLine>\n" +
                            "            <PLine><ItemCode>223300</ItemCode><Count>7</Count></PLine>\n" +
                            "            <PLine><ItemCode discount=\"22%\">200777</ItemCode><Count>7</Count></PLine>\n" +
                            "        </PurchesedItems>\n" +
                            "        <Address xmlns=\"\">\n" +
                            "            <StreetAddress>20, Palm grove, Colombo 3</StreetAddress>\n" +
                            "            <City>Colombo</City>\n" +
                            "            <Zip>00300</Zip>\n" +
                            "            <Country>LK</Country>\n" +
                            "        </Address>\n" +
                            "    </Invoice>"),
    "{\"Invoice\":{\"Invoice\":[\"\\n        \", {\"PurchesedItems\":[\"\\n            \", " +
                            "{\"PLine\":[{\"ItemCode\":\"223345\", \"@xmlns\":\"example.com\"}, " +
                            "{\"Count\":\"10\", \"@xmlns\":\"example.com\"}], \"@xmlns\":\"example.com\"}, " +
                            "\"\\n            \", {\"PLine\":[{\"ItemCode\":\"223300\", \"@xmlns\":\"example.com\"}, " +
                            "{\"Count\":\"7\", \"@xmlns\":\"example.com\"}], \"@xmlns\":\"example.com\"}, " +
                            "\"\\n            \", {\"PLine\":[{\"ItemCode\":\"200777\", " +
                            "\"@xmlns\":\"example.com\", \"@discount\":\"22%\"}, " +
                            "{\"Count\":\"7\", \"@xmlns\":\"example.com\"}], \"@xmlns\":\"example.com\"}, " +
                            "\"\\n        \"], \"@xmlns\":\"example.com\"}, \"\\n        \", " +
                            "{\"Address\":[\"\\n            \", " +
                            "{\"StreetAddress\":\"20, Palm grove, Colombo 3\"}, \"\\n            \", " +
                            "{\"City\":\"Colombo\"}, \"\\n            \", {\"Zip\":\"00300\"}, \"\\n            \", " +
                            "{\"Country\":\"LK\"}, \"\\n        \"], \"@xmlns\":\"\"}, \"\\n    \"], " +
                            "\"@xmlns\":\"example.com\", \"@attr\":\"attr-val\", \"@ns:attr\":\"ns-attr-val\", " +
                            "\"@xmlns:ns\":\"ns.com\"}}",
    msg = "testComplexXMLtoJson result incorrect");
}

public function convertToJson(string xmlStr) returns string = @java:Method {
    'class: "io/ballerina/stdlib/xmldata/testutils/XmlDataTestUtils"
} external;

public function convertChildrenToJson(string xmlStr) returns string = @java:Method {
    'class: "io/ballerina/stdlib/xmldata/testutils/XmlDataTestUtils"
} external;
