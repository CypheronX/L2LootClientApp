---
alias: domain-layer-meldoc-md
title: Domain Layer Meldoc Md
---
---
title: Domain Layer
alias: domain-layer
parentAlias: architecture-overview
workflow: draft
---

## Purpose

The domain layer contains the application's core business logic with zero framework dependencies. It defines the contracts (interfaces) that the data layer implements and the models that flow through the entire application.

## Result Type

Error handling uses a custom `Result<D, E>` sealed interface (`domain/util/Result.kt`) instead of exceptions:

- `Result.Success(data: D)` — successful operation
- `Result.Failure(error: E)` — typed error

Extension functions: `map`, `onSuccess`, `onFailure`. All errors implement the `Error` marker interface. Concrete error types live in `DataError` with variants for `Local`, `Remote`, and `Network` failures.

This pattern ensures compile-time error handling at call sites and avoids exception-driven control flow.

## Domain Models

### MonstersDataModel.kt

Central file containing several related types:

- **`HPMultiplier`** — enum representing monster HP scaling variants (X025 through X6). Each value stores a `Double` multiplier with epsilon-based comparison for floating point safety.
- **`DropCategory`** — enum mapping integer category IDs to types: SPOIL (-1), ADENA (0), EQUIPMENT (1), MATERIALS (2).
- **`MonsterQueryParams`** — value object encapsulating all monster filter criteria (level range, chronicle, HP multipliers, server name, managed prices flag). Used by the repository to select the appropriate query strategy.
- **`DropItemInfo`** — individual drop record with calculated `averageIncome` (chance * price * quantity midpoint / 100).
- **`MonsterResult`** — aggregated monster with its drop list, computing `averageIncome` and `averageSpoilIncome` across all items.

### UserSettings.kt

Persisted user preferences: chronicle, level range, result limit, feature toggles (rift mobs, spoil income, managed prices, analytics tracking), HP multiplier selection, server choice, and engagement counters for consent/support dialog timing.

### ServerName.kt

Enum of supported game servers (e.g., REBORN_TEON, REBORN_FRANZ) with `serverKey` for API lookups and `displayName` for UI. Includes `fromKey()` factory and `DEFAULT` constant.

### SellableItem.kt

Represents a tradeable item with both `originalPrice` (bundled data) and `managedPrice` (remote server price). `getDisplayPrice()` selects the appropriate price based on context. Includes `matchesSearchWithAbbreviation()` for flexible search matching.

## Repository Interfaces

All repositories are defined as interfaces in `domain/repository/`:

| Interface | Responsibility |
|-----------|---------------|
| `MonsterRepository` | Query monsters with filtering; returns `Flow<Result<List<MonsterResult>>>` |
| `LoadDbDataRepository` | Bulk-load bundled JSON into SQLite; exposes `progress: StateFlow<Float>` |
| `SellableRepository` | CRUD for sellable items; fetch managed prices from remote |
| `UserSettingsRepository` | Read/write user preferences |
| `ExternalLinksRepository` | Fetch external links (Discord, etc.) |
| `UpdateCheckerRepository` | Check GitHub Releases for new versions |

Service interfaces in `domain/firebase/`:

| Interface | Responsibility |
|-----------|---------------|
| `AnalyticsService` | Track app events (opens, support clicks) |
| `FirebaseAuthService` | Obtain anonymous Firebase auth tokens |
