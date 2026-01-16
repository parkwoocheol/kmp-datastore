package com.parkwoocheol.kmpdatastore.annotations

/**
 * Opt-in annotation indicating that an API requires a DataStoreSerializer.
 *
 * APIs marked with this annotation will show a warning at compile time
 * if used without opting in, helping developers understand that:
 * - A serializer must be configured in the TypeSafeDataStore constructor
 * - The API is for object serialization, not primitive types
 *
 * Example:
 * ```kotlin
 * // This will show a warning unless you opt-in
 * dataStore.put("user", user)  // Requires serializer
 *
 * // Opt-in to acknowledge the requirement
 * @OptIn(RequiresSerializer::class)
 * fun saveUser(user: User) {
 *     dataStore.put("user", user)
 * }
 * ```
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(
    message =
        "This API requires a DataStoreSerializer to be configured. " +
            "Provide a serializer in the TypeSafeDataStore constructor to use object storage.",
    level = RequiresOptIn.Level.WARNING,
)
@MustBeDocumented
annotation class RequiresSerializer
