# Specification: Ballerina Cache Library

_Owners_: @daneshk @kalaiyarasiganeshalingam @MadhukaHarith92                                       
_Reviewers_: @daneshk  
_Created_: 2021/12/10  
_Updated_: 2021/12/10   
_Issue_: [#2334](https://github.com/ballerina-platform/ballerina-standard-library/issues/2334)

# Introduction
This is the specification for the Xmldata library which provides APIs to perform conversions between XML and JSON/Ballerina records. It is part of Ballerina Standard Library. [Ballerina programming language](https://ballerina.io/) is an open-source programming language for the cloud that makes it easier to use, combine, and create network services.

# Contents
1. [Overview](#1-overview)
2. [XML to JSON Conversion](#2-xml-to-json--conversion)
3. [XML to Record Conversion](#3-xml-to-record-conversion)
4. [JSON to XML Conversion](#4-json-to-xml--conversion)

## 1. Overview
This specification elaborates on the functionalities available in the Xmldata library.

This package considers JSON, XML, and Ballerina record data structure and creates the mapping for conversion by preserving their information and structure, and provides the following conversion between XML and JSON/Ballerina records.
- XML to JSON Conversion
- XML to Ballerina record Conversion
- JSON to XML Conversion

## 2. XML to JSON Conversion

XML to JSON conversion is a mapping between the different forms of XML to a corresponding matching JSON representation.
The following API returns the JSON data to the given XML structure by configuring the `XmlOptions`.
```ballerina
public isolated function toJson(xml xmlValue, XmlOptions options = {}) returns json|Error
```

The `XmlOptions` is used to configure the attribute and namespace prefix and add or eliminate the namespace in the JSON data.

The following rules are used during the conversion process:
- The namespaces will be omitted or added by configuring `preserveNamespaces`.
- Attributes and namespaces will be treated as regular JSON properties, and these keys have a prefix with a string to differentiate them from regular JSON properties.
- Sequences of two or more similar elements will be converted to a JSON array.
- Text nodes will be converted into a JSON property with the key as `#content`.
- PI and comments in the XML will be omitted.
  
A single structured XML element might come in seven patterns. The following table shows the corresponding conversion patterns between XML and JSON.

| XML Description | XML Sample | JSON Structure |
| :---: | :---: | :---: |
| Empty element | `<e/>` | {"e":""} |
| Element with pure text content | `<e>text</e>` | {"e":"text"} |
| Empty element with attributes | `<e name="value" />` | {"e":{"_name":"value"}} |
| Element with pure text content and attributes | `<e name="value">text</e>` | {"e":{"_name":"value","#content":"text"}} |
| Element containing elements with different names | `<e> <a>text</a> <b>text</b> </e>` | {"e":{"a":"text","b":"text"}}  |
| Element containing elements with identical names | `<e> <a>text</a> <a>text</a> </e>`| {"e":{"a":["text","text"]}} |
| Element containing elements and contiguous text | `<e> text <a>text</a> </e>` | {"e":{"#content":"text","a":"text"}} |

## 3. XML to Record Conversion
This conversion is a mapping between the different forms of XML to a corresponding matching Ballerina record representation.
The following API returns the Ballerina record to the given XML structure by configuring the `preserveNamespaces` and `returnType`.
```ballerina
public isolated function toRecord(xml xmlValue, boolean preserveNamespaces = true, typedesc<record {}> returnType = <>) returns returnType|Error
```

The record can be defined as an open or a closed record according to the requirement. If an open record is defined, the returned data will include both defined fields in the record and additional fields by conversion which are not defined in the record. If not given the record type, this will be a returned the record which contains all fields in that record.

This conversion also follows all the rules which will be applied during the XML to the JSON conversion process except the attributes and namespaces rule. Here, attributes and namespaces key will be converted with a prefix as `_` in the record.

The following table shows the corresponding conversion patterns between XML and JSON.

| XML Sample | JSON Structure |
| :---: | :---: |
| `<e/>` | {"e":""} |
| `<e>text</e>` | {"e":"text"} |
| `<e name="value" />` | {"e":{"@name":"value"}} |
| `<e name="value">text</e>` | {"e":{"@name":"value","#content":"text"}} |
| `<e> <a>text</a> <b>text</b> </e>` | {"e":{"a":"text","b":"text"}}  |
| `<e> <a>text</a> <a>text</a> </e>`| {"e":{"a":["text","text"]}} |
| `<e> text <a>text</a> </e>` | {"e":{"#content":"text","a":"text"}} |

## 4. JSON to XML Conversion

This conversion provides a mapping between the different forms of JSON, to a corresponding matching XML representation.

The following API returns the JSON data to the given XML structure by configuring the `JsonOptions`.
```ballerina
public isolated function fromJson(json jsonValue, JsonOptions options = {}) returns xml?|Error
```

The `JsonOptions` is used to configure the attribute prefix for the JSON and array entry tag for XML.

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
  - When JSON data contains a single key-value pair, and that key is as `#content`
     ```ballerina
        json data = {"#content":"text"};
     ``` 
   
- JSON array entries will be converted to individual XML elements.
- For a JSON primitive value, convert the value as the text content of the XML element.
- If JSON properties' keys have the prefix which is defined in the configuration, those will be handled as attributes and namespaces in the XML.

The vice versa conversion of the above table in XML to JSON Conversion shows the corresponding conversion patterns between JSON and XML. 
