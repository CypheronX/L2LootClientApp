---
alias: features-meldoc-md
title: Features Meldoc Md
---
---
title: UI Features
alias: ui-features
parentAlias: architecture-overview
workflow: draft
---

## Overview

The presentation layer uses **Compose Desktop** with an MVVM pattern. Each feature is a self-contained module with a Screen composable, ViewModel, and optional model/components. ViewModels expose `StateFlow` for state and handle events via sealed interfaces.

## Navigation

`App.kt` defines a `NavHost` with three top-level routes (`Explore`, `Sellable`, `Settings`), selected via a `NavigationRail` sidebar. The navigation rail displays SVG icons loaded from compose resources, the app logo (linking to lineage2wiki.org), the version string, and an update notification badge on the Settings icon when an update is available.

## Feature Modules

### Explore (`features/explore/`)

The primary feature. Lets users search the monster database by level range, chronicle, HP multiplier, and rift mob inclusion.

**State** (`ExploreScreenState`): monster list, all active filter values, loading flag, HP multiplier selection, server name. Converts to `MonsterQueryParams` via `toMonsterQueryParams()`.

**Events** (`ExploreScreenEvent`): chronicle/level/limit changes, rift toggle, spoil income toggle, HP multiplier toggle, explore trigger.

**ViewModel**: Loads monsters from `MonsterRepository`, debounces level input changes, syncs filter preferences to `UserSettingsRepository`. Applies a minimum loading delay for smooth shimmer transitions.

**UI**: Search form with dropdowns and inputs at the top, results in a 3-column lazy grid of `MonsterCard` components. Each card shows monster name (clickable wiki link), level, average income, HP multiplier, and a spoil materials table with item names, quantities, and chances. Shimmer loading skeletons display during queries.

### Sellable (`features/sellable/`)

Manages sellable item prices. Users can set custom prices or switch to server-managed prices.

**State** (`SellableScreenState`): items list, managed prices toggle, per-item price map, search value, server selection, loading state, last price update time. Items are split into two columns for display. Search supports abbreviation matching.

**Events**: price change, price source toggle, search input, server change.

**ViewModel**: Loads items with prices from `SellableRepository`, debounces individual price edits, fetches managed prices when toggled on, handles server switches with forced refresh, loads Market Owners Discord link from `ExternalLinksRepository`.

**UI**: Toggle switch for managed vs. custom prices, server selector dropdown (visible when managed prices enabled), search bar, two-column card grid. Each `SellableItem` card shows item image, name, and an editable price field.

### Settings (`features/setting/`)

Application preferences and about information.

**State**: `trackUserEvents` boolean.

**Events**: `SetTracking` toggle.

**UI sections**:
- Share usage data toggle (opt-in analytics)
- Support links (Patreon, Ko-fi)
- Report Bug links (email, Discord, GitHub Issues)
- About info (version, copyright, license, GitHub)

## Shared Presentation Components

Located in `presentation/`:

- **AppDialogManager** — orchestrates consent and support dialogs based on engagement counters and time thresholds in `UserSettings`
- **AppLoadingScreen** — startup progress bar shown during database initialization
- **AppNavigationRail** — sidebar navigation with screen icons and update badge
- **SupportDialog** — Ko-fi/Patreon links with reminder scheduling
- **TrackingConsentDialog** — analytics opt-in prompt
- **UpdateNotification** — bottom-right popup showing available update with download/release-notes buttons, auto-dismisses after 10 seconds

## Design System

Located in `designsystem/`:

- **Theme**: Material 3 dark color scheme with custom `ColorFamily` groupings and a `Spacing` system via `CompositionLocal`
- **Typography**: Lato (body) and Roboto (display) font families
- **Components**: `SearchInput`, `SelectInput`, `GlobalDialog`, `NoResultsFound`, shimmer utilities
- **Modifiers**: `clearFocusOnClick` for keyboard dismissal
