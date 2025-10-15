# L2Loot

A Lineage 2 quality-of-life desktop application for Spoilers, built with Kotlin Multiplatform and Compose Desktop.

## Features

- 🎯 Monster database with drop rates
- 💰 Real-time Aynix price tracking
- 📊 Sellable items management
- 📈 Analytics tracking
- 🔄 Automatic updates

## Setup

### Prerequisites

- JDK 17 or higher
- Gradle 8.x (included via wrapper)

### Configuration

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/L2Loot.git
   cd L2Loot
   ```

2. **Configure Firebase (Required):**
   ```bash
   cp local.properties.template local.properties
   ```
   
   Edit `local.properties` and add your Firebase configuration:
   ```properties
   FIREBASE_FUNCTIONS_BASE_URL=https://your-project.cloudfunctions.net
   FIREBASE_PROJECT_ID=your-project-id
   ```
   
   > **Note:** For the public version, the maintainer will provide these values. If you're forking the project, you'll need to set up your own Firebase project.

3. **Build and run:**
   ```bash
   ./gradlew run
   ```

### Building Installer

**Windows:**
```bash
./gradlew packageMsi
```
or
```bash
./gradlew packageExe
```

The installer will be created in `composeApp/build/compose/binaries/main/`

## Documentation

- [Configuration Setup](docs/CONFIGURATION_SETUP.md) - How to configure Firebase URLs
- [Security Considerations](docs/SECURITY_CONSIDERATIONS_OPEN_SOURCE.md) - Security approach for open-source desktop apps

## Project Structure

```
L2Loot/
├── composeApp/          # Main desktop application
│   └── src/jvmMain/     # JVM-specific code (UI, etc.)
├── shared/              # Shared business logic
│   ├── src/commonMain/  # Cross-platform code
│   └── src/jvmMain/     # JVM-specific implementations
├── firebase-functions/  # Firebase Cloud Functions
├── docs/                # Documentation
└── local.properties     # Local configuration (not committed)
```

## Technologies

- **Kotlin Multiplatform** - Cross-platform code sharing
- **Compose Desktop** - Modern declarative UI
- **Firebase** - Authentication and real-time data
- **SQLDelight** - Type-safe SQL database
- **Ktor** - HTTP client
- **Koin** - Dependency injection

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Set up your own Firebase project and configure `local.properties`
4. Make your changes
5. Commit your changes (`git commit -m 'Add some amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## Security

This is an open-source desktop application. See [Security Considerations](docs/SECURITY_CONSIDERATIONS_OPEN_SOURCE.md) for details on our security approach.

**Important:** Firebase Function URLs are NOT included in the source code. They must be configured via `local.properties` or environment variables.

## License

[Your License Here]

## Support

- ☕ [Ko-fi](https://ko-fi.com/your-account)
- 🎉 [Patreon](https://patreon.com/your-account)

## Acknowledgments

- L2Aynix for pricing data
- Lineage 2 community

---

Built with ❤️ for the Lineage 2 Spoiler community
