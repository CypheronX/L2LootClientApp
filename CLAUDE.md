# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

L2Loot is a **Kotlin Multiplatform** desktop application (Compose Desktop) for Lineage 2 Spoilers. It provides a monster database with drop rates, real-time price tracking, sellable items management, and automatic updates. Target platform is Windows (MSI installer).

## Build & Run Commands

```bash
# Run the app (default prod flavor)
./gradlew run

# Run with dev flavor (debug logging, separate database)
./gradlew run -Pbuildkonfig.flavor=dev

# Build MSI installer (always clean before switching flavors)
./gradlew clean packageReleaseMsi -Pbuildkonfig.flavor=prod
./gradlew clean packageReleaseMsi -Pbuildkonfig.flavor=stage
./gradlew clean packageReleaseMsi -Pbuildkonfig.flavor=dev

# Run tests
./gradlew test

# MSI output location
# composeApp/build/compose/binaries/main-release/msi/
```

**Important:** Always run `clean` before switching between build flavors so BuildKonfig regenerates correctly.

## Build Flavors (dev / stage / prod)

Three flavors configured via BuildKonfig (`-Pbuildkonfig.flavor=<flavor>`):

- **prod**: App name "L2Loot", debug off, auto-updater on, checks public release repo
- **stage**: App name "L2Loot Stage", debug on, auto-updater on, checks test repo
- **dev**: App name "L2Loot Dev", debug on, auto-updater off, uses `*_DEV` backend URLs (falls back to prod)

Each flavor has a unique Windows Upgrade UUID for side-by-side installation and a separate database directory under `%APPDATA%`.

Configuration is defined in `shared/build.gradle.kts` using BuildKonfig. Backend URLs come from `local.properties` (copy from `local.properties.template`). Version comes from `version.properties`.

Runtime access: `Config.BUILD_FLAVOR`, `Config.IS_DEBUG`, `Config.APP_NAME`, `Config.DB_DIR_NAME`.

## Architecture

### Module Structure

```
shared/          Kotlin Multiplatform - domain + data layers (commonMain/jvmMain)
composeApp/      Compose Desktop UI - features, viewmodels, design system (jvmMain)
updater/         Standalone auto-updater executable (separate Compose app)
```

### Layers (Clean Architecture)

**Domain** (`shared/.../domain/`): Interfaces, models, repository contracts, `Result<D, E>` sealed type for error handling.

**Data** (`shared/.../data/`): Repository implementations, Ktor networking, SQLDelight database, Firebase integration (auth + analytics), mappers.

**UI** (`composeApp/.../`): MVVM with Compose. ViewModels use `StateFlow` for state. Three feature screens: Explore (monster filtering), Sellable (item prices), Settings.

### Key Technologies & Patterns

- **DI**: Koin — modules in `shared/.../di/` (sharedModule, jvmModule) and `composeApp/.../di/AppModule.kt`
- **Database**: SQLDelight with JDBC SQLite. Schema files in `shared/src/commonMain/sqldelight/`. Migrations via `AfterVersion` callbacks in `SqlDriver.jvm.kt`. Current schema version: 3.
- **Networking**: Ktor with two HTTP clients (authenticated via Firebase token, unauthenticated) created in `HttpClientFactory.kt`
- **Navigation**: Jetpack Navigation `NavHost` with three routes (Explore, Sellable, Settings) + NavigationRail sidebar
- **State**: `StateFlow` + `viewModelScope` coroutines, `SharingStarted.WhileSubscribed(5000)`
- **Error handling**: `Result<D, E>` sealed interface with `Success`/`Failure` variants and extension functions (`map`, `onSuccess`, `onFailure`)
- **Query strategy**: `MonsterQueryStrategy` pattern selects optimal SQL query based on filter parameters

### Feature Module Convention

```
features/{name}/
  {Name}Screen.kt        # Composable UI
  {Name}ViewModel.kt     # State management
  {Name}Model.kt         # UI models (optional)
  components/             # Feature-specific components (optional)
```

### Repository Pattern

Interface in `domain/repository/` → Implementation in `data/repository/` → Registered in Koin as `singleOf(::FooImpl) bind Foo::class`.

### Data Resources

Monster, drop, and sellable item JSON data is bundled in `shared/src/commonMain/resources/data/` and loaded into SQLite on first run via `LoadDbDataRepositoryImpl`.

## Version Management

Version is in `version.properties` (versionMajor/Minor/Patch). Production releases are tagged `v1.x.x` and trigger CI. Stage/dev builds are tagged `stage-v{version}-{sha}`.

## CI/CD

GitHub Actions (`.github/workflows/main.yml`):
- **Tag push `v*`** → builds prod MSI, releases to public repo `CypheronX/L2LootClientAppReleases`
- **Push to `test` branch** → builds stage MSI, creates pre-release in test repo
- Runs on `windows-latest` with JDK 17

---

<!-- meldoc:begin -->
## Meldoc Documentation

This repository uses **Meldoc** for living, code-adjacent documentation.

- Documentation lives in `*.meldoc.md` files next to the code.
- Only documentation and metadata are synchronized.
- **Source code is never uploaded or modified automatically.**

Official Meldoc documentation:
https://public.meldoc.io/meldoc

Meldoc documentation is treated as **infrastructure**, not prose.

---

## Sources of truth (priority order)

1. `*.meldoc.md` files in this repository
2. MCP tools provided by Meldoc MCP servers:
   - **meldoc-cli** server: CLI tools via `meldoc mcp serve --cli-only`
   - **meldoc** server: npm proxy via `@meldocio/mcp-stdio-proxy`
3. This file
4. Public Meldoc docs (concepts only)

Do **not** guess structure or intent.

---

## Think before acting

Before any change, determine:
1. Text-only vs structural change
2. Single-file vs multi-file impact
3. Identifier / hierarchy / visibility changes

If unclear — **stop and ask**.

---

## Absolute rules

### Always
- Treat `alias` as a **stable identifier**, not a slug.
- Prefer **small, local, incremental changes**.
- Preserve existing structure and intent.
- Use MCP tools to inspect state before acting:
  - `docs_search`, `docs_get`
  - `cli_validate` before publishing

### Ask first
- Before touching multiple documents
- Before changing hierarchy or exposure
- Before renaming, moving, or deleting anything
- Before affecting published or public docs

### Never
- Never delete `*.meldoc.md` files
- Never invent frontmatter fields
- Never mass-rename aliases
- Never auto-resolve conflicts
- Never run destructive commands without dry-run

---

## File format rules

- Files **must** end with `*.meldoc.md`
- YAML frontmatter is mandatory and first

### Frontmatter

Required:
- `title`

Required for publish:
- `alias` (unique, kebab-case, stable)

Optional:
- `parentAlias` / `parent_alias`
- `workflow`: `draft | published`
- `visibility`: `visible | hidden`
- `exposure`: `inherit | private | unlisted | public`

Do not add other fields.

---

## Writing guidelines

- Clear, concise, factual
- No marketing or filler
- Explain intent and boundaries, not obvious code
- Prefer structure (headings, lists)

Bad documentation is worse than missing documentation.

---

## MCP tool usage

Meldoc provides two MCP servers with different tools:

### meldoc-cli Server Tools
Available via **meldoc-cli** MCP server (CLI tools):

**Read-only:**
- `cli_scan` - Scan repository for meldoc files
- `cli_status` - Show sync status
- `cli_validate` - Validate local state
- `cli_version` - Get CLI version

**State-changing (require confirm=true):**
- `cli_publish` - Publish documentation (requires `MELDOC_TOKEN`)
- `cli_pull` - Pull documentation (requires `MELDOC_TOKEN`)
- `cli_migrate` - Migrate files to add alias field
- `cli_track_*` - Track/untrack documents

### meldoc Server Tools
Available via **meldoc** npm proxy server (API tools):

**Read-only:**
- `docs_search` - Search documentation by query
- `docs_get` - Get document by ID or alias
- `docs_list` - List documents in workspace
- `docs_tree` - Get documentation tree structure
- `docs_links` / `docs_backlinks` - Get document links

**State-changing (require internal token):**
- `docs_update` - Update document directly
- `docs_create` - Create document directly
- `docs_delete` - Delete document directly

### Important: CLI Tools vs MCP Direct Tools

**CLI Tools** (`cli_publish`, `cli_pull`, `cli_migrate`):
- Metadata (title, alias, parentAlias, workflow, visibility, exposure) must be specified in **YAML frontmatter** of files
- Document title should **NOT** be included in markdown content (do not use H1 #)
- Title is specified via the `title` field in frontmatter
- Use `cli_validate` before publishing
- Use `cli_scan` for structural changes

**MCP Direct Tools** (`docs_update`, `docs_create`, `docs_delete`):
- Metadata must be passed as **separate parameters**, NOT through frontmatter
- If `contentMd` is provided with frontmatter, parameters from args take priority over frontmatter values
- Document title should **NOT** be included in `contentMd` (do not use H1 #)
- Title is specified via the `title` parameter
- Require authentication via `meldoc mcp auth login` or `MELDOC_ACCESS_TOKEN`

**Always:**
- Run `cli_validate` before publish
- Use `cli_publish` with `dryRun` unless told otherwise
- Treat publish/pull as **dangerous operations**

If unsure — do not execute tools.

---

## Recommended workflows

### Text-only
```
edit `*.meldoc.md`
meldoc publish
```

### Structural / multi-file
```
edit files
meldoc scan
meldoc validate
meldoc publish
```

### Missing aliases
```
meldoc migrate
meldoc validate
meldoc publish
```

---

## Conflicts
- Never auto-resolve
- Stop and ask

---

## Core principle

Meldoc documentation is **code-adjacent infrastructure**.

Correctness and sync safety
matter more than style or wording.
<!-- meldoc:end -->
