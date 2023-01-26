Ballerina XmlData Library
===================

  [![Build](https://github.com/ballerina-platform/module-ballerina-xmldata/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerina-xmldata/actions/workflows/build-timestamped-master.yml)
  [![codecov](https://codecov.io/gh/ballerina-platform/module-ballerina-xmldata/branch/master/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/module-ballerina-xmldata)
  [![Trivy](https://github.com/ballerina-platform/module-ballerina-xmldata/actions/workflows/trivy-scan.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerina-xmldata/actions/workflows/trivy-scan.yml)   
  [![GraalVM Check](https://github.com/ballerina-platform/module-ballerina-xmldata/actions/workflows/build-with-bal-test-native.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerina-xmldata/actions/workflows/build-with-bal-test-native.yml)
  [![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/module-ballerina-xmldata.svg)](https://github.com/ballerina-platform/module-ballerina-xmldata/commits/master)
  [![Github issues](https://img.shields.io/github/issues/ballerina-platform/ballerina-standard-library/module/xmldata.svg?label=Open%20Issues)](https://github.com/ballerina-platform/ballerina-standard-library/labels/module%2Fxmldata)

This library provides APIs to perform conversions between XML and JSON/Ballerina records. It provides APIs to get natural representations of data in XML from JSON and to get JSON or Ballerina records data from natural representations of data in XML.

## Issues and projects 

Issues and Project are disabled for this repository as this is part of the Ballerina Standard Library. To report bugs, request new features, start new discussions, view project boards, etc. please visit Ballerina Standard Library [parent repository](https://github.com/ballerina-platform/ballerina-standard-library). 

This repository only contains the source code for the package.

## Build from the source

### Set up the prerequisites

1. Download and install Java SE Development Kit (JDK) version 11 (from one of the following locations).
   * [Oracle](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
   
   * [OpenJDK](https://adoptium.net/)
   
        > **Note:** Set the JAVA_HOME environment variable to the path name of the directory into which you installed JDK.
     
### Build the source

Execute the commands below to build from source.

1. To build the library:
        
        ./gradlew clean build
        
2. To run the tests:

        ./gradlew clean test
        
3. To build the package without tests:

        ./gradlew clean build -x test

4. To run a group of tests:

        ./gradlew clean test -Pgroups=<test_group_names>

5. To debug package implementation:

        ./gradlew clean build -Pdebug=<port>
        
6. To debug the package with Ballerina language:

        ./gradlew clean build -PbalJavaDebug=<port>
        
7. Publish ZIP artifact to the local `.m2` repository:

        ./gradlew clean build publishToMavenLocal

8. Publish the generated artifacts to the local Ballerina central repository:
   
        ./gradlew clean build -PpublishToLocalCentral=true
9. Publish the generated artifacts to the Ballerina central repository:

        ./gradlew clean build -PpublishToCentral=true

## Contribute to Ballerina

As an open source project, Ballerina welcomes contributions from the community. 

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of conduct

All contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful links

* Chat live with us via our [Discord server](https://discord.gg/ballerinalang).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
* For more information go to the [`xmldata` library](https://lib.ballerina.io/ballerina/xmldata/latest).
* For example demonstrations of the usage, go to [Ballerina By Examples](https://ballerina.io/swan-lake/learn/by-example/).
