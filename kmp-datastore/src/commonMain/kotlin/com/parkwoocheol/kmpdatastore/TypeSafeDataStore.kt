package com.parkwoocheol.kmpdatastore

import com.parkwoocheol.kmpdatastore.annotations.RequiresSerializer
import com.parkwoocheol.kmpdatastore.platform.PreferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * Type-safe DataStore wrapper providing a convenient API for storing and retrieving data.
 *
 * This class provides two ways to store data:
 * 1. Direct methods for primitive types (no serializer needed)
 * 2. Generic methods for custom objects (requires DataStoreSerializer)
 *
 * Example with primitives:
 * ```
 * val dataStore = TypeSafeDataStore("app_preferences")
 *
 * // Store primitives directly
 * dataStore.putInt("age", 25)
 * dataStore.putString("username", "john")
 * dataStore.putBoolean("is_logged_in", true)
 *
 * // Retrieve primitives
 * dataStore.getInt("age").collect { age ->
 *     println("Age: $age")
 * }
 * ```
 *
 * Example with custom objects (requires serializer):
 * ```
 * @Serializable
 * data class User(val name: String, val age: Int)
 *
 * // Using KotlinxDataStoreSerializer (optional dependency)
 * val serializer = KotlinxDataStoreSerializer()
 * val dataStore = TypeSafeDataStore("app_preferences", serializer)
 *
 * // Store and retrieve objects
 * dataStore.put("user", User("John", 25))
 * dataStore.get<User>("user").collect { user ->
 *     println("User: ${user?.name}")
 * }
 * ```
 *
 * Example with custom serializer:
 * ```
 * class MySerializer : DataStoreSerializer {
 *     override fun encode(data: Any?, type: KType): String { ... }
 *     override fun <T> decode(json: String, type: KType): T { ... }
 * }
 *
 * val dataStore = TypeSafeDataStore("app_preferences", MySerializer())
 * ```
 *
 * @param name The name of the DataStore instance
 * @param serializer Optional serializer for custom objects. If not provided, only primitive operations are available.
 */
class TypeSafeDataStore(
    name: String,
    @PublishedApi
    internal val serializer: DataStoreSerializer? = null,
) {
    @PublishedApi
    internal val preferencesDataStore = PreferencesDataStore(name)

    // ========== Primitive Type Methods (No Serializer Required) ==========

    /**
     * Stores an Int value.
     */
    suspend fun putInt(
        key: String,
        value: Int,
    ) {
        preferencesDataStore.putInt(key, value)
    }

    /**
     * Retrieves an Int value.
     */
    fun getInt(key: String): Flow<Int?> {
        return preferencesDataStore.getInt(key)
    }

    /**
     * Stores a Long value.
     */
    suspend fun putLong(
        key: String,
        value: Long,
    ) {
        preferencesDataStore.putLong(key, value)
    }

    /**
     * Retrieves a Long value.
     */
    fun getLong(key: String): Flow<Long?> {
        return preferencesDataStore.getLong(key)
    }

    /**
     * Stores a Float value.
     */
    suspend fun putFloat(
        key: String,
        value: Float,
    ) {
        preferencesDataStore.putFloat(key, value)
    }

    /**
     * Retrieves a Float value.
     */
    fun getFloat(key: String): Flow<Float?> {
        return preferencesDataStore.getFloat(key)
    }

    /**
     * Stores a Double value.
     */
    suspend fun putDouble(
        key: String,
        value: Double,
    ) {
        preferencesDataStore.putDouble(key, value)
    }

    /**
     * Retrieves a Double value.
     */
    fun getDouble(key: String): Flow<Double?> {
        return preferencesDataStore.getDouble(key)
    }

    /**
     * Stores a Boolean value.
     */
    suspend fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        preferencesDataStore.putBoolean(key, value)
    }

    /**
     * Retrieves a Boolean value.
     */
    fun getBoolean(key: String): Flow<Boolean?> {
        return preferencesDataStore.getBoolean(key)
    }

    /**
     * Stores a String value.
     */
    suspend fun putString(
        key: String,
        value: String,
    ) {
        preferencesDataStore.putString(key, value)
    }

    /**
     * Retrieves a String value.
     */
    fun getString(key: String): Flow<String?> {
        return preferencesDataStore.getString(key)
    }

    /**
     * Stores a Set<String> value.
     */
    suspend fun putStringSet(
        key: String,
        value: Set<String>,
    ) {
        preferencesDataStore.putStringSet(key, value)
    }

    /**
     * Retrieves a Set<String> value.
     */
    fun getStringSet(key: String): Flow<Set<String>?> {
        return preferencesDataStore.getStringSet(key)
    }

    // ========== Object Methods (Requires Serializer) ==========

    /**
     * Stores an object using the configured serializer.
     *
     * Requires a DataStoreSerializer to be provided in the constructor.
     *
     * @param key The key to store the value under
     * @param value The value to store
     * @throws IllegalStateException if no serializer is configured
     * @throws SerializationException if serialization fails
     */
    @OptIn(ExperimentalStdlibApi::class)
    @RequiresSerializer
    suspend inline fun <reified T : Any> put(
        key: String,
        value: T,
    ) {
        val s =
            serializer ?: throw IllegalStateException(
                "No serializer configured. Provide a DataStoreSerializer in the constructor to use object storage.",
            )
        val encoded = s.encode(value, typeOf<T>())
        preferencesDataStore.putString(key, encoded)
    }

    /**
     * Stores an object using the configured serializer with explicit type.
     *
     * @param key The key to store the value under
     * @param value The value to store
     * @param type The KType of the value
     * @throws IllegalStateException if no serializer is configured
     * @throws SerializationException if serialization fails
     */
    suspend fun put(
        key: String,
        value: Any?,
        type: KType,
    ) {
        val s =
            serializer ?: throw IllegalStateException(
                "No serializer configured. Provide a DataStoreSerializer in the constructor to use object storage.",
            )
        val encoded = s.encode(value, type)
        preferencesDataStore.putString(key, encoded)
    }

    /**
     * Retrieves an object using the configured serializer.
     *
     * Requires a DataStoreSerializer to be provided in the constructor.
     *
     * @param key The key to retrieve the value for
     * @return Flow emitting the deserialized value or null if not found
     * @throws IllegalStateException if no serializer is configured
     * @throws SerializationException if deserialization fails
     */
    @OptIn(ExperimentalStdlibApi::class)
    @RequiresSerializer
    inline fun <reified T : Any> get(key: String): Flow<T?> {
        val s =
            serializer ?: throw IllegalStateException(
                "No serializer configured. Provide a DataStoreSerializer in the constructor to use object storage.",
            )
        val type = typeOf<T>()
        return preferencesDataStore.getString(key).map { data ->
            data?.let { s.decode<T>(it, type) }
        }
    }

    /**
     * Retrieves an object using the configured serializer with explicit type.
     *
     * @param key The key to retrieve the value for
     * @param type The KType of the value
     * @return Flow emitting the deserialized value or null if not found
     * @throws IllegalStateException if no serializer is configured
     * @throws SerializationException if deserialization fails
     */
    fun <T> get(
        key: String,
        type: KType,
    ): Flow<T?> {
        val s =
            serializer ?: throw IllegalStateException(
                "No serializer configured. Provide a DataStoreSerializer in the constructor to use object storage.",
            )
        return preferencesDataStore.getString(key).map { data ->
            data?.let { s.decode<T>(it, type) }
        }
    }

    // ========== Common Operations ==========

    /**
     * Removes a value for the given key.
     *
     * @param key The key to remove
     */
    suspend fun remove(key: String) {
        preferencesDataStore.remove(key)
    }

    /**
     * Removes all values from the DataStore.
     */
    suspend fun clear() {
        preferencesDataStore.clear()
    }

    /**
     * Retrieves all keys currently stored.
     *
     * @return Flow emitting the set of all keys
     */
    fun getAllKeys(): Flow<Set<String>> {
        return preferencesDataStore.getAllKeys()
    }
}
