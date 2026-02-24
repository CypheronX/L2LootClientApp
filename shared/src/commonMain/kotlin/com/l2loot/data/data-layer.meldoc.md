---
alias: data-layer-meldoc-md
title: Data Layer Meldoc Md
---
---
title: Data Layer
alias: data-layer
parentAlias: architecture-overview
workflow: draft
---

## Purpose

The data layer implements the domain repository interfaces using concrete technologies: SQLDelight for local persistence, Ktor for HTTP networking, and Firebase for authentication and analytics.

## Repository Implementations

### MonsterRepositoryImpl

Uses the **Strategy pattern** to select the optimal SQL query based on active filters. Four strategies exist:

| Strategy | HP Filter | Rift Mobs |
|----------|-----------|-----------|
| `BasicMonsterQueryStrategy` | No | Excluded |
| `HPFilteredMonsterQueryStrategy` | Yes | Excluded |
| `RiftIncludedMonsterQueryStrategy` | No | Included |
| `HPFilteredRiftIncludedMonsterQueryStrategy` | Yes | Included |

`MonsterQueryStrategySelector` picks the strategy based on `MonsterQueryParams`. Each strategy executes a different SQLDelight query optimized for its filter combination, avoiding dynamic query construction at runtime.

Results are grouped by monster, mapped to `MonsterResult` domain objects, and sorted by average income descending.

### LoadDbDataRepositoryImpl

Handles first-run database population:

1. Reads three bundled JSON files from `resources/data/` (monsters, droplist, sellable_items)
2. Deserializes via kotlinx.serialization
3. Bulk-inserts into SQLite in batches of ~100 using transactions
4. Reports progress via `MutableStateFlow<Float>` for the loading screen

The `isDatabaseEmpty()` check prevents re-loading on subsequent launches.

### SellableRepositoryImpl

Manages item pricing from two sources:

- **Local prices**: Stored in `sellable_item.item_price`, editable by the user
- **Managed prices**: Fetched from a remote endpoint per server, cached in `managed_prices` table

Managed prices are polled with a 1-hour cache interval. A `forceRefresh` parameter bypasses the cache. Server-specific prices are stored with composite key `(item_id, server_name)`.

### UserSettingsRepositoryImpl

Thin wrapper around SQLDelight queries. Initializes default settings on first run. HP multipliers are stored as comma-separated strings and parsed back to enum sets. Settings are exposed as `Flow` for reactive UI updates.

### UpdateCheckerRepositoryImpl

Queries the GitHub Releases API for the configured repository (`Config.GITHUB_RELEASE_REPO`). Parses semantic versions from tag names supporting two formats: `v1.2.3` (prod) and `stage-v1.2.3-abc123` (stage). Compares against the current version and returns `UpdateInfo` with download URLs for both MSI and ZIP assets.

### ExternalLinksRepositoryImpl

Fetches external links (e.g., Market Owners Discord) from a remote endpoint. Simple HTTP GET with JSON deserialization.

## Networking

### HttpClientFactory

Creates two Ktor `HttpClient` instances registered via Koin qualifiers:

- **Unauthenticated** (`named("unauthenticated")`) — for public endpoints
- **Authenticated** (`named("authenticated")`) — adds Firebase Bearer token via request interceptor

Both clients share: JSON content negotiation (`ignoreUnknownKeys = true`), 20-second timeout, and conditional HTTP logging (enabled only when `Config.IS_DEBUG` is true).

The auth interceptor skips token injection for the anonymous auth and analytics endpoints themselves to avoid circular dependencies.

## Firebase Integration

### FirebaseAuthServiceImpl

Manages anonymous authentication:

- Calls `ANONYMOUS_AUTH_URL` to obtain an ID token
- Caches token to a `.properties` file in the app data directory
- Tracks token expiration and app version to invalidate stale tokens
- The authenticated HTTP client calls `getIdToken()` before each request

### AnalyticsServiceImpl

Tracks user events (app opens, support link clicks):

- Sends events to `ANALYTICS_URL` with user GUID and event name
- Respects the user's tracking opt-in preference
- Uses the unauthenticated HTTP client

## Database Driver (JVM)

`SqlDriver.jvm.kt` creates the JDBC SQLite driver with performance pragmas:

- **WAL journal mode** for concurrent read/write
- **busy_timeout = 5000ms** to handle lock contention
- **synchronous = NORMAL** for balanced durability/performance

Database file location: `%APPDATA%/{Config.DB_DIR_NAME}/l2loot.db`

Migrations are handled via SQLDelight's `AfterVersion` callbacks, which run additional Kotlin logic after each `.sqm` migration file executes.
