# Specification: Ballerina Cache Library

_Owners_: @daneshk @kalaiyarasiganeshalingam @MadhukaHarith92                                       
_Reviewers_: @daneshk  
_Created_: 2021/12/10  
_Updated_: 2021/12/15   
_Issue_: [#2334](https://github.com/ballerina-platform/ballerina-standard-library/issues/2334)

# Introduction
This is the specification for the Xmldata library which provides APIs to perform conversions between XML and JSON/Ballerina records. It is part of Ballerina Standard Library. [Ballerina programming language](https://ballerina.io/) is an open-source programming language for the cloud that makes it easier to use, combine, and create network services.

# Contents
1. [Overview](#1-overview)
2. [Data structure](#2-data-structure)
    * 2.1 [JSON](#21-json)
    * 2.1 [XML](#22-xml)
    * 2.1 [Record](#23-record)
3. [Rules](#3-rules)
    * 3.1 [Rules for XML to JSON Conversion](#31-rules-for-xml-to-json-conversion)
    * 3.2 [Rules for XML to Record Conversion](#32-rules-for-xml-to-json-conversion)
    * 3.1 [Rules for JSON to XML Conversion](#33-rules-for-json-to-xml-conversion)
4. [Operations](#4-operations)
    * 4.1 [XML to JSON Conversion](#41-xml-to-json-conversion)
        * 4.1.1 [Sample](#411-sample)
    * 4.2 [XML to Record Conversion]()
        * 4.2.1 [Sample](#421-sample)
    * 4.3 [JSON to XML Conversion]()
        * 4.3.1 [Sample1](#431-sample1)

## 1. Overview
This specification elaborates on the functionalities available in the Xmldata library.

This package considers JSON, XML, and Ballerina record data structure and creates the mapping for conversion by preserving their information and structure, and provides the following conversion between XML and JSON/Ballerina records.
- XML to JSON Conversion
- XML to Ballerina record Conversion
- JSON to XML Conversion

## 2. Data Structure

### 2.1 JSON

JSON is a textual format for representing a single or collection of following values: 
 - a simple value (string, number, boolean, null), 
 - an array of values
 - an object


### 2.2 XML

An XML value is a sequence representing the parsed content of an XML element. Values are sequences of zero or more items, where an item is one of the following:
 - element
 - text item consisting of characters
 - processing instruction
 - comment

### 2.3 Record

A record is just a collection of fields. Record equality works the same as map equality. 
A record type descriptor describes a type of mapping value by specifying a type separately for the value of each field.

The record can be defined as an open or a closed record according to the requirement. If a closed record is defined, 
the returned data should have those defined fields with defined types. Otherwise, this is an open record.
Hence, the returned data include both defined fields in the record and additional fields by conversion which are not defined in the record.

## 3. Rules

We have followed some set of rules for every conversion to preserve the information and structure of both input and output.

## 3.1 Rules for XML to JSON Conversion

The following rules are used during the conversion process:

- The namespaces will be omitted or added by configuring `preserveNamespaces`.
- Attributes and namespaces will be treated as regular JSON properties, and these keys have a prefix with a string to differentiate them from regular JSON properties.
- Sequences of two or more similar elements will be converted to a JSON array.
- Text nodes will be converted into a JSON property with the key as `#content`.
- PI and comments in the XML will be omitted.

The following table shows a mapping between the different forms of XML, to a corresponding matching JSON representation by considering the above rules.

<table>
    <thead>
        <tr>
            <th colspan="2">XML</th>
            <th colspan="2">JSON Representation of XML</th>
        </tr>
        <tr>
            <th>Type</th>
            <th>Sample</th>
            <th>Type</th>
            <th>Sample</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Empty element</td>
            <td>

```ballerina
<e/>
```
</td>
            <td>JSON Key-Value pair and value is ""</td>
            <td>

```ballerina
{"e":""}
```
</td>
        </tr>
        <tr>
            <td>Text Item</td>
            <td>

```ballerina
value
```

</td>
        <td>String</td>
        <td>

```ballerina
value
```
</td>
        </tr>
        <tr>
            <td>Comment</td>
            <td>

```ballerina
<!-- value -->
```
</td>
            <td>Empty JSON because it is not considered in this mapping</td>
            <td>

```ballerina
{}
```
</td>
        </tr>
        <tr>
            <td>PI</td>
            <td>

```ballerina
<?doc document="book.doc"?>
```
</td>
         <td>Empty JSON because it is not considered in this mapping</td>
         <td>

```ballerina
{}
```
</td>
        </tr>
        <tr>
            <td>Empty Sequence</td>
            <td>

```ballerina
``
```
</td>
        <td>Empty String</td>
        <td>

```ballerina
''
```
</td>
        </tr>
        <tr>
            <td>XML Sequence, with ‘element’s having distinct keys</td>
            <td>

```ballerina
<key><key1>value1</key1><key2>value2</key2></key>
```
</td>
        <td>JSON Object</td>
        <td>

```ballerina
{"key":{"key1":"value1","key2":"value2"}}
```
</td>
</tr>
<tr>
        <td>XML Sequence, with ‘element’s having identical key</td>
        <td>

```ballerina
<keys><key>value1</key><key>value2</key><key>value3</key><</keys>
```
</td>
        <td>JSON Object which contains JSON array</td>
        <td>

```ballerina
{"keys":{"key":["value1","value2","value3"]}}
```
</td>
</tr>
<tr>
<td>XML Sequence, containing items of type Element and Text</td>
<td>

```ballerina
<key>value1 Value2 <key1>value3</key1><key2>value4</key2></key>
```
</td>

<td>JSON Object </td>
<td>

```ballerina
{"key":{"#content":"value1 Value2","key1":"value3","key2":"value4"}}
```
</td>
</tr>
<tr>
<td>XML with attribute</td>
<td>

```ballerina
<foo key="value">5</foo>
```
</td>
<td> JSON Object. Here, attribute has ‘@’ prefix.
</td>
<td>

```ballerina
{"foo": {"@key": "value","#content": "5"}}
```
</td>
</tr>
<tr>
<td>XML with attribute and namespace</td>
<td>

```ballerina
<foo key="value" xmlns:ns0="http://sample.com/test">5</foo>
```
</td>
<td> JSON Object. Here, attribute and namespace have ‘@’ prefix.
</td>
<td>

```ballerina
{"foo":{"@key":"value","@xmlns:ns0":"http://sample.com/test","#content":"5"}}
```
</td>
</tr>
</tbody>
</table>

## 3.2 Rules for XML to Record Conversion

This conversion also follows all the rules which will be applied during the XML to the JSON conversion process except the attributes and namespaces rule. Here, attributes and namespaces key will be converted with a prefix as `_` in the record.

The table shows a mapping of XML with attribute and namespace to JSON.
<table>
    <thead>
        <tr>
            <th colspan="2">XML</th>
            <th colspan="2">JSON Representation of XML</th>
        </tr>
        <tr>
            <th>Type</th>
            <th>Sample</th>
            <th>Type</th>
            <th>Sample</th>
        </tr>
    </thead>
    <tbody>
<tr>
<td>XML with attribute</td>
<td>

```ballerina
<foo key="value">5</foo>
```
</td>
<td> JSON Object. Here, attribute has ‘@’ prefix.
</td>
<td>

```ballerina
{"foo": {"_key": "value","#content": "5"}}
```
</td>
</tr>
<tr>
<td>XML with attribute and namespace</td>
<td>

```ballerina
<foo key="value" xmlns:ns0="http://sample.com/test">5</foo>
```
</td>
<td> JSON Object. Here, attribute and namespace have ‘@’ prefix.
</td>
<td>

```ballerina
{"foo":{"_key":"value","_xmlns:ns0":"http://sample.com/test","#content":"5"}}
```
</td>
</tr>
</tbody>
</table>

## 3.3 Rules for JSON to XML Conversion

The following rules are used during the conversion process:

- A default root element will be created while the following scenarios:
    - When JSON is a JSON array
      ```ballerina
       json data = [
          {
             "@writer": "Christopher",
             lname: "Nolan",
             age: 30,
             address: ["Uduvil"]
          },
          1
       ];
      ```
    - When JSON data contains multiple key-value pairs
       ```ballerina
        json data = {
                       fname: "John",
                       lname: "Stallone"
              };
       ```
- JSON array entries will be converted to individual XML elements.
- For a JSON primitive value, convert the value as the text node of the XML element.
- If JSON properties' keys have the prefix and that value is the same with `attributePrefix` value which is defined in the `JsonOptions`, those will be handled as attributes and namespaces in the XML.

The following table shows a mapping between the different forms of XML, to a corresponding matching JSON representation by considering the above rules.

<table>
    <thead>
        <tr>
            <th colspan="2">JSON</th>
            <th colspan="2">XML Representation of JSON</th>
        </tr>
        <tr>
            <th>Type</th>
            <th>Sample</th>
            <th>Type</th>
            <th>Sample</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>JSON object has single key-value and value is ""</td>
            <td>

```ballerina
{"e":""}
```
</td>
            <td>Empty element</td>
            <td>

```ballerina
<e/>
```
</td>
        </tr>
        <tr>
            <td>Empty JSON</td>
            <td>

```ballerina
{}
```
</td>
        <td>Empty XML</td>
        <td>

```ballerina
``
```
</td>
        </tr>
        <tr>
            <td>JSON Object with distinct keys</td>
            <td>

```ballerina
{"key":{"key1":"value1","key2":"value2"}}
```
</td>
        <td>XML Sequence</td>
        <td>

```ballerina
<key><key1>value1</key1><key2>value2</key2></key>
```
</td>
</tr>
<tr>
        <td>JSON Array</td>
        <td>

```ballerina
[
    {
        "key": "value1",
    },
    value2
]
```
</td>
        <td>XML Sequence with `root` tag</td>
        <td>

```ballerina
<root>
    <key>value1</key>
    value2
</root>
```
</td>
</tr>
<tr>
<td> JSON Object with key as "#content"</td>
<td>

```ballerina
{"#content":"value1"}
```
</td>

<td>String</td>
<td>

```ballerina
value1
```
</td>
</tr>
<tr>
<td>JSON Object with key prefix as ‘@’</td>
<td>

```ballerina
{"foo": {"@key": "value", @xmlns:ns0="http://sample.com/test"}}
```
</td>
<td> XML element with attribute and namespace
</td>
<td>

```ballerina
<foo key="value" xmlns:ns0="http://sample.com/test"></foo>
```
</td>
</tr>
</tbody>
</table>

## 4. Operations

### 4.1 XML to JSON Conversion

XML to JSON conversion is a mapping between the different forms of XML to a corresponding matching JSON representation.
The following API returns the JSON data to the given XML structure by configuring the `XmlOptions`.
```ballerina
public isolated function toJson(xml xmlValue, XmlOptions options = {}) returns json|Error
```

The `XmlOptions` is used to configure the attribute and namespace prefix and add or eliminate the namespace in the JSON data.
The default value of the configuration is:
- Attribute and namespace prefix is `@`
- Preserving the namespaces is `true`

#### 4.1.1 Sample

```ballerina
xml input = xml `<ns0:bookStore status="online" xmlns:ns0="http://sample.com/test">
                    <ns0:storeName>foo</ns0:storeName>
                    <ns0:postalCode>94</ns0:postalCode>
                    <ns0:isOpen>true</ns0:isOpen>
                    <ns0:address>
                        <ns0:street>foo</ns0:street>
                        <ns0:city>94</ns0:city>
                        <ns0:country>true</ns0:country>
                    </ns0:address>
                    <ns0:codes>
                        <ns0:code>4</ns0:code>
                        <ns0:code>8</ns0:code>
                        <ns0:code>9</ns0:code>
                    </ns0:codes>
                </ns0:bookStore>
                <!-- some comment -->
                <?doc document="book.doc"?>`;
```

The JSON representation of the above XML with the default configuration of the above API.

```ballerina
{
    "ns0:bookStore": {
        "ns0:storeName": "foo",
        "ns0:postalCode": "94",
        "ns0:isOpen": "true",
        "ns0:address": {
            "ns0:street": "No 20, Palm Grove",
            "ns0:city": "Colombo 03",
            "ns0:country": "Sri Lanka"
        },
        "ns0:codes": {
            "ns0:code":["4","8","9"]
        },
        "@xmlns:ns0":"http://sample.com/test",
        "@status":"online"
    }
}
```

When `attributePrefix` is `&` and `preserveNamespaces` is `false`, the JSON representation of the above XML
```ballerina
{
    "bookStore":{
        "storeName":"foo",
        "postalCode":"94",
        "isOpen":"true",
        "address":{
            "street":"foo",
            "city":"94",
            "country":"true"
        },
        "codes":{
            "code":["4","8","9"]
        }
    }
}
```
### 4.2 XML to Record Conversion
This conversion is a mapping between the different forms of XML to a corresponding matching Ballerina record representation.
The following API returns the record to the given XML structure by configuring the `preserveNamespaces` and `returnType`.
```ballerina
public isolated function toRecord(xml xmlValue, boolean preserveNamespaces = true, typedesc<record {}> returnType = <>) returns returnType|Error
```
#### 4.2.1 Sample

```ballerina
xml input = xml `<ns0:bookStore status="online" xmlns:ns0="http://sample.com/test">
                    <ns0:storeName>foo</ns0:storeName>
                    <ns0:postalCode>94</ns0:postalCode>
                    <ns0:isOpen>true</ns0:isOpen>
                    <ns0:address>
                        <ns0:street>foo</ns0:street>
                        <ns0:city>94</ns0:city>
                        <ns0:country>true</ns0:country>
                    </ns0:address>
                    <ns0:codes>
                        <ns0:code>4</ns0:code>
                        <ns0:code>8</ns0:code>
                        <ns0:code>9</ns0:code>
                    </ns0:codes>
                </ns0:bookStore>
                <!-- some comment -->
                <?doc document="book.doc"?>`;
```

The record representation of the above XML with the default configuration of this API.

```ballerina
{
    "ns0:bookStore": {
        "ns0:storeName": "foo",
        "ns0:postalCode": "94",
        "ns0:isOpen": "true",
        "ns0:address": {
            "ns0:street": "No 20, Palm Grove",
            "ns0:city": "Colombo 03",
            "ns0:country": "Sri Lanka"
        },
        "ns0:codes": {
            "ns0:code":["4","8","9"]
        },
        "_xmlns:ns0":"http://sample.com/test",
        "_status":"online"
    }
}
```

When `preserveNamespaces` is `false`, the JSON representation of the above XML.

```ballerina
{
    "bookStore":{
        "storeName":"foo",
        "postalCode":"94",
        "isOpen":"true",
        "address":{
            "street":"foo",
            "city":"94",
            "country":"true"
        },
        "codes":{
            "code":["4","8","9"]
        }
    }
}
```

### 4.3 JSON to XML Conversion

This conversion provides a mapping between the different forms of JSON, to a corresponding matching XML representation.
The following API returns the JSON data to the given XML structure by configuring the `JsonOptions`.
```ballerina
public isolated function fromJson(json jsonValue, JsonOptions options = {}) returns xml?|Error
```

The `JsonOptions` is used to configure the attribute prefix for the JSON and array entry tag for XML. Array entry tag is used to create a tag when JSON array is in without keys.

### 4.3.1 Sample1

```ballerina
json input = {
    "ns0:bookStore": {
        "ns0:storeName": "foo",
        "ns0:postalCode": "94",
        "ns0:isOpen": "true",
        "ns0:address": {
            "ns0:street": "No 20, Palm Grove",
            "ns0:city": "Colombo 03",
            "ns0:country": "Sri Lanka"
        },
        "ns0:codes": {
            "ns0:code":["4","8","9"]
        },
        "@xmlns:ns0":"http://sample.com/test",
        "@status":"online",
    }
};
```
The XML representation of the above JSON with the default configuration of this API.

```ballerina
<ns0:bookStore xmlns:ns0="http://sample.com/test" status="online">
    <storeName>foo</storeName>
    <postalCode>94</postalCode>
    <isOpen>true</isOpen>
    <address>
        <street>No 20, Palm Grove</street>
        <city>Colombo 03</city>
        <country>Sri Lanka</country>
    </address>
    <codes>
        <code>4</code>
        <code>8</code>
        <code>9</code>
    </codes>
</ns0:bookStore>
```

### 4.3.2 Sample2

```ballerina
json input = {
    "books": [
        [
            {
                "&xmlns:ns0": "http://sample.com/test",
                "&writer": "Christopher",
                "bookName": "book1",
                "bookId": 101
            }
        ],
        [
            {
                "@writer": "John",
                "bookName": "book2",
                "bookId": 102
            }
        ]
    ]
};
```

When `attributePrefix` is `&` and `arrayEntryTag` is `list`, the XML representation of the above JSON.

```ballerina
<root>
    <books>
        <list xmlns:ns0="http://sample.com/test" writer="Christopher">
            <bookName>book1</bookName>
            <bookId>101</bookId>
        </list>
    </books>
    <books>
        <list writer="John">
            <bookName>book2</bookName>
            <bookId>102</bookId>
        </list>
    </books>
</root>
```