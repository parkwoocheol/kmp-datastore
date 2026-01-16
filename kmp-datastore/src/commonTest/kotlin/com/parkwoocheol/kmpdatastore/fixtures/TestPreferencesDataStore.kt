package com.parkwoocheol.kmpdatastore.fixtures

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory implementation of PreferencesDataStore for testing.
 * Simulates DataStore behavior without actual file I/O.
 */
class TestPreferencesDataStore(name: String = "test_datastore") {
    // Simulates DataStore's internal storage
    private val data = MutableStateFlow<Map<String, Any>>(emptyMap())

    // Int operations
    suspend fun putInt(
        key: String,
        value: Int,
    ) {
        updateData { it + (key to value) }
    }

    fun getInt(key: String): Flow<Int?> = data.map { it[key] as? Int }

    // Long operations
    suspend fun putLong(
        key: String,
        value: Long,
    ) {
        updateData { it + (key to value) }
    }

    fun getLong(key: String): Flow<Long?> = data.map { it[key] as? Long }

    // Float operations
    suspend fun putFloat(
        key: String,
        value: Float,
    ) {
        updateData { it + (key to value) }
    }

    fun getFloat(key: String): Flow<Float?> = data.map { it[key] as? Float }

    // Double operations
    suspend fun putDouble(
        key: String,
        value: Double,
    ) {
        updateData { it + (key to value) }
    }

    fun getDouble(key: String): Flow<Double?> = data.map { it[key] as? Double }

    // Boolean operations
    suspend fun putBoolean(
        key: String,
        value: Boolean,
    ) {
        updateData { it + (key to value) }
    }

    fun getBoolean(key: String): Flow<Boolean?> = data.map { it[key] as? Boolean }

    // String operations
    suspend fun putString(
        key: String,
        value: String,
    ) {
        updateData { it + (key to value) }
    }

    fun getString(key: String): Flow<String?> = data.map { it[key] as? String }

    // String Set operations
    suspend fun putStringSet(
        key: String,
        value: Set<String>,
    ) {
        updateData { it + (key to value) }
    }

    fun getStringSet(key: String): Flow<Set<String>?> = data.map { it[key] as? Set<String> }

    // Common operations
    suspend fun remove(key: String) {
        updateData { it - key }
    }

    suspend fun clear() {
        updateData { emptyMap() }
    }

    fun getAllKeys(): Flow<Set<String>> = data.map { it.keys }

    // Helper to atomically update data
    private suspend fun updateData(transform: (Map<String, Any>) -> Map<String, Any>) {
        data.value = transform(data.value)
    }

    /**
     * Returns current data snapshot for testing purposes.
     */
    fun getSnapshot(): Map<String, Any> = data.value
}
