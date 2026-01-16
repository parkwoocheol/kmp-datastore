package com.parkwoocheol.kmpdatastore.annotations.validation

/**
 * Validates that a numeric property is at least the specified minimum value.
 *
 * Example:
 * ```kotlin
 * @Serializable
 * data class User(
 *     @Min(0)
 *     val age: Int,
 *
 *     @Min(1)
 *     val score: Long
 * )
 * ```
 *
 * @property value The minimum allowed value (inclusive).
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class Min(val value: Long)
