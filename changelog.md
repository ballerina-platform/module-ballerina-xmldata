# Change Log
This file contains all the notable changes done to the Ballerina XmlData package through the releases.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- [Introduce `toXml` and `fromXml` APIs](https://github.com/ballerina-platform/ballerina-standard-library/issues/2819)

### Changed
- Deprecate `toRecord` API
- Change `XmlOptions` and `JsonOptions` record to a closed record

## [2.2.2] - 2022-05-30

### Fixed
- [Fix the limitations of using the colon in the output of the `toRecord`](https://github.com/ballerina-platform/module-ballerina-xmldata/pull/418)
- [Fix the attribute prefix issue in the `fromJson` API](https://github.com/ballerina-platform/ballerina-standard-library/issues/2763)

## [2.1.0] - 2021-12-13

### Added
- [Add `toRecord` function which converts an XML to a Record](https://github.com/ballerina-platform/ballerina-standard-library/issues/2406)

## [1.1.0-alpha6] - 2021-04-02

### Changed
- [Improve the API for converts a JSON to an XML representation to return the `nil`](https://github.com/ballerina-platform/ballerina-standard-library/issues/1216)

### Added
- [Add more test cases](ttps://github.com/ballerina-platform/ballerina-standard-library/issues/1216)