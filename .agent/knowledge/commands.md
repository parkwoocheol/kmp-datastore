# Commands

## Build Commands

```bash
# Full build
./gradlew build

# Build specific platform
./gradlew :kmp-datastore:desktopMainClasses
./gradlew :kmp-datastore:compileDebugKotlinAndroid
./gradlew :kmp-datastore:compileKotlinIosSimulatorArm64

# Run tests
./gradlew test
./gradlew :kmp-datastore:desktopTest
```

## Clean Commands

```bash
./gradlew clean
./gradlew :kmp-datastore:clean
```

## Publishing

```bash
# Publish to local maven
./gradlew publishToMavenLocal

# JitPack (automated via GitHub)
# - Create a release tag: git tag v0.1.0
# - Push tag: git push origin v0.1.0
```
