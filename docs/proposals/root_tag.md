# Proposal: Introduce a new config in the `fromJson` API to add the root element tag name

_Owners_: @daneshk @kalaiyarasiganeshalingam  
_Reviewers_: @daneshk  
_Created_: 2022/05/27   
_Updated_: 2022/05/27  
_Issues_: [#2943](https://github.com/ballerina-platform/ballerina-standard-library/issues/2943) & [#2511](https://github.com/ballerina-platform/ballerina-standard-library/issues/2511)

## Summary
The existing API is creating a root tag with the name `root` for the following scenarios when converting to XML.

- JSON has multiple nodes

|JSON|XML|
|---|---|
|{<br>&emsp;genre: "Sci-Fi",<br>&emsp;language: "German"<br>}  | `<root>`<br>&emsp;`<genre>Sci-Fi</genre>`<br>&emsp;`<language>German</language>`<br>`</root>` |

- JSON has a single node and is in an array

|JSON|XML|
|---|---|
|{<br>&emsp;"codes": ["4", "8"]<br>} | `<root>`<br>&emsp;`<codes>4</codes>`<br>&emsp;`<codes>8</codes>`<br>`</root>` |

But, the User can define the root element tag, when converting to XML.

We planned to add the root element tag to all the JSON data by introducing configuration to get that name. So, this proposal introduces a new config to get the root element name.

## Goals
Provide a way to add the root element tag name.

## Motivation
As mentioned in the summary, the user doesn't have a way to add the root element name. It would be easy for them if we provided a config to configure it.

## Description
This module has a `JsonOption` record to configure all the configurations which are related to the conversion of JSON to XML.  The `JsonOption` with new config:
```ballerina
# Provides configurations for converting JSON to XML.
#
# + attributePrefix - The prefix of JSON elements' key which is to be treated as an attribute in the XML representation
# + arrayEntryTag - The name of the XML elements that represent a converted JSON array entry
# + rootTag- The name of the root element of the XML that will be created
public type JsonOptions record {
    string attributePrefix = "@";
    string arrayEntryTag = "item";
    string rootTag = "root";
};
```
