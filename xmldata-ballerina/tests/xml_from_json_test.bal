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

type Person record {
    int id;
    int age = -1;
    decimal salary;
    string name;
    boolean married;
};

type Employee record {
    int id;
    string name;
    float salary;
    boolean permanent;
    string[] dependents;
    Contact contact;
};

type Contact record {
    int[] phone;
    Address address;
    string emergency;
};

type Address record {
    int number;
    string street;
};

@test:Config {
    groups: ["fromJson"]
}
isolated function testFromJSON() {
    json data = {
        name: "John",
        age: 30
    };
    xml|Error x = fromJson(data);
    if (x is xml) {
        test:assertEquals(x.toString(), "<name>John</name><age>30</age>", msg = "testFromJSON result incorrect");
    } else {
        test:assertFail("testFromJson result is not xml");
    }
}

@test:Config {
    groups: ["fromJson", "size"]
}
isolated function testJsonDataSizeOne() {
    json data = {id: 30};
    xml|Error x = fromJson(data);
    if (x is xml) {
        test:assertEquals(x.toString(), "<id>30</id>", msg = "testFromJSON result incorrect");
    } else {
        test:assertFail("testFromJson result is not xml");
    }
}

@test:Config {
    groups: ["fromJson", "size"]
}
isolated function testEmptyJson() {
    json data = {};
    xml|Error x = fromJson(data);
    if (x is xml) {
        test:assertEquals(x.toString(), "", msg = "testFromJSON result incorrect");
    } else {
        test:assertFail("testFromJson result is not xml");
    }
}

@test:Config {
    groups: ["fromJson", "size"]
}
isolated function testJsonArray() {
    json data = {   fname: "John",
                    lname: "Stallone",
                    family: [
                        {fname: "Peter", lname: "Stallone"},
                        {fname: "Emma", lname: "Stallone"},
                        {fname: "Jena", lname: "Stallone"},
                        {fname: "Paul", lname: "Stallone"}
                    ]
                };
    xml|Error x = fromJson(data, {attributePrefix:"age"});
    if (x is xml) {
        test:assertEquals(x.toString(),
        "<fname>John</fname><lname>Stallone</lname><family><root><fname>Peter</fname>" +
        "<lname>Stallone</lname></root><root><fname>Emma</fname>" +
        "<lname>Stallone</lname></root><root><fname>Jena</fname>" +
        "<lname>Stallone</lname></root><root><fname>Paul</fname>" +
        "<lname>Stallone</lname></root></family>",
        msg = "testFromJSON result incorrect");
    } else {
        test:assertFail("testFromJson result is not xml");
    }
}

@test:Config {
    groups: ["fromJson", "negative"]
}
isolated function testAttributeValidation() {
    json data =  {
                    writer: {
                         fname: "Christopher",
                         lname: "Nolan",
                         age: 30
                    }
                 };
    xml|error x = trap fromJson(data, {attributePrefix:"writer"});
    if (x is error) {
        test:assertEquals(x.toString(), "error(\"attribute cannot be an object or array\")",
                    msg = "testFromJSON result incorrect");
    } else {
        test:assertFail("Result is not mismatch");
    }
}
