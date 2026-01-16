package com.parkwoocheol.kmpdatastore.platform

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation of PreferencesDataStore using NSUserDefaults.
 *
 * @param name The name of the DataStore instance
 */
@OptIn(ExperimentalForeignApi::class)
actual class PreferencesDataStore actual constructor(name: String) {
    private val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                val documentDirectory =
                    NSFileManager.defaultManager.URLForDirectory(
                        directory = NSDocumentDirectory,
                        inDomain = NSUserDomainMask,
                        appropriateForURL = null,
                        create = true,
                        error = null,
                    )
                val dir = documentDirectory ?: throw IllegalStateException("Failed to obtain document directory path for DataStore.")
                val path = dir.path + "/$name.preferences_pb"
                path.toPath()
            },
        )

    // Int operations
    actual suspend fun putInt(
        key: String,
        value: Int,
    ) = putData(intPreferencesKey(key), value)

    actual fun getInt(key: String): Flow<Int?> = getData(intPreferencesKey(key))

    // Long operations
    actual suspend fun putLong(
        key: String,
        value: Long,
    ) = putData(longPreferencesKey(key), value)

    actual fun getLong(key: String): Flow<Long?> = getData(longPreferencesKey(key))

    // Float operations
    actual suspend fun putFloat(
        key: String,
        value: Float,
    ) = putData(floatPreferencesKey(key), value)

    actual fun getFloat(key: String): Flow<Float?> = getData(floatPreferencesKey(key))

    // Double operations
    actual suspend fun putDouble(
        key: String,
        value: Double,
    ) = putData(doublePreferencesKey(key), value)

    actual fun getDouble(key: String): Flow<Double?> = getData(doublePreferencesKey(key))

    // Boolean operations
    actual suspend fun putBoolean(
        key: String,
        value: Boolean,
    ) = putData(booleanPreferencesKey(key), value)

    actual fun getBoolean(key: String): Flow<Boolean?> = getData(booleanPreferencesKey(key))

    // String operations
    actual suspend fun putString(
        key: String,
        value: String,
    ) = putData(stringPreferencesKey(key), value)

    actual fun getString(key: String): Flow<String?> = getData(stringPreferencesKey(key))

    // String Set operations
    actual suspend fun putStringSet(
        key: String,
        value: Set<String>,
    ) = putData(stringSetPreferencesKey(key), value)

    actual fun getStringSet(key: String): Flow<Set<String>?> = getData(stringSetPreferencesKey(key))

    // Common operations
    actual suspend fun remove(key: String) {
        dataStore.edit { preferences ->
            preferences.remove(intPreferencesKey(key))
            preferences.remove(longPreferencesKey(key))
            preferences.remove(floatPreferencesKey(key))
            preferences.remove(doublePreferencesKey(key))
            preferences.remove(booleanPreferencesKey(key))
            preferences.remove(stringPreferencesKey(key))
            preferences.remove(stringSetPreferencesKey(key))
        }
    }

    actual suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    actual fun getAllKeys(): Flow<Set<String>> {
        return dataStore.data.map { preferences ->
            preferences.asMap().keys.map { it.name }.toSet()
        }
    }

    private fun <T> getData(prefKey: Preferences.Key<T>): Flow<T?> {
        return dataStore.data.map { preferences ->
            preferences[prefKey]
        }
    }

    private suspend fun <T> putData(
        prefKey: Preferences.Key<T>,
        value: T,
    ) {
        dataStore.edit { preferences ->
            preferences[prefKey] = value
        }
    }
}
