package com.parkwoocheol.kmpdatastore.query

import com.parkwoocheol.kmpdatastore.TypeSafeDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.ExperimentalStdlibApi
import kotlin.reflect.typeOf

/**
 * Single key/value entry returned by value queries.
 */
data class QueryEntry<T : Any>(
    val key: String,
    val value: T,
)

private enum class SortOrder {
    ASC,
    DESC,
}

/**
 * Query builder for DataStore key operations.
 *
 * Note: This query builder works with keys only. To fetch values, use the appropriate
 * get methods (getString, getInt, etc.) on the returned keys.
 */
class DataStoreQuery(
    private val dataStore: TypeSafeDataStore,
) {
    private val filters = mutableListOf<(String) -> Boolean>()
    private var limitCount: Int? = null
    private var skipCount: Int = 0
    private var sortOrder: SortOrder = SortOrder.ASC

    /**
     * Filters keys that start with the given prefix.
     */
    fun startsWith(prefix: String): DataStoreQuery {
        filters.add { it.startsWith(prefix) }
        return this
    }

    /**
     * Filters keys that end with the given suffix.
     */
    fun endsWith(suffix: String): DataStoreQuery {
        filters.add { it.endsWith(suffix) }
        return this
    }

    /**
     * Filters keys that contain the given substring.
     */
    fun contains(substring: String): DataStoreQuery {
        filters.add { it.contains(substring) }
        return this
    }

    /**
     * Filters keys using a custom predicate.
     */
    fun filter(predicate: (String) -> Boolean): DataStoreQuery {
        filters.add(predicate)
        return this
    }

    /**
     * Filters keys using a regular expression.
     */
    fun matches(regex: Regex): DataStoreQuery {
        filters.add { regex.containsMatchIn(it) }
        return this
    }

    /**
     * Limits the number of results.
     */
    fun take(count: Int): DataStoreQuery {
        limitCount = count
        return this
    }

    /**
     * Skips the first N results.
     */
    fun skip(count: Int): DataStoreQuery {
        skipCount = count
        return this
    }

    /**
     * Sorts results by key in ascending order.
     */
    fun sortByKeyAscending(): DataStoreQuery {
        sortOrder = SortOrder.ASC
        return this
    }

    /**
     * Sorts results by key in descending order.
     */
    fun sortByKeyDescending(): DataStoreQuery {
        sortOrder = SortOrder.DESC
        return this
    }

    /**
     * Executes the query and returns a Flow of matching keys.
     */
    fun executeKeys(): Flow<List<String>> {
        return dataStore.getAllKeys().map { keys ->
            var filteredKeys = keys

            // Apply filters
            filters.forEach { filter ->
                filteredKeys = filteredKeys.filter(filter).toSet()
            }

            // Sort
            val sortedKeys =
                if (sortOrder == SortOrder.ASC) {
                    filteredKeys.sorted()
                } else {
                    filteredKeys.sortedDescending()
                }

            // Skip and take
            sortedKeys
                .drop(skipCount)
                .let { if (limitCount != null) it.take(limitCount!!) else it }
        }
    }
}

/**
 * Query builder for key + value operations.
 *
 * Note: This query performs an in-memory scan of all keys and reads each value.
 * Prefer key-only queries when possible for better performance.
 *
 * Supported value types:
 * - Primitives (String, Int, Long, Float, Double, Boolean)
 * - Set<String>
 * - Custom objects via configured serializer
 */
class DataStoreValueQuery<T : Any>(
    private val dataStore: TypeSafeDataStore,
    private val valueGetter: suspend (String) -> T?,
) {
    private val keyFilters = mutableListOf<(String) -> Boolean>()
    private val valueFilters = mutableListOf<(String, T) -> Boolean>()
    private var limitCount: Int? = null
    private var skipCount: Int = 0
    private var keySortOrder: SortOrder = SortOrder.ASC
    private var valueComparator: Comparator<T>? = null
    private var valueSortOrder: SortOrder? = null
    private var ignoreErrors: Boolean = true

    /**
     * Filters keys that start with the given prefix.
     */
    fun startsWith(prefix: String): DataStoreValueQuery<T> {
        keyFilters.add { it.startsWith(prefix) }
        return this
    }

    /**
     * Filters keys that end with the given suffix.
     */
    fun endsWith(suffix: String): DataStoreValueQuery<T> {
        keyFilters.add { it.endsWith(suffix) }
        return this
    }

    /**
     * Filters keys that contain the given substring.
     */
    fun contains(substring: String): DataStoreValueQuery<T> {
        keyFilters.add { it.contains(substring) }
        return this
    }

    /**
     * Filters keys using a custom predicate.
     */
    fun filter(predicate: (String) -> Boolean): DataStoreValueQuery<T> {
        keyFilters.add(predicate)
        return this
    }

    /**
     * Filters values using a custom predicate.
     */
    fun filterValue(predicate: (String, T) -> Boolean): DataStoreValueQuery<T> {
        valueFilters.add(predicate)
        return this
    }

    /**
     * Filters keys using a regular expression.
     */
    fun matches(regex: Regex): DataStoreValueQuery<T> {
        keyFilters.add { regex.containsMatchIn(it) }
        return this
    }

    /**
     * Limits the number of results.
     */
    fun take(count: Int): DataStoreValueQuery<T> {
        limitCount = count
        return this
    }

    /**
     * Skips the first N results.
     */
    fun skip(count: Int): DataStoreValueQuery<T> {
        skipCount = count
        return this
    }

    /**
     * Sorts results by key in ascending order.
     */
    fun sortByKeyAscending(): DataStoreValueQuery<T> {
        keySortOrder = SortOrder.ASC
        valueSortOrder = null
        return this
    }

    /**
     * Sorts results by key in descending order.
     */
    fun sortByKeyDescending(): DataStoreValueQuery<T> {
        keySortOrder = SortOrder.DESC
        valueSortOrder = null
        return this
    }

    /**
     * Sorts results by value using the provided comparator (ascending).
     */
    fun sortByValueAscending(comparator: Comparator<T>): DataStoreValueQuery<T> {
        valueComparator = comparator
        valueSortOrder = SortOrder.ASC
        return this
    }

    /**
     * Sorts results by value using the provided comparator (descending).
     */
    fun sortByValueDescending(comparator: Comparator<T>): DataStoreValueQuery<T> {
        valueComparator = comparator
        valueSortOrder = SortOrder.DESC
        return this
    }

    /**
     * Makes the query fail fast on decode or type errors instead of skipping entries.
     */
    fun failOnError(): DataStoreValueQuery<T> {
        ignoreErrors = false
        return this
    }

    /**
     * Executes the query and returns a Flow of matching key/value entries.
     */
    fun execute(): Flow<List<QueryEntry<T>>> {
        return dataStore.getAllKeys().map { keys ->
            var filteredKeys = keys

            // Apply key filters
            keyFilters.forEach { filter ->
                filteredKeys = filteredKeys.filter(filter).toSet()
            }

            // Resolve and filter values
            val entries =
                filteredKeys.mapNotNull { key ->
                    val value =
                        try {
                            valueGetter(key)
                        } catch (e: Exception) {
                            if (ignoreErrors) null else throw e
                        }
                    if (value == null) {
                        null
                    } else if (valueFilters.all { filter -> filter(key, value) }) {
                        QueryEntry(key, value)
                    } else {
                        null
                    }
                }

            // Sort
            val sortedEntries =
                when (valueSortOrder) {
                    SortOrder.ASC -> entries.sortedWith(buildValueComparator(ascending = true))
                    SortOrder.DESC -> entries.sortedWith(buildValueComparator(ascending = false))
                    null -> {
                        if (keySortOrder == SortOrder.ASC) {
                            entries.sortedBy { it.key }
                        } else {
                            entries.sortedByDescending { it.key }
                        }
                    }
                }

            // Skip and take
            sortedEntries
                .drop(skipCount)
                .let { if (limitCount != null) it.take(limitCount!!) else it }
        }
    }

    /**
     * Executes the query and returns a Flow of matching keys.
     */
    fun executeKeys(): Flow<List<String>> {
        return execute().map { entries -> entries.map { it.key } }
    }

    /**
     * Executes the query and returns a Flow of matching values.
     */
    fun executeValues(): Flow<List<T>> {
        return execute().map { entries -> entries.map { it.value } }
    }

    /**
     * Executes the query and returns a Flow of matching key/value map.
     */
    fun executeMap(): Flow<Map<String, T>> {
        return execute().map { entries -> entries.associate { it.key to it.value } }
    }

    private fun buildValueComparator(ascending: Boolean): Comparator<QueryEntry<T>> {
        val comparator =
            valueComparator
                ?: throw IllegalStateException(
                    "Value comparator is required for value sorting.",
                )
        val pairComparator =
            Comparator<QueryEntry<T>> { a, b ->
                val result = comparator.compare(a.value, b.value)
                if (result != 0) {
                    result
                } else {
                    a.key.compareTo(b.key)
                }
            }
        return if (ascending) pairComparator else pairComparator.reversed()
    }
}

/**
 * Starts a query on this DataStore.
 */
fun TypeSafeDataStore.query(): DataStoreQuery {
    return DataStoreQuery(this)
}

/**
 * Starts a key + value query on this DataStore with inferred type.
 */
@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T : Any> TypeSafeDataStore.queryValues(): DataStoreValueQuery<T> {
    val type = typeOf<T>()
    val isStringSet = type.classifier == Set::class && type.isStringSetType()
    val needsSerializer = !type.isPrimitiveType() && !isStringSet
    require(!(needsSerializer && serializer == null)) {
        "queryValues<T>() requires a DataStoreSerializer for non-primitive types " +
            "and collections other than Set<String>."
    }
    val getter: suspend (String) -> T? = { key ->
        when {
            type.classifier == String::class -> getString(key).first() as T?
            type.classifier == Int::class -> getInt(key).first() as T?
            type.classifier == Long::class -> getLong(key).first() as T?
            type.classifier == Float::class -> getFloat(key).first() as T?
            type.classifier == Double::class -> getDouble(key).first() as T?
            type.classifier == Boolean::class -> getBoolean(key).first() as T?
            type.classifier == Set::class && type.isStringSetType() -> getStringSet(key).first() as T?
            else -> get<T>(key).first()
        }
    }
    return DataStoreValueQuery(this, getter)
}

/**
 * Selects all keys matching the given pattern.
 */
fun TypeSafeDataStore.select(pattern: String): Flow<Set<String>> {
    return getAllKeys().map { keys ->
        keys.filter { key -> matchesPattern(key, pattern) }.toSet()
    }
}

/**
 * Selects all key/value pairs matching the given pattern.
 */
inline fun <reified T : Any> TypeSafeDataStore.selectValues(pattern: String): Flow<Map<String, T>> {
    return queryValues<T>()
        .filter { key -> matchesPattern(key, pattern) }
        .executeMap()
}

/**
 * Returns the number of keys in the DataStore.
 */
fun TypeSafeDataStore.count(): Flow<Int> {
    return getAllKeys().map { it.size }
}

/**
 * Returns the number of keys matching the given pattern.
 */
fun TypeSafeDataStore.count(pattern: String): Flow<Int> {
    return getAllKeys().map { keys ->
        keys.count { key -> matchesPattern(key, pattern) }
    }
}

/**
 * Checks if a key exists in the DataStore.
 */
fun TypeSafeDataStore.containsKey(key: String): Flow<Boolean> {
    return getAllKeys().map { keys ->
        key in keys
    }
}

/**
 * Gets all keys that start with the given prefix.
 */
fun TypeSafeDataStore.keysStartingWith(prefix: String): Flow<Set<String>> {
    return getAllKeys().map { keys ->
        keys.filter { it.startsWith(prefix) }.toSet()
    }
}

/**
 * Gets all keys that end with the given suffix.
 */
fun TypeSafeDataStore.keysEndingWith(suffix: String): Flow<Set<String>> {
    return getAllKeys().map { keys ->
        keys.filter { it.endsWith(suffix) }.toSet()
    }
}

/**
 * Gets all keys that contain the given substring.
 */
fun TypeSafeDataStore.keysContaining(substring: String): Flow<Set<String>> {
    return getAllKeys().map { keys ->
        keys.filter { it.contains(substring) }.toSet()
    }
}

/**
 * Groups keys by a custom grouping function.
 */
fun TypeSafeDataStore.groupByKeyPrefix(delimiter: Char = '_'): Flow<Map<String, List<String>>> {
    return getAllKeys().map { keys ->
        keys.groupBy { key ->
            key.substringBefore(delimiter, key)
        }
    }
}

/**
 * Groups keys by a custom selector.
 */
fun TypeSafeDataStore.groupByKey(keySelector: (String) -> String): Flow<Map<String, List<String>>> {
    return getAllKeys().map { keys ->
        keys.groupBy(keySelector)
    }
}

// ========== Value-Based Filtering ==========

/**
 * Filters keys by value content with automatic type inference.
 */
inline fun <reified T : Any> TypeSafeDataStore.filterByValue(noinline predicate: (key: String, value: T) -> Boolean): Flow<Set<String>> {
    return queryValues<T>()
        .filterValue(predicate)
        .executeKeys()
        .map { it.toSet() }
}

/**
 * Searches for keys with String values matching a pattern.
 */
fun TypeSafeDataStore.searchStringValues(searchText: String): Flow<Map<String, String>> {
    return queryValues<String>()
        .valueContains(searchText, ignoreCase = true)
        .executeMap()
}

/**
 * Filters values that equal the given value.
 */
fun <T : Any> DataStoreValueQuery<T>.valueEquals(value: T): DataStoreValueQuery<T> {
    return filterValue { _, current -> current == value }
}

/**
 * Filters values that are contained in the given collection.
 */
fun <T : Any> DataStoreValueQuery<T>.valueIn(values: Collection<T>): DataStoreValueQuery<T> {
    return filterValue { _, current -> current in values }
}

/**
 * Filters values that fall within the given range.
 */
fun <T : Comparable<T>> DataStoreValueQuery<T>.valueBetween(
    min: T? = null,
    max: T? = null,
): DataStoreValueQuery<T> {
    return filterValue { _, current ->
        val lowerOk = min?.let { current >= it } ?: true
        val upperOk = max?.let { current <= it } ?: true
        lowerOk && upperOk
    }
}

/**
 * Filters String values that contain the given text.
 */
fun DataStoreValueQuery<String>.valueContains(
    text: String,
    ignoreCase: Boolean = true,
): DataStoreValueQuery<String> {
    return filterValue { _, current -> current.contains(text, ignoreCase = ignoreCase) }
}

/**
 * Filters String values that match the given regex.
 */
fun DataStoreValueQuery<String>.valueMatches(regex: Regex): DataStoreValueQuery<String> {
    return filterValue { _, current -> regex.containsMatchIn(current) }
}

/**
 * Sorts values in ascending order for Comparable types.
 */
fun <T : Comparable<T>> DataStoreValueQuery<T>.sortByValueAscending(): DataStoreValueQuery<T> {
    return sortByValueAscending(Comparator { a, b -> a.compareTo(b) })
}

/**
 * Sorts values in descending order for Comparable types.
 */
fun <T : Comparable<T>> DataStoreValueQuery<T>.sortByValueDescending(): DataStoreValueQuery<T> {
    return sortByValueDescending(Comparator { a, b -> a.compareTo(b) })
}

/**
 * Checks if a key matches the given pattern.
 */
@PublishedApi
internal fun matchesPattern(
    key: String,
    pattern: String,
): Boolean {
    return when {
        !pattern.contains("*") -> key == pattern
        pattern.startsWith("*") && pattern.endsWith("*") -> {
            val infix = pattern.trim('*')
            key.contains(infix)
        }
        pattern.startsWith("*") -> {
            val suffix = pattern.trimStart('*')
            key.endsWith(suffix)
        }
        pattern.endsWith("*") -> {
            val prefix = pattern.trimEnd('*')
            key.startsWith(prefix)
        }
        else -> {
            // Complex pattern with * in the middle
            val parts = pattern.split("*")
            var index = 0
            for (part in parts) {
                val found = key.indexOf(part, index)
                if (found == -1) return false
                index = found + part.length
            }
            true
        }
    }
}

@PublishedApi
@OptIn(ExperimentalStdlibApi::class)
internal fun kotlin.reflect.KType.isStringSetType(): Boolean {
    if (classifier != Set::class) return false
    val arg = arguments.firstOrNull()?.type ?: return false
    return arg.classifier == String::class
}

@PublishedApi
@OptIn(ExperimentalStdlibApi::class)
internal fun kotlin.reflect.KType.isPrimitiveType(): Boolean {
    return when (classifier) {
        String::class,
        Int::class,
        Long::class,
        Float::class,
        Double::class,
        Boolean::class,
        -> true
        else -> false
    }
}
