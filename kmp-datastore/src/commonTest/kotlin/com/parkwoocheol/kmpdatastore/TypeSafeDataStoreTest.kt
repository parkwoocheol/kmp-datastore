package com.parkwoocheol.kmpdatastore

import com.parkwoocheol.kmpdatastore.fixtures.ComplexData
import com.parkwoocheol.kmpdatastore.fixtures.MockDataStoreSerializer
import com.parkwoocheol.kmpdatastore.fixtures.TestPreferencesDataStore
import com.parkwoocheol.kmpdatastore.fixtures.TestUser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.reflect.typeOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Core API tests for TypeSafeDataStore components.
 * Tests TestPreferencesDataStore (in-memory implementation) and MockDataStoreSerializer.
 *
 * Note: TypeSafeDataStore integration with real PreferencesDataStore will be tested
 * in platform-specific integration tests.
 */
class TypeSafeDataStoreTest {
    private lateinit var testPreferences: TestPreferencesDataStore
    private lateinit var mockSerializer: MockDataStoreSerializer

    @BeforeTest
    fun setup() {
        testPreferences = TestPreferencesDataStore("test")
        mockSerializer = MockDataStoreSerializer()
    }

    // ========== Int Operations ==========

    @Test
    fun `putInt and getInt should store and retrieve integer`() = runTest {
        testPreferences.putInt("test_key", 42)
        val result = testPreferences.getInt("test_key").first()
        assertEquals(42, result)
    }

    @Test
    fun `getInt should return null for non-existent key`() = runTest {
        val result = testPreferences.getInt("non_existent").first()
        assertNull(result)
    }

    @Test
    fun `putInt should update existing value`() = runTest {
        testPreferences.putInt("test_key", 42)
        testPreferences.putInt("test_key", 100)
        val result = testPreferences.getInt("test_key").first()
        assertEquals(100, result)
    }

    // ========== Long Operations ==========

    @Test
    fun `putLong and getLong should store and retrieve long`() = runTest {
        testPreferences.putLong("test_key", 9999999999L)
        val result = testPreferences.getLong("test_key").first()
        assertEquals(9999999999L, result)
    }

    // ========== Float Operations ==========

    @Test
    fun `putFloat and getFloat should store and retrieve float`() = runTest {
        testPreferences.putFloat("test_key", 3.14f)
        val result = testPreferences.getFloat("test_key").first()
        assertEquals(3.14f, result)
    }

    // ========== Double Operations ==========

    @Test
    fun `putDouble and getDouble should store and retrieve double`() = runTest {
        testPreferences.putDouble("test_key", 3.14159265359)
        val result = testPreferences.getDouble("test_key").first()
        assertEquals(3.14159265359, result)
    }

    // ========== Boolean Operations ==========

    @Test
    fun `putBoolean and getBoolean should store and retrieve boolean`() = runTest {
        testPreferences.putBoolean("test_key", true)
        val result = testPreferences.getBoolean("test_key").first()
        assertEquals(true, result)
    }

    @Test
    fun `putBoolean should handle false value`() = runTest {
        testPreferences.putBoolean("test_key", false)
        val result = testPreferences.getBoolean("test_key").first()
        assertEquals(false, result)
    }

    // ========== String Operations ==========

    @Test
    fun `putString and getString should store and retrieve string`() = runTest {
        testPreferences.putString("test_key", "Hello, World!")
        val result = testPreferences.getString("test_key").first()
        assertEquals("Hello, World!", result)
    }

    @Test
    fun `putString should handle empty string`() = runTest {
        testPreferences.putString("test_key", "")
        val result = testPreferences.getString("test_key").first()
        assertEquals("", result)
    }

    @Test
    fun `putString should handle unicode characters`() = runTest {
        testPreferences.putString("test_key", "안녕하세요 🚀")
        val result = testPreferences.getString("test_key").first()
        assertEquals("안녕하세요 🚀", result)
    }

    // ========== StringSet Operations ==========

    @Test
    fun `putStringSet and getStringSet should store and retrieve set`() = runTest {
        val testSet = setOf("apple", "banana", "cherry")
        testPreferences.putStringSet("test_key", testSet)
        val result = testPreferences.getStringSet("test_key").first()
        assertEquals(testSet, result)
    }

    @Test
    fun `putStringSet should handle empty set`() = runTest {
        testPreferences.putStringSet("test_key", emptySet())
        val result = testPreferences.getStringSet("test_key").first()
        assertEquals(emptySet(), result)
    }

    // ========== Object Serialization Tests ==========

    @Test
    fun `MockSerializer should encode and decode simple object`() = runTest {
        val user = TestUser("Alice", 25, "alice@example.com")
        val encoded = mockSerializer.encode(user, typeOf<TestUser>())
        val decoded = mockSerializer.decode<TestUser>(encoded, typeOf<TestUser>())

        assertEquals(user, decoded)
        assertEquals(1, mockSerializer.encodeHistory.size)
        assertEquals(1, mockSerializer.decodeHistory.size)
    }

    @Test
    fun `MockSerializer should handle complex nested objects`() = runTest {
        val data = ComplexData(
            id = 123,
            items = listOf("item1", "item2", "item3"),
            metadata = mapOf("key1" to "value1", "key2" to "value2")
        )
        val encoded = mockSerializer.encode(data, typeOf<ComplexData>())
        val decoded = mockSerializer.decode<ComplexData>(encoded, typeOf<ComplexData>())

        assertEquals(data, decoded)
    }

    @Test
    fun `MockSerializer clearHistory should clear tracking`() = runTest {
        val user = TestUser("Bob", 30, "bob@example.com")
        mockSerializer.encode(user, typeOf<TestUser>())
        mockSerializer.decode<TestUser>("""{"name":"Bob","age":30,"email":"bob@example.com"}""", typeOf<TestUser>())

        assertEquals(1, mockSerializer.encodeHistory.size)
        assertEquals(1, mockSerializer.decodeHistory.size)

        mockSerializer.clearHistory()

        assertEquals(0, mockSerializer.encodeHistory.size)
        assertEquals(0, mockSerializer.decodeHistory.size)
    }

    // ========== Remove Operation ==========

    @Test
    fun `remove should delete key-value pair`() = runTest {
        testPreferences.putString("test_key", "value")
        val beforeRemove = testPreferences.getString("test_key").first()
        assertEquals("value", beforeRemove)

        testPreferences.remove("test_key")
        val afterRemove = testPreferences.getString("test_key").first()
        assertNull(afterRemove)
    }

    @Test
    fun `remove should be idempotent for non-existent key`() = runTest {
        // Should not throw
        testPreferences.remove("non_existent")
        val result = testPreferences.getString("non_existent").first()
        assertNull(result)
    }

    // ========== Clear Operation ==========

    @Test
    fun `clear should remove all keys`() = runTest {
        testPreferences.putString("key1", "value1")
        testPreferences.putInt("key2", 42)
        testPreferences.putBoolean("key3", true)

        val keysBefore = testPreferences.getAllKeys().first()
        assertEquals(3, keysBefore.size)

        testPreferences.clear()

        val keysAfter = testPreferences.getAllKeys().first()
        assertTrue(keysAfter.isEmpty())
    }

    @Test
    fun `clear should be idempotent on empty store`() = runTest {
        testPreferences.clear()
        val keys = testPreferences.getAllKeys().first()
        assertTrue(keys.isEmpty())
    }

    // ========== getAllKeys Operation ==========

    @Test
    fun `getAllKeys should return all stored keys`() = runTest {
        testPreferences.putString("key1", "value1")
        testPreferences.putInt("key2", 42)
        testPreferences.putBoolean("key3", true)

        val keys = testPreferences.getAllKeys().first()
        assertEquals(3, keys.size)
        assertTrue(keys.contains("key1"))
        assertTrue(keys.contains("key2"))
        assertTrue(keys.contains("key3"))
    }

    @Test
    fun `getAllKeys should return empty set for empty store`() = runTest {
        val keys = testPreferences.getAllKeys().first()
        assertTrue(keys.isEmpty())
    }

    @Test
    fun `getAllKeys should update when keys are added or removed`() = runTest {
        testPreferences.putString("key1", "value1")
        var keys = testPreferences.getAllKeys().first()
        assertEquals(1, keys.size)

        testPreferences.putString("key2", "value2")
        keys = testPreferences.getAllKeys().first()
        assertEquals(2, keys.size)

        testPreferences.remove("key1")
        keys = testPreferences.getAllKeys().first()
        assertEquals(1, keys.size)
        assertTrue(keys.contains("key2"))
    }

    // ========== Edge Cases & Data Integrity ==========

    @Test
    fun `TestPreferencesDataStore should maintain data integrity across operations`() = runTest {
        // Store multiple types
        testPreferences.putInt("age", 25)
        testPreferences.putString("name", "Alice")
        testPreferences.putBoolean("active", true)

        // Verify all values
        assertEquals(25, testPreferences.getInt("age").first())
        assertEquals("Alice", testPreferences.getString("name").first())
        assertEquals(true, testPreferences.getBoolean("active").first())

        // Update one value
        testPreferences.putInt("age", 26)

        // Verify others unchanged
        assertEquals(26, testPreferences.getInt("age").first())
        assertEquals("Alice", testPreferences.getString("name").first())
        assertEquals(true, testPreferences.getBoolean("active").first())
    }

    @Test
    fun `TestPreferencesDataStore getSnapshot should return current state`() = runTest {
        testPreferences.putInt("key1", 42)
        testPreferences.putString("key2", "value")

        val snapshot = testPreferences.getSnapshot()
        assertEquals(2, snapshot.size)
        assertEquals(42, snapshot["key1"])
        assertEquals("value", snapshot["key2"])
    }
}
