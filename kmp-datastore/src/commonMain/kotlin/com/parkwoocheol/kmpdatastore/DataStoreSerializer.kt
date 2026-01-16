package com.parkwoocheol.kmpdatastore

import kotlin.reflect.KType

/**
 * Interface for abstracting the serialization and deserialization logic used by TypeSafeDataStore.
 * Implement this interface to support different serialization libraries (e.g., Kotlinx Serialization, Moshi, Gson).
 *
 * This design follows the BridgeSerializer pattern from compose-webview, allowing users to:
 * 1. Use the built-in KotlinxDataStoreSerializer if kotlinx.serialization is available
 * 2. Implement their own serializer for other libraries
 * 3. No serialization library is required as a dependency
 *
 * Example implementation with Kotlinx Serialization:
 * ```
 * class KotlinxDataStoreSerializer : DataStoreSerializer {
 *     private val json = Json { ignoreUnknownKeys = true }
 *
 *     override fun encode(data: Any?, type: KType): String {
 *         // Implementation using kotlinx.serialization
 *     }
 *
 *     override fun <T> decode(json: String, type: KType): T {
 *         // Implementation using kotlinx.serialization
 *     }
 * }
 * ```
 *
 * Example implementation with Moshi:
 * ```
 * class MoshiDataStoreSerializer(private val moshi: Moshi) : DataStoreSerializer {
 *     override fun encode(data: Any?, type: KType): String {
 *         val adapter = moshi.adapter<Any>(type.javaType)
 *         return adapter.toJson(data)
 *     }
 *
 *     override fun <T> decode(json: String, type: KType): T {
 *         val adapter = moshi.adapter<T>(type.javaType)
 *         return adapter.fromJson(json)!!
 *     }
 * }
 * ```
 */
interface DataStoreSerializer {
    /**
     * Encodes the given [data] into a JSON string.
     *
     * @param data The object to encode.
     * @param type The [KType] of the data.
     * @return The JSON string representation of the data.
     * @throws SerializationException if encoding fails
     */
    fun encode(
        data: Any?,
        type: KType,
    ): String

    /**
     * Decodes the given [json] string into an object of type [T].
     *
     * @param json The JSON string to decode.
     * @param type The [KType] of the target object.
     * @return The decoded object.
     * @throws SerializationException if decoding fails
     */
    fun <T> decode(
        json: String,
        type: KType,
    ): T
}

/**
 * Exception thrown when serialization or deserialization fails.
 */
class SerializationException(message: String, cause: Throwable? = null) : Exception(message, cause)
