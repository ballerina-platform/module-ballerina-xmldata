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

type Test record {
    Bar|Bar1? foo;
};

type Foo record {
    Bar|Bar1? foo;
};

type Bar record {
    int? bar;
    Bar2 bar2;
};

type Bar2 record {
    int? bar;
    Bar3 car;
};

type Bar1 record {
    int? bar;
    string car;
};

type Bar3 record {
    int? bar;
    string car;
};

type Bar4 record {
    int? bar;
    string car;
};

type Test5 record {
    int? bar;
    string car;
};

public function main() returns error? {
    xml x1 = xml `<foo><bar>2</bar><car></car></foo>`;
    Foo actual = check xmldata:toRecord(x1);
}
