# KMP DataStore

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blueviolet?style=flat-square&logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Release](https://jitpack.io/v/parkwoocheol/kmp-datastore.svg)](https://jitpack.io/#parkwoocheol/kmp-datastore)

**KMP DataStore** is a Kotlin Multiplatform library that wraps Jetpack DataStore to provide a unified, type-safe API for storage across Android, iOS, and Desktop.

## ✨ Features

- **Multiplatform Support**: seamless support for Android, iOS, and Desktop (JVM).
- **Type-Safe Storage**: easy storage for primitives (`Int`, `String`, `Boolean`, etc.) and fully typesafe object storage.
- **KType-Based Serialization**: flexible serializer architecture (default `KotlinxDataStoreSerializer` provided).
- **Unified Query DSL**: simple, powerful queries with `filterByValue<T>` and `queryValues<T>()`.
- **Lightweight**: minimal dependencies, with `kotlinx.serialization` being optional.

## ✅ Build Requirements

- JDK 17+ (required by AGP 8.x)
- Android SDK for Android sample builds
- Xcode + Command Line Tools for iOS sample builds

## 🧪 Testing

The library includes a comprehensive test suite with 68+ unit tests covering:

- **Core API Tests**: Primitive and object storage operations
- **Serialization Tests**: Encoding/decoding with error handling
- **Query Tests**: Key-based and value-based query operations
- **Integration Tests**: End-to-end scenarios

Run tests:
```bash
./gradlew :kmp-datastore:test
```

## 📦 Installation

Add the repository and dependency to your `build.gradle.kts`:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
        mavenCentral()
    }
}

// build.gradle.kts (commonMain)
implementation("com.github.parkwoocheol:kmp-datastore:0.1.0")

// Optional: for Kotlinx Serialization support
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
```

## 🚀 Quick Start

### 1. Initialize

```kotlin
// Create a DataStore instance
// For primitive types only:
val dataStore = TypeSafeDataStore("user_prefs")

// For object support with Kotlinx Serialization:
val serializer = KotlinxDataStoreSerializer()
val objectDataStore = TypeSafeDataStore("app_data", serializer)
```

### 2. Store & Retrieve Primitives

```kotlin
// Write
dataStore.putString("user_name", "Alice")
dataStore.putInt("login_count", 42)

// Read
dataStore.getString("user_name").collect { name ->
    println("User: $name")
}
```

### 3. Store & Retrieve Objects

```kotlin
@Serializable
data class UserProfile(val name: String, val age: Int)

// Write
objectDataStore.put("profile", UserProfile("Alice", 30))

// Read
objectDataStore.get<UserProfile>("profile").collect { profile ->
    println("Profile: $profile")
}
```

## 📚 Documentation

For more detailed information, check out the [Documentation](docs/index.md):

- [**Getting Started**](docs/getting-started.md): Detailed setup and basic usage.
- [**API Reference**](docs/api/index.md): In-depth API documentation.
- [**Guides**](docs/guides/index.md): Advanced usage, Query DSL, custom serializers.

## 🔍 Query Examples

```kotlin
// Value-based filtering
dataStore.filterByValue<Int> { key, value ->
    value > 18
}.collect { keys ->
    println("Adult user keys: $keys")
}

// Key + value query builder
dataStore.queryValues<String>()
    .startsWith("user_")
    .valueContains("john")
    .sortByValueAscending()
    .take(10)
    .executeMap()
    .collect { results ->
        println("Top results: $results")
    }
```

Note: Key + value queries scan all keys and load each value. Prefer key-only queries when performance matters.

## 🤝 Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
