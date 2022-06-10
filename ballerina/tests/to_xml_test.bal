// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    groups: ["toXml"]
}
isolated function testMapJsonToXml1() returns error? {
    map<json> data = {id: 30, customer: {name: "Asha", age: 10}};
    xml result = check toXml(data);
    test:assertEquals(result, xml `<root><id>30</id><customer><name>Asha</name><age>10</age></customer></root>`,
                    msg = "testMapJsonToXml1 result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testDefaultRecordToXml1() returns error? {
    record {} data = {"id": 30};
    xml result = check toXml(data);
    test:assertEquals(result, xml `<root><id>30</id></root>`, msg = "testDefaultRecordToXml1 result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapStringToXml1() returns error? {
    map<string> data = {"id": "30"};
    xml result = check toXml(data);
    test:assertEquals(result, xml `<root><id>30</id></root>`, msg = "testMapStringToXml1 result incorrect");
}

@Name {
    value: "Customers"
}
@Namespace {
    prefix: "ns",
    uri: "http://sdf.com"
}
type Customer record {

    @Name {
        value: "employeeName"
    }
    @Namespace {
        prefix: "ns"
    }
    @Attribute
    string name;

    int age;
};

@test:Config {
    groups: ["toXml"]
}
isolated function testRecordToXml1() returns error? {
    Customer data = {name: "Asha", age: 10};
    xml result = check toXml(data);
    test:assertEquals(result,
                    xml `<ns:Customers xmlns:ns="http://sdf.com" ns:employeeName="Asha"><age>10</age></ns:Customers>`,
                    msg = "testRecordToXml1 result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapXmLToXml() returns error? {
    map<xml> data = {
        "value": xml `<text>1</text>`,
        "value1": xml `<text>2</text>`
    };
    xml result = check toXml(data);
    test:assertEquals(result, xml `<root><value><text>1</text></value><value1><text>2</text></value1></root>`,
    msg = "testMapXmLToXml result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapStrinToXml2() returns error? {
    map<string> data = {
        "series": "Dark",
        genre: "Sci-Fi",
        language: "German",
        seasons: "3",
        "id": "3296"
    };
    string expected = "<root>" +
                        "<series>Dark</series>" +
                        "<genre>Sci-Fi</genre>" +
                        "<language>German</language>" +
                        "<seasons>3</seasons>" +
                        "<id>3296</id>" +
                    "</root>";
    xml result = check toXml(data);
    test:assertEquals(result.toString(), expected, msg = "testMapStrinToXml2 result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapBooleanToXml1() returns error? {
    map<boolean> data = {
        boolean1: true,
        boolean2: false
    };
    xml result = check toXml(data);
    test:assertEquals(result, xml `<root><boolean1>true</boolean1><boolean2>false</boolean2></root>`,
                    msg = "testMapBooleanToXml1 result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapIntToXml() returns error? {
    map<int> data = {
        value: 5,
        value1: 6
    };
    xml result = check toXml(data);
    test:assertEquals(result, xml `<root><value>5</value><value1>6</value1></root>`,
                    msg = "testFromJSON result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapDecimalToXml() returns error? {
    map<decimal> data = {
        value: 5.0,
        value1: 6.2
    };
    xml result = check toXml(data);
    test:assertEquals(result, xml `<root><value>5.0</value><value1>6.2</value1></root>`,
                    msg = "testFromJSON result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapFloatToXml() returns error? {
    map<float> data = {
        value: 5.0,
        value1: 6.4
    };
    xml result = check toXml(data);
    test:assertEquals(result, xml `<root><value>5.0</value><value1>6.4</value1></root>`,
                    msg = "testFromJSON result incorrect");
}

type NewEmployee record {
    readonly string name;
    int salary;
};

@test:Config {
    groups: ["toXml"]
}
isolated function testMapTableToXml() returns error? {
    table<map<string>> tableValue = table [{key: "value"}];
    map<table<map<string>>> data = {
        data: tableValue
    };
    xml result = check toXml(data);
    test:assertEquals(result, xml `<root><data><key>value</key></data></root>`, msg = "testFromJSON result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapStringArrayToXml() returns error? {
    map<string[]> data = {
        key1: ["value1", "value2"],
        key2: ["value1", "value2"]
    };
    xml result = check toXml(data);
    test:assertEquals(result,
                    xml `<root><key1>value1</key1><key1>value2</key1><key2>value1</key2><key2>value2</key2></root>`,
                    msg = "testMapStringArrayToXml result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapIntArrayToXml() returns error? {
    map<int[]> data = {
        key1: [1, 2],
        key2: [1, 2],
        key3: [1, 2]
    };
    string expected = "<root>" +
                            "<key1>1</key1>" +
                            "<key1>2</key1>" +
                            "<key2>1</key2>" +
                            "<key2>2</key2>" +
                            "<key3>1</key3>" +
                            "<key3>2</key3>" +
                        "</root>";
    xml result = check toXml(data);
    test:assertEquals(result.toString(), expected, msg = "testMapIntArrayToXml result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapDecimalArrayToXml() returns error? {
    map<decimal[]> data = {
        key1: [1.0, 2.0],
        key2: [1.0, 2.0],
        key3: [1.0, 2.0]
    };
    string expected = "<root>" +
                        "<key1>1.0</key1>" +
                        "<key1>2.0</key1>" +
                        "<key2>1.0</key2>" +
                        "<key2>2.0</key2>" +
                        "<key3>1.0</key3>" +
                        "<key3>2.0</key3>" +
                    "</root>";
    xml result = check toXml(data);
    test:assertEquals(result.toString(), expected, msg = "testMapDecimalArrayToXml result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapFloatArrayToXml() returns error? {
    map<float[]> data = {
        key1: [1.0, 2.0],
        key2: [1.0, 2.0],
        key3: [1.0, 2.0]
    };
    string expected = "<root>" +
                            "<key1>1.0</key1>" +
                            "<key1>2.0</key1>" +
                            "<key2>1.0</key2>" +
                            "<key2>2.0</key2>" +
                            "<key3>1.0</key3>" +
                            "<key3>2.0</key3>" +
                        "</root>";
    xml result = check toXml(data);
    test:assertEquals(result.toString(), expected, msg = "testMapFloatArrayToXml result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapJsonArrayToXml1() returns error? {
    map<json[]> data = {customer: [{name: "Asha", age: 10}, {name: "Kalai", age: 12}]};
    string expected = "<root>" +
                        "<customer>" +
                            "<name>Asha</name>" +
                            "<age>10</age>" +
                        "</customer>" +
                        "<customer>" +
                            "<name>Kalai</name>" +
                            "<age>12</age>" +
                        "</customer>" +
                    "</root>";
    xml result = check toXml(data);
    test:assertEquals(result.toString(), expected, msg = "testMapJsonToXml1 result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapXmlArrayToXml1() returns error? {
    map<xml[]> data = {
        customers:
        [
            xml `<customer><name>Asha</name><age>10</age></customer>`,
            xml `<customer><name>Kalai</name><age>13</age></customer>`
        ],
        employees:
        [
            xml `<employee><name>Asha</name><age>10</age></employee>`,
            xml `<employee><name>Kalai</name><age>13</age></employee>`
        ]
    };
    string expected = "<root>" +
                        "<customers>" +
                            "<customer>" +
                                "<name>Asha</name>" +
                                "<age>10</age>" +
                            "</customer>" +
                            "<customer>" +
                                "<name>Kalai</name>" +
                                "<age>13</age>" +
                            "</customer>" +
                        "</customers>" +
                        "<employees>" +
                            "<employee>" +
                                "<name>Asha</name>" +
                                "<age>10</age>" +
                            "</employee>" +
                            "<employee>" +
                                "<name>Kalai</name>" +
                                "<age>13</age>" +
                            "</employee>" +
                        "</employees>" +
                    "</root>";
    xml result = check toXml(data);
    test:assertEquals(result.toString(), expected, msg = "testMapXmlArrayToXml1 result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testRecordArrayToXml1() returns error? {
    Customer[] customers = [{name: "Asha", age: 10}, {name: "Kalai", age: 10}];
    map<Customer[]> data = {customers: customers};
    string expected = "<root>" +
                        "<customers>" +
                            "<ns:Customers xmlns:ns=\"http://sdf.com\" ns:employeeName=\"Asha\">" +
                                "<age>10</age>" +
                            "</ns:Customers>" +
                        "</customers>" +
                        "<customers>" +
                            "<ns:Customers xmlns:ns=\"http://sdf.com\" ns:employeeName=\"Kalai\">" +
                                "<age>10</age>" +
                            "</ns:Customers>" +
                        "</customers>" +
                    "</root>";
    xml result = check toXml(data);
    test:assertEquals(result.toString(), expected, msg = "testRecordToXml1 result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testRecordArrayToXml2() returns error? {
    Customer[] customers = [{name: "Asha", age: 10}, {name: "Kalai", age: 10}];
    map<Customer[]> data = {customer1: customers, customer2: customers};
    string expected = "<root>" +
                        "<customer1>" +
                            "<ns:Customers xmlns:ns=\"http://sdf.com\" ns:employeeName=\"Asha\">" +
                                "<age>10</age>" +
                            "</ns:Customers>" +
                        "</customer1>" +
                        "<customer1>" +
                            "<ns:Customers xmlns:ns=\"http://sdf.com\" ns:employeeName=\"Kalai\">" +
                                "<age>10</age>" +
                            "</ns:Customers>" +
                        "</customer1>" +
                        "<customer2>" +
                            "<ns:Customers xmlns:ns=\"http://sdf.com\" ns:employeeName=\"Asha\">" +
                                "<age>10</age>" +
                            "</ns:Customers>" +
                        "</customer2>" +
                        "<customer2>" +
                            "<ns:Customers xmlns:ns=\"http://sdf.com\" ns:employeeName=\"Kalai\">" +
                                "<age>10</age>" +
                            "</ns:Customers>" +
                        "</customer2>" +
                    "</root>";
    xml result = check toXml(data);
    test:assertEquals(result.toString(), expected, msg = "testRecordToXml1 result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapTableToXml1() returns error? {
    table<map<string>> tableValue = table [
            {key: "value", key1: "value1"},
            {key2: "value2", key3: "value3"}
        ];
    map<table<map<string>>> data = {
        data: tableValue
    };
    string expected = "<root>" +
                        "<data>" +
                            "<key>value</key>" +
                            "<key1>value1</key1>" +
                        "</data>" +
                        "<data>" +
                            "<key2>value2</key2>" +
                            "<key3>value3</key3>" +
                        "</data>" +
                        "</root>";
    xml result = check toXml(data);
    test:assertEquals(result.toString(), expected, msg = "testMapTableToXml1 result incorrect");
}

@Namespace {
    prefix: "nso",
    uri: "http://www.w3.org/"
}
type Root record {
    string name;

    @Attribute
    string xmlns\:asd = "http://www.w3.org1/";
};

@test:Config {
    groups: ["toXml"]
}
isolated function testRecordToXml2() returns error? {
    Root data = {name: "Asha"};
    xml result = check toXml(data);
    string expected = "<nso:Root xmlns:nso=\"http://www.w3.org/\" xmlns:asd=\"http://www.w3.org1/\">" +
                        "<name>Asha</name>" +
                    "</nso:Root>";
    test:assertEquals(result.toString(), expected, msg = "testRecordToXml1 result incorrect");
}

type Root31 record {
    string name;

    @Attribute
    string xmlns\:nso = "http://www.w3.org1/";
};

@test:Config {
    groups: ["toXml"]
}
isolated function testRecordToXml3() returns error? {
    Root31 data = {name: "Asha"};
    xml result = check toXml(data);
    string expected = "<Root31 xmlns:nso=\"http://www.w3.org1/\"><name>Asha</name></Root31>";
    test:assertEquals(result.toString(), expected, msg = "testRecordToXml3 result incorrect");
}

type Commercial6 record {
    BookStore6 bookstore;
};

type BookStore6 record {
    string storeName;
    int postalCode;
    boolean isOpen;
    Address6 address;
    Codes6 codes;
    @Attribute
    string status;
    @Attribute
    string 'xmlns\:ns0;
};

type Address6 record {
    string street;
    string city;
    string country;
};

type Codes6 record {
    int[] item;
};

@test:Config {
    groups: ["toXml"]
}
isolated function testComplexRecordToXml() returns error? {
    Commercial6 data = {
        bookstore: {
            storeName: "foo",
            postalCode: 94,
            isOpen: true,
            address: {
                street: "Galle Road",
                city: "Colombo",
                country: "Sri Lanka"
            },
            codes: {
                item: [4, 8, 9]
            },
            'xmlns\:ns0: "http://sample.com/test",
            status: "online"
        }
    };
    string expected = "<Commercial6>" +
                        "<bookstore xmlns:ns0=\"http://sample.com/test\" status=\"online\">" +
                            "<storeName>foo</storeName>" +
                            "<postalCode>94</postalCode>" +
                            "<isOpen>true</isOpen>" +
                            "<address>" +
                                "<street>Galle Road</street>" +
                                "<city>Colombo</city>" +
                                "<country>Sri Lanka</country>" +
                            "</address>" +
                            "<codes>" +
                                "<item>4</item>" +
                                "<item>8</item>" +
                                "<item>9</item>" +
                            "</codes>" +
                        "</bookstore>" +
                    "</Commercial6>";
    xml result = check toXml(data);
    test:assertEquals(result.toString(), expected, msg = "testComplexRecordToXml result incorrect");
}
