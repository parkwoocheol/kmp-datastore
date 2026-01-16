package com.parkwoocheol.kmpdatastore.annotations.validation

/**
 * Validates that a string property is not blank (not null, empty, or whitespace only).
 *
 * Example:
 * ```kotlin
 * @Serializable
 * data class User(
 *     @NotBlank
 *     val name: String,
 *
 *     @NotBlank
 *     val email: String
 * )
 * ```
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class NotBlank
