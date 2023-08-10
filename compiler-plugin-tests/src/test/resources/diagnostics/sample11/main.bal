// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

@xmldata:Name{
    value: "Foo1"
}
type Foo record {
    Bar foo;
};

@xmldata:Name{
    value: "Bar1"
}
type Bar record {
    int bar;
    Bar2 bar2;
};

@xmldata:Name{
    value: "Bar4"
}
@xmldata:Namespace {
    prefix: "ns",
    uri: "http://sdf.com"
}
type Bar2 record {
    int bar;
    string car;
};

public function main() returns error? {
    xml x1 = xml `<foo><bar>2</bar><car></car></foo>`;
    Foo actual = check xmldata:fromXml(x1);
    Bar result = check xmldata:fromXml(x1);
}
