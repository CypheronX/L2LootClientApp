# L2Loot

A Lineage 2 quality-of-life desktop application for Spoilers, built with Kotlin Multiplatform and Compose Desktop.

## Features

- 🎯 Monster database with drop rates
- 💰 Real-time price tracking
- 📊 Sellable items management
- 🔄 Automatic updates

## Quick Start

### Prerequisites
- JDK 17 or higher

### Setup

1. Clone and configure:
   ```bash
   git clone https://github.com/your-username/L2Loot.git
   cd L2Loot
   cp local.properties.template local.properties
   ```

2. Edit `local.properties` with Firebase configuration:
   ```properties
   FIREBASE_FUNCTIONS_BASE_URL=https://your-project.cloudfunctions.net
   FIREBASE_PROJECT_ID=your-project-id
   ```

3. Run:
   ```bash
   ./gradlew run
   ```

### Build Installer

```bash
./gradlew clean packageReleaseMsi -Pbuildkonfig.flavor=prod
```

Output: `composeApp/build/compose/binaries/main-release/msi/`

See [BUILD_FLAVORS.md](BUILD_FLAVORS.md) for dev/prod build options.

## Technologies

Kotlin Multiplatform • Compose Desktop • Firebase • SQLDelight • Ktor • Koin

## Documentation

- [Build Flavors](BUILD_FLAVORS.md)
- [Configuration Setup](docs/CONFIGURATION_SETUP.md)
- [Security Considerations](docs/SECURITY_CONSIDERATIONS_OPEN_SOURCE.md)

## License

[Your License Here]

## Support

- ☕ [Ko-fi](https://ko-fi.com/cypheron)
- 🎉 [Patreon](https://patreon.com/Cypheron?utm_medium=unknown&utm_source=join_link&utm_campaign=creatorshare_creator&utm_content=copyLink)

## Acknowledgments

- Aynix for pricing data
- Lineage 2 community

---

Built with ❤️ for the Lineage 2 Spoiler community
