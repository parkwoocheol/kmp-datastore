package com.parkwoocheol.kmpdatastore.annotations

/**
 * Specifies a custom key name for DataStore storage.
 *
 * Use this annotation to:
 * - Define custom key names different from property names
 * - Mark properties as required
 * - Improve key naming consistency
 *
 * Example:
 * ```kotlin
 * @Serializable
 * data class User(
 *     @DataStoreKey("user_name", required = true)
 *     val name: String,
 *
 *     @DataStoreKey("user_age")
 *     val age: Int
 * )
 * ```
 *
 * @property value Custom key name. If empty, uses the property name.
 * @property required Whether this key is required (cannot be null/empty).
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class DataStoreKey(
    /**
     * Custom key name for storage.
     * If empty, the property name will be used as the key.
     */
    val value: String = "",
    /**
     * Whether this key is required.
     * If true, validation will fail when the value is null or empty.
     */
    val required: Boolean = false,
)
