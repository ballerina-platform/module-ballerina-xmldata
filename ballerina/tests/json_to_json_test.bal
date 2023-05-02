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

type R record {
    int id;
    string name;
    Address address;
};

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

type Author record {
    string name;
    string birthdate;
    string hometown;
};

type Publisher record {
    string name;
    int year;
    string location;
};

type Book record {
    string title;
    Author author;
    Publisher publisher;
};

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
                    "books": 23
                },
                "publisher": {
                    "name": "J. B. Lippincott & Co.",
                    "year": 1960,
                    "location": "Philadelphia"
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
    test:assertEquals(x.publisher.location, "Philadelphia");
}

type School record {
    string name;
    decimal number;
    string optfield?;
    boolean flag;
};

@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson4() returns error? {
                    json jsonContent = {
                    "name": "School Twelve",
                    "city": "Anytown",
                    "number": 12,
                    "flag": true
                };

    byte[] bytes = jsonContent.toString().toBytes();

    School x = check fromJsonByteArrayWithType(bytes, School);
    test:assertEquals(x.name, "School Twelve");
    test:assertEquals(x.number, 12d);
    test:assertEquals(x.optfield, ());
    test:assertEquals(x.flag, true);
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
function testJsonToJson5() returns error?{
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
    groups: ["jsonToJson"]
}
function testJsonToJson7() returns error?{
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
    // TODO fix test:assertEquals(x.nested1.intValue, 5) running with other tests
}
