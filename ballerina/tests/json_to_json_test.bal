// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Address record {
    string street;
    string city;
};

type R record {|
    int id;
    string name;
    Address address;
|};

@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson1() returns error? {
    json jsonContent = {
                "id": 2,
                "name": "Anne",
                "address": {
                    "street": "Main",
                    "city": "94"
                }
            };

    byte[] bytes = jsonContent.toString().toBytes();

    R x = check fromJsonByteArrayWithType(bytes, R);
    test:assertEquals(x.id, 2);
    test:assertEquals(x.name, "Anne");
    test:assertEquals(x.address.street, "Main");
    test:assertEquals(x.address.city, "94");
}

type Coordinates record {
    float latitude;
    float longitude;
};

type AddressWithCord record {
    string street;
    int zipcode;
    Coordinates coordinates;
};

type Person record {
    string name;
    int age;
    AddressWithCord address;
};

@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson2() returns error? {
    json jsonContent = {
                "name": "John",
                "age": 30,
                "address": {
                    "street": "123 Main St",
                    "zipcode": 10001,
                    "coordinates": {
                        "latitude": 40.7128,
                        "longitude": -74.0060
                    }
                }
            };

    byte[] bytes = jsonContent.toString().toBytes();

    Person x = check fromJsonByteArrayWithType(bytes, Person);
    test:assertEquals(x.name, "John");
    test:assertEquals(x.age, 30);
    test:assertEquals(x.address.street, "123 Main St");
    test:assertEquals(x.address.zipcode, 10001);
    test:assertEquals(x.address.coordinates.latitude, 40.7128);
    test:assertEquals(x.address.coordinates.longitude, -74.0060);
}

type Author record {|
    string name;
    string birthdate;
    string hometown;
    boolean...;
|};

type Publisher record {|
    string name;
    int year;
    string...;
|};

type Book record {|
    string title;
    Author author;
    Publisher publisher;
    float...;
|};

@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson3() returns error? {
    json jsonContent = {
                "title": "To Kill a Mockingbird",
                "author": {
                    "name": "Harper Lee",
                    "birthdate": "1926-04-28",
                    "hometown": "Monroeville, Alabama",
                    "local": false
                },
                "price": 10.5,
                "publisher": {
                    "name": "J. B. Lippincott & Co.",
                    "year": 1960,
                    "location": "Philadelphia",
                    "tp": 12345
                }
            };

    byte[] bytes = jsonContent.toString().toBytes();

    Book x = check fromJsonByteArrayWithType(bytes, Book);
    test:assertEquals(x.title, "To Kill a Mockingbird");
    test:assertEquals(x.author.name, "Harper Lee");
    test:assertEquals(x.author.birthdate, "1926-04-28");
    test:assertEquals(x.author.hometown, "Monroeville, Alabama");
    test:assertEquals(x.publisher.name, "J. B. Lippincott & Co.");
    test:assertEquals(x.publisher.year, 1960);
    test:assertEquals(x.publisher["location"], "Philadelphia");
    test:assertEquals(x["price"], 10.5);
    test:assertEquals(x.author["local"], false);
}

type School record {|
    string name;
    int number;
    boolean flag;
    int...;
|};

@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson4() returns error? {
                    json jsonContent = {
                    "name": "School Twelve",
                    "city": 23,
                    "number": 12,
                    "section": 2,
                    "flag": true,
                    "tp": 12345
                };

    byte[] bytes = jsonContent.toString().toBytes();

    School x = check fromJsonByteArrayWithType(bytes, School);
    test:assertEquals(x.name, "School Twelve");
    test:assertEquals(x.number, 12);
    test:assertEquals(x.flag, true);
    test:assertEquals(x["section"], 2);
    test:assertEquals(x["tp"], 12345);
}

type TestRecord record {
    int intValue;
    float floatValue;
    string stringValue;
    decimal decimalValue;
};

@test:Config {
    groups: ["jsonToJson"]
}
function testJsonToJson5() returns error? {
    json jsonContent = {
        "intValue": 10,
        "floatValue": 10.5,
        "stringValue": "test",
        "decimalValue": 10.50
    };
    byte[] bytes = jsonContent.toString().toBytes();

    TestRecord x = check fromJsonByteArrayWithType(bytes, TestRecord);
    test:assertEquals(x.intValue, 10);
    test:assertEquals(x.floatValue, 10.5f);
    test:assertEquals(x.stringValue, "test");
    test:assertEquals(x.decimalValue, 10.50d);
}

type SchoolAddress record {
    string street;
    string city;
};

type School1 record {
    string name;
    SchoolAddress address;
};

type Student1 record {
    int id;
    string name;
    School1 school;
};

type Teacher record {
    int id;
    string name;
};

type Class record {
    int id;
    string name;
    Student1 student;
    Teacher teacher;
};

@test:Config {
    groups: ["jsonToJson"]
}
function testJsonToJson6() returns error? {
    json jsonContent = {
        "id": 1,
        "name": "Class A",
        "student": {
            "id": 2,
            "name": "John Doe",
            "school": {
                "name": "ABC School",
                "address": {
                    "street": "Main St",
                    "city": "New York"
                }
            }
        },
        "teacher": {
            "id": 3,
            "name": "Jane Smith"
        }
    };
    byte[] bytes = jsonContent.toString().toBytes();

    Class x = check fromJsonByteArrayWithType(bytes, Class);
    test:assertEquals(x.id, 1);
    test:assertEquals(x.name, "Class A");
    test:assertEquals(x.student.id, 2);
    test:assertEquals(x.student.name, "John Doe");
    test:assertEquals(x.student.school.name, "ABC School");
    test:assertEquals(x.student.school.address.street, "Main St");
    test:assertEquals(x.student.school.address.city, "New York");
    test:assertEquals(x.teacher.id, 3);
    test:assertEquals(x.teacher.name, "Jane Smith");
}

type TestRecord2 record {
    int intValue;
    TestRecord nested1;
};

@test:Config {
    groups: ["jsonToJsont"]
}
function testJsonToJson7() returns error? {
    json nestedJson = {
        "intValue": 5,
        "floatValue": 2.5,
        "stringValue": "nested",
        "decimalValue": 5.00
    };

    json jsonContent = {
        "intValue": 10,
        "nested1": nestedJson
    };

    byte[] bytes = jsonContent.toString().toBytes();

    TestRecord2 x = check fromJsonByteArrayWithType(bytes, TestRecord2);
    test:assertEquals(x.intValue, 10);
    test:assertEquals(x.nested1.intValue, 5);
}

type TestR record {|
    string street;
    string city;
|};

@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson8() returns error? {
    json jsonContent = {
                "street": "Main",
                "city": "Mahar",
                "house": 94
            };

    byte[] bytes = jsonContent.toString().toBytes();

    TestR x = check fromJsonByteArrayWithType(bytes, TestR);
    test:assertEquals(x.street, "Main");
    test:assertEquals(x.city, "Mahar");
}

type TestArr1 record {
    string street;
    string city;
    int[] houses;
};

@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson9() returns error? {
    json jsonContent = {
        "street": "Main",
        "city": "Mahar",
        "houses": [94, 95, 96]
    };
    byte[] bytes = jsonContent.toString().toBytes();

    TestArr1 x = check fromJsonByteArrayWithType(bytes, TestArr1);
    test:assertEquals(x.street, "Main");
    test:assertEquals(x.city, "Mahar");
    test:assertEquals(x.houses, [94, 95, 96]);
}

type TestArr2 record {
    string street;
    int city;
    [int, string] house;
};

@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson10() returns error? {
    json jsonContent = {
        "street": "Main",
        "city": 11,
        "house": [94, "Gedara"]
    };
    byte[] bytes = jsonContent.toString().toBytes();

    TestArr2 x = check fromJsonByteArrayWithType(bytes, TestArr2);
    test:assertEquals(x.street, "Main");
    test:assertEquals(x.city, 11);
    test:assertEquals(x.house, [94, "Gedara"]);
}

type TestArr3 record {
    string street;
    string city;
    [int, int[3]] house;
};

@test:Config {
    groups: ["jsonToJsonl"]
}
isolated function testJsonToJson11() returns error? {
    json jsonContent = {
        "street": "Main",
        "city": "Mahar",
        "house": [94, [1, 2, 3]]
    };
    byte[] bytes = jsonContent.toString().toBytes();

    TestArr3 x = check fromJsonByteArrayWithType(bytes, TestArr3);
    test:assertEquals(x.street, "Main");
    test:assertEquals(x.city, "Mahar");
    test:assertEquals(x.house, [94, [1, 2, 3]]);
}

type TestJson record {
    string street;
    json city;
    boolean flag;
};

@test:Config {
    groups: ["jsonToJsonj"]
}
isolated function testJsonToJson12() returns error? {
    json jsonContent = {
        "street": "Main",
        "city": {
            "name": "Mahar",
            "code": 94
        },
        "flag": true
    };
    byte[] bytes = jsonContent.toString().toBytes();

    TestJson x = check fromJsonByteArrayWithType(bytes, TestJson);
    test:assertEquals(x.street, "Main");
    test:assertEquals(x.city, {"name": "Mahar", "code": 94});
}


type AddressN record {
    string street;
    string city;
    int id;
};

type RN record {|
    int id;
    string name;
    AddressN address;
|};

@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson13() returns error? {
    // json jsonContent = {
    //             "id": 12,
    //             "name": "Anne",
    //             "address": {
    //                 "street": "Main",
    //                 "city": "94",
    //                 "id": true
    //             }
    //         };

    string strContent = "{\n\"id\": 12,\n\"name\": \"Anne\",\n\"address\": {\n\"street\": \"Main\",\n\"city\": \"94\",\n\"id\": true\n}\n}";

    byte[] bytes = strContent.toBytes();

    RN|Error x = fromJsonByteArrayWithType(bytes, RN);
    test:assertTrue(x is error);
    test:assertEquals((<error>x).message(), "incompatible value 'true' for type 'int' in field 'address.id' at line: 8 column: 0");
}

type RN2 record {|
    int id;
    string name;
|};

@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson14() returns error? {
    json jsonContent = {
                "id": 12
            };

    byte[] bytes = jsonContent.toString().toBytes();

    RN2|Error x = fromJsonByteArrayWithType(bytes, RN2);
    test:assertTrue(x is error);
    test:assertEquals((<error>x).message(), "required field 'name' not present in JSON at line: 1 column: 10");
}

@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson15() returns error? {
    json jsonContent = {
                "id": 12,
                "name": "Anne",
                "address": {
                    "street": "Main",
                    "city": "94"
                }
            };

    byte[] bytes = jsonContent.toString().toBytes();

    RN|Error x = fromJsonByteArrayWithType(bytes, RN);
    test:assertTrue(x is error);
    test:assertEquals((<error>x).message(), "required field 'id' not present in JSON at line: 1 column: 63");
}


@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson16() returns error? {
    json jsonContent = {
        "street": "Main",
        "city": "Mahar",
        "house": [94, [1, 3, "4"]]
    };
    byte[] bytes = jsonContent.toString().toBytes();

    TestArr3|error x = fromJsonByteArrayWithType(bytes, TestArr3);
    test:assertTrue(x is error);
}

@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson17() returns error? {
    json jsonContent = {
                "id": 12,
                "name": "Anne",
                "address": {
                    "id": 34,
                    "city": "94"
                }
            };

    byte[] bytes = jsonContent.toString().toBytes();

    RN|Error x = fromJsonByteArrayWithType(bytes, RN);
    test:assertTrue(x is error);
    test:assertEquals((<error>x).message(), "required field 'street' not present in JSON at line: 1 column: 56");
}

type intArr int[];

@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson18() returns error? {
    json jsonContent = [1, 2, 3];
    byte[] bytes = jsonContent.toString().toBytes();

    intArr x = check fromJsonByteArrayWithType(bytes, intArr);
    test:assertEquals(x, [1, 2, 3]);
}

type tup [int, string, [int, float]];

@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson19() returns error? {
    json jsonContent = [1, "abc", [3, 4.0]];
    byte[] bytes = jsonContent.toString().toBytes();

    tup|Error x = check fromJsonByteArrayWithType(bytes, tup);
    test:assertEquals(x, [1, "abc", [3, 4.0]]);
}

@test:Config {
    groups: ["jsonToJsonj2"]
}
isolated function testJsonToJson20() returns error? {
    json jsonContent = {
        "street": "Main",
        "city": {
            "name": "Mahar",
            "code": 94,
            "internal": {
                "id": 12,
                "agent": "Anne"
            }
        },
        "flag": true
    };
    byte[] bytes = jsonContent.toString().toBytes();

    TestJson x = check fromJsonByteArrayWithType(bytes, TestJson);
    test:assertEquals(x.street, "Main");
    test:assertEquals(x.city,{"name": "Mahar", "code": 94, "internal": {"id": 12, "agent": "Anne"}});
}

type DebugType record {|
    json id;
    readonly color;
    int...;
|};

@test:Config {
    groups: ["debugFunction"]
}
isolated function debugFunction() returns error? {
    json jsonContent = "{\"id\": 12, \"color\":true, \"name\": \"Anne\", \"address\": 34}";
    byte[] bytes = jsonContent.toString().toBytes();
    
    DebugType x = check fromJsonByteArrayWithType(bytes, DebugType);
    test:assertEquals(x.id, 12);
    test:assertEquals(x["address"], 34);
    test:assertEquals(x["name"], null);
    test:assertEquals(x.color, true);

}