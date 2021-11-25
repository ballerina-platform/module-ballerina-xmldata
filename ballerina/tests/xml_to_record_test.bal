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
type Student record {
    string name;
    int age;
    Address1 address;
    float gpa;
    boolean married;
};

type Address1 record {
    string city;
    int code;
    Contact1 contact;
};

type Contact1 record {
    int home;
    int office;
};

@test:Config {
    groups: ["toJson"]
}
isolated function testToRecord() returns Error? {
    xml x = xml `<?xml version="1.0" encoding="UTF-8"?>
                <!-- outer comment -->
                <name>Alex</name>
                <age>29</age>
                <address>
                    <city>Colombo</city>
                    <code>10230</code>
                    <contact>
                        <home>768122</home>
                        <office>955433</office>
                    </contact>
                </address>
                <gpa>3.986</gpa>
                <married>true</married>`;
    Student expected = {
        name: "Alex",
        age: 29,
        address: {
            city: "Colombo",
            code: 10230,
            contact: {
                home: 768122,
                office: 955433
            }
        },
        gpa: 3.986,
        married: true
    };
    Student|Error actual = toRecord(x);
    if actual is Error {
        test:assertFail("failed to convert xml to record: " + actual.message());
    } else {
        test:assertEquals(actual, expected, msg = "testToRecord result incorrect");
    }
}