# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

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

[Unreleased]: https://github.com/gchq/stroom-expression/compare/v1.3.1...HEAD
[v1.3.1]: https://github.com/gchq/stroom-expression/compare/v1.3.0...v1.3.1
[v1.3.0]: https://github.com/gchq/stroom-expression/compare/v1.2.0...v1.3.0
[v1.2.0]: https://github.com/gchq/stroom-expression/compare/v1.1.0...v1.2.0
[v1.1.0]: https://github.com/gchq/stroom-expression/compare/v1.0.3...v1.1.0
[v1.0.3]: https://github.com/gchq/stroom-expression/compare/v1.0.2...v1.0.3
[v1.0.2]: https://github.com/gchq/stroom-expression/compare/v1.0.1...v1.0.2
[v1.0.1]: https://github.com/gchq/stroom-expression/compare/v1.0.0...v1.0.1
[v1.0.0]: https://github.com/gchq/stroom-expression/compare/v0.1.0...v1.0.0
[v0.1.0]: https://github.com/gchq/stroom-expression/releases/tag/v0.1.0
