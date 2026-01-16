# Getting Started

## Build Requirements

- JDK 17+ (required by AGP 8.x)
- Android SDK for Android builds
- Xcode + Command Line Tools for iOS builds

## Installation

### JitPack (Recommended for Android/Desktop)

Add the repository and dependency:

```kotlin
// settings.gradle.kts
repositories {
    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

// build.gradle.kts (in commonMain sourceSet)
dependencies {
    implementation("com.github.parkwoocheol:kmp-datastore:0.1.0")

    // Optional: For @Serializable object support
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}
```

### GitHub Packages (Required for iOS)

```kotlin
// settings.gradle.kts
repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/parkwoocheol/kmp-datastore")
        credentials {
            username = findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

Add to `~/.gradle/gradle.properties`:
```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_PERSONAL_ACCESS_TOKEN
```

---

## Platform-Specific Setup

### Android

Initialize the context in your `Application` class:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KmpDataStoreContext.init(this)
    }
}
```

Don't forget to register it in `AndroidManifest.xml`:
```xml
<application
    android:name=".MyApplication"
    ...>
</application>
```

### iOS

No setup required! DataStore automatically uses NSUserDefaults internally.

### Desktop (JVM)

No setup required! DataStore uses file-based storage in `~/.config/{app}/datastore/`.

---

## Basic Usage

### 1. Create a DataStore

```kotlin
// For primitive types only (no serializer needed)
val dataStore = TypeSafeDataStore("user_preferences")

// For object storage (with optional serializer)
val serializer = KotlinxDataStoreSerializer()
val objectStore = TypeSafeDataStore("app_data", serializer)
```

### 2. Store and Retrieve Primitives

```kotlin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

// Store primitives
runBlocking {
    dataStore.putInt("age", 25)
    dataStore.putString("username", "john_doe")
    dataStore.putBoolean("is_premium", true)
    dataStore.putStringSet("tags", setOf("kotlin", "multiplatform"))
}

// Retrieve primitives
dataStore.getInt("age").collect { age ->
    println("Age: $age")  // Age: 25
}

dataStore.getString("username").collect { name ->
    println("Username: $name")  // Username: john_doe
}

dataStore.getBoolean("is_premium").collect { isPremium ->
    println("Premium: $isPremium")  // Premium: true
}
```

### 3. Store and Retrieve Objects

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val name: String,
    val age: Int,
    val email: String
)

// With serializer
val serializer = KotlinxDataStoreSerializer()
val dataStore = TypeSafeDataStore("app_data", serializer)

// Store object
val user = User("John Doe", 25, "john@example.com")
dataStore.put("current_user", user)

// Retrieve object
dataStore.get<User>("current_user").collect { user ->
    println("User: ${user?.name}, Age: ${user?.age}")
    // User: John Doe, Age: 25
}
```

### 4. Remove and Clear

```kotlin
// Remove a single key
dataStore.remove("username")

// Clear all data
dataStore.clear()

// Get all keys
dataStore.getAllKeys().collect { keys ->
    println("All keys: $keys")
}
```

---

## Query Operations

### Pattern Matching

```kotlin
// Find all keys starting with "user_"
dataStore.select("user_*").collect { keys ->
    println("User keys: $keys")
}

// Keys ending with "_id"
dataStore.keysEndingWith("_id").collect { idKeys ->
    println("ID keys: $idKeys")
}

// Keys containing "temp"
dataStore.keysContaining("temp").collect { tempKeys ->
    println("Temporary keys: $tempKeys")
}
```

### Value-Based Filtering

```kotlin
// Filter by string value
dataStore.filterByValue<String> { key, value ->
    value.contains("john", ignoreCase = true)
}.collect { matchingKeys ->
    println("Keys with 'john': $matchingKeys")
}

// Filter by int value
dataStore.filterByValue<Int> { key, value ->
    value > 18
}.collect { adultKeys ->
    println("Keys with value > 18: $adultKeys")
}

// Filter by boolean value
dataStore.filterByValue<Boolean> { key, value ->
    value == true
}.collect { trueKeys ->
    println("Keys with true value: $trueKeys")
}
```

### Key + Value Query Builder

```kotlin
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

Note: Key + value queries perform an in-memory scan of all keys and read each value.
Use key-only queries when performance matters.
For non-primitive types (including collections other than `Set<String>`), a serializer is required.

### Search and Grouping

```kotlin
// Search string values
dataStore.searchStringValues("search term").collect { results ->
    results.forEach { (key, value) ->
        println("Found: $key = $value")
    }
}

// Group keys by prefix (before delimiter)
dataStore.groupByKeyPrefix('_').collect { grouped ->
    grouped.forEach { (prefix, keys) ->
        println("$prefix: $keys")
    }
}

// Count total keys
dataStore.count().collect { count ->
    println("Total keys: $count")
}

// Check if key exists
dataStore.containsKey("username").collect { exists ->
    println("Username exists: $exists")
}
```

---

## Compose Integration

```kotlin
@Composable
fun UserProfile() {
    val age by dataStore.getInt("age")
        .collectAsState(initial = null)

    val username by dataStore.getString("username")
        .collectAsState(initial = null)

    Column {
        Text("Username: ${username ?: "Loading..."}")
        Text("Age: ${age ?: "Loading..."}")
    }
}
```

---

## Error Handling

```kotlin
try {
    dataStore.put("user", user)
} catch (e: SerializationException) {
    // Handle serialization error
    println("Failed to serialize: ${e.message}")
} catch (e: Exception) {
    // Handle other errors
    println("Error: ${e.message}")
}
```

---

## Next Steps

- [API Reference](api/index.md) - Complete API documentation
- [Guides](guides/index.md) - Advanced usage patterns
- [SPEC.md](../SPEC.md) - Full technical specification
