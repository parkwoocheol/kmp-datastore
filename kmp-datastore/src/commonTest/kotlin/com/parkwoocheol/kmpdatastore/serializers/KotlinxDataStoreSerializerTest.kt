package com.parkwoocheol.kmpdatastore.serializers

import com.parkwoocheol.kmpdatastore.SerializationException
import com.parkwoocheol.kmpdatastore.fixtures.ComplexData
import com.parkwoocheol.kmpdatastore.fixtures.TestSettings
import com.parkwoocheol.kmpdatastore.fixtures.TestUser
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Tests for KotlinxDataStoreSerializer.
 * Tests encode/decode for various types and error handling.
 */
class KotlinxDataStoreSerializerTest {
    private lateinit var serializer: KotlinxDataStoreSerializer

    @BeforeTest
    fun setup() {
        serializer = KotlinxDataStoreSerializer()
    }

    // ========== Basic Object Serialization ==========

    @Test
    fun `encode and decode simple data class`() {
        val user = TestUser("Alice", 25, "alice@example.com")
        val encoded = serializer.encode(user, typeOf<TestUser>())
        val decoded = serializer.decode<TestUser>(encoded, typeOf<TestUser>())

        assertEquals(user, decoded)
    }

    @Test
    fun `encode should produce valid JSON`() {
        val user = TestUser("Bob", 30, "bob@example.com")
        val encoded = serializer.encode(user, typeOf<TestUser>())

        // Should be valid JSON containing the fields
        assert(encoded.contains("\"name\""))
        assert(encoded.contains("\"Bob\""))
        assert(encoded.contains("\"age\""))
        assert(encoded.contains("30"))
    }

    @Test
    fun `decode should handle JSON with all fields`() {
        val json = """{"name":"Charlie","age":35,"email":"charlie@example.com"}"""
        val decoded = serializer.decode<TestUser>(json, typeOf<TestUser>())

        assertEquals("Charlie", decoded.name)
        assertEquals(35, decoded.age)
        assertEquals("charlie@example.com", decoded.email)
    }

    // ========== Complex Type Serialization ==========

    @Test
    fun `encode and decode data class with defaults`() {
        val settings = TestSettings(theme = "dark", notifications = true)
        val encoded = serializer.encode(settings, typeOf<TestSettings>())
        val decoded = serializer.decode<TestSettings>(encoded, typeOf<TestSettings>())

        assertEquals(settings, decoded)
        assertEquals("en", decoded.language) // default value
    }

    @Test
    fun `encode and decode complex nested structure`() {
        val data = ComplexData(
            id = 123,
            items = listOf("item1", "item2", "item3"),
            metadata = mapOf("key1" to "value1", "key2" to "value2")
        )
        val encoded = serializer.encode(data, typeOf<ComplexData>())
        val decoded = serializer.decode<ComplexData>(encoded, typeOf<ComplexData>())

        assertEquals(data, decoded)
        assertEquals(3, decoded.items.size)
        assertEquals(2, decoded.metadata.size)
    }

    @Test
    fun `encode and decode list of objects`() {
        val users = listOf(
            TestUser("Alice", 25, "alice@example.com"),
            TestUser("Bob", 30, "bob@example.com"),
            TestUser("Charlie", 35, "charlie@example.com")
        )
        val encoded = serializer.encode(users, typeOf<List<TestUser>>())
        val decoded = serializer.decode<List<TestUser>>(encoded, typeOf<List<TestUser>>())

        assertEquals(3, decoded.size)
        assertEquals(users, decoded)
    }

    // ========== Null Handling ==========

    @Test
    fun `encode null should return null string`() {
        val encoded = serializer.encode(null, typeOf<TestUser?>())
        assertEquals("null", encoded)
    }

    @Test
    fun `decode null string should return null`() {
        val decoded = serializer.decode<TestUser?>("null", typeOf<TestUser?>())
        assertNull(decoded)
    }

    // ========== Edge Cases ==========

    @Test
    fun `encode and decode empty list`() {
        val emptyList = emptyList<String>()
        val encoded = serializer.encode(emptyList, typeOf<List<String>>())
        val decoded = serializer.decode<List<String>>(encoded, typeOf<List<String>>())

        assertEquals(emptyList, decoded)
        assertEquals(0, decoded.size)
    }

    @Test
    fun `encode and decode empty map`() {
        val emptyMap = emptyMap<String, String>()
        val encoded = serializer.encode(emptyMap, typeOf<Map<String, String>>())
        val decoded = serializer.decode<Map<String, String>>(encoded, typeOf<Map<String, String>>())

        assertEquals(emptyMap, decoded)
        assertEquals(0, decoded.size)
    }

    // ========== Error Handling ==========

    @Test
    fun `decode should throw SerializationException for invalid JSON`() {
        val invalidJson = "{ invalid json }"

        assertFailsWith<SerializationException> {
            serializer.decode<TestUser>(invalidJson, typeOf<TestUser>())
        }
    }

    @Test
    fun `decode should throw SerializationException for mismatched types`() {
        val json = """{"name":"Alice","age":"not a number","email":"alice@example.com"}"""

        assertFailsWith<SerializationException> {
            serializer.decode<TestUser>(json, typeOf<TestUser>())
        }
    }

    @Test
    fun `decode should handle missing optional fields with defaults`() {
        // Missing "language" field, should use default value "en"
        val json = """{"theme":"dark","notifications":true}"""
        val decoded = serializer.decode<TestSettings>(json, typeOf<TestSettings>())

        assertEquals("dark", decoded.theme)
        assertEquals(true, decoded.notifications)
        assertEquals("en", decoded.language) // default value
    }

    // ========== Custom Json Configuration ==========

    @Test
    fun `custom Json configuration should be respected`() {
        val customJson = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
        val customSerializer = KotlinxDataStoreSerializer(customJson)

        val user = TestUser("Alice", 25, "alice@example.com")
        val encoded = customSerializer.encode(user, typeOf<TestUser>())

        // Pretty print should add newlines and indentation
        assert(encoded.contains("\n"))
    }

    @Test
    fun `ignoreUnknownKeys should allow extra fields in JSON`() {
        // JSON with extra field "extra"
        val json = """{"name":"Alice","age":25,"email":"alice@example.com","extra":"field"}"""
        val decoded = serializer.decode<TestUser>(json, typeOf<TestUser>())

        assertEquals("Alice", decoded.name)
        assertEquals(25, decoded.age)
    }
}
