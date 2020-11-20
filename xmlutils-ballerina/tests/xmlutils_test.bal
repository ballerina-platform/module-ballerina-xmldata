// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

@test:Config {}
isolated function testFromJSON() {
    json data = {
        name: "John",
        age: 30
    };
    xml|error x = fromJSON(data);
    if (x is xml) {
        test:assertEquals(x.toString(), "<name>John</name><age>30</age>", msg = "testFromJSON result incorrect");   
    } else {
        test:assertFail("testFromJSON result is not xml");
    }  
}

@test:Config {}
public isolated function testFromTable() {
    table<Person> personTable = table[ { id: 1, age: 30,  salary: 300.5, name: "Mary", married: true },
          { id: 2, age: 20,  salary: 300.5, name: "John", married: true }
        ];
    test:assertEquals(fromTable(personTable).toString(), "<results>" +
                "<result>" +
                    "<id>1</id><age>30</age><salary>300.5</salary><name>Mary</name><married>true</married>" +
                "</result>" +
                "<result>" +
                    "<id>2</id><age>20</age><salary>300.5</salary><name>John</name><married>true</married>" +
                "</result>" +
                "</results>", 
    msg = "testFromTable result incorrect");   
}

@test:Config {}
public isolated function testFromTable2() {
    table<Employee> employeeTable = table [
                {id: 1, name: "Mary", salary: 300.5, permanent: true, dependents: ["Mike", "Rachel"],
                    contact: {
                        phone: [445566, 778877],
                        address: {number: 34, street: "Straford"},
                        emergency: "Stephen"}},
                {id: 2, name: "John", salary: 200.5, permanent: false, dependents: ["Kyle"],
                    contact: {
                        phone: [6060606, 556644],
                        address: {number: 10, street: "Oxford"},
                        emergency: "Elizabeth"}} ,
                {id: 3, name: "Jim", salary: 330.5, permanent: true, dependents: [],
                    contact: {
                        phone: [960960, 889889],
                        address: {number: 46, street: "Queens"},
                        emergency: "Veronica"}}
            ];
    test:assertEquals(fromTable(employeeTable).toString(), "<results>" +
                        "<result>" +
                        "<id>1</id><name>Mary</name><salary>300.5</salary><permanent>true</permanent>" +
                        "<dependents><element>Mike</element><element>Rachel</element></dependents>" +
                        "<contact>" +
                            "<phone>[445566,778877]</phone>" +
                            "<address><number>34</number><street>Straford</street></address>" +
                            "<emergency>Stephen</emergency>" +
                        "</contact>" +
                        "</result>" +
                        "<result>" +
                        "<id>2</id><name>John</name><salary>200.5</salary><permanent>false</permanent>" +
                        "<dependents><element>Kyle</element></dependents>" +
                        "<contact>" +
                            "<phone>[6060606,556644]</phone>" +
                            "<address><number>10</number><street>Oxford</street></address>" +
                            "<emergency>Elizabeth</emergency>" +
                        "</contact>" +
                        "</result>" +
                        "<result>" +
                        "<id>3</id><name>Jim</name><salary>330.5</salary><permanent>true</permanent>" +
                        "<dependents/>" +
                        "<contact>" +
                            "<phone>[960960,889889]</phone>" +
                            "<address><number>46</number><street>Queens</street></address>" +
                            "<emergency>Veronica</emergency>" +
                        "</contact>" +
                        "</result>" +
                        "</results>", 
    msg = "testFromTable2 result incorrect");  
}
