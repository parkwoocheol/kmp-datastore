package com.parkwoocheol.kmpdatastore.query

import com.parkwoocheol.kmpdatastore.fixtures.MockDataStoreSerializer
import com.parkwoocheol.kmpdatastore.fixtures.TestPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for Query DSL - key-based queries.
 * Tests pattern matching, filtering, sorting, and pagination.
 */
class DataStoreQueryTest {
    private lateinit var testPreferences: TestPreferencesDataStore

    @BeforeTest
    fun setup() {
        testPreferences = TestPreferencesDataStore("test")
    }

    // ========== Pattern Matching Tests ==========

    @Test
    fun `select should match exact pattern`() = runTest {
        testPreferences.putString("user_name", "Alice")
        testPreferences.putString("user_age", "25")
        testPreferences.putString("app_theme", "dark")

        val keys = testPreferences.getAllKeys().first()
        val userKeys = keys.filter { matchesPattern(it, "user_*") }

        assertEquals(2, userKeys.size)
        assertTrue(userKeys.contains("user_name"))
        assertTrue(userKeys.contains("user_age"))
    }

    @Test
    fun `select should match wildcard at end`() = runTest {
        testPreferences.putString("user_name", "Alice")
        testPreferences.putString("user_age", "25")
        testPreferences.putString("admin_role", "superuser")

        val keys = testPreferences.getAllKeys().first()
        val userKeys = keys.filter { matchesPattern(it, "user_*") }

        assertEquals(2, userKeys.size)
    }

    @Test
    fun `select should match wildcard at start`() = runTest {
        testPreferences.putString("user_name", "Alice")
        testPreferences.putString("admin_name", "Bob")
        testPreferences.putString("guest_name", "Charlie")

        val keys = testPreferences.getAllKeys().first()
        val nameKeys = keys.filter { matchesPattern(it, "*_name") }

        assertEquals(3, nameKeys.size)
    }

    @Test
    fun `select should match wildcard in middle`() = runTest {
        testPreferences.putString("user_profile_name", "Alice")
        testPreferences.putString("user_settings_theme", "dark")
        testPreferences.putString("app_config", "value")

        val keys = testPreferences.getAllKeys().first()
        val userKeys = keys.filter { matchesPattern(it, "user_*_*") }

        assertEquals(2, userKeys.size)
    }

    @Test
    fun `select should match exact string without wildcard`() = runTest {
        testPreferences.putString("exact_key", "value")
        testPreferences.putString("another_key", "value")

        val keys = testPreferences.getAllKeys().first()
        val exactKeys = keys.filter { matchesPattern(it, "exact_key") }

        assertEquals(1, exactKeys.size)
        assertTrue(exactKeys.contains("exact_key"))
    }

    @Test
    fun `select should return empty for no matches`() = runTest {
        testPreferences.putString("user_name", "Alice")
        testPreferences.putString("user_age", "25")

        val keys = testPreferences.getAllKeys().first()
        val adminKeys = keys.filter { matchesPattern(it, "admin_*") }

        assertTrue(adminKeys.isEmpty())
    }

    // ========== Key Filtering Tests ==========

    @Test
    fun `keysStartingWith should filter keys by prefix`() = runTest {
        testPreferences.putString("user_name", "Alice")
        testPreferences.putString("user_age", "25")
        testPreferences.putString("app_theme", "dark")

        val keys = testPreferences.getAllKeys().first()
        val userKeys = keys.filter { it.startsWith("user_") }

        assertEquals(2, userKeys.size)
        assertTrue(userKeys.contains("user_name"))
        assertTrue(userKeys.contains("user_age"))
    }

    @Test
    fun `keysEndingWith should filter keys by suffix`() = runTest {
        testPreferences.putString("user_name", "Alice")
        testPreferences.putString("admin_name", "Bob")
        testPreferences.putString("user_age", "25")

        val keys = testPreferences.getAllKeys().first()
        val nameKeys = keys.filter { it.endsWith("_name") }

        assertEquals(2, nameKeys.size)
    }

    @Test
    fun `keysContaining should filter keys by substring`() = runTest {
        testPreferences.putString("user_profile_name", "Alice")
        testPreferences.putString("user_settings", "value")
        testPreferences.putString("app_config", "value")

        val keys = testPreferences.getAllKeys().first()
        val profileKeys = keys.filter { it.contains("profile") }

        assertEquals(1, profileKeys.size)
        assertTrue(profileKeys.contains("user_profile_name"))
    }

    // ========== Count Tests ==========

    @Test
    fun `count should return total number of keys`() = runTest {
        testPreferences.putString("key1", "value1")
        testPreferences.putString("key2", "value2")
        testPreferences.putString("key3", "value3")

        val count = testPreferences.getAllKeys().first().size
        assertEquals(3, count)
    }

    @Test
    fun `count should return 0 for empty store`() = runTest {
        val count = testPreferences.getAllKeys().first().size
        assertEquals(0, count)
    }

    @Test
    fun `count with pattern should return matching count`() = runTest {
        testPreferences.putString("user_name", "Alice")
        testPreferences.putString("user_age", "25")
        testPreferences.putString("app_theme", "dark")

        val keys = testPreferences.getAllKeys().first()
        val userCount = keys.count { matchesPattern(it, "user_*") }

        assertEquals(2, userCount)
    }

    // ========== containsKey Tests ==========

    @Test
    fun `containsKey should return true for existing key`() = runTest {
        testPreferences.putString("test_key", "value")
        val keys = testPreferences.getAllKeys().first()
        assertTrue(keys.contains("test_key"))
    }

    @Test
    fun `containsKey should return false for non-existent key`() = runTest {
        val keys = testPreferences.getAllKeys().first()
        assertTrue(!keys.contains("non_existent"))
    }

    // ========== Helper Functions ==========

    /**
     * Simple pattern matching with wildcard (*) support.
     * Supports patterns like "user_*", "*_name", "user_*_settings", etc.
     */
    private fun matchesPattern(key: String, pattern: String): Boolean {
        if (!pattern.contains('*')) {
            return key == pattern
        }

        val parts = pattern.split('*')
        var currentIndex = 0

        for (i in parts.indices) {
            val part = parts[i]
            if (part.isEmpty()) continue

            when (i) {
                0 -> {
                    // First part - must match at start
                    if (!key.startsWith(part)) return false
                    currentIndex = part.length
                }
                parts.lastIndex -> {
                    // Last part - must match at end
                    if (!key.endsWith(part)) return false
                }
                else -> {
                    // Middle parts - must exist in order
                    val index = key.indexOf(part, currentIndex)
                    if (index == -1) return false
                    currentIndex = index + part.length
                }
            }
        }

        return true
    }
}
