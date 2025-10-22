[![License: BUSL 1.1 (3yr)](https://img.shields.io/badge/License-BUSL%201.1%20(3yr)-blue.svg)](https://mariadb.com/bsl1-1-text)

# L2Loot by Cypheron

**Lineage 2 Quality-of-Life app for Spoilers**

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

## Support

- ☕ [Ko-fi](https://ko-fi.com/cypheron)
- 🎉 [Patreon](https://patreon.com/Cypheron?utm_medium=unknown&utm_source=join_link&utm_campaign=creatorshare_creator&utm_content=copyLink)

## Acknowledgments

- Aynix for pricing data
- Lineage 2 community

---

Built with ❤️ for the Lineage 2 Spoiler community
