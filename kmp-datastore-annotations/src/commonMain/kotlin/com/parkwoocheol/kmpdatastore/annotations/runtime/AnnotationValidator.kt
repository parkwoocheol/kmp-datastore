package com.parkwoocheol.kmpdatastore.annotations.runtime

/**
 * Runtime annotation validator for DataStore objects.
 *
 * Provides validation utilities for objects annotated with DataStore annotations.
 * This is primarily used for runtime validation of [@SafeSerializable] annotated classes.
 *
 * Example:
 * ```kotlin
 * @SafeSerializable(version = 1, description = "User data")
 * @Serializable
 * data class User(val name: String, val age: Int)
 *
 * // Validate using annotation info (if available at runtime)
 * val result = AnnotationValidator.validate(user)
 * ```
 *
 * Note: For compile-time validation (Min, Max, Pattern, NotBlank),
 * use the KSP processor which generates validator classes.
 */
object AnnotationValidator {
    /**
     * Validates an object for basic constraints.
     *
     * This is a simple validation that can be extended.
     * For annotation-based validation, use the generated validators from KSP.
     *
     * @param obj The object to validate
     * @return ValidationResult indicating success or failure
     */
    fun validate(obj: Any): ValidationResult {
        // Basic non-null validation
        return ValidationResult.success()
    }

    /**
     * Validates that a value is not null.
     *
     * @param value The value to check
     * @param fieldName The name of the field for error messages
     * @return ValidationResult indicating success or failure
     */
    fun validateNotNull(
        value: Any?,
        fieldName: String,
    ): ValidationResult {
        return if (value != null) {
            ValidationResult.success()
        } else {
            ValidationResult.error("$fieldName must not be null")
        }
    }

    /**
     * Validates that a string is not blank.
     *
     * @param value The string to check
     * @param fieldName The name of the field for error messages
     * @return ValidationResult indicating success or failure
     */
    fun validateNotBlank(
        value: String?,
        fieldName: String,
    ): ValidationResult {
        return if (!value.isNullOrBlank()) {
            ValidationResult.success()
        } else {
            ValidationResult.error("$fieldName must not be blank")
        }
    }

    /**
     * Validates that a number is within range.
     *
     * @param value The number to check
     * @param min The minimum allowed value
     * @param max The maximum allowed value
     * @param fieldName The name of the field for error messages
     * @return ValidationResult indicating success or failure
     */
    fun validateRange(
        value: Long,
        min: Long,
        max: Long,
        fieldName: String,
    ): ValidationResult {
        return when {
            value < min -> ValidationResult.error("$fieldName must be at least $min, got $value")
            value > max -> ValidationResult.error("$fieldName must be at most $max, got $value")
            else -> ValidationResult.success()
        }
    }

    /**
     * Validates that a string matches a pattern.
     *
     * @param value The string to check
     * @param pattern The regex pattern to match
     * @param fieldName The name of the field for error messages
     * @return ValidationResult indicating success or failure
     */
    fun validatePattern(
        value: String,
        pattern: String,
        fieldName: String,
    ): ValidationResult {
        return if (Regex(pattern).matches(value)) {
            ValidationResult.success()
        } else {
            ValidationResult.error("$fieldName must match pattern $pattern")
        }
    }

    /**
     * Combines multiple validation results.
     *
     * @param results The validation results to combine
     * @return Combined ValidationResult
     */
    fun combine(vararg results: ValidationResult): ValidationResult {
        val errors = results.flatMap { it.getErrorMessages() }
        return ValidationResult.errors(errors)
    }
}
