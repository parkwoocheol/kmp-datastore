# KMP DataStore

KMP DataStore is a Kotlin Multiplatform library that wraps Jetpack DataStore to provide a unified API for Android, iOS, and Desktop.

## Features

- **Multiplatform Support**: Android, iOS, and Desktop (JVM).
- **Type-Safe**: Supports primitive types and custom objects using `KType`-based serialization.
- **Unified Query DSL**: Filter and search data easily with `filterByValue<T>` and `queryValues<T>()`.
- **Built-in Kotlinx Serializer**: `kmp-datastore` includes `KotlinxDataStoreSerializer` out of the box.
- **Modular Add-ons**: annotations are packaged separately, KSP processor is optional.

## Documentation

- [Getting Started](getting-started.md)
- [API Reference](api/index.md)
- [Guides](guides/index.md)
