package com.parkwoocheol.kmpdatastore.annotations.validation

/**
 * Validates that a string property matches the specified regular expression pattern.
 *
 * Example:
 * ```kotlin
 * @Serializable
 * data class User(
 *     @Pattern("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}")
 *     val email: String,
 *
 *     @Pattern("^[A-Z][a-z]+$")
 *     val name: String
 * )
 * ```
 *
 * @property regex The regular expression pattern to match against.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class Pattern(val regex: String)
