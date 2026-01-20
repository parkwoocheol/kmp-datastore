# KMP DataStore Overview

KMP DataStore is a Kotlin Multiplatform library that provides a type-safe and convenient DataStore wrapper.

## Key Features

- **Type-Safe Storage**: Using DataStoreSerializer with KType-based serialization
- **Kotlinx Serialization Optional**: No mandatory dependency on kotlinx.serialization
- **Query DSL**: Pattern matching, filtering, grouping operations
- **Multiplatform Support**: Android, iOS, Desktop (JVM)

## Module Structure

```
kmp-datastore/
├── kmp-datastore/     # Core library module
├── sample/            # Sample applications
│   ├── shared/        # Shared Compose UI
│   ├── androidApp/    # Android sample
│   ├── desktopApp/    # Desktop sample
│   └── iosApp/        # iOS sample
└── docs/              # Documentation
```

## Target Platforms

| Platform | Backend |
|----------|---------|
| Android | androidx.datastore.preferences |
| iOS | NSUserDefaults |
| Desktop | File-based storage |
