---
alias: database-schema-meldoc-md
title: Database Schema Meldoc Md
---
---
title: Database Schema
alias: database-schema
parentAlias: architecture-overview
workflow: draft
---

## Overview

The application uses **SQLDelight** with a JDBC SQLite driver. The database file is stored at `%APPDATA%/{DB_DIR_NAME}/l2loot.db`. Schema version is tracked via SQLite's `PRAGMA user_version`.

## Tables

### monsters

Primary data source for the Explore screen. Composite primary key on `(id, chronicle)` allows the same monster ID across different game chronicles.

| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER | Monster ID |
| name | TEXT | Monster name |
| level | INTEGER | Monster level |
| exp | INTEGER | Experience points |
| is_rift | INTEGER | 1 if rift mob |
| chronicle | TEXT | Game chronicle version |
| hp_multiplier | TEXT | HPMultiplier enum value (X1, X025, etc.) |

Index: `monsters_id_idx` on `id`.

### droplist

Drop records linking monsters to sellable items. Auto-increment `id` primary key (changed from composite key in migration 1 to support duplicate drops per mob).

| Column | Type | Notes |
|--------|------|-------|
| id | INTEGER | Auto-increment PK |
| mob_id | INTEGER | FK to monsters.id |
| item_id | INTEGER | FK to sellable_item.item_id |
| min | INTEGER | Minimum drop quantity |
| max | INTEGER | Maximum drop quantity |
| chance | REAL | Drop chance percentage |
| category | INTEGER | DropCategory enum value (-1=SPOIL, 0=ADENA, 1=EQUIP, 2=MAT) |
| chronicle | TEXT | Chronicle version |

Indexes on `mob_id`, `item_id`, `category`.

### sellable_item

Master item table with base prices.

| Column | Type | Notes |
|--------|------|-------|
| item_id | INTEGER | Primary key |
| key | TEXT | Item identifier string |
| name | TEXT | Display name |
| item_price | INTEGER | Base price (user-editable) |

### managed_prices

Server-specific prices fetched from the remote API. Composite primary key `(item_id, server_name)`.

| Column | Type | Notes |
|--------|------|-------|
| item_id | INTEGER | FK to sellable_item.item_id |
| server_name | TEXT | ServerName enum key |
| price | INTEGER | Managed price value |

### user_settings

Single-row table (id=1) for persisted preferences. See `UserSettings` domain model for field documentation.

## Query Strategy

Monster queries use `COALESCE` to prefer managed prices over original prices when the `is_managed_prices` flag is set. The four query variants in `Monsters.sq` correspond to the `MonsterQueryStrategy` implementations:

- Basic level range filtering
- Level range + HP multiplier filtering (using `IN` clause)
- Level range + rift mob inclusion
- Level range + HP multiplier + rift mob inclusion

Each query joins `monsters` → `droplist` → `sellable_item`, optionally joining `managed_prices`, and groups results by monster for the repository to aggregate.

## Migrations

Schema migrations are in `shared/src/commonMain/sqldelight/migrations/`:

| Version | File | Changes |
|---------|------|---------|
| 1 → 2 | `1.sqm` | Rebuilt droplist with auto-increment PK (was composite key). Added missing double spoil entries for specific mobs. |
| 2 → 3 | `2.sqm` | Renamed `aynix_prices` to `managed_prices` with multi-server support. Renamed `is_aynix_prices` to `is_managed_prices` in user_settings. |
| 3 → 4 | `3.sqm` | Added `show_spoil_income` column to user_settings. Fixed droplist entry for mob 22008. |

Additional Kotlin logic runs via `AfterVersion` callbacks in `SqlDriver.jvm.kt` when migrations require operations beyond pure SQL.

## Initialization

On first launch, `LoadDbDataRepositoryImpl` detects an empty database and bulk-loads three bundled JSON files (`monsters.json`, `droplist.json`, `sellable_items.json`) from `shared/src/commonMain/resources/data/`. Loading progress is reported to the UI via `StateFlow<Float>`.
