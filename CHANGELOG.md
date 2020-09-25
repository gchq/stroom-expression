# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

## Unreleased

* Added support for Kryo serialisation.


## [v3.0.4] - 2020-09-08

* Issue **#stroom#1083** : Added `top()` and `bottom()` selection functions to select child values of grouped items.

* Issue **#stroom#1083** : Added `any()`, `first()`, `last()` and `nth()` selection functions to select child values of grouped items.

* Issue **#stroom#1837** : Added limit to the number of items added when using `joining()`.

* Issue **#stroom#1837** : Added `joining()` function to concatenate supplied fields in child rows.

* Issue **#stroom#1837** : Added `joining()` function to concatenate supplied fields in child rows.

* Issue **#stroom#1784** : Several functions were previously prevented from working on results from aggregate functions but are now applied regardless.

## [v3.0.3] - 2019-12-06

* Issue **#stroom#1328** : Fix so `stepping()` function is correctly registered.

## [v3.0.2] - 2019-11-04

* Fix build.

## [v3.0.1] - 2019-11-04

* Issue **#stroom#1265** : Added `modulus()` function along with alias `mod()` and modulus operator `%`.

* Issue **#stroom#1300** : Added `annotation()` link creation function, `currentUser()` alias for `param('currentUser()')` and additional link creation functions for `data()` and `stepping()`.

* Issue **#stroom#1263** : Fixed issues related to URL encoding/decoding with the `dashboard()` function.

* Issue **#stroom#1263** : Fixed issues related to URL encoding/decoding.

* Issue **#stroom#1262** : Improved behaviour of `+` when using for concatenation.

## [v2.0.8] - 2019-07-29

* Issue **#stroom#1143** : Fix visibility of StaticValueFunction class.

* Issue **#stroom#1143** : Added mechanism to inject statically mapped values so that dashboard parameters can be echoed by expressions to create dashboard links.

## [v2.0.7] - 2019-06-25

* Issue **#26** : Added `dashboard` linking helper function.

* Issue **#28** : Added `encodeUrl` and `decodeUrl` functions.

* Issue **#29** : The `link` function now encodes title, url and type to ensure these values don't break the link format.

## [v2.0.6] - 2019-01-04

* Migrated to `java-library` plugin.

## [v2.0.5] - 2019-01-04

* Fix for Junit 5 testing.

## [v2.0.4] - 2019-01-03

* Issue **#27** : Strings can now be concatenated with the use of `+`.

* Issue **#stroom#989** : Improved the `link` function to only use 3 parameters.

## [v2.0.3] - 2018-11-22

* Downgrade to Java 10.

## [v2.0.2] - 2018-11-20

* Updated Gradle to 4.10.2.

## [v2.0.1] - 2018-11-20

* Updated to Java 11.

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

[Unreleased]: https://github.com/gchq/stroom-expression/compare/v3.0.4...HEAD
[v3.0.4]: https://github.com/gchq/stroom-expression/compare/v3.0.3...v3.0.4
[v3.0.3]: https://github.com/gchq/stroom-expression/compare/v3.0.2...v3.0.3
[v3.0.2]: https://github.com/gchq/stroom-expression/compare/v3.0.1...v3.0.2
[v3.0.1]: https://github.com/gchq/stroom-expression/compare/v3.0.0...v3.0.1
[v3.0.0]: https://github.com/gchq/stroom-expression/compare/v2.0.8...v3.0.0
[v2.0.8]: https://github.com/gchq/stroom-expression/compare/v2.0.7...v2.0.8
[v2.0.7]: https://github.com/gchq/stroom-expression/compare/v2.0.6...v2.0.7