---
alias: dependency-injection-meldoc-md
title: Dependency Injection Meldoc Md
---
---
title: Dependency Injection
alias: dependency-injection
parentAlias: architecture-overview
workflow: draft
---

## Overview

The application uses **Koin** for dependency injection, organized into three modules that mirror the architectural layers.

## Module Hierarchy

```
appModule (composeApp)
  includes --> jvmModule (shared/jvmMain)
  includes --> sharedModule (shared/commonMain)
```

`initKoin` is called once from `main.kt` with `appModule`, which transitively includes the other modules.

## sharedModule

Defined in `shared/.../di/Modules.kt`. Provides the core infrastructure:

- **Database**: `createDatabase(DriverFactory)` singleton
- **Logger**: `KermitLogger` singleton
- **HTTP clients**: Two `HttpClient` instances distinguished by named qualifiers:
  - `named("unauthenticated")` — no auth header
  - `named("authenticated")` — injects Firebase Bearer token
- **Repositories**: All five repository implementations bound to their interfaces via `singleOf(::Impl) bind Interface::class`

## jvmModule

Defined in `shared/.../di/JvmModule.kt`. Provides JVM-specific implementations:

- **HttpEngine**: `OkHttp.create()` for Ktor
- **AnalyticsService**: `AnalyticsServiceImpl` singleton
- **UpdateCheckerRepository**: `UpdateCheckerRepositoryImpl` singleton
- **FirebaseAuthService**: `FirebaseAuthServiceImpl` singleton
- **FileLogger**: Conditional on `Config.IS_DEBUG` — writes to `l2loot-debug.log` in the app data directory

## appModule

Defined in `composeApp/.../di/AppModule.kt`. Provides the presentation layer:

- **DriverFactory**: Singleton for SQLDelight driver creation
- **ViewModels**: `MainViewModel`, `ExploreViewModel`, `SellableViewModel`, `SettingsViewModel` — registered via `viewModelOf()`
- Includes `jvmModule` (which includes `sharedModule`)

## Conventions

- Repository implementations are always **singletons** (`singleOf`) to share database connections and HTTP clients.
- ViewModels are scoped per-navigation via `koinViewModel()` in Composables.
- Named qualifiers distinguish multiple instances of the same type (HTTP clients).
- Constructor injection is used throughout — no service locator pattern except in `main.kt` for bootstrapping.
