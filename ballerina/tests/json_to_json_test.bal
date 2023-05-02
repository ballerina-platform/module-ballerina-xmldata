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

    _ = check fromJsonByteArrayWithType(bytes, R);
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

    _ = check fromJsonByteArrayWithType(bytes, Person);
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
                    "hometown": "Monroeville, Alabama"
                },
                "publisher": {
                    "name": "J. B. Lippincott & Co.",
                    "year": 1960,
                    "location": "Philadelphia"
                }
            };

    byte[] bytes = jsonContent.toString().toBytes();

    _ = check fromJsonByteArrayWithType(bytes, Book);
}

type School record {
    string name;
    string city;
    decimal number;
};

@test:Config {
    groups: ["jsonToJson"]
}
isolated function testJsonToJson4() returns error? {
                    json jsonContent = {
                    "name": "School Twelve",
                    "city": "Anytown",
                    "number": 12
                };

    byte[] bytes = jsonContent.toString().toBytes();

    _ = check fromJsonByteArrayWithType(bytes, School);
}