package com.parkwoocheol.kmpdatastore.fixtures

import com.parkwoocheol.kmpdatastore.DataStoreSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KType

/**
 * Test data classes for unit tests.
 */
@Serializable
data class TestUser(
    val name: String,
    val age: Int,
    val email: String,
)

@Serializable
data class TestSettings(
    val theme: String,
    val notifications: Boolean,
    val language: String = "en",
)

@Serializable
data class ComplexData(
    val id: Int,
    val items: List<String>,
    val metadata: Map<String, String>,
)

/**
 * Mock DataStoreSerializer for testing without kotlinx.serialization dependency.
 * Records encode/decode history for verification.
 */
class MockDataStoreSerializer : DataStoreSerializer {
    val encodeHistory = mutableListOf<Pair<Any?, KType>>()
    val decodeHistory = mutableListOf<Pair<String, KType>>()

    private val json = Json {
        prettyPrint = false
        isLenient = true
    }

    override fun encode(data: Any?, type: KType): String {
        encodeHistory.add(data to type)
        return json.encodeToString(serializer(type), data)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> decode(json: String, type: KType): T {
        decodeHistory.add(json to type)
        return this.json.decodeFromString(serializer(type), json) as T
    }

    /**
     * Clears history for test isolation.
     */
    fun clearHistory() {
        encodeHistory.clear()
        decodeHistory.clear()
    }
}
