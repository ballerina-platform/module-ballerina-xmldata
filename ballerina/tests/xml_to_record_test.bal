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

type Employee1 record {
    string name;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecord() {
    var x1 = xml `<!-- outer comment -->`;
    var x2 = xml `<name>Supun</name>`;
    xml x3 = x1 + x2;

    Employee1 expected = {
        name: "Supun"
    };

    Employee1|Error actual = toRecord(x3);
    if actual is Error {
        test:assertFail("failed to convert xml to record: " + actual.message());
    } else {
        test:assertEquals(actual, expected, msg = "testToRecord result incorrect");
    }
}

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithEscapedString() {
    var x1 = xml `<!-- outer comment -->`;
    var x2 = xml `<name>"Supun"</name>`;
    xml x3 = x1 + x2;

    Employee1 expected = {
        name: "\"Supun\""
    };

    Employee1|Error actual = toRecord(x3);
    if actual is Error {
        test:assertFail("failed to convert xml to record: " + actual.message());
    } else {
        test:assertEquals(actual, expected, msg = "testToRecordWithEscapedString result incorrect");
    }
}

type Order record {
    Invoice Invoice;
};

type Invoice record {
    PurchesedItems PurchesedItems;
    Address1 Address;
    string _xmlns?;
    string _xmlns_ns?;
    string _attr?;
    string _ns_attr?;
};

type PurchesedItems record {
    Purchase[] PLine;
};

type Purchase record {
    string ItemCode;
    string Count;
};

type Address1 record {
    string StreetAddress;
    string City;
    string Zip;
    string Country;
    string _xmlns?;
};

xml e2 = xml `<Invoice xmlns="example.com" attr="attr-val" xmlns:ns="ns.com" ns:attr="ns-attr-val">
                <PurchesedItems>
                    <PLine><ItemCode>223345</ItemCode><Count>10</Count></PLine>
                    <PLine><ItemCode>223300</ItemCode><Count>7</Count></PLine>
                    <PLine><ItemCode>200777</ItemCode><Count>7</Count></PLine>
                </PurchesedItems>
                <Address xmlns="">
                    <StreetAddress>20, Palm grove, Colombo 3</StreetAddress>
                    <City>Colombo</City>
                    <Zip>00300</Zip>
                    <Country>LK</Country>
                </Address>
              </Invoice>`;

@test:Config {
    groups: ["toRecord"]
}
function testToRecordComplexXmlElement() returns Error? {
    Order expected = {
        Invoice: {
            PurchesedItems: {
                PLine: [
                    {ItemCode: "223345", Count: "10"},
                    {ItemCode: "223300", Count: "7"},
                    {ItemCode: "200777", Count: "7"}
                ]
            },
            Address: {
                StreetAddress: "20, Palm grove, Colombo 3",
                City: "Colombo",
                Zip: "00300",
                Country: "LK",
                _xmlns: ""
            },
            _xmlns: "example.com",
            _xmlns_ns: "ns.com",
            _attr: "attr-val",
            _ns_attr: "ns-attr-val"
        }
    };
    Invoice|Error actual = toRecord(e2);
    if actual is Error {
        test:assertFail("failed to convert xml to record: " + actual.message());
    } else {
        test:assertEquals(actual, expected, msg = "testToRecordComplexXmlElement result incorrect");
    }
}

@test:Config {
    groups: ["toRecord"]
}
function testToRecordComplexXmlElementWithoutPreserveNamespaces() returns Error? {
    Order expected = {
        Invoice: {
            PurchesedItems: {
                PLine: [
                    {ItemCode: "223345", Count: "10"},
                    {ItemCode: "223300", Count: "7"},
                    {ItemCode: "200777", Count: "7"}
                ]
            },
            Address: {
                StreetAddress: "20, Palm grove, Colombo 3",
                City: "Colombo",
                Zip: "00300",
                Country: "LK"
            }
        }
    };
    Invoice|Error actual = toRecord(e2, {preserveNamespaces: false});
    if actual is Error {
        test:assertFail("failed to convert xml to record: " + actual.message());
    } else {
        test:assertEquals(actual, expected, msg = "testToRecordComplexXmlElementWithoutPreserveNamespaces result incorrect");
    }
}

type Student record {
    string name;
    int age;
    Address2 address;
    float gpa;
    boolean married;
    Courses courses;
};

type Address2 record {
    string city;
    int code;
    Contacts contact;
};

type Contacts record {
    int[] item;
};

type Courses record {
    string[] item;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithMultiLevelRecords() {
    xml payload = xml `
                <?xml version="1.0" encoding="UTF-8"?>
                <!-- outer comment -->
                <name>Alex</name>
                <age>29</age>
                <address>
                    <city>Colombo</city>
                    <code>10230</code>
                    <contact>
                        <item>768122</item>
                        <item>955433</item>
                    </contact>
                </address>
                <gpa>3.986</gpa>
                <married>true</married>
                <courses>
                    <item>Math</item>
                    <item>Physics</item>
                </courses>`;

    Student expected = {
        name: "Alex",
        age: 29,
        address: {
            city: "Colombo",
            code: 10230,
            contact: {item: [768122, 955433]}
        },
        gpa: 3.986,
        married: true,
        courses: {item: ["Math", "Physics"]}
    };

    Student|Error actual = toRecord(payload);
    if actual is Error {
        test:assertFail("failed to convert xml to record: " + actual.message());
    } else {
        test:assertEquals(actual, expected, msg = "testToRecordWithMultiLevelRecords result incorrect");
    }
}

type Commercial record {
    BookStore bookstore;
};

type BookStore record {
    string storeName;
    int postalCode;
    boolean isOpen;
    Address3 address;
    Codes codes;
    string _status;
};

type Address3 record {
    string street;
    string city;
    string country;
};

type Codes record {
    int[] item;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithAttribues() {
    xml payload = xml `
                    <bookstore status="online">
                        <storeName>foo</storeName>
                        <postalCode>94</postalCode>
                        <isOpen>true</isOpen>
                        <address>
                            <street>Galle Road</street>
                            <city>Colombo</city>
                            <country>Sri Lanka</country>
                        </address>
                        <codes>
                            <item>4</item>
                            <item>8</item>
                            <item>9</item>
                        </codes>
                    </bookstore>
                    <!-- some comment -->
                    <?doc document="book.doc"?>`;

    Commercial expected = {
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
            _status: "online"
        }
    };

    BookStore|Error actual = toRecord(payload);
    if actual is Error {
        test:assertFail("failed to convert xml to record: " + actual.message());
    } else {
        test:assertEquals(actual, expected, msg = "testToRecordWithAttribues result incorrect");
    }
}

type Commercial2 record {
    BookStore2 bookstore;
};

type BookStore2 record {
    string storeName;
    int postalCode;
    boolean isOpen;
    Address3 address;
    Codes codes;
    string _status;
    string _xmlns_ns0;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithNamespaces() {
    xml payload = xml `
                    <bookstore status="online" xmlns:ns0="http://sample.com/test">
                        <storeName>foo</storeName>
                        <postalCode>94</postalCode>
                        <isOpen>true</isOpen>
                        <address>
                            <street>Galle Road</street>
                            <city>Colombo</city>
                            <country>Sri Lanka</country>
                        </address>
                        <codes>
                            <item>4</item>
                            <item>8</item>
                            <item>9</item>
                        </codes>
                    </bookstore>
                    <!-- some comment -->
                    <?doc document="book.doc"?>`;

    Commercial2 expected = {
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
            _xmlns_ns0: "http://sample.com/test",
            _status: "online"
        }
    };

    BookStore|Error actual = toRecord(payload);
    if actual is Error {
        test:assertFail("failed to convert xml to record: " + actual.message());
    } else {
        test:assertEquals(actual, expected, msg = "testToRecordWithNamespaces result incorrect");
    }
}
