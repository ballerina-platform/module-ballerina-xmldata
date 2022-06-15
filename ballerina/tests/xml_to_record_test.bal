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
isolated function testToRecord() returns error? {
    var x1 = xml `<!-- outer comment -->`;
    var x2 = xml `<name>Supun</name>`;
    xml x3 = x1 + x2;

    Employee1 expected = {
        name: "Supun"
    };

    record {} actual = check toRecord(x3);
    test:assertEquals(actual, expected, msg = "testToRecord result incorrect");
}

type Employees record {
    string? name;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithOptionalfield() returns error? {
    var x1 = xml `<!-- outer comment -->`;
    var x2 = xml `<name>Supun</name>`;
    xml x3 = x1 + x2;

    Employees|Error actual = toRecord(x3);
    if (actual is Error) {
        test:assertEquals(actual.message(), "Failed to convert xml to record type: The record field: name does not " +
                          "support the optional value type: string?",
                          msg = "testToRecordWithOptionalfield result incorrect");
    } else {
        test:assertFail("testToRecordWithOptionalfield result incorrect");
    }
}

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithEscapedString() returns error? {
    var x1 = xml `<!-- outer comment -->`;
    var x2 = xml `<name>"Supun"</name>`;
    xml x3 = x1 + x2;

    Employee1 expected = {
        name: "\"Supun\""
    };

    Employee1 actual = check toRecord(x3);
    test:assertEquals(actual, expected, msg = "testToRecordWithEscapedString result incorrect");
}

xml e2 = xml `<Invoice xmlns="example.com" attr="attr-val" xmlns:ns="ns.com" ns:attr="ns-attr-val">
                <PurchesedItems>
                    <PLine><ItemCode>223345</ItemCode><Count>10</Count></PLine>
                    <PLine><ItemCode>223300</ItemCode><Count>7</Count></PLine>
                    <PLine><ItemCode discount="22%">200777</ItemCode><Count>7</Count></PLine>
                </PurchesedItems>
                <Address xmlns="">
                    <StreetAddress>20, Palm grove, Colombo 3</StreetAddress>
                    <City>Colombo</City>
                    <Zip>300</Zip>
                    <Country>LK</Country>
                </Address>
              </Invoice>`;

@test:Config {
    groups: ["toRecord"]
}
function testToRecordComplexXmlElement() returns error? {
    Order expected = {
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
                Country: "LK",
                _xmlns: ""
            },
            _xmlns: "example.com",
            _xmlns\:ns: "ns.com",
            _attr: "attr-val",
            _ns\:attr: "ns-attr-val"
        }
    };
    Order actual = check toRecord(e2);
    test:assertEquals(actual, expected, msg = "testToRecordComplexXmlElement result incorrect");
}

@test:Config {
    groups: ["toRecord"]
}
function testToRecordComplexXmlElementWithoutPreserveNamespaces() returns error? {
    Order expected = {
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
            "_attr": "attr-val"
        }
    };
    Order actual = check toRecord(e2, preserveNamespaces = false);
    test:assertEquals(actual, expected,
                msg = "testToRecordComplexXmlElementWithoutPreserveNamespaces result incorrect");
}

type mail record {
    Envelope Envelope;
};

type Envelope record {
    string Header;
    Body Body;
};

type Body record {
    getSimpleQuoteResponse getSimpleQuoteResponse;
};

type getSimpleQuoteResponse record {
    'return 'return;
};

type 'return record {
    string change;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordComplexXmlElementWithoutPreserveNamespaces2() returns error? {
    xml x1 = xml `<?xml version="1.0" encoding="UTF-8"?>
                  <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                     <soapenv:Header/>
                     <soapenv:Body>
                        <ns:getSimpleQuoteResponse xmlns:ns="http://services.samples">
                           <ns:return xmlns:ax21="http://services.samples/xsd"
                            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:type="ax21:GetQuoteResponse">
                              <ax21:change>4.49588025550579</ax21:change>
                           </ns:return>
                        </ns:getSimpleQuoteResponse>
                     </soapenv:Body>
                  </soapenv:Envelope>`;

    mail expected = {
        Envelope: {
            Header: "",
            Body: {
                getSimpleQuoteResponse: {
                    "return": {change: "4.49588025550579"}
                }
            }
        }
    };
    mail actual = check toRecord(x1, preserveNamespaces = false);
    test:assertEquals(actual, expected,
                msg = "testToRecordComplexXmlElementWithoutPreserveNamespaces2 result incorrect");
}

type emptyChild record {
    foo foo;
};

type foo record {
    string bar;
    string car;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithEmptyChildren() returns error? {
    xml x = xml `<foo><bar>2</bar><car></car></foo>`;
    emptyChild expected = {foo: {bar: "2", car: ""}};

    emptyChild actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithEmptyChildren result incorrect");
}

type r1 record {
    Root1 Root;
};

type Root1 record {
    string[] A;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordSameKeyArray() returns Error? {
    xml x = xml `<Root><A>A</A><A>B</A><A>C</A></Root>`;
    r1 expected = {
        Root: {
            A: ["A", "B", "C"]
        }
    };

    r1 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordSameKeyArray result incorrect");
}

type r2 record {
    Root2 Root;
};

type Root2 record {
    string _xmlns\:ns;
    string _ns\:x;
    string _x;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithMultipleAttributesAndNamespaces() returns Error? {
    xml x = xml `<Root xmlns:ns="ns.com" ns:x="y" x="z"/>`;

    r2 expected = {
        Root: {
            _xmlns\:ns: "ns.com",
            _ns\:x: "y",
            _x: "z"
        }
    };

    r2 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithMultipleAttributesAndNamespaces result incorrect");
}

type r3 record {
    Root3 Root;
};

type Root3 record {
    string _xmlns\:ns;
    string _ns\:x;
    string _x;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithOptinalValues() returns Error? {
    xml x = xml `<Root xmlns:ns="ns.com" ns:x="y" x="z"/>`;

    r3 expected = {
        Root: {
            _xmlns\:ns: "ns.com",
            _ns\:x: "y",
            _x: "z"
        }
    };

    r3 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithMultipleAttributesAndNamespaces result incorrect");
}

type empty record {
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithComment() returns error? {
    xml x = xml `<?xml version="1.0" encoding="UTF-8"?>`;
    empty actual = check toRecord(x);

    empty expected = {};
    test:assertEquals(actual, expected, msg = "testToRecordWithComment result incorrect");
}

type shelf record {
    books books;
};

type books record {
    string[] item;
    string[] item1;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithMultipleArray() returns error? {
    xml x = xml `<books>
                      <item>book1</item>
                      <item>book2</item>
                      <item>book3</item>
                      <item1>book1</item1>
                      <item1>book2</item1>
                      <item1>book3</item1>
                  </books>
                  `;
    shelf actual = check toRecord(x);

    shelf expected = {
        books: {
            item: ["book1", "book2", "book3"],
            item1: ["book1", "book2", "book3"]
        }
    };
    test:assertEquals(actual, expected, msg = "testToRecordWithMultipleArray result incorrect");
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
isolated function testToRecordWithMultiLevelRecords() returns error? {
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

    Student actual = check toRecord(payload);
    test:assertEquals(actual, expected, msg = "testToRecordWithMultiLevelRecords result incorrect");
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
isolated function testToRecordWithAttribues() returns error? {
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

    Commercial actual = check toRecord(payload);
    test:assertEquals(actual, expected, msg = "testToRecordWithAttribues result incorrect");
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
    string _xmlns\:ns0;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithNamespaces() returns error? {
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
            _xmlns\:ns0: "http://sample.com/test",
            _status: "online"
        }
    };

    Commercial2 actual = check toRecord(payload);
    test:assertEquals(actual, expected, msg = "testToRecordWithNamespaces result incorrect");
}

type Employee2 record {
    string name;
    int age;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordNagativeOpenRecord() {
    var x1 = xml `<!-- outer comment -->`;
    var x2 = xml `<name>Supun</name>`;
    xml x3 = x1 + x2;

    Employee2|Error actual = toRecord(x3);
    if actual is Error {
        test:assertTrue(actual.toString().includes("missing required field 'age' of type 'int' in record 'xmldata:Employee2'"));
    } else {
        test:assertFail("testToRecordNagativeOpenRecord result is not a mismatch");
    }
}

type Employee3 record {|
    string name;
|};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordNagativeClosedRecord() {
    var x1 = xml `<!-- outer comment -->`;
    var x2 = xml `<name>Supun</name><age>29</age>`;
    xml x3 = x1 + x2;

    Employee3|error actual = toRecord(x3);
    if actual is error {
        test:assertTrue(actual.toString().includes("field 'age' cannot be added to the closed record 'xmldata:Employee3'"));
    } else {
        test:assertFail("testToRecordNagative result is not a mismatch");
    }
}

type emptyChild1 record {
    foo1 foo;
};

type foo1 record {
    int bar;
    string car;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithOptinalField2() returns error? {
    xml x = xml `<foo><bar>2</bar><car></car></foo>`;
    emptyChild1 expected = {foo: {bar: 2, car: ""}};

    emptyChild1 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithEmptyChildren result incorrect");
}

type Root4 record {
    Root5 Root;
};

type Root5 record {
    int[] A;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithOptionalArrayValues() returns Error? {
    xml x = xml `<Root><A>2</A><A>3</A><A>4</A></Root>`;
    Root4 expected = {
        Root: {
            A: [2, 3, 4]
        }
    };

    Root4 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithOptionalArrayValues result incorrect");
}

type Root6 record {
    Root7 Root;
};

type Root7 record {
    decimal[] A;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithOptionalArrayValues2() returns Error? {
    xml x = xml `<Root><A>2.3</A><A>3.3</A><A>4.3</A></Root>`;
    Root6 expected = {
        Root: {
            A: [2.3, 3.3, 4.3]
        }
    };

    Root6 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithOptionalArrayValues2 result incorrect");
}

type Root8 record {
    Root9 Root;
};

type Root9 record {
    decimal[]|string[] A;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithUnionArrayValues3() returns Error? {
    xml x = xml `<Root><A>value1</A><A>value2</A><A>value3</A></Root>`;
    Root8 expected = {
        Root: {
            A: ["value1", "value2", "value3"]
        }
    };

    Root8 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithUnionArrayValues3 result incorrect");
}

type Root10 record {
    Root11 Root;
};

type Root11 record {
    decimal[]|string[] A;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithUnionArrayValues4() returns Error? {
    xml x = xml `<Root><A>2.4</A><A>3.4</A><A>4.4</A></Root>`;
    Root10 expected = {
        Root: {
            A: [2.4, 3.4, 4.4]
        }
    };

    Root10 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithUnionArrayValues4 result incorrect");
}

type Root12 record {
    Root13 Root;
};

type Root13 record {
    decimal|int A;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithUnionArrayValues5() returns Error? {
    xml x = xml `<Root><A>2</A></Root>`;
    Root12 expected = {
        Root: {
            A: 2.0d
        }
    };

    Root12 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithUnionArrayValues5 result incorrect");
}

type Root14 record {
    Root15 Root;
};

type Root15 record {
    int|decimal A;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithUnionArrayValues6() returns Error? {
    xml x = xml `<Root><A>2.5</A></Root>`;
    Root14 expected = {
        Root: {
            A: 2.5
        }
    };

    Root14 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithUnionArrayValues6 result incorrect");
}

type Root16 record {
    Root17 Root;
};

type Root17 record {
    float[]|string[] A;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithUnionArrayValues7() returns Error? {
    xml x = xml `<Root><A>2.0</A><A>3.0</A><A>4.0</A></Root>`;
    Root16 expected = {
        Root: {
            A: [2.0, 3.0, 4.0]
        }
    };

    Root16 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithUnionArrayValues7 result incorrect");
}

type Root18 record {
    Root19 Root;
};

type Root19 record {
    boolean[]|string[] A;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithUnionArrayValues8() returns Error? {
    xml x = xml `<Root><A>true</A><A>true</A><A>true</A></Root>`;
    Root18 expected = {
        Root: {
            A: [true, true, true]
        }
    };

    Root18 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithUnionArrayValues8 result incorrect");
}

type Root20 record {
    Root21 Root;
};

type Root21 record {
    float[]|int[] A;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithUnionArrayValues9() returns Error? {
    xml x = xml `<Root><A>2.4</A><A>2.4</A><A>2.4</A></Root>`;
    Root20 expected = {
        Root: {
            A: [2.4f, 2.4f, 2.4f]
        }
    };

    Root20 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithUnionArrayValues9 result incorrect");
}

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithOptionalArrayValues10() returns Error? {
    xml x = xml `<Root><A>2</A><A>2</A><A>2</A></Root>`;
    Root20 expected = {
        Root: {
            A: [2.0, 2.0, 2.0]
        }
    };

    Root20 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithUnionArrayValues10 result incorrect");
}

type Root24 record {
    Root25 Root;
};

type Root25 record {
    int[]|float[] A;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithUnionArrayValues12() returns Error? {
    xml x = xml `<Root><A>2</A><A>2</A><A>2</A></Root>`;
    Root24 expected = {
        Root: {
            A: [<int>2, <int>2, <int>2]
        }
    };

    Root24 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithUnionArrayValues12 result incorrect");
}

type Root26 record {
    Root27 Root;
};

type Root27 record {
    int A?;
    int B;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithOptinalField12() returns Error? {
    xml x = xml `<Root><B>2</B></Root>`;
    Root26 expected = {
        Root: {B: 2}
    };
    Root26 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithOptinalValues12 result incorrect");
}

type Root28 record {
    Root29 Root;
};

type Root29 record {
    int A?;
    int[] B;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithArrayField13() returns Error? {
    xml x = xml `<Root><B>2</B></Root>`;
    Root28 expected = {
        Root: {B: [2]}
    };

    Root28 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithOptinalValues13 result incorrect");
}

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithArrayField14() returns Error? {
    xml x = xml `<Root><B></B></Root>`;
    Root28 expected = {
        Root: {B: []}
    };

    Root28 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithOptinalValues14 result incorrect");
}

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithArrayField15() returns Error? {
    xml x = xml `<Root><B>2</B><B></B></Root>`;
    Root28 expected = {
        Root: {B: [2]}
    };

    Root28 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithOptinalValues15 result incorrect");
}

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithArrayField16() returns Error? {
    xml x = xml `<Root><B></B><B>2</B></Root>`;
    Root28 expected = {
        Root: {B: [2]}
    };

    Root28 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithOptinalValues16 result incorrect");
}

public type ListOfContinentsByNameResponse record {
    ArrayOftContinent ListOfContinentsByNameResult;
};

public type ArrayOftContinent record {
    tContinent[] tContinent?;
};

public type tContinent record {
    string sCode;
    string sName;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithArrayField17() returns Error? {
    xml responsePayload = xml `<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <m:ListOfContinentsByNameResponse xmlns:m="http://www.oorsprong.org/websamples.countryinfo">
          <m:ListOfContinentsByNameResult>
            <m:tContinent>
              <m:sCode>AF</m:sCode>
              <m:sName>Africa</m:sName>
            </m:tContinent>
            <m:tContinent>
              <m:sCode>AN</m:sCode>
              <m:sName>Antarctica</m:sName>
            </m:tContinent>
            <m:tContinent>
              <m:sCode>AS</m:sCode>
              <m:sName>Asia</m:sName>
            </m:tContinent>
            <m:tContinent>
              <m:sCode>EU</m:sCode>
              <m:sName>Europe</m:sName>
            </m:tContinent>
            <m:tContinent>
              <m:sCode>OC</m:sCode>
              <m:sName>Ocenania</m:sName>
            </m:tContinent>
            <m:tContinent>
              <m:sCode>AM</m:sCode>
              <m:sName>The Americas</m:sName>
            </m:tContinent>
          </m:ListOfContinentsByNameResult>
        </m:ListOfContinentsByNameResponse>
      </soap:Body>
    </soap:Envelope>`;
    ListOfContinentsByNameResponse expected = {
        "ListOfContinentsByNameResult": {
            "tContinent": [
                {"sCode": "AF", "sName": "Africa"},
                {"sCode": "AN", "sName": "Antarctica"},
                {"sCode": "AS", "sName": "Asia"},
                {"sCode": "EU", "sName": "Europe"},
                {"sCode": "OC", "sName": "Ocenania"},
                {"sCode": "AM", "sName": "The Americas"}
            ]
        }
    };
    ListOfContinentsByNameResponse actual = check toRecord(responsePayload/*/*/*, false);
    test:assertEquals(actual, expected, msg = "testToRecordWithArrayField17 result incorrect");
}

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithArrayField18() returns Error? {
    xml responsePayload = xml `<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <m:ListOfContinentsByNameResponse xmlns:m="http://www.oorsprong.org/websamples.countryinfo">
          <m:ListOfContinentsByNameResult>
            <m:tContinent>
              <m:sCode>AF</m:sCode>
              <m:sName>Africa</m:sName>
            </m:tContinent>
          </m:ListOfContinentsByNameResult>
        </m:ListOfContinentsByNameResponse>
      </soap:Body>
    </soap:Envelope>`;
    ListOfContinentsByNameResponse expected = {
        "ListOfContinentsByNameResult": {
            "tContinent": [
                {"sCode": "AF", "sName": "Africa"}
            ]
        }
    };
    ListOfContinentsByNameResponse actual = check toRecord(responsePayload/*/*/*, false);
    test:assertEquals(actual, expected, msg = "testToRecordWithArrayField18 result incorrect");
}

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithArrayField19() returns Error? {
    xml responsePayload = xml `<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <m:ListOfContinentsByNameResponse xmlns:m="http://www.oorsprong.org/websamples.countryinfo">
          <m:ListOfContinentsByNameResult>
            <m:tContinent>
            </m:tContinent>
          </m:ListOfContinentsByNameResult>
        </m:ListOfContinentsByNameResponse>
      </soap:Body>
    </soap:Envelope>`;
    ListOfContinentsByNameResponse expected = {
        "ListOfContinentsByNameResult": {
            "tContinent": []
        }
    };
    ListOfContinentsByNameResponse actual = check toRecord(responsePayload/*/*/*, false);
    test:assertEquals(actual, expected, msg = "testToRecordWithArrayField19 result incorrect");
}

public type SoapEnvelope record {
    Envelope1 soap\:Envelope;
};

public type Envelope1 record {
    Body1 soap\:Body;
    string _xmlns\:soap;
};

public type Body1 record {
    ListOfContinentsByNameResponse1 m\:ListOfContinentsByNameResponse;
};

public type ListOfContinentsByNameResponse1 record {
    ListOfContinentsByNameResult1 m\:ListOfContinentsByNameResult;
    string _xmlns\:m;
};

public type ListOfContinentsByNameResult1 record {
    Continent[] m\:tContinent;
};

public type Continent record {
    string m\:sCode;
    string m\:sName;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithArrayField20() returns Error? {
    xml responsePayload = xml `<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <m:ListOfContinentsByNameResponse xmlns:m="http://www.oorsprong.org/websamples.countryinfo">
          <m:ListOfContinentsByNameResult>
            <m:tContinent>
              <m:sCode>AF</m:sCode>
              <m:sName>Africa</m:sName>
            </m:tContinent>
            <m:tContinent>
              <m:sCode>AN</m:sCode>
              <m:sName>Antarctica</m:sName>
            </m:tContinent>
            <m:tContinent>
              <m:sCode>AS</m:sCode>
              <m:sName>Asia</m:sName>
            </m:tContinent>
            <m:tContinent>
              <m:sCode>EU</m:sCode>
              <m:sName>Europe</m:sName>
            </m:tContinent>
            <m:tContinent>
              <m:sCode>OC</m:sCode>
              <m:sName>Ocenania</m:sName>
            </m:tContinent>
            <m:tContinent>
              <m:sCode>AM</m:sCode>
              <m:sName>The Americas</m:sName>
            </m:tContinent>
          </m:ListOfContinentsByNameResult>
        </m:ListOfContinentsByNameResponse>
      </soap:Body>
    </soap:Envelope>`;
    SoapEnvelope expected = {
        soap\:Envelope: {
            soap\:Body: {
                m\:ListOfContinentsByNameResponse: {
                    m\:ListOfContinentsByNameResult: {
                        m\:tContinent: [
                            {m\:sCode: "AF", m\:sName: "Africa"},
                            {m\:sCode: "AN", m\:sName: "Antarctica"},
                            {m\:sCode: "AS", m\:sName: "Asia"},
                            {m\:sCode: "EU", m\:sName: "Europe"},
                            {m\:sCode: "OC", m\:sName: "Ocenania"},
                            {m\:sCode: "AM", m\:sName: "The Americas"}
                        ]
                    },
                    _xmlns\:m: "http://www.oorsprong.org/websamples.countryinfo"
                }
            },
            _xmlns\:soap: "http://schemas.xmlsoap.org/soap/envelope/"
        }
    };
    SoapEnvelope actual = check toRecord(responsePayload);
    test:assertEquals(actual, expected, msg = "testToRecordWithArrayField20 result incorrect");
}

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithArrayField21() returns Error? {
    xml responsePayload = xml `<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <m:ListOfContinentsByNameResponse xmlns:m="http://www.oorsprong.org/websamples.countryinfo">
          <m:ListOfContinentsByNameResult>
            <m:tContinent>
              <m:sCode>AF</m:sCode>
              <m:sName>Africa</m:sName>
            </m:tContinent>
          </m:ListOfContinentsByNameResult>
        </m:ListOfContinentsByNameResponse>
      </soap:Body>
    </soap:Envelope>`;
    SoapEnvelope expected = {
        soap\:Envelope: {
            soap\:Body: {
                m\:ListOfContinentsByNameResponse: {
                    m\:ListOfContinentsByNameResult: {
                        m\:tContinent: [
                            {m\:sCode: "AF", m\:sName: "Africa"}
                        ]
                    },
                    _xmlns\:m: "http://www.oorsprong.org/websamples.countryinfo"
                }
            },
            _xmlns\:soap: "http://schemas.xmlsoap.org/soap/envelope/"
        }
    };
    SoapEnvelope actual = check toRecord(responsePayload);
    test:assertEquals(actual, expected, msg = "testToRecordWithArrayField21 result incorrect");
}

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithArrayField22() returns Error? {
    xml responsePayload = xml `<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <m:ListOfContinentsByNameResponse xmlns:m="http://www.oorsprong.org/websamples.countryinfo">
          <m:ListOfContinentsByNameResult>
            <m:tContinent>
            </m:tContinent>
          </m:ListOfContinentsByNameResult>
        </m:ListOfContinentsByNameResponse>
      </soap:Body>
    </soap:Envelope>`;
    SoapEnvelope expected = {
        soap\:Envelope: {
            soap\:Body: {
                m\:ListOfContinentsByNameResponse: {
                    m\:ListOfContinentsByNameResult: {
                        m\:tContinent: []
                    },
                    _xmlns\:m: "http://www.oorsprong.org/websamples.countryinfo"
                }
            },
            _xmlns\:soap: "http://schemas.xmlsoap.org/soap/envelope/"
        }
    };
    SoapEnvelope actual = check toRecord(responsePayload);
    test:assertEquals(actual, expected, msg = "testToRecordWithArrayField22 result incorrect");
}

type Root30 record {
    FuelEvents s\:FuelEvents;
};

type FuelEvents record {
    FuelEvent[] s\:FuelEvent;
    string _xmlns\:s;
};

type FuelEvent record {
    int s\:odometerReading;
    float s\:gallons;
    float s\:gasPrice;
    string _employeeId;
};

@test:Config {
    groups: ["toRecord"]
}
isolated function testToRecordWithSameAttribute() returns Error? {
    xml x = xml `<s:FuelEvents xmlns:s="http://www.so2w.org">
                     <s:FuelEvent employeeId="2312">
                         <s:odometerReading>230</s:odometerReading>
                         <s:gallons>18.561</s:gallons>
                         <s:gasPrice>4.56</s:gasPrice>
                     </s:FuelEvent>
                     <s:FuelEvent employeeId="2312">
                         <s:odometerReading>500</s:odometerReading>
                         <s:gallons>19.345</s:gallons>
                         <s:gasPrice>4.89</s:gasPrice>
                     </s:FuelEvent>
                 </s:FuelEvents>`;
    Root30 expected = {
        s\:FuelEvents: {
            s\:FuelEvent: [
                {
                    _employeeId: "2312",
                    s\:odometerReading: 230,
                    s\:gallons: 18.561,
                    s\:gasPrice: 4.56
                },
                {
                    _employeeId: "2312",
                    s\:odometerReading: 500,
                    s\:gallons: 19.345,
                    s\:gasPrice: 4.89
                }
            ],
            _xmlns\:s: "http://www.so2w.org"
        }
    };

    Root30 actual = check toRecord(x);
    test:assertEquals(actual, expected, msg = "testToRecordWithSameAttribute result incorrect");
}
