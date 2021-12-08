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
isolated function testJsonDataSize() {
    json data = {id: 30};
    xml expected = xml `<id>30</id>`;
    xml|Error? result = fromJson(data);
    if result is xml {
        test:assertEquals(result, expected, msg = "testFromJSON result incorrect");
    } else {
        test:assertFail("testFromJson result is not xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testEmptyJson() {
    json data = {};
    xml expected = xml ``;
    xml|Error? result = fromJson(data);
    if result is xml {
        test:assertEquals(result, expected, msg = "testFromJSON result incorrect");
    } else {
        test:assertFail("testFromJson result is not xml");
    }
}

@test:Config {
    groups: ["fromJson", "size"]
}
isolated function testJsonArray() {
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
        "<family>" +
            "<item><fname>Peter</fname><lname>Stallone</lname></item>" +
            "<item><fname>Emma</fname><lname>Stallone</lname></item>" +
            "<item><fname>Jena</fname><lname>Stallone</lname></item>" +
            "<item><fname>Paul</fname><lname>Stallone</lname></item>" +
        "</family>" +
    "</root>";
    xml|Error? result = fromJson(data);
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
isolated function testNodeNameNull() {
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
            "<address><item>Uduvil</item></address>" +
        "</item>" +
        "<item>1</item>" +
    "</root>";
    xml|Error? result = fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected, msg = "testFromJSON result incorrect");
    } else {
        test:assertFail("Result is not mismatch");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testJsonAsInt() {
    json data = 5;
    xml|Error? result = fromJson(data);
    if result is Error {
        test:assertTrue(result.toString().includes("failed to parse xml"), msg = "testFromJSON result incorrect");
    } else {
        test:assertEquals(result, "");
        test:assertFail("Result is not mismatch");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testJsonAsNull() {
    json data = null;
    xml expected = xml ``;
    xml?|Error result = fromJson(data);
    if !(result is Error) {
        test:assertEquals(result, expected);
        test:assertTrue(result is ());
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testSingleElement() {
    json data = {
        name: "Alex"
    };
    xml expected = xml `<name>Alex</name>`;
    xml?|error result = fromJson(data);
    if result is xml {
        test:assertEquals(result, expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testMultipleElements() {
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
    xml?|error result = fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testMultipleLevels() {
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
    xml?|error result = fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testStringArray() {
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
    xml?|error result = fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testMultiLevelJsonArray() {
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
    xml?|error result = fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testArray() {
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
    xml?|error result = fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testArrayWithArrayEntryTag() {
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
    xml?|error result = fromJson(data, {arrayEntryTag: "hello"});
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testWithAttribute() {
    json data = {
        "@series": "Dark",
        genre: "Sci-Fi",
        language: "German",
        seasons: 3,
        "@id": 3296
    };
    xml?|error result = fromJson(data);
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
isolated function testNamespace() {
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
            "<ns0:codes>" +
                "<item>4</item>" +
                "<item>8</item>" +
                "<item>9</item>" +
            "</ns0:codes>" +
        "</ns0:bookStore>" +
        "<metaInfo>some info</metaInfo>" +
    "</root>";
    xml?|error result = fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testMultipleNamespaces() {
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
            "<ns0:codes>" +
                "<item>4</item>" +
                "<item>8</item>" +
                "<item>9</item>" +
            "</ns0:codes>" +
        "</ns0:bookStore>" +
        "<metaInfo>some info</metaInfo>" +
    "</root>";
    xml?|error result = fromJson(data);
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}

@test:Config {
    groups: ["fromJson"]
}
isolated function testWithCustomAttribute() {
    json data = {
        "$series": "Dark",
        genre: "Sci-Fi",
        language: "German",
        seasons: 3,
        "$id": 3296
    };
    xml?|error result = fromJson(data, {attributePrefix: "$"});
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
isolated function testWithAttribute1() {
    json data = {"Store": {
        "@id": "AST",
        "name": "Anne",
        "address": {
            "street": "Main",
            "city": "94"
        },
        "codes": ["4", "8"]
    }};
    xml?|error result = fromJson(data);
    string expected =
    "<Store id=\"AST\">" +
        "<name>Anne</name>" +
        "<address>" +
            "<street>Main</street>" +
            "<city>94</city>" +
        "</address>" +
        "<codes>" +
            "<item>4</item>" +
            "<item>8</item>" +
        "</codes>" +
    "</Store>";
    if result is xml {
        test:assertEquals(result.toString(), expected);
    } else {
        test:assertFail("failed to convert json to xml");
    }
}
