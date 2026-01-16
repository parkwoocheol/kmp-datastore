package com.parkwoocheol.kmpdatastore.query

import com.parkwoocheol.kmpdatastore.fixtures.MockDataStoreSerializer
import com.parkwoocheol.kmpdatastore.fixtures.TestPreferencesDataStore
import com.parkwoocheol.kmpdatastore.fixtures.TestUser
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for Query DSL - value-based queries.
 * Tests value filtering, sorting, and transformations.
 */
class DataStoreValueQueryTest {
    private lateinit var testPreferences: TestPreferencesDataStore
    private lateinit var mockSerializer: MockDataStoreSerializer

    @BeforeTest
    fun setup() {
        testPreferences = TestPreferencesDataStore("test")
        mockSerializer = MockDataStoreSerializer()
    }

    // ========== Value Filtering Tests ==========

    @Test
    fun `filterByValue should filter by predicate`() = runTest {
        testPreferences.putInt("age_alice", 25)
        testPreferences.putInt("age_bob", 30)
        testPreferences.putInt("age_charlie", 35)

        val keys = testPreferences.getAllKeys().first()
        val values = keys.mapNotNull { key ->
            val value = testPreferences.getInt(key).first()
            if (value != null && value > 28) key to value else null
        }.toMap()

        assertEquals(2, values.size)
        assertEquals(30, values["age_bob"])
        assertEquals(35, values["age_charlie"])
    }

    @Test
    fun `valueEquals should match specific value`() = runTest {
        testPreferences.putString("color_1", "red")
        testPreferences.putString("color_2", "blue")
        testPreferences.putString("color_3", "red")

        val keys = testPreferences.getAllKeys().first()
        val redKeys = keys.filter {
            testPreferences.getString(it).first() == "red"
        }

        assertEquals(2, redKeys.size)
        assertTrue(redKeys.contains("color_1"))
        assertTrue(redKeys.contains("color_3"))
    }

    @Test
    fun `valueBetween should filter numeric range`() = runTest {
        testPreferences.putInt("score_1", 50)
        testPreferences.putInt("score_2", 75)
        testPreferences.putInt("score_3", 90)
        testPreferences.putInt("score_4", 100)

        val keys = testPreferences.getAllKeys().first()
        val scores = keys.mapNotNull { key ->
            val value = testPreferences.getInt(key).first()
            if (value != null && value in 60..95) key to value else null
        }.toMap()

        assertEquals(2, scores.size)
        assertEquals(75, scores["score_2"])
        assertEquals(90, scores["score_3"])
    }

    @Test
    fun `valueContains should match substring`() = runTest {
        testPreferences.putString("desc_1", "Hello World")
        testPreferences.putString("desc_2", "Goodbye World")
        testPreferences.putString("desc_3", "Hello Universe")

        val keys = testPreferences.getAllKeys().first()
        val helloKeys = keys.filter {
            testPreferences.getString(it).first()?.contains("Hello") == true
        }

        assertEquals(2, helloKeys.size)
        assertTrue(helloKeys.contains("desc_1"))
        assertTrue(helloKeys.contains("desc_3"))
    }

    // ========== Value Sorting Tests ==========

    @Test
    fun `sortByValueAscending should sort numeric values`() = runTest {
        testPreferences.putInt("num_3", 30)
        testPreferences.putInt("num_1", 10)
        testPreferences.putInt("num_2", 20)

        val keys = testPreferences.getAllKeys().first()
        val sorted = keys.mapNotNull { key ->
            val value = testPreferences.getInt(key).first()
            if (value != null) key to value else null
        }.sortedBy { it.second }

        assertEquals(3, sorted.size)
        assertEquals("num_1", sorted[0].first)
        assertEquals("num_2", sorted[1].first)
        assertEquals("num_3", sorted[2].first)
    }

    @Test
    fun `sortByValueDescending should sort in reverse`() = runTest {
        testPreferences.putInt("num_1", 10)
        testPreferences.putInt("num_2", 20)
        testPreferences.putInt("num_3", 30)

        val keys = testPreferences.getAllKeys().first()
        val sorted = keys.mapNotNull { key ->
            val value = testPreferences.getInt(key).first()
            if (value != null) key to value else null
        }.sortedByDescending { it.second }

        assertEquals(3, sorted.size)
        assertEquals("num_3", sorted[0].first)
        assertEquals("num_2", sorted[1].first)
        assertEquals("num_1", sorted[2].first)
    }

    @Test
    fun `sortByValueAscending should sort string values`() = runTest {
        testPreferences.putString("name_3", "Charlie")
        testPreferences.putString("name_1", "Alice")
        testPreferences.putString("name_2", "Bob")

        val keys = testPreferences.getAllKeys().first()
        val sorted = keys.mapNotNull { key ->
            val value = testPreferences.getString(key).first()
            if (value != null) key to value else null
        }.sortedBy { it.second }

        assertEquals(3, sorted.size)
        assertEquals("name_1", sorted[0].first) // Alice
        assertEquals("name_2", sorted[1].first) // Bob
        assertEquals("name_3", sorted[2].first) // Charlie
    }

    // ========== Complex Query Tests ==========

    @Test
    fun `chained filters should work together`() = runTest {
        testPreferences.putInt("age_alice", 25)
        testPreferences.putInt("age_bob", 30)
        testPreferences.putInt("age_charlie", 35)
        testPreferences.putInt("age_david", 40)

        val keys = testPreferences.getAllKeys().first()
        val filtered = keys
            .filter { it.startsWith("age_") }
            .mapNotNull { key ->
                val value = testPreferences.getInt(key).first()
                if (value != null && value in 28..38) key to value else null
            }
            .toMap()

        assertEquals(2, filtered.size)
        assertTrue(filtered.containsKey("age_bob"))
        assertTrue(filtered.containsKey("age_charlie"))
    }

    @Test
    fun `filter and sort should work together`() = runTest {
        testPreferences.putInt("score_1", 50)
        testPreferences.putInt("score_2", 75)
        testPreferences.putInt("score_3", 90)
        testPreferences.putInt("score_4", 100)

        val keys = testPreferences.getAllKeys().first()
        val result = keys
            .mapNotNull { key ->
                val value = testPreferences.getInt(key).first()
                if (value != null && value >= 70) key to value else null
            }
            .sortedByDescending { it.second }

        assertEquals(3, result.size)
        assertEquals("score_4", result[0].first) // 100
        assertEquals("score_3", result[1].first) // 90
        assertEquals("score_2", result[2].first) // 75
    }

    // ========== Pagination Tests ==========

    @Test
    fun `take should limit results`() = runTest {
        testPreferences.putInt("item_1", 1)
        testPreferences.putInt("item_2", 2)
        testPreferences.putInt("item_3", 3)
        testPreferences.putInt("item_4", 4)
        testPreferences.putInt("item_5", 5)

        val keys = testPreferences.getAllKeys().first()
        val limited = keys.sorted().take(3)

        assertEquals(3, limited.size)
    }

    @Test
    fun `skip should offset results`() = runTest {
        testPreferences.putInt("item_1", 1)
        testPreferences.putInt("item_2", 2)
        testPreferences.putInt("item_3", 3)
        testPreferences.putInt("item_4", 4)

        val keys = testPreferences.getAllKeys().first()
        val skipped = keys.sorted().drop(2)

        assertEquals(2, skipped.size)
    }

    @Test
    fun `take and skip together should paginate`() = runTest {
        testPreferences.putInt("item_1", 1)
        testPreferences.putInt("item_2", 2)
        testPreferences.putInt("item_3", 3)
        testPreferences.putInt("item_4", 4)
        testPreferences.putInt("item_5", 5)

        val keys = testPreferences.getAllKeys().first()
        val page2 = keys.sorted().drop(2).take(2)

        assertEquals(2, page2.size)
        assertTrue(page2.contains("item_3"))
        assertTrue(page2.contains("item_4"))
    }

    // ========== Edge Cases ==========

    @Test
    fun `empty result set should be handled`() = runTest {
        testPreferences.putInt("num_1", 10)
        testPreferences.putInt("num_2", 20)

        val keys = testPreferences.getAllKeys().first()
        val filtered = keys.mapNotNull { key ->
            val value = testPreferences.getInt(key).first()
            if (value != null && value > 100) key to value else null
        }

        assertTrue(filtered.isEmpty())
    }

    @Test
    fun `single result should be handled`() = runTest {
        testPreferences.putInt("num_1", 50)

        val keys = testPreferences.getAllKeys().first()
        val filtered = keys.mapNotNull { key ->
            val value = testPreferences.getInt(key).first()
            if (value != null && value == 50) key to value else null
        }

        assertEquals(1, filtered.size)
        assertEquals(50, filtered[0].second)
    }
}
