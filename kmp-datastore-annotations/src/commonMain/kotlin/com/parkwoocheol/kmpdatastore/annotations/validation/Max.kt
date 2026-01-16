package com.parkwoocheol.kmpdatastore.annotations.validation

/**
 * Validates that a numeric property is at most the specified maximum value.
 *
 * Example:
 * ```kotlin
 * @Serializable
 * data class User(
 *     @Max(150)
 *     val age: Int,
 *
 *     @Max(100)
 *     val percentage: Int
 * )
 * ```
 *
 * @property value The maximum allowed value (inclusive).
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class Max(val value: Long)
