# Contributing to KMP DataStore

Thank you for your interest in contributing to KMP DataStore!

## Getting Started

1. Fork the repository
2. Clone your fork
3. Create a feature branch: `git checkout -b feature/your-feature`
4. Make your changes
5. Run tests: `./gradlew test`
6. Commit your changes: `git commit -m 'Add your feature'`
7. Push to your fork: `git push origin feature/your-feature`
8. Create a Pull Request

## Development Setup

### Requirements

- JDK 17+ (required by AGP 8.x)
- Android SDK for Android builds/samples
- Xcode + Command Line Tools for iOS builds/samples

```bash
# Clone the repository
git clone https://github.com/parkwoocheol/kmp-datastore.git
cd kmp-datastore

# Build the project
./gradlew build

# Run tests
./gradlew test
```

## Code Style

- Follow Kotlin coding conventions
- Use 4 spaces for indentation
- Add KDoc comments for public APIs

## Reporting Issues

When reporting issues, please include:

- Kotlin version
- Platform (Android, iOS, Desktop)
- Minimal reproduction steps
- Expected vs actual behavior

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
