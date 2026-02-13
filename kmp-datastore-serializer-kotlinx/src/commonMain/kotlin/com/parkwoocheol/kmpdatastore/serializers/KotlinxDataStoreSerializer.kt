package com.parkwoocheol.kmpdatastore.serializers

import com.parkwoocheol.kmpdatastore.DataStoreSerializer
import com.parkwoocheol.kmpdatastore.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType

/**
 * DataStoreSerializer implementation using Kotlinx Serialization.
 *
 * This is an optional implementation that requires kotlinx-serialization-json dependency.
 * If you don't want to use kotlinx.serialization, you can implement your own DataStoreSerializer.
 *
 * Usage:
 * ```
 * val serializer = KotlinxDataStoreSerializer()
 * val dataStore = TypeSafeDataStore("my_store", serializer)
 * ```
 *
 * With custom Json configuration:
 * ```
 * val json = Json {
 *     ignoreUnknownKeys = true
 *     prettyPrint = true
 * }
 * val serializer = KotlinxDataStoreSerializer(json)
 * ```
 *
 * @param json Optional Json configuration (default: ignoreUnknownKeys = true)
 */
class KotlinxDataStoreSerializer(
    private val json: Json = Json { ignoreUnknownKeys = true },
) : DataStoreSerializer {
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    override fun encode(
        data: Any?,
        type: KType,
    ): String {
        return try {
            if (data == null) return "null"
            val serializer = json.serializersModule.serializer(type)
            json.encodeToString(serializer, data)
        } catch (e: kotlinx.serialization.SerializationException) {
            throw SerializationException("Failed to encode value: ${e.message}", e)
        } catch (e: Exception) {
            throw SerializationException("Unexpected error during encoding: ${e.message}", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    override fun <T> decode(
        jsonString: String,
        type: KType,
    ): T {
        return try {
            if (jsonString == "null") return null as T
            val serializer = json.serializersModule.serializer(type)
            json.decodeFromString(serializer, jsonString) as T
        } catch (e: kotlinx.serialization.SerializationException) {
            throw SerializationException("Failed to decode value: ${e.message}", e)
        } catch (e: Exception) {
            throw SerializationException("Unexpected error during decoding: ${e.message}", e)
        }
    }
}
