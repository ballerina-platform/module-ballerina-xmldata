# Overview

The `world_bank` project demonstrates how the Ballerina `xmldata` module can be used to convert an `xml` value to `json` type and user-defined record types.
In this example [The world bank API](https://datahelpdesk.worldbank.org/knowledgebase/articles/889392-about-the-indicators-api-documentation) is used to retrieve an `xml` value. The returned `xml` value consists of population data in the United States from 1971 to 2021.  

## Run the example

First, clone this repository, and then run the following commands to run this example in your local machine.

    $ cd examples/world-bank
    $ bal run


## Output of the example

As can be seen in the console, first the `xml` value is converted to `json` type. Next, the `xml` value is converted to a `WorldBank` record type defined as below.

```ballerina
type WorldBank record {
    Data data;
};

type Data record {
    Population[] data;
};

type Population record {
    string countryiso3code;
    int date;
    int value;
    string unit;
    string obs_status;
    int 'decimal;
};
```