# API Reference

Complete API documentation for KMP DataStore.

---

## Core Classes

### TypeSafeDataStore

Main API class providing type-safe data storage across all platforms.

#### Constructor

```kotlin
class TypeSafeDataStore(
    name: String,
    serializer: DataStoreSerializer? = null
)
```

**Parameters:**
- `name: String` - The name of the DataStore instance. On Android, this becomes the filename.
- `serializer: DataStoreSerializer?` - Optional serializer for custom object storage. If not provided, only primitive operations are available.

**Example:**

```kotlin
// For primitive types only
val dataStore = TypeSafeDataStore("user_preferences")

// For object storage
val serializer = KotlinxDataStoreSerializer()
val objectStore = TypeSafeDataStore("app_data", serializer)
```

---

### Primitive Type Operations

Methods for storing and retrieving primitive types. **No serializer required.**

#### Int Operations

```kotlin
suspend fun putInt(key: String, value: Int)
fun getInt(key: String): Flow<Int?>
```

**Example:**

```kotlin
// Store
dataStore.putInt("age", 25)

// Retrieve
dataStore.getInt("age").collect { age ->
    println("Age: $age")
}
```

#### Long Operations

```kotlin
suspend fun putLong(key: String, value: Long)
fun getLong(key: String): Flow<Long?>
```

#### Float Operations

```kotlin
suspend fun putFloat(key: String, value: Float)
fun getFloat(key: String): Flow<Float?>
```

#### Double Operations

```kotlin
suspend fun putDouble(key: String, value: Double)
fun getDouble(key: String): Flow<Double?>
```

#### Boolean Operations

```kotlin
suspend fun putBoolean(key: String, value: Boolean)
fun getBoolean(key: String): Flow<Boolean?>
```

**Example:**

```kotlin
dataStore.putBoolean("is_premium", true)

dataStore.getBoolean("is_premium").collect { isPremium ->
    println("Premium: $isPremium")
}
```

#### String Operations

```kotlin
suspend fun putString(key: String, value: String)
fun getString(key: String): Flow<String?>
```

**Example:**

```kotlin
dataStore.putString("username", "john_doe")

dataStore.getString("username").collect { name ->
    println("Username: $name")
}
```

#### String Set Operations

```kotlin
suspend fun putStringSet(key: String, value: Set<String>)
fun getStringSet(key: String): Flow<Set<String>?>
```

**Example:**

```kotlin
dataStore.putStringSet("tags", setOf("kotlin", "multiplatform"))

dataStore.getStringSet("tags").collect { tags ->
    println("Tags: $tags")
}
```

---

### Object Operations

Methods for storing custom objects. **Requires a DataStoreSerializer.**

#### Generic Put

```kotlin
@OptIn(ExperimentalStdlibApi::class)
suspend inline fun <reified T : Any> put(key: String, value: T)
```

Stores an object using the configured serializer.

**Type Parameters:**
- `T: Any` - The type of object to store. Must be serializable by the configured serializer.

**Parameters:**
- `key: String` - The key to store the value under
- `value: T` - The value to store

**Throws:**
- `IllegalStateException` - If no serializer is configured
- `SerializationException` - If serialization fails

**Example:**

```kotlin
@Serializable
data class User(val name: String, val age: Int)

val serializer = KotlinxDataStoreSerializer()
val dataStore = TypeSafeDataStore("app_data", serializer)

val user = User("John Doe", 25)
dataStore.put("current_user", user)
```

#### Generic Get

```kotlin
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T : Any> get(key: String): Flow<T?>
```

Retrieves an object using the configured serializer.

**Type Parameters:**
- `T: Any` - The type of object to retrieve

**Parameters:**
- `key: String` - The key to retrieve the value for

**Returns:**
- `Flow<T?>` - Flow emitting the deserialized value or null if not found

**Throws:**
- `IllegalStateException` - If no serializer is configured
- `SerializationException` - If deserialization fails

**Example:**

```kotlin
dataStore.get<User>("current_user").collect { user ->
    println("User: ${user?.name}, Age: ${user?.age}")
}
```

#### Explicit Type Put/Get

For advanced use cases where you need to pass KType explicitly:

```kotlin
suspend fun put(key: String, value: Any?, type: KType)
fun <T> get(key: String, type: KType): Flow<T?>
```

---

### Common Operations

#### Remove

```kotlin
suspend fun remove(key: String)
```

Removes a value for the given key.

**Example:**

```kotlin
dataStore.remove("username")
```

#### Clear

```kotlin
suspend fun clear()
```

Removes all values from the DataStore.

**Example:**

```kotlin
dataStore.clear()
```

#### Get All Keys

```kotlin
fun getAllKeys(): Flow<Set<String>>
```

Retrieves all keys currently stored.

**Returns:**
- `Flow<Set<String>>` - Flow emitting the set of all keys

**Example:**

```kotlin
dataStore.getAllKeys().collect { keys ->
    println("All keys: $keys")
}
```

---

## Serialization

### DataStoreSerializer

Interface for abstracting serialization logic. Implement this to support different serialization libraries.

```kotlin
interface DataStoreSerializer {
    fun encode(data: Any?, type: KType): String
    fun <T> decode(json: String, type: KType): T
}
```

#### encode

```kotlin
fun encode(data: Any?, type: KType): String
```

Encodes the given data into a JSON string.

**Parameters:**
- `data: Any?` - The object to encode
- `type: KType` - The KType of the data

**Returns:**
- `String` - The JSON string representation

**Throws:**
- `SerializationException` - If encoding fails

#### decode

```kotlin
fun <T> decode(json: String, type: KType): T
```

Decodes the given JSON string into an object of type T.

**Parameters:**
- `json: String` - The JSON string to decode
- `type: KType` - The KType of the target object

**Returns:**
- `T` - The decoded object

**Throws:**
- `SerializationException` - If decoding fails

---

### KotlinxDataStoreSerializer

Built-in implementation using Kotlinx Serialization. **Optional dependency.**

```kotlin
class KotlinxDataStoreSerializer(
    private val json: Json = Json { ignoreUnknownKeys = true }
)
```

**Parameters:**
- `json: Json` - Optional Json configuration. Default ignores unknown keys.

**Example:**

```kotlin
// Default configuration
val serializer = KotlinxDataStoreSerializer()

// Custom configuration
val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    encodeDefaults = false
}
val customSerializer = KotlinxDataStoreSerializer(json)
```

**Requirements:**
- Add dependency: `org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3`
- Mark data classes with `@Serializable`

---

### Custom Serializer Example

```kotlin
class MoshiDataStoreSerializer(private val moshi: Moshi) : DataStoreSerializer {
    override fun encode(data: Any?, type: KType): String {
        val adapter = moshi.adapter<Any>(type.javaType)
        return adapter.toJson(data)
    }

    override fun <T> decode(json: String, type: KType): T {
        val adapter = moshi.adapter<T>(type.javaType)
        return adapter.fromJson(json)!!
    }
}

// Usage
val moshi = Moshi.Builder().build()
val serializer = MoshiDataStoreSerializer(moshi)
val dataStore = TypeSafeDataStore("app_data", serializer)
```

---

### SerializationException

Exception thrown when serialization or deserialization fails.

```kotlin
class SerializationException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
```

---

## Query Extensions

Powerful querying capabilities for finding and filtering data.

### Pattern Matching

#### select

```kotlin
fun TypeSafeDataStore.select(pattern: String): Flow<Set<String>>
```

Selects all keys matching the given pattern.

**Supported Patterns:**
- `"prefix*"` - Keys starting with "prefix"
- `"*suffix"` - Keys ending with "suffix"
- `"*infix*"` - Keys containing "infix"
- `"exact"` - Exact key match

**Returns:**
- `Flow<Set<String>>` - Flow emitting matching keys

**Example:**

```kotlin
// Find all user-related keys
dataStore.select("user_*").collect { keys ->
    println("User keys: $keys")
}

// Find all ID keys
dataStore.select("*_id").collect { idKeys ->
    println("ID keys: $idKeys")
}

// Find keys containing "temp"
dataStore.select("*temp*").collect { tempKeys ->
    println("Temporary keys: $tempKeys")
}
```

#### keysStartingWith

```kotlin
fun TypeSafeDataStore.keysStartingWith(prefix: String): Flow<Set<String>>
```

Gets all keys starting with the given prefix.

**Example:**

```kotlin
dataStore.keysStartingWith("user_").collect { keys ->
    println("Keys starting with 'user_': $keys")
}
```

#### keysEndingWith

```kotlin
fun TypeSafeDataStore.keysEndingWith(suffix: String): Flow<Set<String>>
```

Gets all keys ending with the given suffix.

**Example:**

```kotlin
dataStore.keysEndingWith("_id").collect { keys ->
    println("Keys ending with '_id': $keys")
}
```

#### keysContaining

```kotlin
fun TypeSafeDataStore.keysContaining(substring: String): Flow<Set<String>>
```

Gets all keys containing the given substring.

**Example:**

```kotlin
dataStore.keysContaining("temp").collect { keys ->
    println("Keys containing 'temp': $keys")
}
```

---

### Value-Based Filtering

#### filterByValue

```kotlin
suspend inline fun <reified T : Any> TypeSafeDataStore.filterByValue(
    noinline predicate: (key: String, value: T) -> Boolean
): Flow<Set<String>>
```

Filters keys by value content with automatic type inference.

**Supported Types:**
- Primitives: `String`, `Int`, `Long`, `Float`, `Double`, `Boolean`
- Custom objects (requires serializer)

**Parameters:**
- `predicate: (String, T) -> Boolean` - Function that returns true for keys to include

**Returns:**
- `Flow<Set<String>>` - Flow emitting keys whose values match the predicate

**Example with String:**

```kotlin
dataStore.filterByValue<String> { key, value ->
    value.contains("john", ignoreCase = true)
}.collect { keys ->
    println("Keys with 'john': $keys")
}
```

**Example with Int:**

```kotlin
dataStore.filterByValue<Int> { key, value ->
    value > 18
}.collect { keys ->
    println("Keys with value > 18: $keys")
}
```

**Example with custom object:**

```kotlin
@Serializable
data class User(val name: String, val age: Int)

val dataStore = TypeSafeDataStore("store", KotlinxDataStoreSerializer())

dataStore.filterByValue<User> { key, user ->
    user.age > 18
}.collect { keys ->
    println("Keys with adult users: $keys")
}
```

#### searchStringValues

```kotlin
suspend fun TypeSafeDataStore.searchStringValues(
    searchText: String
): Flow<Map<String, String>>
```

Searches for keys with String values matching a pattern (case-insensitive).

**Parameters:**
- `searchText: String` - The text to search for

**Returns:**
- `Flow<Map<String, String>>` - Flow emitting map of keys to matching values

**Example:**

```kotlin
dataStore.searchStringValues("john").collect { results ->
    results.forEach { (key, value) ->
        println("Found: $key = $value")
    }
}
```

---

### Grouping and Counting

#### groupByKeyPrefix

```kotlin
fun TypeSafeDataStore.groupByKeyPrefix(
    delimiter: Char = '_'
): Flow<Map<String, List<String>>>
```

Groups keys by prefix (text before delimiter).

**Parameters:**
- `delimiter: Char` - The delimiter character (default: '_')

**Returns:**
- `Flow<Map<String, List<String>>>` - Flow emitting grouped keys

**Example:**

```kotlin
// Keys: "user_name", "user_age", "settings_theme", "settings_lang"
dataStore.groupByKeyPrefix('_').collect { grouped ->
    // Result: {"user": ["user_name", "user_age"],
    //          "settings": ["settings_theme", "settings_lang"]}
    grouped.forEach { (prefix, keys) ->
        println("$prefix: $keys")
    }
}
```

#### groupByKey

```kotlin
fun TypeSafeDataStore.groupByKey(
    keySelector: (String) -> String
): Flow<Map<String, List<String>>>
```

Groups keys by a custom selector function.

**Example:**

```kotlin
// Group by first character
dataStore.groupByKey { key ->
    key.first().toString()
}.collect { grouped ->
    grouped.forEach { (group, keys) ->
        println("Group $group: $keys")
    }
}
```

#### count

```kotlin
fun TypeSafeDataStore.count(): Flow<Int>
fun TypeSafeDataStore.count(pattern: String): Flow<Int>
```

Returns the number of keys in the DataStore.

**Example:**

```kotlin
// Total count
dataStore.count().collect { total ->
    println("Total keys: $total")
}

// Count matching pattern
dataStore.count("user_*").collect { userCount ->
    println("User keys: $userCount")
}
```

#### containsKey

```kotlin
fun TypeSafeDataStore.containsKey(key: String): Flow<Boolean>
```

Checks if a key exists in the DataStore.

**Example:**

```kotlin
dataStore.containsKey("username").collect { exists ->
    if (exists) {
        println("Username exists")
    }
}
```

---

### Query Builder

Fluent API for building complex queries.

#### query

```kotlin
fun TypeSafeDataStore.query(): DataStoreQuery
```

Starts a query builder.

**Returns:**
- `DataStoreQuery` - Query builder instance

#### DataStoreQuery Methods

```kotlin
class DataStoreQuery {
    fun startsWith(prefix: String): DataStoreQuery
    fun endsWith(suffix: String): DataStoreQuery
    fun contains(substring: String): DataStoreQuery
    fun filter(predicate: (String) -> Boolean): DataStoreQuery
    fun take(count: Int): DataStoreQuery
    fun skip(count: Int): DataStoreQuery
    fun sortByKeyAscending(): DataStoreQuery
    fun sortByKeyDescending(): DataStoreQuery
    fun executeKeys(): Flow<List<String>>
}
```

**Example:**

```kotlin
// Complex query: Find first 10 user keys, sorted
dataStore.query()
    .startsWith("user_")
    .sortByKeyAscending()
    .take(10)
    .executeKeys()
    .collect { keys ->
        // Fetch values for each key
        keys.forEach { key ->
            dataStore.getString(key).first()?.let { value ->
                println("$key = $value")
            }
        }
    }
```

**Chaining Example:**

```kotlin
dataStore.query()
    .contains("active")
    .endsWith("_status")
    .skip(5)
    .take(10)
    .sortByKeyDescending()
    .executeKeys()
    .collect { keys ->
        println("Keys: $keys")
    }
```

---

### Value Query Builder

Fluent API for building key + value queries with filtering and sorting.

Note: Key + value queries scan all keys and read each value. Prefer key-only queries for better performance.

#### queryValues

```kotlin
inline fun <reified T : Any> TypeSafeDataStore.queryValues(): DataStoreValueQuery<T>
```

Starts a key + value query builder.

**Returns:**
- `DataStoreValueQuery<T>` - Query builder instance

#### selectValues

```kotlin
inline fun <reified T : Any> TypeSafeDataStore.selectValues(
    pattern: String
): Flow<Map<String, T>>
```

Selects all key/value pairs matching the given pattern.

#### DataStoreValueQuery Methods

```kotlin
class DataStoreValueQuery<T : Any> {
    fun startsWith(prefix: String): DataStoreValueQuery<T>
    fun endsWith(suffix: String): DataStoreValueQuery<T>
    fun contains(substring: String): DataStoreValueQuery<T>
    fun filter(predicate: (String) -> Boolean): DataStoreValueQuery<T>
    fun filterValue(predicate: (String, T) -> Boolean): DataStoreValueQuery<T>
    fun matches(regex: Regex): DataStoreValueQuery<T>
    fun take(count: Int): DataStoreValueQuery<T>
    fun skip(count: Int): DataStoreValueQuery<T>
    fun sortByKeyAscending(): DataStoreValueQuery<T>
    fun sortByKeyDescending(): DataStoreValueQuery<T>
    fun sortByValueAscending(comparator: Comparator<T>): DataStoreValueQuery<T>
    fun sortByValueDescending(comparator: Comparator<T>): DataStoreValueQuery<T>
    fun failOnError(): DataStoreValueQuery<T>
    fun execute(): Flow<List<QueryEntry<T>>>
    fun executeKeys(): Flow<List<String>>
    fun executeValues(): Flow<List<T>>
    fun executeMap(): Flow<Map<String, T>>
}
```

**Supported Types:**
- Primitives: `String`, `Int`, `Long`, `Float`, `Double`, `Boolean`
- `Set<String>`
- Collections and custom objects via configured serializer

#### Convenience Value Filters

```kotlin
fun <T : Any> DataStoreValueQuery<T>.valueEquals(value: T): DataStoreValueQuery<T>
fun <T : Any> DataStoreValueQuery<T>.valueIn(values: Collection<T>): DataStoreValueQuery<T>
fun <T : Comparable<T>> DataStoreValueQuery<T>.valueBetween(
    min: T? = null,
    max: T? = null
): DataStoreValueQuery<T>
fun DataStoreValueQuery<String>.valueContains(
    text: String,
    ignoreCase: Boolean = true
): DataStoreValueQuery<String>
fun DataStoreValueQuery<String>.valueMatches(regex: Regex): DataStoreValueQuery<String>
fun <T : Comparable<T>> DataStoreValueQuery<T>.sortByValueAscending(): DataStoreValueQuery<T>
fun <T : Comparable<T>> DataStoreValueQuery<T>.sortByValueDescending(): DataStoreValueQuery<T>
```

**Example:**

```kotlin
dataStore.queryValues<Int>()
    .startsWith("score_")
    .valueBetween(min = 50, max = 100)
    .sortByValueDescending()
    .take(5)
    .execute()
    .collect { entries ->
        entries.forEach { (key, value) ->
            println("$key = $value")
        }
    }
```

---

## Platform-Specific APIs

### Android: KmpDataStoreContext

Context initialization for Android platform.

```kotlin
object KmpDataStoreContext {
    fun init(context: Context)
}
```

**Must be called in Application.onCreate():**

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KmpDataStoreContext.init(this)
    }
}
```

**Don't forget to register in AndroidManifest.xml:**

```xml
<application
    android:name=".MyApplication"
    ...>
</application>
```

### iOS

No initialization required. Uses NSUserDefaults internally.

```kotlin
// iOS - Just create and use
val dataStore = TypeSafeDataStore("app_preferences")
```

### Desktop (JVM)

No initialization required. Uses file-based storage in `~/.config/{app}/datastore/`.

```kotlin
// Desktop - Just create and use
val dataStore = TypeSafeDataStore("app_preferences")
```

---

## Error Handling

### Common Exceptions

```kotlin
// SerializationException - when serialization fails
try {
    dataStore.put("user", user)
} catch (e: SerializationException) {
    println("Failed to serialize: ${e.message}")
}

// IllegalStateException - when serializer not configured
try {
    val dataStore = TypeSafeDataStore("store") // No serializer
    dataStore.put("user", user) // Throws!
} catch (e: IllegalStateException) {
    println("No serializer configured: ${e.message}")
}
```

### Best Practices

```kotlin
// 1. Always provide serializer for object storage
val dataStore = TypeSafeDataStore(
    "app_data",
    KotlinxDataStoreSerializer()
)

// 2. Handle Flow errors
dataStore.getString("key")
    .catch { e ->
        println("Error: ${e.message}")
        emit(null) // Emit default value
    }
    .collect { value ->
        println("Value: $value")
    }

// 3. Use try-catch for suspend functions
try {
    dataStore.putString("key", "value")
} catch (e: Exception) {
    println("Failed to save: ${e.message}")
}
```

---

## Flow Integration

### Jetpack Compose

```kotlin
@Composable
fun UserProfile() {
    val username by dataStore.getString("username")
        .collectAsState(initial = null)

    val age by dataStore.getInt("age")
        .collectAsState(initial = null)

    Column {
        Text("Username: ${username ?: "Loading..."}")
        Text("Age: ${age ?: "Loading..."}")
    }
}
```

### StateFlow Conversion

```kotlin
val usernameState: StateFlow<String?> = dataStore.getString("username")
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
```

### Combine Multiple Flows

```kotlin
combine(
    dataStore.getString("username"),
    dataStore.getInt("age"),
    dataStore.getBoolean("is_premium")
) { username, age, isPremium ->
    UserProfile(username, age, isPremium)
}.collect { profile ->
    println("Profile: $profile")
}
```

---

## Performance Considerations

### Read Operations

- **Cold Flow**: Data is read on collection, not on Flow creation
- **No Caching**: Each collection reads from storage
- **Reactive**: Automatically emits new values when data changes

```kotlin
// Flow is cold - doesn't read until collected
val flow = dataStore.getString("key")

// Reading happens here
flow.collect { value ->
    println(value)
}
```

### Write Operations

- **Asynchronous**: All write operations are suspend functions
- **Thread-Safe**: DataStore handles concurrency
- **Atomic**: Each write is atomic

```kotlin
// Safe to call from multiple coroutines
launch { dataStore.putString("key1", "value1") }
launch { dataStore.putString("key2", "value2") }
```

### Query Performance

- **In-Memory Filtering**: Queries filter all keys in memory
- **Best for Small Datasets**: Efficient for hundreds of keys
- **Consider Indexing**: For thousands of keys, consider dedicated database

```kotlin
// Efficient: Direct get
dataStore.getString("username").first()

// Less efficient: Query all keys
dataStore.select("user_*").first()
```

---

## Type Safety

### Compile-Time Type Checking

```kotlin
// ✅ Correct - type-safe
dataStore.putInt("age", 25)
dataStore.getInt("age").collect { age: Int? -> }

// ❌ Compile error - type mismatch
// dataStore.putInt("age", "25") // String instead of Int

// ✅ Correct - with reified generics
dataStore.put("user", User("John", 25))
dataStore.get<User>("user").collect { user: User? -> }

// ❌ Wrong type - runtime error
dataStore.get<String>("user") // User stored, String requested
```

### Runtime Type Checking

```kotlin
// filterByValue checks types at runtime
dataStore.filterByValue<Int> { key, value ->
    value > 18 // Only Int values pass through
}.collect { keys ->
    // These keys definitely have Int values
}
```

---

## See Also

- [Getting Started](../getting-started.md) - Basic usage examples
- [Guides](../guides/index.md) - Advanced patterns and best practices
- [SPEC.md](../../SPEC.md) - Technical specification
