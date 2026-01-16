package com.parkwoocheol.kmpdatastore.platform

import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic DataStore interface.
 *
 * Each platform provides its own implementation using DataStore's native preference keys:
 * - Android: androidx.datastore.preferences
 * - iOS: androidx.datastore.preferences (KMP)
 * - Desktop: androidx.datastore.preferences (KMP)
 *
 * This class provides direct access to primitive types without unnecessary serialization.
 * For complex objects, use TypeSafeDataStore with KotlinxSerializer.
 */
expect class PreferencesDataStore(name: String) {
    // Int operations
    suspend fun putInt(
        key: String,
        value: Int,
    )

    fun getInt(key: String): Flow<Int?>

    // Long operations
    suspend fun putLong(
        key: String,
        value: Long,
    )

    fun getLong(key: String): Flow<Long?>

    // Float operations
    suspend fun putFloat(
        key: String,
        value: Float,
    )

    fun getFloat(key: String): Flow<Float?>

    // Double operations
    suspend fun putDouble(
        key: String,
        value: Double,
    )

    fun getDouble(key: String): Flow<Double?>

    // Boolean operations
    suspend fun putBoolean(
        key: String,
        value: Boolean,
    )

    fun getBoolean(key: String): Flow<Boolean?>

    // String operations
    suspend fun putString(
        key: String,
        value: String,
    )

    fun getString(key: String): Flow<String?>

    // String Set operations
    suspend fun putStringSet(
        key: String,
        value: Set<String>,
    )

    fun getStringSet(key: String): Flow<Set<String>?>

    // Common operations
    suspend fun remove(key: String)

    suspend fun clear()

    fun getAllKeys(): Flow<Set<String>>
}
