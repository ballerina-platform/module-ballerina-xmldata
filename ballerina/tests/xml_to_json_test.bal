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
isolated function testFromXML() returns error? {
    var x1 = xml `<!-- outer comment -->`;
    var x2 = xml `<name>supun</name>`;
    xml x3 = x1 + x2;
    json j = check toJson(x3);
    test:assertEquals(j, {"name":"supun"}, msg = "testFromXML result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
isolated function testtoJson() returns error? {
    var x1 = xml `<!-- outer comment -->`;
    var x2 = xml `<name>"supun"</name>`;
    xml x3 = x1 + x2;
    json j = check toJson(x3);
    test:assertEquals(j, {"name":"\"supun\""}, msg = "testtoJson result incorrect");
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
    json expectedOutput = {"Invoice":{"PurchesedItems":{"PLine":[{"ItemCode":"223345","Count":"10"},
                          {"ItemCode":"223300","Count":"7"},{"ItemCode":{"@discount":"22%","#content":"200777"},
                          "Count":"7"}]},"Address":{"StreetAddress":"20, Palm grove, Colombo 3",
                          "City":"Colombo","Zip":"00300","Country":"LK","@xmlns":""},
                          "@xmlns:ns":"ns.com","@xmlns":"example.com","@attr":"attr-val","@ns:attr":"ns-attr-val"}};
    test:assertEquals(j, expectedOutput, msg = "testComplexXMLElementToJson result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testComplexXMLElementToJsonNoPreserveNS() returns error? {
    json j = check toJson(e, { preserveNamespaces: false });
    json expectedOutput = {"Invoice":{"PurchesedItems":{"PLine":[{"ItemCode":"223345","Count":"10"},
                          {"ItemCode":"223300","Count":"7"},{"ItemCode":"200777","Count":"7"}]},
                          "Address":{"StreetAddress":"20, Palm grove, Colombo 3",
                          "City":"Colombo","Zip":"00300","Country":"LK"}}};
    test:assertEquals(j, expectedOutput, msg = "testComplexXMLElementToJsonNoPreserveNS result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
isolated function testUsingConvertedJsonValue() returns error? {
    json j = check toJson(xml `<Element><A>BCD</A><A>ZZZ</A></Element>`);
    json[] ar = <json[]>(check j.Element.A);
    test:assertEquals((<string> ar[0]) + ":" + (<string> ar[1]), "BCD:ZZZ",
                msg = "testUsingConvertedJsonValue result incorrect");
}

type PInfo record {
    string name;
    string age;
    string gender;
};

@test:Config {
    groups: ["toJson"]
}
isolated function testXmlToJsonToPInfo() returns error? {
    json j = check toJson(
        xml `<PInfo><name>Jane</name><age>33</age><gender>not-specified</gender></PInfo>`);
    json k =  check j.PInfo;
    PInfo p = check k.cloneWithType(PInfo);
    test:assertEquals(p, {"name":"Jane","age":"33","gender":"not-specified"},
            msg = "testXmlToJsonToPInfo result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
isolated function testXMLWithEmptyChildren() returns Error? {
    xml x = xml `<foo><bar>2</bar><car></car></foo>`;
    json j = check toJson(x);
    test:assertEquals(j, {"foo":{"bar":"2","car":""}}, msg = "testXMLWithEmptyChildren result incorrect");
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
    json expected = {"Root": {"A": [{"@attr": "hello","#content": "A"},{"@attr": "name","#content": "B"},"C"]}};
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
                            "{\"root\":{\"hello\":[\"1\", \"2\"]}}", msg =
                            "testElementWithSameNamedChildrenElementItems result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testElementWithSameNamedChildrenElementItemsWithNonConvertible() {
    test:assertEquals(convertToJson("<root><hello>1</hello><!--cmnt--><hello>2</hello></root>"),
                            "{\"root\":{\"hello\":[\"1\", \"2\"]}}", msg =
                            "testElementWithSameNamedChildrenElementItemsWithNonConvertible result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testElementWithSameNamedChildrenElementItemsWithNonConvertibleBegin() {
    test:assertEquals(convertToJson("<root><!--cmnt--><hello>1</hello><hello>2</hello></root>"),
                            "{\"root\":{\"hello\":[\"1\", \"2\"]}}", msg =
                            "testElementWithSameNamedChildrenElementItemsWithNonConvertibleBegin result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testElementWithSameNamedChildrenElementItemsWithNonConvertibleEnd() {
    test:assertEquals(convertToJson("<root><hello>1</hello><hello>2</hello><!--cmnt--></root>"),
                            "{\"root\":{\"hello\":[\"1\", \"2\"]}}", msg =
                            "testElementWithSameNamedChildrenElementItemsWithNonConvertibleEnd result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testElementWithSameNamedEmptyChildren() {
    test:assertEquals(convertToJson("<root><hello attr0=\"hello\"></hello><hello></hello></root>"),
                      "{\"root\":{\"hello\":[{\"@attr0\":\"hello\"}, \"\"]}}", msg =
                      "testElementWithSameNamedEmptyChildren result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
function testComplexXMLtoJson() {
    test:assertEquals(convertToJson(e.toString()),"{\"Invoice\":{\"PurchesedItems\":{\"PLine\":[" +
                            "{\"ItemCode\":\"223345\", \"Count\":\"10\"}, {\"ItemCode\":\"223300\", \"Count\":\"7\"}, " +
                            "{\"ItemCode\":{\"@discount\":\"22%\", \"#content\":\"200777\"}, \"Count\":\"7\"}]}, " +
                            "\"Address\":{\"StreetAddress\":\"20, Palm grove, Colombo 3\", \"City\":\"Colombo\", " +
                            "\"Zip\":\"00300\", \"Country\":\"LK\", \"@xmlns\":\"\"}, " +
                            "\"@xmlns\":\"example.com\", \"@attr\":\"attr-val\", \"@ns:attr\":\"ns-attr-val\", " +
                            "\"@xmlns:ns\":\"ns.com\"}}", msg = "testComplexXMLtoJson result incorrect");
}

@test:Config {
    groups: ["toJson"]
}
isolated function testFromXMLWithNull() returns error? {
    xml x1 = xml ``;
    json j = check toJson(x1);
    test:assertEquals(j.toJsonString(), "\"\"");
}

@test:Config {
    groups: ["toJson"]
}
isolated function testFromXMLWithComment() returns error? {
    xml x1 = xml `<?xml version="1.0" encoding="UTF-8"?>`;
    json j = check toJson(x1);
    test:assertEquals(j, {});
}

@test:Config {
    groups: ["toJson"]
}
isolated function testFromXMLWithXmlSequence() returns error? {
    xml x1 = xml `<family><root><fname>Peter</fname></root><root></root><?pi test?><!-- my comment --></family>`;
    json j = check toJson(x1);
    test:assertEquals(j, {"family":{"root":[{"fname":"Peter"},""]}});
}

@test:Config {
    groups: ["toJson"]
}
isolated function testComplexXmlWithoutNamespace() returns error? {
    xml x1 = xml `<bookStore status="online">
                    <storeName>foo</storeName>
                    <postalCode>94</postalCode>
                    <isOpen>true</isOpen>
                    <address>
                      <street>foo</street>
                      <city>94</city>
                      <country>true</country>
                    </address>
                    <codes>
                      <item>4</item>
                      <item>8</item>
                      <item>9</item>
                    </codes>
                  </bookStore>
                  <!-- some comment -->
                  <?doc document="book.doc"?>`;
    json j = check toJson(x1);
    test:assertEquals(j, {"bookStore":{"storeName":"foo","postalCode":"94","isOpen":"true",
                          "address":{"street":"foo","city":"94","country":"true"},
                          "codes":{"item":["4","8","9"]},"@status":"online"}});
}

@test:Config {
    groups: ["toJson"]
}
isolated function testComplexXmlWithNamespace() returns error?  {
    xml x1 = xml `<ns0:bookStore status="online" xmlns:ns0="http://sample.com/test">
                    <ns0:storeName>foo</ns0:storeName>
                    <ns0:postalCode>94</ns0:postalCode>
                    <ns0:isOpen>true</ns0:isOpen>
                    <ns0:address>
                      <ns0:street>foo</ns0:street>
                      <ns0:city>94</ns0:city>
                      <ns0:country>true</ns0:country>
                    </ns0:address>
                    <ns0:codes>
                      <ns0:item>4</ns0:item>
                      <ns0:item>8</ns0:item>
                      <ns0:item>9</ns0:item>
                    </ns0:codes>
                  </ns0:bookStore>
                  <!-- some comment -->
                  <?doc document="book.doc"?>`;
    json j = check toJson(x1);
    test:assertEquals(j, {"ns0:bookStore":{"ns0:storeName":"foo","ns0:postalCode":"94","ns0:isOpen":"true",
                          "ns0:address":{"ns0:street":"foo","ns0:city":"94","ns0:country":"true"},
                          "ns0:codes":{"ns0:item":["4","8","9"]},"@xmlns:ns0":"http://sample.com/test",
                          "@status":"online"}});
}

@test:Config {
    groups: ["toJson"]
}
isolated function testComplexXmlWithOutNamespace() returns error?  {
    xml x1 = xml `<ns0:bookStore status="online" xmlns:ns0="http://sample.com/test">
                    <ns0:storeName>foo</ns0:storeName>
                    <ns0:postalCode>94</ns0:postalCode>
                    <ns0:isOpen>true</ns0:isOpen>
                    <ns0:address>
                      <ns0:street>foo</ns0:street>
                      <ns0:city>94</ns0:city>
                      <ns0:country>true</ns0:country>
                    </ns0:address>
                    <ns0:codes>
                      <ns0:item>4</ns0:item>
                      <ns0:item>8</ns0:item>
                      <ns0:item>9</ns0:item>
                    </ns0:codes>
                  </ns0:bookStore>
                  <!-- some comment -->
                  <?doc document="book.doc"?>
                  <metaInfo>some info</metaInfo>`;
    json j = check toJson(x1, { preserveNamespaces: false });
    test:assertEquals(j, {"bookStore":{"storeName":"foo","postalCode":"94","isOpen":"true",
                          "address":{"street":"foo","city":"94","country":"true"},
                          "codes":{"item":["4","8","9"]}},"metaInfo":"some info"});
}

@test:Config {
    groups: ["toJson"]
}
isolated function testComplexXmlWithOutNamespace2() returns error?  {
    xml x1 = xml `<?xml version="1.0" encoding="UTF-8"?>
                  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                     <soapenv:Header />
                     <soapenv:Body>
                        <ns:getSimpleQuoteResponse xmlns:ns="http://services.samples">
                           <ns:return xmlns:ax21="http://services.samples/xsd"
                           xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ax21:GetQuoteResponse">
                              <ax21:change>4.49588025550579</ax21:change>
                           </ns:return>
                        </ns:getSimpleQuoteResponse>
                     </soapenv:Body>
                  </soapenv:Envelope>`;
    json j = check toJson(x1, { preserveNamespaces: false });
    test:assertEquals(j, {"Envelope":{"Header":"","Body":{"getSimpleQuoteResponse":{
                          "return":{"change":"4.49588025550579"}}}}});
}

@test:Config {
    groups: ["toJson"]
}
isolated function testComplexXmlWithOutNamespace3() returns error?  {
    xml x1 = xml `<xmlns_prefix:A xmlns:xmlns_prefix="http://sample.com/test" xmlns:xsi="http://sample.com/test">
                    <xsi:B>test_Value</xsi:B>
                  </xmlns_prefix:A>`;
    json j = check toJson(x1, { preserveNamespaces: false });
    test:assertEquals(j, {"A":{"B":"test_Value"}});
}

@test:Config {
    groups: ["toJson"]
}
isolated function testComplexXmlWithOutNamespace4() returns error?  {
    xml x1 = xml `<xmlns_prefix:A xmlns:xmlns_prefix="http://sample.com/test" xmlns:xsi="http://sample.com/test">
                    <xsi:B>test_Value</xsi:B>
                  </xmlns_prefix:A>`;
    json j = check toJson(x1);
    test:assertEquals(j, {"xmlns_prefix:A":{"xsi:B":"test_Value","@xmlns:xsi":"http://sample.com/test",
                          "@xmlns:xmlns_prefix":"http://sample.com/test"}});
}

@test:Config {
    groups: ["toJson"]
}
isolated function testXmlWithOutNamespace() returns error? {
    xml x1 = xml `<books>
                      <item>
                          <bookName>book1</bookName>
                          <bookId>101</bookId>
                      </item>
                      <item>
                          <bookName>book2</bookName>
                          <bookId>102</bookId>
                      </item>
                      <item>
                          <bookName>book3</bookName>
                          <bookId>103</bookId>
                      </item>
                  </books>`;
    json j = check toJson(x1);
    test:assertEquals(j, {"books":{"item":[{"bookName":"book1","bookId":"101"},{"bookName":"book2","bookId":"102"},
                          {"bookName":"book3","bookId":"103"}]}});
}

@test:Config {
    groups: ["toJson"]
}
isolated function testXmlWithOutNamespace1() returns error? {
    xml x1 = xml `<books>
                    <item>
                       <item>
                          <bookName>book1</bookName>
                          <bookId>101</bookId>
                       </item>
                    </item>
                    <item>
                       <item>
                          <bookName>book2</bookName>
                          <bookId>102</bookId>
                       </item>
                    </item>
                    <item>
                      <item>
                          <bookName>book3</bookName>
                          <bookId>103</bookId>
                       </item>
                    </item>
                  </books>
                  `;
    json j = check toJson(x1);
    test:assertEquals(j, {"books":{"item":[{"item":{"bookName":"book1","bookId":"101"}},
                          {"item":{"bookName":"book2","bookId":"102"}},{"item":{"bookName":"book3","bookId":"103"}}]}});
}

@test:Config {
    groups: ["toJson"]
}
isolated function testXmlWithArray() returns error? {
    xml x1 = xml `<books>
                      <item>book1</item>
                      <item>book2</item>
                      <item>book3</item>
                  </books>
                  `;
    json j = check toJson(x1);
    test:assertEquals(j, {"books":{"item":["book1","book2","book3"]}});
}

@test:Config {
    groups: ["toJson"]
}
isolated function testXmlWithMultipleArray() returns error? {
    xml x1 = xml `<books>
                      <item>book1</item>
                      <item>book2</item>
                      <item>book3</item>
                      <item1>book1</item1>
                      <item1>book2</item1>
                      <item1>book3</item1>
                  </books>
                  `;
    json j = check toJson(x1);
    test:assertEquals(j, {"books":{"item":["book1","book2","book3"],"item1":["book1","book2","book3"]}});
}


@test:Config {
    groups: ["toJson"]
}
isolated function testXmlWithKeyValue() returns error? {
    xml x1 = xml `<books>
                      book1
                      book2
                      book3
                  </books>
                  `;
    json j = check toJson(x1);
    test:assertEquals(j, {"books":"book1\n" +
                            "                      book2\n                      " +
                            "book3"});
}

@test:Config {
    groups: ["toJson"]
}
isolated function testComplexXml() returns error? {
    xml x1 = xml `<books>
                      book3
                      book4
                      <item>book1</item>
                      <item>book2</item>
                      <item>book6</item>
                  </books>
                  `;
    json j = check toJson(x1);
    test:assertEquals(j, {"books":{"#content":"book3\n" +
                        "                      book4","item":["book1","book2","book6"]}});
}

public function convertToJson(string xmlStr) returns string = @java:Method {
    'class: "io.ballerina.stdlib.xmldata.testutils.XmlDataTestUtils"
} external;

public function convertChildrenToJson(string xmlStr) returns string = @java:Method {
    'class: "io.ballerina.stdlib.xmldata.testutils.XmlDataTestUtils"
} external;
