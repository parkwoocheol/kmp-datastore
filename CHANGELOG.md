# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-01-16

This is the first production-ready release of KMP DataStore with full annotation support and KSP code generation.

### Added

#### Annotation System (`kmp-datastore-annotations`)

- `@SafeSerializable` - Schema versioning and documentation for serializable classes
- `@DataStoreKey` - Custom key naming and required field marking
- `@RequiresSerializer` - Opt-in annotation for serializer-dependent APIs
- `@DataStoreIndex` - KSP trigger for query builder generation
- `@DataStoreSchema` - KSP trigger for schema export
- Validation annotations: `@Min`, `@Max`, `@Pattern`, `@NotBlank`
- Runtime validation utilities: `ValidationResult`, `AnnotationValidator`

#### KSP Code Generation (`kmp-datastore-ksp`)

- `QueryBuilderGenerator` - Generates type-safe query builders from `@DataStoreIndex`
- `SchemaGenerator` - Exports schema JSON from `@DataStoreSchema`
- `ValidationGenerator` - Generates validators from validation annotations
- Full KSP 2.0 support (compatible with Kotlin 2.2.0)

#### Documentation Improvements

- [Annotations Guide](docs/annotations.md) - Complete annotation reference
- [KSP Guide](docs/ksp-guide.md) - KSP setup and usage guide
- Updated README with annotation examples

### Changed

- iOS `PreferencesDataStore` now creates document directory if it doesn't exist (`create = true`)
- `@RequiresSerializer` applied to `put<T>()` and `get<T>()` methods for better IDE warnings
- Sample app updated with annotation demos and validation examples

### Fixed

- iOS crash on app termination due to null document directory
- Test files using `assert()` instead of `assertTrue()` causing iOS compilation issues

---

## [0.1.0] - Initial Release

### Core Features

- Type-safe Kotlin Multiplatform DataStore wrapper
- Primitive type storage (Int, Long, Float, Double, Boolean, String, StringSet)
- Object storage with optional serialization (BridgeSerializer pattern)
- Powerful Query DSL (key-based and value-based queries)
- Pattern matching with wildcard support (`*`)
- Platform support: Android (minSdk 24), iOS, Desktop (JVM)

### Serialization

- Abstract `DataStoreSerializer` interface
- Optional `KotlinxDataStoreSerializer` implementation
- KType-based serialization (no string-based type names)
- Proper error handling with `SerializationException`

### Query Features

- Key filtering: `startsWith`, `endsWith`, `contains`, `matches`
- Value filtering: `filterByValue`, `valueEquals`, `valueBetween`, `valueContains`
- Pattern matching: `select("pattern*")`
- Sorting: `sortByKeyAscending`, `sortByValueAscending`
- Pagination: `take`, `skip`
- Multiple output formats: `executeKeys`, `executeValues`, `executeMap`

### Test Infrastructure

- Comprehensive test suite (68+ unit tests)
- Test fixtures: `TestPreferencesDataStore`, `MockDataStoreSerializer`
- Test coverage:
  - Core API operations (25 tests)
  - Serialization (15 tests)
  - Key-based queries (14 tests)
  - Value-based queries (14 tests)
- CI/CD integration with automated testing

### Documentation

- Complete API documentation (1,000+ lines)
- Getting Started guide with platform-specific setup
- Advanced usage guides:
  - Custom serializers (Moshi, Gson, Protobuf examples)
  - Migration strategies
  - Query DSL patterns
  - Performance optimization
  - Testing strategies
- Sample applications for all platforms (Android, iOS, Desktop)

### Platform Support

- **Android**: Uses androidx.datastore.preferences v1.2.0
  - Requires context initialization via `KmpDataStoreContext`
  - Stores at: `/data/data/{package}/files/datastore/`
- **iOS**: Uses androidx.datastore.preferences v1.2.0 (KMP)
  - No setup required
  - Stores at: Documents directory
- **Desktop (JVM)**: Uses androidx.datastore.preferences v1.2.0 (KMP)
  - File-based storage
  - Stores at: `~/.kmp-datastore/`

### Dependencies

- Kotlin 2.2.0
- Kotlinx Coroutines 1.10.1
- AndroidX DataStore 1.2.0
- Kotlinx Serialization 1.9.0 (optional)

[1.0.0]: https://github.com/parkwoocheol/kmp-datastore/compare/v0.1.0...v1.0.0
[0.1.0]: https://github.com/parkwoocheol/kmp-datastore/releases/tag/v0.1.0
