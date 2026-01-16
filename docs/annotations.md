# Annotations Guide

KMP DataStore provides a comprehensive annotation system for enhanced type safety, validation, and code generation.

## Installation

```kotlin
// build.gradle.kts (commonMain)
implementation("com.github.parkwoocheol:kmp-datastore-annotations:<version>")
```

## Available Annotations

### @SafeSerializable

Marks a class as safe for DataStore serialization with version tracking.

```kotlin
@SafeSerializable(version = 2, description = "User profile data")
@Serializable
data class User(val name: String, val age: Int)
```

**Parameters:**

- `version: Int` - Schema version for migration support (default: 1)
- `description: String` - Human-readable description (default: "")

### @DataStoreKey

Specifies a custom key name for DataStore storage.

```kotlin
@Serializable
data class User(
    @DataStoreKey("user_name", required = true)
    val name: String,
    
    @DataStoreKey("user_age")
    val age: Int
)
```

**Parameters:**

- `value: String` - Custom key name (default: property name)
- `required: Boolean` - Whether the value is required (default: false)

### @RequiresSerializer

Opt-in annotation for APIs that require a serializer.

```kotlin
// Shows warning if used without @OptIn
dataStore.put("user", user)  // Warning!

// Opt-in to acknowledge
@OptIn(RequiresSerializer::class)
fun saveUser(user: User) {
    dataStore.put("user", user)  // OK
}
```

## KSP Annotations (For Code Generation)

These annotations trigger code generation when used with the KSP processor.

### @DataStoreIndex

Generates type-safe query builders for the annotated class.

```kotlin
@DataStoreIndex(properties = ["age", "email"])
@Serializable
data class User(val name: String, val age: Int, val email: String)

// Generated: UserQueryBuilder class
// Generated: dataStore.queryUser() extension function
```

**Parameters:**

- `properties: Array<String>` - Properties to index for query generation
- `generateQueryBuilder: Boolean` - Whether to generate query builder (default: true)

**Generated Methods:**

- For all types: `whereXxxEquals(value)`
- For numeric types: `whereXxxBetween(min, max)`, `whereXxxGreaterThan(value)`
- For String: `whereXxxContains(value)`, `whereXxxStartsWith(prefix)`

### @DataStoreSchema

Generates schema documentation and migration helpers.

```kotlin
@DataStoreSchema(version = 2, exportSchema = true)
@Serializable
data class UserSettings(
    val theme: String,
    val notifications: Boolean
)

// Generated: schemas/UserSettings-schema-v2.json
```

**Parameters:**

- `version: Int` - Schema version number
- `exportSchema: Boolean` - Export schema to JSON (default: true)

## Validation Annotations (For Code Generation)

### @Min / @Max

Validates numeric range.

```kotlin
@Serializable
data class User(
    @Min(0) @Max(150)
    val age: Int
)
```

### @Pattern

Validates string against regex pattern.

```kotlin
@Serializable
data class User(
    @Pattern("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}")
    val email: String
)
```

### @NotBlank

Validates that string is not blank.

```kotlin
@Serializable
data class User(
    @NotBlank
    val name: String
)
```

**Generated Validator:**

```kotlin
// Generated: UserValidator object
val result = UserValidator.validate(user)

when (result) {
    is ValidationResult.Success -> println("Valid!")
    is ValidationResult.Error -> println(result.message)
    is ValidationResult.Errors -> result.messages.forEach { println(it) }
}
```

## Runtime Validation

For cases where KSP-generated validators are not available, use `AnnotationValidator`:

```kotlin
import com.parkwoocheol.kmpdatastore.annotations.runtime.AnnotationValidator
import com.parkwoocheol.kmpdatastore.annotations.runtime.ValidationResult

// Validate not null
val result = AnnotationValidator.validateNotNull(value, "fieldName")

// Validate not blank
val result = AnnotationValidator.validateNotBlank(string, "fieldName")

// Validate range
val result = AnnotationValidator.validateRange(value, min = 0, max = 150, "age")

// Validate pattern
val result = AnnotationValidator.validatePattern(email, "[a-z]+@[a-z]+", "email")

// Combine results
val combined = AnnotationValidator.combine(result1, result2, result3)
```

## Best Practices

1. **Use @SafeSerializable for all stored objects** - Helps with version tracking and migrations.

2. **Always provide descriptive names** - Use `@DataStoreKey` for clear, consistent key naming.

3. **Be specific with @DataStoreIndex** - Only index properties you'll actually query on.

4. **Combine with @Serializable** - All data classes should have both annotations.

```kotlin
@SafeSerializable(version = 1, description = "User profile with contact info")
@DataStoreIndex(properties = ["age"])
@Serializable
data class User(
    @DataStoreKey("user_name") @NotBlank
    val name: String,
    
    @Min(0) @Max(150)
    val age: Int,
    
    @Pattern("[a-z]+@[a-z]+\\.[a-z]+")
    val email: String
)
```
