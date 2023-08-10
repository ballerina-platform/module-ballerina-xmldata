// Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/xmldata;

type Foo record {
    Bar? foo;
};

type Bar record {
    int? bar;
    string car;
};

public function main() returns error? {
    xml x = xml `<foo><bar>2</bar><car></car></foo>`;
    _ = check getValue(x);
}

type Foo1 record {
    string|Bar1? foo;
};

type Bar1 record {
    int? bar;
    Foo1|string car;
};

function getValue(xml x) returns Foo|error {
    Foo actual = check xmldata:toRecord(x);
    Foo1 actual1 = check xmldata:toRecord(x);
    return actual;
}
