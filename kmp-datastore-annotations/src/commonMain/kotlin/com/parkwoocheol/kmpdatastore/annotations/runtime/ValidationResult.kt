package com.parkwoocheol.kmpdatastore.annotations.runtime

/**
 * Result of a validation operation.
 *
 * Use [ValidationResult.Success] for successful validation,
 * or [ValidationResult.Error] / [ValidationResult.Errors] for failures.
 *
 * Example:
 * ```kotlin
 * val result = AnnotationValidator.validateSafeSerializable(user)
 * when (result) {
 *     is ValidationResult.Success -> println("Valid!")
 *     is ValidationResult.Error -> println("Error: ${result.message}")
 *     is ValidationResult.Errors -> result.messages.forEach { println("Error: $it") }
 * }
 * ```
 */
sealed class ValidationResult {
    /**
     * Validation passed successfully.
     */
    object Success : ValidationResult()

    /**
     * Validation failed with a single error message.
     */
    data class Error(val message: String) : ValidationResult()

    /**
     * Validation failed with multiple error messages.
     */
    data class Errors(val messages: List<String>) : ValidationResult()

    /**
     * Returns true if validation was successful.
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * Returns true if validation failed.
     */
    val isFailure: Boolean
        get() = this !is Success

    /**
     * Returns all error messages, or empty list if successful.
     */
    fun getErrorMessages(): List<String> =
        when (this) {
            is Success -> emptyList()
            is Error -> listOf(message)
            is Errors -> messages
        }

    companion object {
        /**
         * Creates a success result.
         */
        fun success(): ValidationResult = Success

        /**
         * Creates an error result with a single message.
         */
        fun error(message: String): ValidationResult = Error(message)

        /**
         * Creates an errors result with multiple messages.
         */
        fun errors(messages: List<String>): ValidationResult = if (messages.isEmpty()) Success else Errors(messages)
    }
}
