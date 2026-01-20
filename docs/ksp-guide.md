# KSP Code Generation Guide

KMP DataStore provides a KSP (Kotlin Symbol Processing) processor that generates type-safe query builders and validators at compile time.

## Installation

### 1. Add KSP Plugin

```kotlin
// build.gradle.kts
plugins {
    id("com.google.devtools.ksp") version "2.2.0-2.0.2"
}
```

### 2. Add Dependencies

```kotlin
dependencies {
    // Annotations module (required for KSP)
    implementation("io.github.parkwoocheol:kmp-datastore-annotations:<version>")
    
    // KSP processor
    ksp("io.github.parkwoocheol:kmp-datastore-ksp:<version>")
}
```

### 3. For KMP Projects

```kotlin
// For Kotlin Multiplatform projects
dependencies {
    add("kspCommonMainMetadata", project(":kmp-datastore-ksp"))
}

// Ensure KSP runs before compilation
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

// Add generated sources to source set
kotlin.sourceSets.commonMain {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}
```

## Generated Code

### Query Builder (@DataStoreIndex)

For a class annotated with `@DataStoreIndex`:

```kotlin
@DataStoreIndex(properties = ["age", "email"])
@Serializable
data class User(
    val name: String,
    val age: Int,
    val email: String
)
```

The processor generates:

```kotlin
// UserQueryBuilder.kt (generated)

class UserQueryBuilder(private val dataStore: TypeSafeDataStore) {
    
    // For age (Int property)
    fun whereAgeEquals(value: Int): Flow<Map<String, User>>
    fun whereAgeBetween(min: Int, max: Int): Flow<Map<String, User>>
    fun whereAgeGreaterThan(value: Int): Flow<Map<String, User>>
    
    // For email (String property)
    fun whereEmailEquals(value: String): Flow<Map<String, User>>
    fun whereEmailContains(value: String, ignoreCase: Boolean = true): Flow<Map<String, User>>
    fun whereEmailStartsWith(prefix: String): Flow<Map<String, User>>
}

// Extension function
fun TypeSafeDataStore.queryUser(): UserQueryBuilder = UserQueryBuilder(this)
```

**Usage:**

```kotlin
// Find all users between 18 and 65
val adults = dataStore.queryUser()
    .whereAgeBetween(18, 65)
    .first()

// Find users with gmail addresses
val gmailUsers = dataStore.queryUser()
    .whereEmailContains("@gmail.com")
    .first()
```

### Schema Export (@DataStoreSchema)

For a class annotated with `@DataStoreSchema`:

```kotlin
@DataStoreSchema(version = 2, exportSchema = true)
@Serializable
data class UserSettings(
    val theme: String,
    val notifications: Boolean,
    val language: String?
)
```

The processor generates:

```json
// schemas/UserSettings-schema-v2.json (generated)
{
  "schemaVersion": 2,
  "className": "UserSettings",
  "packageName": "com.example.app",
  "generatedAt": "2026-01-16T10:00:00Z",
  "properties": [
    {
      "name": "theme",
      "type": "String",
      "nullable": false
    },
    {
      "name": "notifications",
      "type": "Boolean",
      "nullable": false
    },
    {
      "name": "language",
      "type": "String",
      "nullable": true
    }
  ]
}
```

### Validator (Validation Annotations)

For a class with validation annotations:

```kotlin
@Serializable
data class User(
    @NotBlank
    val name: String,
    
    @Min(0) @Max(150)
    val age: Int,
    
    @Pattern("[a-z]+@[a-z]+\\.[a-z]+")
    val email: String
)
```

The processor generates:

```kotlin
// UserValidator.kt (generated)

object UserValidator {
    fun validate(obj: User): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (obj.name.toString().isBlank()) {
            errors.add("name must not be blank")
        }
        
        if (obj.age < 0) {
            errors.add("age must be at least 0")
        }
        
        if (obj.age > 150) {
            errors.add("age must be at most 150")
        }
        
        if (!Regex("[a-z]+@[a-z]+\\.[a-z]+").matches(obj.email.toString())) {
            errors.add("email must match pattern: [a-z]+@[a-z]+\\.[a-z]+")
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.errors(errors)
        }
    }
}
```

**Usage:**

```kotlin
val user = User(name = "", age = 200, email = "invalid")
val result = UserValidator.validate(user)

if (result.isFailure) {
    result.getErrorMessages().forEach { error ->
        println("Validation error: $error")
    }
}
```

## Query Methods by Type

| Property Type | Generated Methods |
| :--- | :--- |
| `Int`, `Long`, `Float`, `Double` | `where{Property}Equals`, `where{Property}Between`, `where{Property}GreaterThan` |
| `String` | `where{Property}Equals`, `where{Property}Contains`, `where{Property}StartsWith` |
| `Boolean` | `where{Property}Equals` |
| Other types | `where{Property}Equals` |

## Best Practices

### 1. Index Only What You Query

```kotlin
// Good: Only index properties you'll actually query
@DataStoreIndex(properties = ["age"])
data class User(val name: String, val age: Int, val email: String)

// Avoid: Indexing everything
@DataStoreIndex(properties = ["name", "age", "email", "address", "phone"])
data class User(...)
```

### 2. Combine Annotations Appropriately

```kotlin
@SafeSerializable(version = 1, description = "User profile")
@DataStoreIndex(properties = ["age"])
@DataStoreSchema(version = 1)
@Serializable
data class User(
    @DataStoreKey("user_name") @NotBlank
    val name: String,
    
    @Min(0) @Max(150)
    val age: Int
)
```

### 3. Handle Validation Results

```kotlin
fun saveUser(user: User) {
    val validationResult = UserValidator.validate(user)
    
    when {
        validationResult.isSuccess -> {
            dataStore.put("user_${user.name}", user)
        }
        else -> {
            throw IllegalArgumentException(
                "Validation failed: ${validationResult.getErrorMessages().joinToString()}"
            )
        }
    }
}
```

## Troubleshooting

### Generated code not found

1. Make sure KSP plugin is applied
2. Run `./gradlew kspCommonMainKotlinMetadata` manually
3. Check `build/generated/ksp/` for generated files
4. Verify the source set includes generated directory

### Compilation errors in generated code

1. Ensure annotations module is in `implementation` (not `compileOnly`)
2. Check that all imports are available in the target source set
3. Update KSP version to match Kotlin version (2.2.0-2.0.2 for Kotlin 2.2.0)

### IDE doesn't recognize generated code

1. Sync Gradle project
2. Invalidate caches and restart IDE
3. Manually add `build/generated/ksp/metadata/commonMain/kotlin` to source roots
