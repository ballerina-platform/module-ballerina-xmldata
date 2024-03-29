# Proposal: Introduce APIs to perform conversions between the Map/Ballerina record and XML

_Owners_: @daneshk @kalaiyarasiganeshalingam  
_Reviewers_: @daneshk  
_Created_: 2022/06/17   
_Updated_: 2022/06/17  
_Issues_: [#2819](https://github.com/ballerina-platform/ballerina-standard-library/issues/2819)

## Summary
The Ballerina Xmldata module doesn't have any API to convert the Ballerina Record/Map to XML and XML to map. 
So, this proposal introduces a new `fromXml` API to convert the XML data to Record/Map and `toXml` API to convert the Record/Map to XML.

## Goals
Provide a way to perform conversions between XML and Record/Map.

## Motivation
At the moment, users have to write their own custom implementation to perform conversions between Ballerina records/Map to XML.
Therefore, It would be easier for them if we provided APIs to convert.

When writing a Ballerina connector for a SOAP backend service like Netsuite connector, we need to convert the Ballerina 
record to XML and vice versa. In Ballerina, we don't have a standard way of converting Ballerina records to XML and 
followed our own custom logic in each connector. From this feature, we are trying to standardize 
the conversion and provide an easier way for conversions than reimplementing the logic.

## Description

### APIs definition:

```ballerina
# Converts an XML to its `Map` or `Record` representation.
# The namespaces and attributes will not be treated as special cases.
#
# + xmlValue - The XML source to be converted to a given target type
# + returnType - The `typedesc` of the `map<anydata>` that should be returned as a result
# + return - The given target type representation of the given XML on success,
#            else, returns an `xmldata:Error`
public isolated function fromXml(xml xmlValue, typedesc<(map<anydata>)> returnType = <>) returns returnType|Error;
```

```ballerina
# Converts a `Map` or `Record` representation to its XML representation.
# Attribute and Namespace annotations provided in the Ballerina record definition 
# get into account when converting to XML.
#
# + mapValue - The `Map` or `Record` representation source to be converted to XML
# + return - XML representation of the given source if the source is
#            successfully converted or else, an `xmldata:Error`
public isolated function toXml(map<anydata> mapValue) returns xml|Error;
```

### Record Annotation Definitions:

```ballerina
# Defines the XML element name which matches the record field. The default value is the record field name.
#
# + value - The XML element name
public type NameConfig record {|
    string value;
|};
```

```ballerina
# The Annotation is used to specify which XML element matches the record field.
public annotation NameConfig Name on type, record field;

# Defines the namespace of the XML element
#
# + prefix - The value of the prefix of the namespace
# + uri - The value of the URI of the namespace
public type NamespaceConfig record {|
    string prefix;
    string uri?;
|};

# The annotation is used to specify the namespace's prefix and URI of the XML element.
public annotation NamespaceConfig Namespace on type, record field;
```

```ballerina
# The annotation is used to denote the field that is considered an attribute.
public annotation Attribute on record field;
```
### Rules for performing conversions between Map and XML

We have followed some set of rules for every conversion to preserve the information and structure of both input and output based on OpenAPI specifications.

The following table shows mapping the XML to the different forms of map representation.

|XML Type  | XML Sample | Map Type | Output |
|---|---|---|---|
|XML Element | `<key>value</key>` | `map<BALLERINA_PRIMITIVE_TYPE>` | `{key: "VALUE_IN_DEFINED_TYPE"}` |
|XML Element | `<key>value</key>` | `map<BALLERINA_PRIMITIVE_TYPE_ARRAY>` | `{key: "VALUE_IN_DEFINED_ARRAY_TYPE"}` |
|XML Element | `<key>value</key>` | `map<xml>` | `{#content: <key>value</key>}` |
|XML Element | `<key>value</key>` | `map<json>` | `{key: "value"}` |
|XML Sequence | `<keys><key>value</key></keys>` | `map<BALLERINA_PRIMITIVE_TYPE>` | ERROR |
|XML Sequence | `<keys><key>value</key></keys>` | `map<BALLERINA_PRIMITIVE_TYPE_ARRAY>` | ERROR |
|XML Sequence | `<keys><key>value</key></keys>` | `map<json>` | `{keys: {key: "value"}` |
|XML Sequence | `<keys><key>value</key></keys>` | `map<xml>` | `{#content: <keys><key>value</key></keys>}` |
|XML Sequence | `<keys><key>value</key></keys>` | `map<table<map<string>>>` | `{keys: table [key: "value"]}` |

The following table shows mapping the map data to a corresponding matching XML representation.

|Map Type  | Map Sample | XML |
|---|---|---|
|`map<BALLERINA_PRIMITIVE_TYPE>` | `{key1: value1, key2: value2}` | `<root>`<br> &emsp;&emsp;`<key1>value1</key1>`<br> &emsp;&emsp;`<key2>value2</key2>`<br>`</root>` |
|`map<BALLERINA_PRIMITIVE_ARRAY_TYPE>`| `{key1: [v1,v2], key2: [v3,v4]}` | `<root>`<br> &emsp;&emsp;`<key1>value1</key1>`<br> &emsp;&emsp;`<key1>value2</key1>`<br> &emsp;&emsp;`<key2>value3</key2>`<br> &emsp;&emsp;`<key2>value4</key2>`<br>`</root>` |
| `map<json>`| `{keys: {key1: value1, key2: value2}}` | `<root>`<br> &emsp;&emsp;`<keys>`<br> &emsp;&emsp;&emsp;&emsp;`<key1>value1</key1>`<br> &emsp;&emsp;&emsp;&emsp;`<key2>value2</key2>`<br> &emsp;&emsp;`</keys>`<br>`</root>` |
|`map<xml>` | `{keys: xml <key>value</key>}` | `<root>`<br> &emsp;&emsp;`<keys>`<br> &emsp;&emsp;&emsp;&emsp;`<key>value</key>`<br> &emsp;&emsp;&emsp;&emsp;`</keys>`<br>`</root>` |
|`map<table<map<string>>>`|`{keys: table [{key: "value"}]}`|`<root>`<br> &emsp; &emsp;`<keys>`<br> &emsp; &emsp; &emsp; &emsp;`<key>value</key>`<br> &emsp; &emsp;`</keys>`<br>`</root>`|
|`map<json[]>`| `{keys: [{key1: value1},{key2: value2}]}` |`<root>`<br> &emsp;&emsp;`<keys>`<br> &emsp;&emsp;&emsp;&emsp;`<key1>value1</key1>`<br> &emsp;&emsp;&emsp;&emsp;`<key2>value2</key2>`<br> &emsp;&emsp;`</keys>`<br>`</root>`|
|`map<xml[]>` | `{keys: [xml <key1>value1</key1>, xml <key2>value2</key2>]}` | `<root>`<br> &emsp;&emsp;`<keys>`<br> &emsp;&emsp;&emsp;`<key1>value1</key1>`<br> &emsp;&emsp;&emsp;`<key2>value2</key2>`<br> &emsp;&emsp;`</keys>`<br>`</root>`|

### Rules for performing conversions between Ballerina record and XML

**Basic Conversion**

The following ballerina record definitions are consistent with the OpenAPI definition to map records to XML without any additional configurations.

|Ballerina Record Definition  | OpenAPI Definition | XML format |
|---|---|---|
|**Record with single field** <br><br> type Root record { <br> &emsp;string key?; <br>}| components:<br>&emsp;schemas:<br>&emsp;&emsp;Root:<br>&emsp;&emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;properties:<br>&emsp;&emsp;&emsp;&emsp;key:<br>&emsp;&emsp;&emsp;&emsp;&emsp;type: string| `<Root>`<br>&emsp;`<key>string</key>`<br>`</Root>`<br> |
|**Record with multiple key** <br><br>type Root record {  <br> &emsp;string key1?; <br> &emsp;string key2?; <br>}|components:<br>&emsp;schemas:<br>&emsp;&emsp;Root:<br>&emsp;&emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;properties:<br>&emsp;&emsp;&emsp;&emsp;key1:<br>&emsp;&emsp;&emsp;&emsp;&emsp;type: string <br>&emsp;&emsp;&emsp;&emsp;key2:<br>&emsp;&emsp;&emsp;&emsp;&emsp;type: string|`<Root>`<br>&emsp;`<key1>string</key1>`<br>&emsp;`<key2>string</key2>`<br>`</Root>`<br>|
|**Nested Record**<br><br>type Root record {  <br> &emsp;Store store?;  <br>}<br><br>type Store record { <br>&emsp;string name?;<br> &emsp;Address address?;<br>}<br><br>type Address record {  <br> &emsp;string street?;<br> &emsp;int city?;<br>}|components:<br>&emsp;schemas:<br>&emsp;&emsp;Root:<br>&emsp;&emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;properties:<br>&emsp;&emsp;&emsp;&emsp;store<br>&emsp;&emsp;&emsp;&emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;&emsp;&emsp;properties:<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;name<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;type: string<br>&emsp;&emsp;&emsp;&emsp;address:<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;properties<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;street:<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;type: string<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;city<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp; type: integer|`<Root>`<br>&emsp;`<store>`<br>&emsp;&emsp;`<name>string</name>`<br>&emsp;&emsp;`<address>`<br>&emsp;&emsp;&emsp;`<street>string</street>`<br>&emsp;&emsp;&emsp;`<city>0</city>`<br>&emsp;&emsp;`/address>`<br>&emsp;`</store>`<br>`</Root>`|
|**Array**<br><br>type Root record {<br> &emsp; string[] key?; <br>}|Root:<br>&emsp;type: object<br>&emsp;properties<br>&emsp;&emsp;key:<br>&emsp;&emsp;&emsp;type: array<br>&emsp;&emsp;items<br>&emsp;&emsp;&emsp;type: string|`<Root>`<br>&emsp;`<key>string</key>`<br>&emsp;`<key>string</key>`<br>`</Root>`<br>|
|**Record field type as XML** <br><br>type Root record {  <br>&emsp;xml key?; <br>}|components:<br>&emsp;schemas<br>&emsp;&emsp;Root:<br>&emsp;&emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;properties<br>&emsp;&emsp;&emsp;&emsp;key:<br>&emsp;&emsp;&emsp;&emsp;&emsp;type: object|`<Root>`<br>&emsp;&emsp;&emsp;`<key>`<br>&emsp;&emsp;&emsp;&emsp;`xml object`<br>&emsp;&emsp;&emsp;`</key>`<br>`</Root>`<br>|
|**Record field type as table** <br><br>table<map<string>> t = table [{key:"value"}]; <br><br> type Root record { <br>&emsp;table key?; <br>}|components:<br>&emsp;schemas:<br>&emsp;&emsp;Root:<br>&emsp;&emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;properties:<br>&emsp;&emsp;&emsp;key:<br>&emsp;&emsp;&emsp;&emsp;type: array<br>&emsp;&emsp;&emsp;items<br>&emsp;&emsp;&emsp;&emsp;type: object|`<Root>`<br>&emsp;`<key>xml object</key>`<br>&emsp;`<key>xml object</key>`<br>`</Root>`|
|**Required Field**<br><br>type Root record {  <br>&emsp;int id; <br>&emsp;string uname; <br>&emsp;string name?; <br>}|components:<br>&emsp;schemas:<br>&emsp;&emsp;root:<br>&emsp;&emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;properties:<br>&emsp;&emsp;&emsp;&emsp;id<br>&emsp;&emsp;&emsp;&emsp;&emsp;type: integer<br>&emsp;&emsp;&emsp;&emsp;uname:<br>&emsp;&emsp;&emsp;&emsp;&emsp;type: string<br>&emsp;&emsp;&emsp;&emsp;name:<br>&emsp;&emsp;&emsp;&emsp;&emsp;type: string<br>&emsp;&emsp;&emsp;&emsp;required:<br>&emsp;&emsp;&emsp;&emsp;&emsp;- id<br>&emsp;&emsp;&emsp;&emsp;&emsp;- uname|`<Root>`<br>&emsp;`<id>0</id>`<br>&emsp;`<uname>string</uname>`<br>&emsp;`<name>string</name>`<br>`</Root>`|
|**Close record**<br><br> type Person record {&#124;<br>&emsp;string name;<br>&#124;};|components:<br>&emsp;schemas<br>&emsp;&emsp; Person:<br>&emsp; &emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;&emsp;properties<br>&emsp;&emsp;&emsp; name:<br>&emsp;&emsp;&emsp;&emsp; type: string<br>&emsp;&emsp;&emsp;required<br>&emsp;&emsp;&emsp;&emsp; - name<br>&emsp;&emsp;&emsp;additionalProperties: false|`<Preson>`<br>&emsp;`<name>string</name>`<br>`</Person>`|
|**open record** <br><br>type Person record {<br>&emsp;string name;<br>};|components:<br>&emsp;schemas<br>&emsp;&emsp; Person:<br>&emsp; &emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;&emsp;properties<br>&emsp;&emsp;&emsp; name:<br>&emsp;&emsp;&emsp;&emsp; type: string<br>&emsp;&emsp;&emsp;required<br>&emsp;&emsp;&emsp;&emsp; - name<br>&emsp;&emsp;&emsp;additionalProperties: true|`<Preson>`<br>&emsp;`<name>string</name>`<br>&emsp;`<id>string</id>`<br>`</Person>`|
|**Union Type Field**<br><br>type Location record { <br>&emsp; string\|Address address?; <br>}<br><br>type Address record { <br>&emsp;int id;<br>&emsp;string uname;<br>&emsp;string name?; <br>}|components:<br>&emsp;schemas<br>&emsp;&emsp;Location:<br>&emsp;&emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;properties:<br>&emsp;&emsp;&emsp;&emsp;key<br>&emsp;&emsp;&emsp;&emsp;&emsp;oneOf<br>&emsp;&emsp;&emsp;&emsp;&emsp; - $ref: '#/components/schemas/Address'<br>&emsp;&emsp;&emsp;&emsp;&emsp;- type: string     <br>&emsp;&emsp;Address:<br>&emsp;&emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;properties:<br>&emsp;&emsp;&emsp;&emsp; id:<br>&emsp;&emsp;&emsp;&emsp;&emsp;type: integer<br>&emsp;&emsp;&emsp;&emsp;username:<br>&emsp;&emsp;&emsp;&emsp;&emsp;type: string<br>&emsp;&emsp;&emsp;&emsp;name:<br>&emsp;&emsp;&emsp;&emsp;&emsp; type: string<br>&emsp;&emsp;&emsp;&emsp;required:<br>&emsp;&emsp;&emsp;&emsp;&emsp; - id<br>&emsp;&emsp;&emsp;&emsp;&emsp;- uname|`<Location>`<br>&emsp;`<address>`<br>&emsp;&emsp;`<id>0</id>`<br>&emsp;&emsp;`<uname>string</uname>`<br>&emsp;&emsp;`<name>string</name>`<br>&emsp;`</address>`<br>`</Location>` <br><br>OR<br><br>`<Location>`<br>&emsp;`<address>string</address>`<br>`</Location>`|

**Conversion with Attributes and Namespaces**

The OpenAPI definition has metadata objects that allow for more fine-tuned XML model definitions. You can find those here.  https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.3.md#fixed-fields-22

So, In Ballerina, we are going to introduce some annotations to support these metadata.

|OpenAPI metadata | OpenAPI Definition | Ballerina Record Definition <br> with annotation | XML format|
|---|---|---|---|
|XML Name <br> Replacement|components:<br>&emsp; schemas:<br>&emsp;&emsp; animals:<br>&emsp;&emsp; &emsp;  type: object<br>&emsp;&emsp; &emsp; properties:<br>&emsp;&emsp; &emsp; &emsp;  id:<br>&emsp;&emsp; &emsp; &emsp; &emsp;  type: integer<br>&emsp; &emsp;&emsp; &emsp;  **xml:**<br>&emsp;&emsp;&emsp; &emsp;&emsp;  **name: ID**<br>&emsp; &emsp; &emsp; **xml:**<br>&emsp;&emsp;&emsp;  &emsp;**name: animal**|**@xmldata:name {**<br>&emsp;**value: animal**<br>**}** <br>type animals record {<br>&emsp;**@xmldata:name{**<br>&emsp;&emsp;**value: ID**<br>&emsp;**}**<br>&emsp; string id?;<br>};|`<animal>`<br>&emsp;`<ID>0</ID>`<br>`</animal>`|
|XML Attribute|components:<br>&emsp;schemas:<br>&emsp;&emsp;Pline:<br>&emsp;&emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;properties:<br>&emsp;&emsp;&emsp;&emsp;discount:<br>&emsp;&emsp;&emsp;&emsp;&emsp;type: string<br>&emsp;&emsp;&emsp;&emsp;&emsp;**xml:**<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;**attribute: true**|type Pline record {<br>&emsp;**@xmldata:attribute**<br>&emsp;int discount?;<br>};| `<Pline discount= "string">`<br>`</Pline>`|
|XML Namespace|components:<br>&emsp;schemas:<br>&emsp;&emsp;Root<br>&emsp;&emsp;&emsp;type: object<br>&emsp;&emsp;**xml:**<br>&emsp;&emsp;&emsp;**prefix: ns0**<br>&emsp;&emsp;&emsp;**namespace: 'http://www.w3.org/'**|**@xmldata:namespace {**<br>&emsp;**prefix:”nso”,**<br> &emsp;**uri = ”http://www.w3.org/”**<br>**}**<br>type Root record {};|`<ns0:Root xmlns:ns0 = "http://www.w3.org/">`<br>`</ns0:Root>`|
|XML Namespace <br> and Prefix|components:<br>&emsp;schemas:<br>&emsp;&emsp;Pline:<br>&emsp;&emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;**xml:**<br>&emsp;&emsp;&emsp;&emsp;**prefix: 'nso'**<br>&emsp;&emsp;&emsp;&emsp;**namespace: 'http://www.w3.org/'**<br>&emsp;&emsp;&emsp;properties<br>&emsp;&emsp;&emsp;&emsp;foo:<br>&emsp;&emsp;&emsp;&emsp;&emsp;type: string<br>&emsp;&emsp;&emsp;&emsp;&emsp;**xml:**<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;**prefix: 'nso'**|**@xmldata:namespace {**<br>&emsp;**prefix: “nso”,**        <br>&emsp;**uri = ”'http://www.w3.org/”**<br>**}** <br>type Pline record {<br>&emsp;**@xmldata:namespae{**<br>&emsp;&emsp;**prefix: “nso”**<br>&emsp;**}** <br>&emsp;string foo;<br>};|`<nso:Pline xmlns:ns0="http://www.w3.org/">`<br>&emsp;`<nso:foo></nso:foo>`<br>`</nso:Pline>`|
|XML Prefix with Namespaces <br><br>**Noted:** OpenAPI <br> Specification <br>[does not support](https://github.com/OAI/OpenAPI-Specification/issues/1456) <br> multiple XML <br> namespaces <br> within a single element. <br>As a workaround, <br>we can define additional <br>namespaces as <br>regular attributes <br>(that is, schema <br>properties with xml.attribute=true)|components:<br>&emsp;schemas:<br>&emsp;&emsp;Root<br>&emsp;&emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;properties<br>&emsp;&emsp;&emsp;&emsp;key:<br>&emsp;&emsp;&emsp;&emsp;&emsp;type: string<br>&emsp;&emsp;&emsp;&emsp;&emsp;**xmlns:asd**<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;enum<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;**- 'http://www.w3.org/'**<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;**xml**<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;**attribute: true**<br>&emsp;&emsp;&emsp;&emsp;&emsp;**xml:**<br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;**prefix: ns0** &emsp;&emsp;&emsp;&emsp;&emsp;&emsp;**namespace: 'http://www.w3.org/'**|**@xmldata:namespace {**<br>&emsp;**prefix:”nso”**, <br>&emsp;**uri = ”http://www.w3.org/”**<br>**}**<br>type Root record {<br>&emsp;string key?;<br>&emsp;**@xmldata:attribute**<br>&emsp;**string xmlns\:asd =  "http://www.w3.org/"**;<br>};|`<ns0:root xmlns:ns0="http://www.w3.org/" xmlns:asd="http://www.w3.org/">`<br>&emsp;`<key>string</key>`<br>`</ns0:root>`|
|Signifies whether <br>the array is wrapped or not.|One of the below open <br> API definitions can be used to <br>define the ballerina record array field definition. <br>So, we don’t need to introduce <br>new annotations for wrapped metadata.<br><br>1. Unwrap array definition<br>components:<br>&emsp;schemas:<br>&emsp;&emsp;root:<br>&emsp;&emsp;&emsp;type: object<br>&emsp;&emsp;&emsp;properties:<br>&emsp;&emsp;&emsp;&emsp;root<br>&emsp;&emsp;&emsp;&emsp;&emsp;type:array<br>&emsp; &emsp;&emsp;&emsp;&emsp;&emsp;items: <br>&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;type: string<br><br>2. Wrap array definition.<br>components:<br>&emsp;schemas:<br>&emsp;&emsp;root:<br>&emsp;&emsp;&emsp;type: array<br>&emsp; &emsp;&emsp;&emsp;items:<br>&emsp; &emsp;&emsp;&emsp;&emsp;type: string<br>&emsp;&emsp;&emsp;xml:<br>&emsp;&emsp;&emsp;wrapped: true|type root record {  <br>&emsp;string[] root?; <br>}|`<root>`<br>&emsp;`<root>string</root>`<br>`</root>`|

**Convert XML element with attributes(Unsupported in OpenAPI)**

OpenAPI does not support XML which has elements with attributes.
For more info, please see this issue: [https://github.com/OAI/OpenAPI-Specification/issues/630](https://github.com/OAI/OpenAPI-Specification/issues/630)

But this usecase is commonly used in XML. Therefore, In Ballerina, we support through the special field name `#content` like below.

|Ballerina Record Definition | XML Sample | 
|---|---|
|type PLine record {<br>&emsp; ItemCode itemCode?;<br>}<br><br>type ItemCode record { <br>&emsp; string discount?;<br>&emsp;&emsp;int \#content?;// If the value doesn't have a key, <br> can initialize that value with the default ey name`#content`<br>}|`<PLine>`<br>&emsp;`<itemCode discount=22%>`<br>&emsp;&emsp;`200777`<br>&emsp;`</itemCode>`<br>`</PLine>`|