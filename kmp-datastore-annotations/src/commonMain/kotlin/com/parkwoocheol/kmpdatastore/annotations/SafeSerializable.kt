package com.parkwoocheol.kmpdatastore.annotations

/**
 * Marks a class as safe for DataStore serialization.
 *
 * This annotation provides metadata for:
 * - Schema versioning for migration support
 * - Documentation and description
 * - Runtime validation (optional)
 *
 * Example:
 * ```kotlin
 * @SafeSerializable(version = 2, description = "User profile data")
 * @Serializable
 * data class User(val name: String, val age: Int)
 * ```
 *
 * @property version Schema version for migration support. Increment when making breaking changes.
 * @property description Human-readable description of this data class.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class SafeSerializable(
    /**
     * Schema version for migration support.
     * Increment this value when making breaking changes to the data structure.
     */
    val version: Int = 1,
    /**
     * Human-readable description of this data class.
     * Used for documentation and debugging purposes.
     */
    val description: String = "",
)
