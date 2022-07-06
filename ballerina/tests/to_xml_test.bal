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
    record {int id;} data = {id: 30};
    xml result = check toXml(data);
    test:assertEquals(result, xml `<id>30</id>`, msg = "testDefaultRecordToXml1 result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testDefaultRecordToXml2() returns error? {
    record {int id; string name;} data = {id: 30, name: "Asha"};
    xml result = check toXml(data);
    test:assertEquals(result, xml `<root><id>30</id><name>Asha</name></root>`,
                      msg = "testDefaultRecordToXml2 result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapStringToXml1() returns error? {
    map<string> data = {"id": "30"};
    xml result = check toXml(data);
    test:assertEquals(result, xml `<id>30</id>`, msg = "testMapStringToXml1 result incorrect");
}

@test:Config {
    groups: ["toXml"]
}
isolated function testMapStringToXml2() returns error? {
    map<string> data = {"id": "30", "name": "Asha"};
    xml result = check toXml(data);
    test:assertEquals(result, xml `<root><id>30</id><name>Asha</name></root>`,
                      msg = "testMapStringToXml2 result incorrect");
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
    @Attribute
    string ns\:name;

    int age;
};

@test:Config {
    groups: ["toXml"]
}
isolated function testRecordWithAnnotationToXml1() returns error? {
    Customer data = {ns\:name: "Asha", age: 10};
    xml result = check toXml(data);
    test:assertEquals(result,
                    xml `<ns:Customers xmlns:ns="http://sdf.com" ns:employeeName="Asha"><age>10</age></ns:Customers>`,
                    msg = "testRecordWithAnnotationToXml1 result incorrect");
}

@Namespace {
    prefix: "ns",
    uri: "http://sdf.com"
}
@Name {
    value: "Customers"
}
type Customer2 record {

    @Name {
        value: "employeeName"
    }
    @Attribute
    string ns\:name;

    int age;
};

@test:Config {
    groups: ["toXml"]
}
isolated function testRecordWithAnnotationToXml2() returns error? {
    Customer2 data = {ns\:name: "Asha", age: 10};
    xml result = check toXml(data);
    test:assertEquals(result,
                    xml `<ns:Customers xmlns:ns="http://sdf.com" ns:employeeName="Asha"><age>10</age></ns:Customers>`,
                    msg = "testRecordWithAnnotationToXml2 result incorrect");
}

@Namespace {
    prefix: "ns",
    uri: "http://sdf.com"
}
@Name {
    value: "Customers"
}
type Customer3 record {

    @Attribute
    @Name {
        value: "employeeName"
    }
    string ns\:name;

    int age;
};

@test:Config {
    groups: ["toXml"]
}
isolated function testRecordWithAnnotationToXml3() returns error? {
    Customer3 data = {ns\:name: "Asha", age: 10};
    xml result = check toXml(data);
    test:assertEquals(result,
                    xml `<ns:Customers xmlns:ns="http://sdf.com" ns:employeeName="Asha"><age>10</age></ns:Customers>`,
                    msg = "testRecordWithAnnotationToXml3 result incorrect");
}

@Namespace {
    prefix: "ns",
    uri: "http://sdf.com"
}
@Name {
    value: "Customers"
}
type Customer4 record {

    @Attribute
    @Name {
        value: "employeeName"
    }
    string ns\:name;

    int age;
};

@test:Config {
    groups: ["toXml"]
}
isolated function testRecordWithAnnotationToXml4() returns error? {
    Customer4 data = {ns\:name: "Asha", age: 10};
    xml result = check toXml(data);
    test:assertEquals(result,
                    xml `<ns:Customers xmlns:ns="http://sdf.com" ns:employeeName="Asha"><age>10</age></ns:Customers>`,
                    msg = "testRecordWithAnnotationToXml4 result incorrect");
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
    Customer[] customers = [{ns\:name: "Asha", age: 10}, {ns\:name: "Kalai", age: 10}];
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
    Customer[] customers = [{ns\:name: "Asha", age: 10}, {ns\:name: "Kalai", age: 10}];
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

@Namespace {
    uri: "example.com"
}
type Purchesed_Bill record {
    Purchesed_Items PurchesedItems;
    Purchesed_Address Address;
    @Attribute
    string 'xmlns\:ns?;
    @Attribute
    string attr?;
    @Attribute
    string ns\:attr?;
};

type Purchesed_Items record {
    Purchesed_Purchase[] PLine;
};

type Purchesed_Purchase record {
    string|Purchesed_ItemCode ItemCode;
    int Count;
};

type Purchesed_ItemCode record {
    @Attribute
    string discount;
    string \#content?;
};

@Namespace {
    uri: ""
}
type Purchesed_Address record {
    string StreetAddress;
    string City;
    int Zip;
    string Country;
};

@test:Config {
    groups: ["toXml"]
}
isolated function testRecordWithNamaspaceAnnotationToXml() returns error? {
    Purchesed_Bill input = {
        PurchesedItems: {
                PLine: [
                    {ItemCode: "223345", Count: 10},
                    {ItemCode: "223300", Count: 7},
                    {
                        ItemCode: {discount: "22%", \#content: "200777"},
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
        'xmlns\:ns: "ns.com",
        attr: "attr-val",
        ns\:attr: "ns-attr-val"
    };
    string expected =
        "<Purchesed_Bill xmlns=\"example.com\" xmlns:ns=\"ns.com\" attr=\"attr-val\" ns:attr=\"ns-attr-val\">" +
            "<PurchesedItems>" +
                "<PLine>" +
                    "<ItemCode>223345</ItemCode>" +
                    "<Count>10</Count>" +
                "</PLine>" +
                "<PLine>" +
                    "<ItemCode>223300</ItemCode>" +
                    "<Count>7</Count>" +
                "</PLine>" +
                "<PLine>" +
                    "<ItemCode discount=\"22%\">200777</ItemCode>" +
                    "<Count>7</Count>" +
                "</PLine>" +
            "</PurchesedItems>" +
            "<Address>" +
                "<StreetAddress>20, Palm grove, Colombo 3</StreetAddress>" +
                "<City>Colombo</City>" +
                "<Zip>300</Zip>" +
                "<Country>LK</Country>" +
            "</Address>" +
        "</Purchesed_Bill>";
    xml result = check toXml(input);
    test:assertEquals(result.toString(), expected, msg = "testComplexRecordToXml result incorrect");
}

@Namespace {
    uri: "example.com"
}
type Purchesed_Bill1 record {
    Purchesed_Items1 PurchesedItems;
    @Attribute
    string 'xmlns\:ns?;
    @Attribute
    string attr?;
    @Attribute
    string ns\:attr?;
};

@Namespace {
    prefix: "ns0",
    uri: "example.com"
}
type Purchesed_Items1 record {
    Purchesed_Purchase1[] PLine;
};

type Purchesed_Purchase1 record {
    string|Purchesed_ItemCode1 ItemCode;
    int Count;
};

@Namespace {
    prefix: "ns1",
    uri: "example.com"
}
type Purchesed_ItemCode1 record {
    @Attribute
    string discount;
    string \#content?;
};

@test:Config {
    groups: ["toXml"]
}
isolated function testRecordWithNamaspaceAnnotationToXml1() returns error? {
    Purchesed_Bill1 input = {
            PurchesedItems: {
                    PLine: [
                        {ItemCode: "223345", Count: 10},
                        {ItemCode: "223300", Count: 7},
                        {
                            ItemCode: {discount: "22%", \#content: "200777"},
                            Count: 7
                        }
                    ]
            },
            'xmlns\:ns: "ns.com",
            attr: "attr-val",
            ns\:attr: "ns-attr-val"
        };
    string expected =
        "<Purchesed_Bill1 xmlns=\"example.com\" xmlns:ns=\"ns.com\" attr=\"attr-val\" ns:attr=\"ns-attr-val\">" +
            "<PurchesedItems xmlns:ns0=\"example.com\">" +
                "<PLine>" +
                    "<ItemCode>223345</ItemCode>" +
                    "<Count>10</Count>" +
                "</PLine>" +
                "<PLine>" +
                    "<ItemCode>223300</ItemCode>" +
                    "<Count>7</Count>" +
                "</PLine>" +
                "<PLine>" +
                    "<ItemCode xmlns:ns1=\"example.com\" discount=\"22%\">200777</ItemCode>" +
                    "<Count>7</Count>" +
                "</PLine>" +
            "</PurchesedItems>" +
        "</Purchesed_Bill1>";
    xml result = check toXml(input);
    test:assertEquals(result.toString(), expected, msg = "testRecordWithNamaspaceAnnotationToXml1 result incorrect");
}

@Namespace {
    prefix: "ns0",
    uri: "example.com"
}
type Purchesed_Bill2 record {
    Purchesed_Items2 PurchesedItems;
    @Attribute
    string 'xmlns\:ns?;
    @Attribute
    string attr?;
    @Attribute
    string ns\:attr?;
};

@Namespace {
    uri: "example.com"
}
type Purchesed_Items2 record {
    Purchesed_Purchase2[] PLine;
};

type Purchesed_Purchase2 record {
    string|Purchesed_ItemCode2 ItemCode;
    int Count;
};

@Namespace {
    uri: "example1.com"
}
type Purchesed_ItemCode2 record {
    @Attribute
    string discount;
    string \#content?;
};

@test:Config {
    groups: ["toXml"]
}
isolated function testRecordWithNamaspaceAnnotationToXml2() returns error? {
    Purchesed_Bill2 input = {
            PurchesedItems: {
                    PLine: [
                        {ItemCode: "223345", Count: 10},
                        {ItemCode: "223300", Count: 7},
                        {
                            ItemCode: {discount: "22%", \#content: "200777"},
                            Count: 7
                        }
                    ]
            },
            'xmlns\:ns: "ns.com",
            attr: "attr-val",
            ns\:attr: "ns-attr-val"
        };
    string expected =
        "<ns0:Purchesed_Bill2 xmlns:ns0=\"example.com\" xmlns:ns=\"ns.com\" attr=\"attr-val\" ns:attr=\"ns-attr-val\">" +
            "<PurchesedItems xmlns=\"example.com\">" +
                "<PLine>" +
                    "<ItemCode>223345</ItemCode>" +
                    "<Count>10</Count>" +
                "</PLine>" +
                "<PLine>" +
                    "<ItemCode>223300</ItemCode>" +
                    "<Count>7</Count>" +
                "</PLine>" +
                "<PLine>" +
                    "<ItemCode xmlns=\"example1.com\" discount=\"22%\">200777</ItemCode>" +
                    "<Count>7</Count>" +
                "</PLine>" +
            "</PurchesedItems>" +
        "</ns0:Purchesed_Bill2>";
    xml result = check toXml(input);
    test:assertEquals(result.toString(), expected, msg = "testRecordWithNamaspaceAnnotationToXml2 result incorrect");
}
