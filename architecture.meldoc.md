---
alias: architecture-meldoc-md
title: Architecture Meldoc Md
---
---
alias: architecture-overview
title: Architecture Overview
---

## Project Structure

L2Loot is a Kotlin Multiplatform desktop application built with Compose Desktop, targeting Windows. The codebase is organized into three Gradle modules:

| Module | Role | Source sets |
|--------|------|------------|
| `shared` | Domain + Data layers (business logic, persistence, networking) | `commonMain`, `jvmMain` |
| `composeApp` | Presentation layer (Compose UI, ViewModels, navigation) | `jvmMain` |
| `updater` | Standalone auto-updater executable | `jvmMain` |

## Layered Architecture

The app follows **Clean Architecture** with unidirectional dependency flow:

```
composeApp (UI) --> shared/domain (contracts) <-- shared/data (implementations)
```

- **Domain layer** defines repository interfaces, models, and a `Result<D, E>` sealed type. It has zero framework dependencies.
- **Data layer** implements repositories using SQLDelight (local DB), Ktor (HTTP), and Firebase (auth + analytics).
- **UI layer** uses MVVM: ViewModels expose `StateFlow`, Composables observe and render.

## Dependency Injection

Koin manages the object graph across three modules:

1. **sharedModule** (`shared/di/Modules.kt`) — database, logger, HTTP clients, repositories
2. **jvmModule** (`shared/di/JvmModule.kt`) — platform-specific: OkHttp engine, Firebase services, file logger
3. **appModule** (`composeApp/di/AppModule.kt`) — ViewModels, DriverFactory; includes jvmModule

Initialization happens in `main.kt` via `initKoin { modules(appModule) }`.

## Build Configuration

Three build flavors (prod, stage, dev) are managed by **BuildKonfig** in `shared/build.gradle.kts`. Each flavor produces a separate Windows MSI installer with its own app name, database directory, upgrade UUID, and backend URLs.

Configuration values are read at compile time from `local.properties` and `version.properties`, then exposed via the generated `Config` object.

## Navigation

The app uses Jetpack Navigation's `NavHost` with three top-level routes — Explore, Sellable, Settings — accessible through a `NavigationRail` sidebar. Each route maps to a feature module containing a Screen composable and a ViewModel.

## Data Flow

1. **Startup**: `MainViewModel` triggers Firebase anonymous auth, loads sellable items, and checks for updates.
2. **Database init**: On first run, `LoadDbDataRepository` reads bundled JSON resources and bulk-inserts into SQLite.
3. **Querying**: `MonsterRepository` uses a Strategy pattern to select the optimal SQL query based on active filters (level range, HP multiplier, rift mobs).
4. **Price management**: `SellableRepository` supports both local custom prices and server-managed prices fetched from a remote endpoint with 1-hour cache.
5. **Updates**: `UpdateCheckerRepository` queries GitHub Releases API, compares semantic versions, and surfaces update info. The `updater` module handles download, extraction, and installation.
