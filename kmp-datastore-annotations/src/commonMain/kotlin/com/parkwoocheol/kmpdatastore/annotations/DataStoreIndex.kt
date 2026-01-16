package com.parkwoocheol.kmpdatastore.annotations

/**
 * Marks a class for KSP query builder generation.
 *
 * When applied to a data class, the KSP processor will generate:
 * - Type-safe query builder class (`{ClassName}QueryBuilder`)
 * - Extension function on TypeSafeDataStore (`query{ClassName}()`)
 * - Property-specific query methods (whereXxxEquals, whereXxxBetween, etc.)
 *
 * Example:
 * ```kotlin
 * @DataStoreIndex(properties = ["age", "email"])
 * @Serializable
 * data class User(val name: String, val age: Int, val email: String)
 *
 * // Generated usage:
 * val adults = dataStore.queryUser()
 *     .whereAgeBetween(18, 65)
 *     .execute()
 * ```
 *
 * @property properties Properties to index for query generation.
 * @property generateQueryBuilder Whether to generate the query builder class.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class DataStoreIndex(
    /**
     * Properties to index for query generation.
     * Only these properties will have generated query methods.
     *
     * Example: ["age", "email"]
     */
    val properties: Array<String> = [],
    /**
     * Whether to generate a type-safe query builder.
     * Set to false if you only want schema metadata without code generation.
     */
    val generateQueryBuilder: Boolean = true,
)
