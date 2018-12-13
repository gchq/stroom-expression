# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

* Issue **#stroom#989** : Improved the `link` function to only use 3 parameters.

## [v2.0.0] - 2018-11-20

* Added automatic module name for Java 9+ builds.

## [v1.4.13] - 2018-12-06

* Issue **#stroom#989** : Added `link` function.

* Issue **#stroom#991** : Concat now supports a single parameter.

## [v1.4.12] - 2018-11-20

* Issue **#22** : Added additional type checking functions `isBoolean()`, `isDouble()`, `isError()`, `isInteger()`, `isLong()`, `isNull()`, `isNumber()`, `isString()`, `isValue()`. Testing equality of null with `x=null()` is no longer valid and must be replaced with `isNull(x)`.

* Issue **#19** : Fix handling of `err()` in multi child generators

## [v1.4.3] - 2018-08-20

* Released to fix CHANGELOG.

## [v1.4.2] - 2018-08-20

* Issue **#24** : Fixed issue where comparison method was violating its general contract when sorting. Also added caching of some `toDouble` and `toString` conversions to improve sorting performance at the expense of greater memory usage.

## [v1.4.1] - 2018-05-22

* Reverted to Java 8.

## [v1.4.0] - 2018-05-18

* Issue **#22** : Add `typeOf(...)` function, e.g. `typeOf("abc")` => `string`.

* Issue **#18** : Fix handling of VarErr in `=`; now `(err()=err())` returns `err()`.

* Issue **#21** : Fix handling of division by zero, now returns ValErr.

* Issue **#18** : Fix handling of VarNull in `>=`, `<=`, `>` and `<`; now returns VarErr. Fix handling of VarNull in `=`; now `(null()=null())` returns `true()`.

* Issue **#20** : Fix parse errors when using `>=` and `<=`

## [v1.3.2] - 2018-05-10

* Updated slf4j-api to 1.7.25 to keep inline with dropwizard version 1.2.5.

## [v1.3.1] - 2018-05-02

* Simplified Key.

## [v1.3.0] - 2018-05-02

* Added Key type for child grouping.

## [v1.2.0] - 2018-04-30

* Changed `Var` to `Val`

* Issue **#16** : Added cast functions `toBoolean`, `toDouble`, `toInteger`, `toLong` and `toString`.

* Issue **#17** : Added `include` and `exclude` functions.

* Issue **#15** : Added `if` and `not` functions.

* Issue **#13** : Added value functions `true()`, `false()`, `null()` and `err()`.

* Issue **#14** : Added `match` boolean function.

* Issue **#12** : Added `variance` and `stDev` functions.

* Issue **#8** : Added `hash` function.

* Issue **#10** : Added `formatDate` function.

* Issue **#9** : Added `parseDate` function.

* Issue **#6** : Made `substring` and `decode` functions capable of accepting functional parameters.

* Issue **#5** : Added `substringBefore`, `substringAfter`, `indexOf` and `lastIndexOf` functions.

* Issue **#7** : Added `countUnique` function.

## [v1.1.0] - 2018-04-10

### Added

* Add FieldIndexMap.getMap()

## [v1.0.3] - 2018-01-26

* Issue **#2** : The expression function `extractSchemeSpecificPortFromUri` has now been corrected to be called `extractSchemeSpecificPartFromUri`.

* Issue **#2** : The missing expression function `extractQueryFromUri` has been added.

## [v1.0.2] - 2018-01-14

* Issue **#2** : Added expression functions to extract various URI components.

## [v1.0.1] - 2017-11-28

### Added

* Add bintray deployment to build

* Add generation of javadoc and sources to build

### Changed

* Fix invalid javadoc

## [v1.0.0] - 2017-08-11

### Changed

* Added v1 to package names

## [v0.1.0] - 2017-05-02

* Initial release

[Unreleased]: https://github.com/gchq/stroom-expression/compare/v1.4.14...HEAD
[v1.4.14]: https://github.com/gchq/stroom-expression/compare/v1.4.13...v1.4.14
[v1.4.13]: https://github.com/gchq/stroom-expression/compare/v1.4.12...v1.4.13
[v1.4.12]: https://github.com/gchq/stroom-expression/compare/v1.4.11...v1.4.12
[v1.4.11]: https://github.com/gchq/stroom-expression/compare/v1.4.10...v1.4.11
[v1.4.10]: https://github.com/gchq/stroom-expression/compare/v1.4.9...v1.4.10
[v1.4.9]: https://github.com/gchq/stroom-expression/compare/v1.4.8...v1.4.9
[v1.4.8]: https://github.com/gchq/stroom-expression/compare/v1.4.7...v1.4.8
[v1.4.7]: https://github.com/gchq/stroom-expression/compare/v1.4.6...v1.4.7
[v1.4.6]: https://github.com/gchq/stroom-expression/compare/v1.4.5...v1.4.6
[v1.4.5]: https://github.com/gchq/stroom-expression/compare/v1.4.4...v1.4.5
[v1.4.4]: https://github.com/gchq/stroom-expression/compare/v1.4.3...v1.4.4
[v1.4.3]: https://github.com/gchq/stroom-expression/compare/v1.4.2...v1.4.3
[v1.4.2]: https://github.com/gchq/stroom-expression/compare/v1.4.1...v1.4.2
[v1.4.1]: https://github.com/gchq/stroom-expression/compare/v1.4.0...v1.4.1
[v1.4.0]: https://github.com/gchq/stroom-expression/compare/v1.3.2...v1.4.0
[v1.3.2]: https://github.com/gchq/stroom-expression/compare/v1.3.1...v1.3.2
[v1.3.1]: https://github.com/gchq/stroom-expression/compare/v1.3.0...v1.3.1
[v1.3.0]: https://github.com/gchq/stroom-expression/compare/v1.2.0...v1.3.0
[v1.2.0]: https://github.com/gchq/stroom-expression/compare/v1.1.0...v1.2.0
[v1.1.0]: https://github.com/gchq/stroom-expression/compare/v1.0.3...v1.1.0
[v1.0.3]: https://github.com/gchq/stroom-expression/compare/v1.0.2...v1.0.3
[v1.0.2]: https://github.com/gchq/stroom-expression/compare/v1.0.1...v1.0.2
[v1.0.1]: https://github.com/gchq/stroom-expression/compare/v1.0.0...v1.0.1
[v1.0.0]: https://github.com/gchq/stroom-expression/compare/v0.1.0...v1.0.0
[v0.1.0]: https://github.com/gchq/stroom-expression/releases/tag/v0.1.0
