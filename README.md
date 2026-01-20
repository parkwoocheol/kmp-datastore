# KMP DataStore

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blueviolet?style=flat-square&logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.parkwoocheol/kmp-datastore.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/io.github.parkwoocheol/kmp-datastore)

**KMP DataStore** is a Kotlin Multiplatform library that wraps Jetpack DataStore to provide a unified, type-safe API for storage across Android, iOS, and Desktop.

## ✨ Features

- **Multiplatform Support**: seamless support for Android, iOS, and Desktop (JVM).
- **Type-Safe Storage**: easy storage for primitives (`Int`, `String`, `Boolean`, etc.) and fully typesafe object storage.
- **KType-Based Serialization**: flexible serializer architecture (default `KotlinxDataStoreSerializer` provided).
- **Unified Query DSL**: simple, powerful queries with `filterByValue<T>` and `queryValues<T>()`.
- **Annotation System**: `@SafeSerializable`, `@DataStoreKey`, `@RequiresSerializer` and validation annotations.
- **KSP Code Generation**: Type-safe query builders and validators generated at compile time (optional).
- **Lightweight**: minimal dependencies, with `kotlinx.serialization` being optional.

## ✅ Build Requirements

- JDK 17+ (required by AGP 8.x)
- Android SDK for Android sample builds
- Xcode + Command Line Tools for iOS sample builds

## 📦 Installation

kmp-datastore is available on **Maven Central**. No authentication required!

### Basic Setup (Kotlin DSL)

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

// build.gradle.kts (for Kotlin Multiplatform)
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.parkwoocheol:kmp-datastore:1.0.0")

            // Optional: for Kotlinx Serialization support
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
    }
}

// build.gradle.kts (for Android/JVM)
dependencies {
    implementation("io.github.parkwoocheol:kmp-datastore:1.0.0")
}
```

### Version Catalog (libs.versions.toml)

For modern projects using Gradle version catalogs:

```toml
[versions]
kmpDatastore = "1.0.0"

[libraries]
kmp-datastore = { module = "io.github.parkwoocheol:kmp-datastore", version.ref = "kmpDatastore" }
kmp-datastore-annotations = { module = "io.github.parkwoocheol:kmp-datastore-annotations", version.ref = "kmpDatastore" }
kmp-datastore-ksp = { module = "io.github.parkwoocheol:kmp-datastore-ksp", version.ref = "kmpDatastore" }
```

Then use it in your `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.kmp.datastore)
}
```

### Annotations (Optional)

```kotlin
dependencies {
    implementation("io.github.parkwoocheol:kmp-datastore-annotations:1.0.0")
}
```

### KSP Code Generation (Optional)

```kotlin
plugins {
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
}

dependencies {
    ksp("io.github.parkwoocheol:kmp-datastore-ksp:1.0.0")
}
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

## 🏷️ Annotations

### Quick Wins Annotations

```kotlin
// Mark a class as safe for serialization with version tracking
@SafeSerializable(version = 2, description = "User profile data")
@Serializable
data class User(
    @DataStoreKey("user_name", required = true)
    val name: String,
    val age: Int
)
```

### KSP Code Generation

```kotlin
// Generate type-safe query builder
@DataStoreIndex(properties = ["age", "email"])
@Serializable
data class User(
    val name: String,
    @Min(0) @Max(150) val age: Int,
    @Pattern("[a-z]+@[a-z]+\\.[a-z]+") val email: String
)

// Generated usage:
val adults = dataStore.queryUser()
    .whereAgeBetween(18, 65)
    .first()

// Generated validator:
val result = UserValidator.validate(user)
if (result.isFailure) {
    println(result.getErrorMessages())
}
```

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

## 📚 Documentation

For more detailed information, check out the [Documentation](docs/index.md):

- [**Getting Started**](docs/getting-started.md): Detailed setup and basic usage.
- [**API Reference**](docs/api/index.md): In-depth API documentation.
- [**Guides**](docs/guides/index.md): Advanced usage, Query DSL, custom serializers.
- [**Annotations Guide**](docs/annotations.md): Annotation system usage.
- [**KSP Guide**](docs/ksp-guide.md): KSP code generation setup and usage.

## 🤝 Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
