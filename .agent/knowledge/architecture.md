# Architecture

## Core Components

### DataStoreSerializer (Interface)

- KType-based serialization abstraction
- No dependency on specific serialization library
- Users can implement custom serializers

### TypeSafeDataStore (Main API)

- Primitive type methods: putInt, getString, etc.
- Object storage with serializer: put<T>, get<T>
- Constructor takes optional serializer

### PreferencesDataStore (Platform Abstraction)

- Expect/actual pattern for multiplatform
- Android: androidx.datastore.preferences
- iOS: NSUserDefaults
- Desktop: File-based

### QueryExtensions

- Key-based queries: select, contains, startsWith
- Value-based filtering: filterByValue<T>
- Grouping: groupByKeyPrefix, groupByKey

## Design Decisions

1. **KType-based Serialization**: Follows compose-webview's BridgeSerializer pattern for flexibility
2. **Optional Serializer**: Primitive types work without serializer, objects require one
3. **No SerializerRegistry**: Simplified design - serializer is passed to constructor
