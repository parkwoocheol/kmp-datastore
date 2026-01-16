# Advanced Guides

In-depth guides for advanced usage patterns and best practices.

---

## Table of Contents

1. [Custom Serializers](#custom-serializers)
2. [Migration Strategies](#migration-strategies)
3. [Query DSL Patterns](#query-dsl-patterns)
4. [Performance Optimization](#performance-optimization)
5. [Testing Strategies](#testing-strategies)
6. [Error Handling](#error-handling)
7. [Platform-Specific Features](#platform-specific-features)
8. [State Management Integration](#state-management-integration)

---

## Custom Serializers

### Implementing a Custom Serializer

KMP DataStore uses the BridgeSerializer pattern, allowing you to plug in any serialization library.

#### Moshi Example

```kotlin
class MoshiDataStoreSerializer(private val moshi: Moshi) : DataStoreSerializer {
    override fun encode(data: Any?, type: KType): String {
        if (data == null) return "null"
        val adapter = moshi.adapter<Any>(type.javaType)
        return adapter.toJson(data)
    }

    override fun <T> decode(json: String, type: KType): T {
        if (json == "null") return null as T
        val adapter = moshi.adapter<T>(type.javaType)
        return adapter.fromJson(json)
            ?: throw SerializationException("Failed to decode: null result")
    }
}

// Usage
val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

val serializer = MoshiDataStoreSerializer(moshi)
val dataStore = TypeSafeDataStore("app_data", serializer)
```

#### Gson Example

```kotlin
class GsonDataStoreSerializer(private val gson: Gson = Gson()) : DataStoreSerializer {
    override fun encode(data: Any?, type: KType): String {
        return gson.toJson(data)
    }

    override fun <T> decode(json: String, type: KType): T {
        val javaType = type.javaType
        return gson.fromJson(json, javaType)
    }
}

// Usage
val gson = GsonBuilder()
    .setLenient()
    .create()

val serializer = GsonDataStoreSerializer(gson)
val dataStore = TypeSafeDataStore("app_data", serializer)
```

#### Custom Binary Serializer

```kotlin
class ProtobufDataStoreSerializer : DataStoreSerializer {
    override fun encode(data: Any?, type: KType): String {
        // Convert to protobuf bytes, then Base64 encode
        val bytes = (data as? GeneratedMessageV3)?.toByteArray()
            ?: throw SerializationException("Not a protobuf message")
        return Base64.getEncoder().encodeToString(bytes)
    }

    override fun <T> decode(json: String, type: KType): T {
        // Decode Base64, then parse protobuf
        val bytes = Base64.getDecoder().decode(json)
        // Use reflection or type-specific parsing
        // ...
    }
}
```

### Serializer Best Practices

1. **Handle null values explicitly**
2. **Provide clear error messages**
3. **Support custom configurations**
4. **Thread-safety is not required** (DataStore handles it)

---

## Migration Strategies

### From SharedPreferences (Android)

```kotlin
suspend fun migrateFromSharedPreferences(
    context: Context,
    prefsName: String = "app_prefs"
) {
    val sharedPrefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    val dataStore = TypeSafeDataStore("migrated_prefs")

    // Migrate all entries
    sharedPrefs.all.forEach { (key, value) ->
        when (value) {
            is String -> dataStore.putString(key, value)
            is Int -> dataStore.putInt(key, value)
            is Long -> dataStore.putLong(key, value)
            is Float -> dataStore.putFloat(key, value)
            is Boolean -> dataStore.putBoolean(key, value)
            is Set<*> -> {
                @Suppress("UNCHECKED_CAST")
                dataStore.putStringSet(key, value as Set<String>)
            }
        }
    }

    // Clear old preferences
    sharedPrefs.edit().clear().apply()
}

// Usage in Application.onCreate()
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KmpDataStoreContext.init(this)

        // Run migration in background
        lifecycleScope.launch {
            migrateFromSharedPreferences(this@MyApplication)
        }
    }
}
```

### From UserDefaults (iOS)

```kotlin
// iOS-specific migration
suspend fun migrateFromUserDefaults() {
    val defaults = NSUserDefaults.standardUserDefaults
    val dataStore = TypeSafeDataStore("migrated_prefs")

    val allKeys = defaults.dictionaryRepresentation().keys
    allKeys.forEach { key ->
        val value = defaults.objectForKey(key as String)
        when (value) {
            is String -> dataStore.putString(key, value)
            is NSNumber -> {
                // NSNumber can be Int, Long, Float, Double, or Boolean
                if (CFNumberIsFloatType(value as CFNumberRef)) {
                    dataStore.putDouble(key, value.doubleValue)
                } else {
                    dataStore.putLong(key, value.longValue)
                }
            }
            // Handle other types...
        }
    }
}
```

### Version Migration

```kotlin
class DataStoreVersionManager(private val dataStore: TypeSafeDataStore) {
    companion object {
        private const val VERSION_KEY = "_datastore_version"
        private const val CURRENT_VERSION = 2
    }

    suspend fun migrate() {
        val currentVersion = dataStore.getInt(VERSION_KEY).first() ?: 0

        when (currentVersion) {
            0 -> migrateV0toV1()
            1 -> migrateV1toV2()
        }

        dataStore.putInt(VERSION_KEY, CURRENT_VERSION)
    }

    private suspend fun migrateV0toV1() {
        // Example: Rename keys
        dataStore.getString("old_username").first()?.let { value ->
            dataStore.putString("user_name", value)
            dataStore.remove("old_username")
        }
    }

    private suspend fun migrateV1toV2() {
        // Example: Change data format
        dataStore.getString("user_age").first()?.let { ageStr ->
            val age = ageStr.toIntOrNull() ?: 0
            dataStore.putInt("user_age", age)
        }
    }
}

// Usage
val versionManager = DataStoreVersionManager(dataStore)
versionManager.migrate()
```

---

## Query DSL Patterns

### Complex Filtering

```kotlin
// Find active premium users
suspend fun findActivePremiumUsers(dataStore: TypeSafeDataStore): List<String> {
    val premiumKeys = dataStore.filterByValue<Boolean> { key, value ->
        key.endsWith("_premium") && value == true
    }.first()

    val activeKeys = dataStore.filterByValue<Boolean> { key, value ->
        key.endsWith("_active") && value == true
    }.first()

    // Find intersection (users who are both premium AND active)
    return premiumKeys.intersect(activeKeys)
        .map { it.substringBefore("_premium") }
}
```

### Pagination Pattern

```kotlin
class DataStorePaginator(
    private val dataStore: TypeSafeDataStore,
    private val pageSize: Int = 20
) {
    private var currentPage = 0

    suspend fun nextPage(): List<Pair<String, String>> {
        val keys = dataStore.query()
            .sortByKeyAscending()
            .skip(currentPage * pageSize)
            .take(pageSize)
            .executeKeys()
            .first()

        currentPage++

        return keys.mapNotNull { key ->
            dataStore.getString(key).first()?.let { value ->
                key to value
            }
        }
    }

    fun reset() {
        currentPage = 0
    }
}

// Usage
val paginator = DataStorePaginator(dataStore, pageSize = 10)
val page1 = paginator.nextPage()
val page2 = paginator.nextPage()
```

### Search with Ranking

```kotlin
suspend fun searchWithRanking(
    dataStore: TypeSafeDataStore,
    searchTerm: String
): List<Pair<String, Int>> {
    val results = dataStore.searchStringValues(searchTerm).first()

    // Rank by number of occurrences
    return results.map { (key, value) ->
        val occurrences = value.split(searchTerm, ignoreCase = true).size - 1
        key to occurrences
    }.sortedByDescending { it.second }
}

// Usage
searchWithRanking(dataStore, "kotlin").forEach { (key, rank) ->
    println("$key: $rank occurrences")
}
```

### Multi-Condition Queries

```kotlin
// Find users aged 18-30 with premium status
suspend fun findYoungPremiumUsers(dataStore: TypeSafeDataStore): List<String> {
    // First, find all user keys
    val userKeys = dataStore.select("user_*").first()

    // Filter by age
    val ageFilteredKeys = userKeys.filter { key ->
        val ageKey = "${key}_age"
        val age = dataStore.getInt(ageKey).first()
        age != null && age in 18..30
    }

    // Further filter by premium status
    return ageFilteredKeys.filter { key ->
        val premiumKey = "${key}_premium"
        dataStore.getBoolean(premiumKey).first() == true
    }
}
```

---

## Performance Optimization

### Caching Patterns

#### StateFlow Caching

```kotlin
class UserPreferencesRepository(private val dataStore: TypeSafeDataStore) {
    // Cache frequently accessed data
    val username: StateFlow<String?> = dataStore.getString("username")
        .stateIn(
            scope = CoroutineScope(Dispatchers.Default),
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val theme: StateFlow<String?> = dataStore.getString("theme")
        .stateIn(
            scope = CoroutineScope(Dispatchers.Default),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "light"
        )
}
```

#### In-Memory Cache

```kotlin
class CachedDataStore(private val dataStore: TypeSafeDataStore) {
    private val cache = mutableMapOf<String, Any?>()
    private val mutex = Mutex()

    suspend fun getString(key: String): String? {
        mutex.withLock {
            if (cache.containsKey(key)) {
                return cache[key] as? String
            }
        }

        val value = dataStore.getString(key).first()
        mutex.withLock {
            cache[key] = value
        }
        return value
    }

    suspend fun putString(key: String, value: String) {
        dataStore.putString(key, value)
        mutex.withLock {
            cache[key] = value
        }
    }

    suspend fun invalidate() {
        mutex.withLock {
            cache.clear()
        }
    }
}
```

### Batch Operations

```kotlin
suspend fun batchUpdate(
    dataStore: TypeSafeDataStore,
    updates: Map<String, String>
) {
    // Execute all updates in parallel
    coroutineScope {
        updates.map { (key, value) ->
            async { dataStore.putString(key, value) }
        }.awaitAll()
    }
}

// Usage
batchUpdate(dataStore, mapOf(
    "user_name" to "John",
    "user_email" to "john@example.com",
    "user_age" to "25"
))
```

### Debouncing Writes

```kotlin
class DebouncedDataStore(private val dataStore: TypeSafeDataStore) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val pendingWrites = mutableMapOf<String, Job>()

    fun putStringDebounced(
        key: String,
        value: String,
        delayMs: Long = 500
    ) {
        pendingWrites[key]?.cancel()
        pendingWrites[key] = scope.launch {
            delay(delayMs)
            dataStore.putString(key, value)
            pendingWrites.remove(key)
        }
    }

    suspend fun flush() {
        pendingWrites.values.forEach { it.join() }
    }
}

// Usage for rapid user input
val debouncedStore = DebouncedDataStore(dataStore)
// User types rapidly
debouncedStore.putStringDebounced("search_query", "kotlin")
debouncedStore.putStringDebounced("search_query", "kotlin m")
debouncedStore.putStringDebounced("search_query", "kotlin multiplatform")
// Only the last value is written after 500ms
```

### Large Dataset Optimization

```kotlin
// For large datasets, consider indexing
class IndexedDataStore(private val dataStore: TypeSafeDataStore) {
    private val indexKey = "_index"

    suspend fun addToIndex(category: String, key: String) {
        val currentIndex = dataStore.getString(indexKey).first()
            ?.let { Json.decodeFromString<Map<String, List<String>>>(it) }
            ?: emptyMap()

        val updated = currentIndex.toMutableMap()
        updated[category] = (updated[category] ?: emptyList()) + key

        dataStore.putString(indexKey, Json.encodeToString(updated))
    }

    suspend fun getKeysInCategory(category: String): List<String> {
        val index = dataStore.getString(indexKey).first()
            ?.let { Json.decodeFromString<Map<String, List<String>>>(it) }
            ?: emptyMap()

        return index[category] ?: emptyList()
    }
}
```

---

## Testing Strategies

### Unit Testing

```kotlin
class DataStoreTest {
    private lateinit var dataStore: TypeSafeDataStore

    @BeforeTest
    fun setup() {
        // Use unique name for each test
        dataStore = TypeSafeDataStore("test_${UUID.randomUUID()}")
    }

    @AfterTest
    fun teardown() = runTest {
        dataStore.clear()
    }

    @Test
    fun testPutAndGet() = runTest {
        dataStore.putString("key", "value")
        val result = dataStore.getString("key").first()
        assertEquals("value", result)
    }

    @Test
    fun testRemove() = runTest {
        dataStore.putString("key", "value")
        dataStore.remove("key")
        val result = dataStore.getString("key").first()
        assertNull(result)
    }
}
```

### Mocking for Tests

```kotlin
class FakeDataStore : TypeSafeDataStore("fake") {
    private val storage = mutableMapOf<String, Any?>()

    override suspend fun putString(key: String, value: String) {
        storage[key] = value
    }

    override fun getString(key: String): Flow<String?> {
        return flowOf(storage[key] as? String)
    }

    // Implement other methods...
}

// Usage in tests
class ViewModelTest {
    @Test
    fun testViewModel() {
        val fakeStore = FakeDataStore()
        val viewModel = MyViewModel(fakeStore)
        // Test without real DataStore
    }
}
```

### Integration Testing

```kotlin
@RunWith(AndroidJUnit4::class)
class DataStoreIntegrationTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var dataStore: TypeSafeDataStore

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        KmpDataStoreContext.init(context)
        dataStore = TypeSafeDataStore("integration_test")
    }

    @Test
    fun testConcurrentWrites() = runBlocking {
        val jobs = (1..100).map { i ->
            launch {
                dataStore.putInt("key_$i", i)
            }
        }
        jobs.joinAll()

        val count = dataStore.count().first()
        assertEquals(100, count)
    }
}
```

---

## Error Handling

### Graceful Degradation

```kotlin
suspend fun safeGetString(
    dataStore: TypeSafeDataStore,
    key: String,
    default: String = ""
): String {
    return try {
        dataStore.getString(key).first() ?: default
    } catch (e: Exception) {
        Log.e("DataStore", "Failed to get $key: ${e.message}")
        default
    }
}
```

### Retry Logic

```kotlin
suspend fun <T> retryIO(
    times: Int = 3,
    delayMs: Long = 1000,
    block: suspend () -> T
): T {
    repeat(times - 1) { attempt ->
        try {
            return block()
        } catch (e: IOException) {
            Log.w("DataStore", "Attempt ${attempt + 1} failed: ${e.message}")
            delay(delayMs)
        }
    }
    return block() // Last attempt - let exception propagate
}

// Usage
retryIO {
    dataStore.putString("key", "value")
}
```

### Error Reporting

```kotlin
class DataStoreErrorHandler(
    private val dataStore: TypeSafeDataStore,
    private val errorReporter: ErrorReporter
) {
    suspend fun putStringSafely(key: String, value: String) {
        try {
            dataStore.putString(key, value)
        } catch (e: Exception) {
            errorReporter.report("DataStore write failed", e, mapOf(
                "key" to key,
                "operation" to "putString"
            ))
            throw e
        }
    }
}
```

---

## Platform-Specific Features

### Android: Encrypted DataStore

```kotlin
class EncryptedDataStore(
    name: String,
    context: Context,
    serializer: DataStoreSerializer? = null
) : TypeSafeDataStore(name, serializer) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        name,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Implement encryption wrapper for sensitive data
}
```

### iOS: iCloud Sync

```kotlin
// iOS-specific: Use NSUbiquitousKeyValueStore for iCloud sync
expect class CloudDataStore(name: String) : TypeSafeDataStore

// iOS actual implementation
actual class CloudDataStore(name: String) : TypeSafeDataStore(name) {
    private val cloudStore = NSUbiquitousKeyValueStore.defaultStore()

    init {
        // Listen for cloud updates
        NSNotificationCenter.defaultCenter.addObserver(
            observer = this,
            selector = NSSelectorFromString("cloudStoreDidChange:"),
            name = NSUbiquitousKeyValueStoreDidChangeExternallyNotification,
            `object` = cloudStore
        )
    }

    // Override methods to use cloudStore
}
```

### Desktop: File Watching

```kotlin
class WatchedDataStore(name: String) : TypeSafeDataStore(name) {
    private val watchService = FileSystems.getDefault().newWatchService()
    private val dataStoreDir = Paths.get(System.getProperty("user.home"), ".config", name)

    init {
        dataStoreDir.register(
            watchService,
            StandardWatchEventKinds.ENTRY_MODIFY
        )

        // Monitor for external changes
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val key = watchService.take()
                key.pollEvents().forEach { event ->
                    // Handle external file changes
                    println("DataStore file changed externally")
                }
                key.reset()
            }
        }
    }
}
```

---

## State Management Integration

### ViewModel Integration

```kotlin
class UserViewModel(private val dataStore: TypeSafeDataStore) : ViewModel() {
    val username: StateFlow<String?> = dataStore.getString("username")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val isPremium: StateFlow<Boolean> = dataStore.getBoolean("is_premium")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun updateUsername(newName: String) {
        viewModelScope.launch {
            dataStore.putString("username", newName)
        }
    }
}
```

### MVI Pattern

```kotlin
sealed class DataStoreIntent {
    data class SaveUsername(val name: String) : DataStoreIntent()
    data class LoadUser(val userId: String) : DataStoreIntent()
}

sealed class DataStoreState {
    object Loading : DataStoreState()
    data class Success(val data: Any) : DataStoreState()
    data class Error(val message: String) : DataStoreState()
}

class DataStoreStore(private val dataStore: TypeSafeDataStore) {
    private val _state = MutableStateFlow<DataStoreState>(DataStoreState.Loading)
    val state: StateFlow<DataStoreState> = _state.asStateFlow()

    fun processIntent(intent: DataStoreIntent) {
        when (intent) {
            is DataStoreIntent.SaveUsername -> saveUsername(intent.name)
            is DataStoreIntent.LoadUser -> loadUser(intent.userId)
        }
    }

    private fun saveUsername(name: String) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                dataStore.putString("username", name)
                _state.value = DataStoreState.Success(name)
            } catch (e: Exception) {
                _state.value = DataStoreState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun loadUser(userId: String) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val user = dataStore.get<User>("user_$userId").first()
                _state.value = DataStoreState.Success(user ?: User.Empty)
            } catch (e: Exception) {
                _state.value = DataStoreState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
```

### Repository Pattern

```kotlin
interface UserRepository {
    suspend fun saveUser(user: User)
    fun getUser(id: String): Flow<User?>
    fun getAllUsers(): Flow<List<User>>
}

class DataStoreUserRepository(
    private val dataStore: TypeSafeDataStore
) : UserRepository {
    override suspend fun saveUser(user: User) {
        dataStore.put("user_${user.id}", user)

        // Update index
        val allUserIds = dataStore.getStringSet("_all_user_ids").first() ?: emptySet()
        dataStore.putStringSet("_all_user_ids", allUserIds + user.id)
    }

    override fun getUser(id: String): Flow<User?> {
        return dataStore.get<User>("user_$id")
    }

    override fun getAllUsers(): Flow<List<User>> {
        return dataStore.getStringSet("_all_user_ids").map { ids ->
            ids?.mapNotNull { id ->
                dataStore.get<User>("user_$id").first()
            } ?: emptyList()
        }
    }
}
```

---

## Best Practices Summary

1. **Use Primitives When Possible** - Faster and no serializer needed
2. **Cache Frequently Accessed Data** - Use StateFlow for hot streams
3. **Batch Operations** - Reduce DataStore I/O overhead
4. **Handle Errors Gracefully** - Provide defaults and retry logic
5. **Test Thoroughly** - Use unique DataStore names per test
6. **Version Your Schema** - Plan for data format changes
7. **Index for Performance** - For large datasets, maintain indexes
8. **Platform-Specific Optimizations** - Use native features when appropriate

---

## See Also

- [API Reference](../api/index.md) - Complete API documentation
- [Getting Started](../getting-started.md) - Basic usage
- [SPEC.md](../../SPEC.md) - Technical specification
