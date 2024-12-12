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

import ballerina/test;

@test:Config {
    groups: ["fromJson"]
}
isolated function testJsonDataSize() returns error? {
    json data = {id: 30};
    xml expected = xml `<id>30</id>`;
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result, expected, msg = "testFromJSON result incorrect");
    } else {
        test:assertFail("testFromJson result is not xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testEmptyJson() returns error? {
    json data = {};
    xml expected = xml ``;
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result, expected, msg = "testFromJSON result incorrect");
    } else {
        test:assertFail("testFromJson result is not xml");
    }
}

@test:Config {
    groups: ["fromJson", "size"]
}
isolated function testJsonArray() returns error? {
    json data = {
        fname: "John",
        lname: "Stallone",
        family: [
            {fname: "Peter", lname: "Stallone"},
            {fname: "Emma", lname: "Stallone"},
            {fname: "Jena", lname: "Stallone"},
            {fname: "Paul", lname: "Stallone"}
        ]
    };
    string expected =
    "<root>" +
        "<fname>John</fname>" +
        "<lname>Stallone</lname>" +
        "<family><fname>Peter</fname><lname>Stallone</lname></family>" +
        "<family><fname>Emma</fname><lname>Stallone</lname></family>" +
        "<family><fname>Jena</fname><lname>Stallone</lname></family>" +
        "<family><fname>Paul</fname><lname>Stallone</lname></family>" +
    "</root>";
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected, msg = "testFromJSON result incorrect");
    } else {
        test:assertFail("testFromJson result is not xml");
    }
}

@test:Config {
    groups: ["fromJson", "negative"]
}
isolated function testAttributeValidation() {
    json data = {
        "@writer": {
            fname: "Christopher",
            lname: "Nolan",
            age: 30
        }
    };
    xml|Error? result = fromJson(data);
    if result is Error {
        test:assertTrue(result.toString().includes("attribute cannot be an object or array"),
                    msg = "testFromJSON result incorrect");
    } else {
        test:assertFail("Result is not mismatch");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testNodeNameNull() returns error? {
    json data = [
        {
            "@writer": "Christopher",
            lname: "Nolan",
            age: 30,
            address: ["Uduvil"]
        },
        1
    ];
    string expected =
    "<root>" +
        "<item writer=\"Christopher\">" +
            "<lname>Nolan</lname>" +
            "<age>30</age>" +
            "<address>Uduvil</address>" +
        "</item>" +
        "<item>1</item>" +
    "</root>";
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected, msg = "testFromJSON result incorrect");
    } else {
        test:assertFail("Result is not mismatch");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testJsonAsInt() returns error? {
    json data = 5;
    xml expected = xml `5`;
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result, expected, msg = "testJsonAsInt result incorrect");
    } else {
        test:assertFail("Result is not mismatch");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testJsonAsString() returns error? {
    json data = "data";
    xml expected = xml `data`;
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result, expected, msg = "testJsonAsString result incorrect");
    } else {
        test:assertFail("Result is not mismatch");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testJsonAsBoolean() returns error? {
    json data = false;
    xml expected = xml `false`;
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result, expected, msg = "testJsonAsString result incorrect");
    } else {
        test:assertFail("Result is not mismatch");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testJsonAsDecimal() returns error? {
    json data = 0.5;
    xml expected = xml `0.5`;
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result, expected, msg = "testJsonAsString result incorrect");
    } else {
        test:assertFail("Result is not mismatch");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testJsonAsFloat() returns error? {
    json data = 0.5;
    xml expected = xml `0.5`;
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result, expected, msg = "testJsonAsString result incorrect");
    } else {
        test:assertFail("Result is not mismatch");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testJsonAsNull() returns error? {
    json data = null;
    xml expected = xml ``;
    xml? result = check fromJson(data);
    test:assertEquals(result, expected, msg = "testJsonAsNull result incorrect");
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testSingleElement() returns error? {
    json data = {
        name: "Alex"
    };
    xml expected = xml `<name>Alex</name>`;
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result, expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testMultipleElements() returns error? {
    json data = {
        name: "Alex",
        age: 32,
        married: true
    };
    string expected =
    "<root>" +
        "<name>Alex</name>" +
        "<age>32</age>" +
        "<married>true</married>" +
    "</root>";
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testMultipleLevels() returns error? {
    json data = {
        name: "Alex",
        age: 32,
        married: true,
        address: {
            street: "No 20, Palm Grove",
            city: "Colombo 03",
            country: "Sri Lanka"
        },
        contact: {
            telephone: {
                office: 777334555,
                home: 94112546456
            }
        }
    };
    string expected =
    "<root>" +
        "<name>Alex</name>" +
        "<age>32</age>" +
        "<married>true</married>" +
        "<address>" +
            "<street>No 20, Palm Grove</street>" +
            "<city>Colombo 03</city>" +
            "<country>Sri Lanka</country>" +
        "</address>" +
        "<contact>" +
            "<telephone>" +
                "<office>777334555</office>" +
                "<home>94112546456</home>" +
            "</telephone>" +
        "</contact>" +
    "</root>";
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testStringArray() returns error? {
    json data = {
        "books": [
            "book1",
            "book2",
            "book3"
        ]
    };
    string expected =
    "<root>" +
        "<books>book1</books>" +
        "<books>book2</books>" +
        "<books>book3</books>" +
    "</root>";
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testMultiLevelJsonArray() returns error? {
    json data = {
        "books": [
            [
                {
                    "bookName": "book1",
                    "bookId": 101
                }
            ],
            [
                {
                    "bookName": "book2",
                    "bookId": 102
                }
            ],
            [
                {
                    "bookName": "book3",
                    "bookId": 103
                }
            ]
        ]
    };
    string expected =
    "<root>" +
        "<books>" +
            "<item>" +
                "<bookName>book1</bookName>" +
                "<bookId>101</bookId>" +
            "</item>" +
        "</books>" +
        "<books>" +
            "<item>" +
                "<bookName>book2</bookName>" +
                "<bookId>102</bookId>" +
            "</item>" +
        "</books>" +
        "<books>" +
            "<item>" +
                "<bookName>book3</bookName>" +
                "<bookId>103</bookId>" +
            "</item>" +
        "</books>" +
    "</root>";
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testArray() returns error? {
    json data = [
        {
            fname: "foo",
            lname: "bar"
        },
        1
    ];
    string expected =
    "<root>" +
        "<item>" +
            "<fname>foo</fname>" +
            "<lname>bar</lname>" +
        "</item>" +
        "<item>1</item>" +
    "</root>";
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testArrayWithArrayEntryTag() returns error? {
    json data = [
        {
            fname: "foo",
            lname: "bar"
        },
        1
    ];
    string expected =
    "<root>" +
        "<hello>" +
            "<fname>foo</fname>" +
            "<lname>bar</lname>" +
        "</hello>" +
        "<hello>1</hello>" +
    "</root>";
    xml? result = check fromJson(data, {arrayEntryTag: "hello"});
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testWithAttribute() returns error? {
    json data = {
        "@series": "Dark",
        genre: "Sci-Fi",
        language: "German",
        seasons: 3,
        "@id": 3296
    };
    xml? result = check fromJson(data);
    string expected =
    "<root series=\"Dark\" id=\"3296\">" +
        "<genre>Sci-Fi</genre>" +
        "<language>German</language>" +
        "<seasons>3</seasons>" +
    "</root>";
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testNamespace() returns error? {
    json data = {
        "ns0:bookStore": {
            "@xmlns:ns0": "http://sample.com/test",
            "@status": "online",
            "ns0:storeName": "foo",
            "ns0:postalCode": "94",
            "ns0:isOpen": "true",
            "ns0:address": {
                "ns0:street": "No 20, Palm Grove",
                "ns0:city": "Colombo 03",
                "ns0:country": "Sri Lanka"
            },
            "ns0:codes": ["4", "8", "9"]
        },
        "metaInfo": "some info"
    };
    string expected =
    "<root>" +
        "<ns0:bookStore xmlns:ns0=\"http://sample.com/test\" status=\"online\">" +
            "<ns0:storeName>foo</ns0:storeName>" +
            "<ns0:postalCode>94</ns0:postalCode>" +
            "<ns0:isOpen>true</ns0:isOpen>" +
            "<ns0:address>" +
                "<ns0:street>No 20, Palm Grove</ns0:street>" +
                "<ns0:city>Colombo 03</ns0:city>" +
                "<ns0:country>Sri Lanka</ns0:country>" +
            "</ns0:address>" +
            "<ns0:codes>4</ns0:codes>" +
            "<ns0:codes>8</ns0:codes>" +
            "<ns0:codes>9</ns0:codes>" +
        "</ns0:bookStore>" +
        "<metaInfo>some info</metaInfo>" +
    "</root>";
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testMultipleNamespaces() returns error? {
    json data = {
        "ns0:bookStore": {
            "@xmlns:ns0": "http://sample.com/foo",
            "@status": "online",
            "ns0:storeName": "foo",
            "ns0:postalCode": "94",
            "ns0:isOpen": "true",
            "ns0:address": {
                "@xmlns:ns1": "http://sample.com/bar",
                "ns0:street": "No 20, Palm Grove",
                "ns0:city": "Colombo 03",
                "ns0:country": "Sri Lanka",
                "ns1:state": "Western"
            },
            "ns1:capacity": {
                "@xmlns:ns0": "http://sample.com/alpha",
                "@xmlns:ns1": "http://sample.com/beta",
                "ns1:shelves": "100"
            },
            "ns0:codes": ["4", "8", "9"]
        },
        "metaInfo": "some info"
    };
    string expected =
    "<root>" +
        "<ns0:bookStore xmlns:ns0=\"http://sample.com/foo\" status=\"online\">" +
            "<ns0:storeName>foo</ns0:storeName>" +
            "<ns0:postalCode>94</ns0:postalCode>" +
            "<ns0:isOpen>true</ns0:isOpen>" +
            "<ns0:address xmlns:ns1=\"http://sample.com/bar\">" +
                "<ns0:street>No 20, Palm Grove</ns0:street>" +
                "<ns0:city>Colombo 03</ns0:city>" +
                "<ns0:country>Sri Lanka</ns0:country>" +
                "<ns1:state>Western</ns1:state>" +
            "</ns0:address>" +
            "<ns1:capacity xmlns:ns1=\"http://sample.com/beta\" xmlns:ns0=\"http://sample.com/alpha\">" +
            "<ns1:shelves>100</ns1:shelves>" +
            "</ns1:capacity>" +
            "<ns0:codes>4</ns0:codes>" +
            "<ns0:codes>8</ns0:codes>" +
            "<ns0:codes>9</ns0:codes>" +
        "</ns0:bookStore>" +
        "<metaInfo>some info</metaInfo>" +
    "</root>";
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testWithCustomAttribute() returns error? {
    json data = {
        "$series": "Dark",
        genre: "Sci-Fi",
        language: "German",
        seasons: 3,
        "$id": 3296
    };
    xml? result = check fromJson(data, {attributePrefix: "$"});
    string expected =
    "<root series=\"Dark\" id=\"3296\">" +
        "<genre>Sci-Fi</genre>" +
        "<language>German</language>" +
        "<seasons>3</seasons>" +
    "</root>";
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testWithAttribute1() returns error? {
    json data = {
        "Store": {
            "@id": "AST",
            "name": "Anne",
            "address": {
                "street": "Main",
                "city": "94"
            },
            "codes": ["4", "8"]
        }
    };
    xml? result = check fromJson(data);
    string expected =
        "<Store id=\"AST\">" +
            "<name>Anne</name>" +
            "<address>" +
                "<street>Main</street>" +
                "<city>94</city>" +
            "</address>" +
            "<codes>4</codes>" +
            "<codes>8</codes>" +
        "</Store>";
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testMultiLevelJsonArray1() returns error? {
    json data = {
        "books": [
            [
                {
                    "bookName": "book1",
                    "bookId": 101
                }
            ],
            [
                {
                    "bookName": "book2",
                    "bookId": 102
                }
            ],
            [
                {
                    "bookName": "book3",
                    "bookId": 103
                }
            ]
        ],
        "books1": [
            [
                {
                    "bookName": "book1",
                    "bookId": 101
                }
            ]
        ]
    };
    string expected =
    "<root>" +
        "<books>" +
            "<item>" +
                "<bookName>book1</bookName>" +
                "<bookId>101</bookId>" +
            "</item>" +
        "</books>" +
        "<books>" +
            "<item>" +
                "<bookName>book2</bookName>" +
                "<bookId>102</bookId>" +
            "</item>" +
        "</books>" +
        "<books>" +
            "<item>" +
                "<bookName>book3</bookName>" +
                "<bookId>103</bookId>" +
            "</item>" +
        "</books>" +
        "<books1>" +
            "<item>" +
                "<bookName>book1</bookName>" +
                "<bookId>101</bookId>" +
            "</item>" +
        "</books1>" +
    "</root>";
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testJsonKey() returns error? {
    json data = {"#content": "text"};
    xml expected = xml `text`;
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result, expected, msg = "testJsonKey result incorrect");
    } else {
        test:assertFail("testJsonKey result is not xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testJsonWithDefaultKey() returns error? {
    json data = {
        "books": {
            "#content": "book3",
            item: ["book1", "book2", "book6"]
        }
    };
    xml expected = xml `<books>book3<item>book1</item><item>book2</item><item>book6</item></books>`;
    xml? result = check fromJson(data);
    if result is xml {
        test:assertEquals(result, expected, msg = "testJsonKey result incorrect");
    } else {
        test:assertFail("testJsonKey result is not xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testWithAttribute2() {
    json data = {
        "Store": {
            "@id": "AST",
            "name": "Anne",
            "address": {
                "@id": "AST",
                "street": "Main",
                "city": "94"
            },
            "codes": ["4", "8"]
        }
    };
    xml?|error result = fromJson(data);
    string expected =
        "<Store id=\"AST\">" +
            "<name>Anne</name>" +
            "<address id=\"AST\">" +
                "<street>Main</street>" +
                "<city>94</city>" +
            "</address>" +
            "<codes>4</codes>" +
            "<codes>8</codes>" +
        "</Store>";
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testWithAttribute3() {
    json data = {
        "Store": {
            "#id": "AST",
            "name": "Anne",
            "address": {
                "#id": "AST",
                "street": "Main",
                "city": "94"
            },
            "codes": ["4", "8"]
        }
    };
    xml?|error result = fromJson(data, {attributePrefix: "#"});
    string expected =
        "<Store id=\"AST\">" +
            "<name>Anne</name>" +
            "<address id=\"AST\">" +
                "<street>Main</street>" +
                "<city>94</city>" +
            "</address>" +
            "<codes>4</codes>" +
            "<codes>8</codes>" +
        "</Store>";
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testWithAttribute4() {
    json j = {
        "soapenv:Envelope": {
            "soapenv:Header": {
                "applicationInfo": {
                    "applicationId": "testID123"
                },
                "tokenPassport": {
                    "account": "testvalueaccount",
                    "consumerKey": "testvalueconsumerKey",
                    "token": "testvaluetoken",
                    "nonce": "testvaluenonce",
                    "timestamp": "testvaluetimestamp",
                    "signature": {
                        "#content": "Value123",
                        "@algorithm": "HMA_SHA256"
                    }
                }
            },
            "soapenv:Body": {
                "urn:get": {
                    "urn:baseRef": {
                        "internalId": "valueID",
                        "type": "vendor",
                        "@xsi:type": "urn1:RecordRef"
                    }
                }
            },
            "@xmlns:soapenv": "http://schemas.xmlsoap.org/soap/envelope/",
            "@xmlns:urn": "urn:messages_2020_2.platform.webservices.netsuite.com",
            "@xmlns:urn1": "urn:core_2020_2.platform.webservices.netsuite.com",
            "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance"
        }
    };
    string expected =
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
        "xmlns:urn=\"urn:messages_2020_2.platform.webservices.netsuite.com\" " +
        "xmlns:urn1=\"urn:core_2020_2.platform.webservices.netsuite.com\" " +
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "<soapenv:Header>" +
                "<applicationInfo>" +
                    "<applicationId>testID123</applicationId>" +
                "</applicationInfo>" +
                "<tokenPassport>" +
                    "<account>testvalueaccount</account>" +
                    "<consumerKey>testvalueconsumerKey</consumerKey>" +
                    "<token>testvaluetoken</token>" +
                    "<nonce>testvaluenonce</nonce>" +
                    "<timestamp>testvaluetimestamp</timestamp>" +
                    "<signature algorithm=\"HMA_SHA256\">Value123</signature>" +
                "</tokenPassport>" +
            "</soapenv:Header>" +
            "<soapenv:Body>" +
                "<urn:get>" +
                    "<urn:baseRef xsi:type=\"urn1:RecordRef\">" +
                        "<internalId>valueID</internalId>" +
                        "<type>vendor</type>" +
                    "</urn:baseRef>" +
                "</urn:get>" +
            "</soapenv:Body>" +
        "</soapenv:Envelope>";
    xml|Error? x = fromJson(j);
    if x is xml {
        test:assertEquals(x.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testWithAttribute5() {
    json j = {
        "soapenv:Envelope": {
            "soapenv:Header": {
                "applicationInfo": {
                    "applicationId": "testID123"
                },
                "tokenPassport": {
                    "account": "testvalueaccount",
                    "consumerKey": "testvalueconsumerKey",
                    "token": "testvaluetoken",
                    "nonce": "testvaluenonce",
                    "timestamp": "testvaluetimestamp",
                    "signature": {
                        "#content": "Value123",
                        "@algorithm": "HMA_SHA256"
                    }
                }
            },
            "soapenv:Body": {
                "urn:get": {
                    "urn:baseRef": {
                        "internalId": "valueID",
                        "type": "vendor",
                        "@xsi:type": "urn1:RecordRef"
                    }
                },
                "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance"
            },
            "@xmlns:soapenv": "http://schemas.xmlsoap.org/soap/envelope/",
            "@xmlns:urn": "urn:messages_2020_2.platform.webservices.netsuite.com",
            "@xmlns:urn1": "urn:core_2020_2.platform.webservices.netsuite.com"
        }
    };
    string expected =
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
        "xmlns:urn=\"urn:messages_2020_2.platform.webservices.netsuite.com\" " +
        "xmlns:urn1=\"urn:core_2020_2.platform.webservices.netsuite.com\">" +
            "<soapenv:Header>" +
                "<applicationInfo>" +
                    "<applicationId>testID123</applicationId>" +
                "</applicationInfo>" +
                "<tokenPassport>" +
                    "<account>testvalueaccount</account>" +
                    "<consumerKey>testvalueconsumerKey</consumerKey>" +
                    "<token>testvaluetoken</token>" +
                    "<nonce>testvaluenonce</nonce>" +
                    "<timestamp>testvaluetimestamp</timestamp>" +
                    "<signature algorithm=\"HMA_SHA256\">Value123</signature>" +
                "</tokenPassport>" +
            "</soapenv:Header>" +
            "<soapenv:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                "<urn:get>" +
                    "<urn:baseRef xsi:type=\"urn1:RecordRef\">" +
                        "<internalId>valueID</internalId>" +
                        "<type>vendor</type>" +
                    "</urn:baseRef>" +
                "</urn:get>" +
            "</soapenv:Body>" +
        "</soapenv:Envelope>";
    xml|Error? x = fromJson(j);
    if x is xml {
        test:assertEquals(x.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

type Order record {
    Invoice Invoice;
};

type Invoice record {
    PurchesedItems PurchesedItems;
    Address1 Address;
    string _xmlns?;
    string _xmlns\:ns?;
    string _attr?;
    string _ns\:attr?;
};

type PurchesedItems record {
    Purchase[] PLine;
};

type Purchase record {
    string|ItemCode ItemCode;
    int Count;
};

type ItemCode record {
    string _discount;
    string \#content;
};

type Address1 record {
    string StreetAddress;
    string City;
    int Zip;
    string Country;
    string _xmlns?;
};

@test:Config {
    groups: ["fromJson"]
}
isolated function testfromjsonwithRecord() {
    Order data = {
        Invoice: {
            PurchesedItems: {
                PLine: [
                    {ItemCode: "223345", Count: 10},
                    {ItemCode: "223300", Count: 7},
                    {
                        ItemCode: {_discount: "22%", \#content: "200777"},
                        Count: 7
                    }
                ]
            },
            Address: {
                StreetAddress: "20, Palm grove, Colombo 3",
                City: "Colombo",
                Zip: 300,
                Country: "LK"
            },
            _attr: "attr-val",
            _xmlns: "example.com",
            _xmlns\:ns: "ns.com"
        }
    };
    string expected =
        "<Invoice xmlns=\"example.com\" xmlns:ns=\"ns.com\" attr=\"attr-val\">" +
            "<PurchesedItems>" +
                "<PLine><ItemCode>223345</ItemCode><Count>10</Count></PLine>" +
                "<PLine><ItemCode>223300</ItemCode><Count>7</Count></PLine>" +
                "<PLine><ItemCode discount=\"22%\">200777</ItemCode><Count>7</Count></PLine>" +
            "</PurchesedItems>" +
            "<Address>" +
                "<StreetAddress>20, Palm grove, Colombo 3</StreetAddress>" +
                "<City>Colombo</City>" +
                "<Zip>300</Zip>" +
                "<Country>LK</Country>" +
            "</Address>" +
        "</Invoice>";
    json jsonData = data.toJson();
    xml?|error result = fromJson(jsonData, {attributePrefix: "_"});
    if result is xml {
        test:assertEquals(result.toString(), expected.toString());
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

type TemplateGetOperation record {
    record {
        record {} 'soapenv\:Header;
        record {
            record {
                record {
                    string 'urn1\:name;
                } 'urn\:baseRef;
            } 'urn\:get;
        } 'soapenv\:Body;
        string _xmlns\:soapenv = "http://schemas.xmlsoap.org/soap/envelope/";
        string _xmlns\:urn = "urn:messages_2020_2.platform.webservices.netsuite.com";
        string _xmlns\:urn1 = "urn:core_2020_2.platform.webservices.netsuite.com";
    } 'soapenv\:Envelope;
};

@test:Config {
    groups: ["fromJson"]
}
isolated function testfromjsonwithRecord1() {
    TemplateGetOperation gettemp = {
        'soapenv\:Envelope: {
            'soapenv\:Header: {"Authorization": 35},
            'soapenv\:Body: {
                urn\:get: {
                    'urn\:baseRef: {
                        'urn1\:name: "details"
                    }
                }
            }
        }
    };
    json jsonData = gettemp.toJson();
    string expected =
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
        "xmlns:urn=\"urn:messages_2020_2.platform.webservices.netsuite.com\" " +
        "xmlns:urn1=\"urn:core_2020_2.platform.webservices.netsuite.com\">" +
            "<soapenv:Header>" +
            "<Authorization>35</Authorization>" +
            "</soapenv:Header>" +
            "<soapenv:Body>" +
                "<urn:get>" +
                    "<urn:baseRef>" +
                        "<urn1:name>details</urn1:name>" +
                    "</urn:baseRef>" +
                "</urn:get>" +
            "</soapenv:Body>" +
        "</soapenv:Envelope>";
    xml?|error result = fromJson(jsonData, {attributePrefix: "_"});
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testWithRootTagConfig() {
    json data = {
        "Store": {
            "#id": "AST",
            "name": "Anne",
            "address": {
                "#id": "AST",
                "street": "Main",
                "city": "94"
            },
            "codes": ["4", "8"]
        }
    };
    xml?|error result = fromJson(data, {attributePrefix: "#", rootTag: "Output"});
    string expected =
    "<Output>" +
        "<Store id=\"AST\">" +
            "<name>Anne</name>" +
            "<address id=\"AST\">" +
                "<street>Main</street>" +
                "<city>94</city>" +
            "</address>" +
            "<codes>4</codes>" +
            "<codes>8</codes>" +
        "</Store>" +
    "</Output>";
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
function testFromJsonWithNull() returns error? {
    json data = null;
    xml? result = check fromJson({"name":"Sherlock Holmes", "details":{"author":null, "language":"English"}}, {rootTag: "book"});
    xml expected = xml `<book><name>Sherlock Holmes</name><details><author/><language>English</language></details></book>`;
    test:assertEquals(result, expected, msg = "testFromJsonWithNull result incorrect");
}
