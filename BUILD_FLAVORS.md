# Build Flavors - Dev, Stage, and Prod

This project now supports three build flavors: **dev**, **stage**, and **prod**. Each flavor creates separate MSI installers that can be installed side-by-side on Windows without conflicts.

## Quick Start

### Building Production MSI
```bash
./gradlew clean
./gradlew packageReleaseMsi -Pbuildkonfig.flavor=prod
```

### Building Stage MSI (for testers)
```bash
./gradlew clean
./gradlew packageReleaseMsi -Pbuildkonfig.flavor=stage
```

### Building Development MSI
```bash
./gradlew clean  
./gradlew packageReleaseMsi -Pbuildkonfig.flavor=dev
```

> **Important:** Always `clean` before switching flavors to ensure BuildKonfig regenerates correctly.

The installers will be created in: `composeApp/build/compose/binaries/main-release/msi/`

## What's Different Between Flavors?

### Production (prod)
- **App Name**: "L2Loot"
- **Install Directory**: `%ProgramFiles%\L2Loot`
- **Start Menu**: "L2Loot"
- **Database Directory**: `%APPDATA%\L2Loot`
- **Upgrade UUID**: `a8e9c7c4-5f4d-4e8a-9c3b-8f2d1e4a5b6c`
- **Backend URLs**: Uses standard environment variables (no suffix)
- **Auto-Updater**: Enabled ✅
- **Debug Mode**: Disabled

### Stage (stage) - For Testers
- **App Name**: "L2Loot Stage"
- **Install Directory**: `%ProgramFiles%\L2Loot Stage`
- **Start Menu**: "L2Loot Stage"
- **Database Directory**: `%APPDATA%\L2Loot Stage`
- **Upgrade UUID**: `c0e1f9f6-7f6f-6f0c-be5d-0f4f3f6c7d8e` (different from dev and prod)
- **Backend URLs**: Uses standard environment variables (same as prod)
- **Auto-Updater**: Enabled ✅
- **Debug Mode**: Disabled

### Development (dev)
- **App Name**: "L2Loot Dev"
- **Install Directory**: `%ProgramFiles%\L2Loot Dev`
- **Start Menu**: "L2Loot Dev"
- **Database Directory**: `%APPDATA%\L2LootDev`
- **Upgrade UUID**: `b9f0d8d5-6f5e-5f9b-ad4c-9f3e2f5b6c7d` (different from stage and prod)
- **Backend URLs**: Uses `*_DEV` suffixed environment variables, falls back to prod URLs if not set
- **Auto-Updater**: Disabled ❌ (for development convenience)
- **Debug Mode**: Enabled

## Configuration

### local.properties Setup

Copy `local.properties.template` to `local.properties` and configure your endpoints:

```properties
# Production endpoints (required)
FIREBASE_ANALYTICS_URL=https://analytics-xxxxx.a.run.app
SELLABLE_ITEMS_URL=https://sellableitems-xxxxx.a.run.app
ANONYMOUS_AUTH_URL=https://anonymousauth-xxxxx.a.run.app
EXTERNAL_LINKS_URL=https://externallinks-xxxxx.a.run.app

# Development endpoints (optional - falls back to prod if not set)
FIREBASE_ANALYTICS_URL_DEV=https://analytics-dev-xxxxx.a.run.app
SELLABLE_ITEMS_URL_DEV=https://sellableitems-dev-xxxxx.a.run.app
ANONYMOUS_AUTH_URL_DEV=https://anonymousauth-dev-xxxxx.a.run.app
EXTERNAL_LINKS_URL_DEV=https://externallinks-dev-xxxxx.a.run.app
```

### Why Different Upgrade UUIDs?

Each flavor has a unique `upgradeUuid`. This is critical because:
- Windows uses the UUID to identify applications
- Different UUIDs allow dev and prod to be installed simultaneously
- Each installation maintains its own database and settings
- Windows won't try to "upgrade" one when installing the other

## Key Features

### Separate Databases
Dev and prod use completely separate database directories:
- Prod: `%APPDATA%\L2Loot\l2loot.db`
- Dev: `%APPDATA%\L2LootDev\l2loot.db`

This ensures your production data is never affected by development testing.

### Debug Logging
Logging levels are automatically configured based on the build flavor:

**Production (`IS_DEBUG = false`):**
- ❌ DEBUG logs: Disabled
- ❌ INFO logs: Disabled  
- ✅ WARNING logs: Enabled
- ✅ ERROR logs: Enabled
- ❌ HTTP request/response logging: Disabled

**Development (`IS_DEBUG = true`):**
- ✅ DEBUG logs: Enabled
- ✅ INFO logs: Enabled
- ✅ WARNING logs: Enabled
- ✅ ERROR logs: Enabled
- ✅ HTTP request/response logging: Enabled (full details)

This keeps production builds clean and performant while giving you detailed debugging information during development.

### Backend Configuration
The dev flavor automatically falls back to production URLs if dev-specific URLs aren't configured. This means:
- You can immediately build dev MSIs using prod backend
- When dev backend is ready, just add `*_DEV` variables to `local.properties`
- No code changes needed to switch between backends

### Identifying the Flavor at Runtime
The build flavor is available in code via:
```kotlin
import com.l2loot.Config

val flavor = Config.BUILD_FLAVOR   // "dev" or "prod"
val appName = Config.APP_NAME      // "L2Loot Dev" or "L2Loot"
val isDebug = Config.IS_DEBUG      // true for dev, false for prod
val dbDir = Config.DB_DIR_NAME     // "L2LootDev" or "L2Loot"
```

### Using Debug Logging in Code
```kotlin
import com.l2loot.Config

// Use the logger - it automatically filters by flavor
logger.debug("This only logs in dev builds")
logger.info("This only logs in dev builds")
logger.warn("This logs in both dev and prod")
logger.error("This logs in both dev and prod")

// Only use manual checks for expensive operations
if (Config.IS_DEBUG) {
    val expensiveData = computeExpensiveDebugInfo()
    logger.debug("Expensive debug info: $expensiveData")
}

// Direct println still needs manual check
if (Config.IS_DEBUG) {
    println("Debug information: $someValue")
}
```

**Best Practice:**
- ✅ Call `logger.debug()` and `logger.info()` directly - they're already filtered
- ✅ Use `if (Config.IS_DEBUG)` only for expensive computations before logging
- ✅ Use `if (Config.IS_DEBUG)` for direct `println()` statements
- ❌ Don't wrap simple logger calls - it's redundant

## Technical Details

### BuildKonfig
This implementation uses [BuildKonfig](https://github.com/yshrsmz/BuildKonfig) v0.17.1 to generate compile-time configuration. The configuration is defined in `shared/build.gradle.kts` and generates a `BuildKonfig` object that's available throughout the codebase.

### Gradle Tasks
- `packageMsiProd` - Builds production MSI installer
- `packageMsiDev` - Builds development MSI installer

These tasks configure the `buildkonfig.flavor` property before invoking the standard `packageReleaseMsi` task, which reads the property to apply the correct configuration.

### BuildKonfig Configuration Fields

Each flavor generates the following compile-time constants:

| Field | Type | Dev Value | Prod Value |
|-------|------|-----------|------------|
| `VERSION_NAME` | String | From version.properties | From version.properties |
| `BUILD_FLAVOR` | String | "dev" | "prod" |
| `APP_NAME` | String | "L2Loot Dev" | "L2Loot" |
| `DB_DIR_NAME` | String | "L2LootDev" | "L2Loot" |
| `IS_DEBUG` | Boolean | `true` | `false` |
| `ANALYTICS_URL` | String | From env (with _DEV suffix) | From env |
| `SELLABLE_ITEMS_URL` | String | From env (with _DEV suffix) | From env |
| `ANONYMOUS_AUTH_URL` | String | From env (with _DEV suffix) | From env |
| `EXTERNAL_LINKS_URL` | String | From env (with _DEV suffix) | From env |

## Troubleshooting

### Both apps show the same version
This is expected - both use the same version from `version.properties`. Only the app name and internal configuration differ.

### Can't install dev and prod at the same time
Ensure you're building with the correct tasks:
- `./gradlew packageMsiDev` (not `packageMsi`)
- `./gradlew packageMsiProd` (not `packageMsi`)

The different upgrade UUIDs are only applied when using the flavor-specific tasks.

### Dev build uses prod database
Check that the dev MSI was built with `packageMsiDev`. Verify by checking:
- The installer name should include "Dev"
- After installation, check the Start Menu for "L2Loot Dev"
- The app window title should show "L2Loot Dev"

### How to test locally during development
For local development with `./gradlew run`, the default flavor is "prod". To run with dev flavor during development:
```bash
./gradlew run -Pbuildkonfig.flavor=dev
```

## GitHub Actions CI/CD

The project uses a dual-workflow setup for automated builds:

### Production Builds (Tags)
Triggered when you push a version tag (e.g., `v1.2.0`):
- Runs the `build-prod` job
- Builds production MSI using `./gradlew packageMsiProd`
- Uploads to public releases repository
- Uses production backend URLs from GitHub secrets

```bash
git tag v1.2.0
git push origin v1.2.0
```

### Development Builds (Develop Branch)
Triggered when you push to the `develop` branch:
- Runs the `build-dev` job
- Builds development MSI using `./gradlew packageMsiDev`
- Creates a **pre-release** in the private repository
- Also uploads as artifact (7-day retention) for quick access
- Uses dev backend URLs from GitHub secrets (falls back to prod if not set)

```bash
git push origin develop
```

**Tag format:** `dev-v{version}-{commit-sha}` (e.g., `dev-v1.2.0-a1b2c3d`)
- Marked as pre-release to distinguish from production
- Suitable for auto-updater testing

### Required GitHub Secrets

Add these secrets to your repository (Settings → Secrets and variables → Actions):

**Production URLs (required):**
- `FIREBASE_ANALYTICS_URL`
- `SELLABLE_ITEMS_URL`
- `ANONYMOUS_AUTH_URL`
- `EXTERNAL_LINKS_URL`
- `FIREBASE_PROJECT_ID`
- `PUBLIC_REPO_TOKEN` (for releasing to public repository)

**Development URLs (optional - will fall back to prod if not set):**
- `FIREBASE_ANALYTICS_URL_DEV`
- `SELLABLE_ITEMS_URL_DEV`
- `ANONYMOUS_AUTH_URL_DEV`
- `EXTERNAL_LINKS_URL_DEV`

### Workflow Behavior

| Trigger | Job | Task | Output | Backend |
|---------|-----|------|--------|---------|
| Tag `v*` | build-prod | `packageMsiProd` | Public release | Production |
| Push to `develop` | build-dev | `packageMsiDev` | Private pre-release + artifact | Dev (or prod fallback) |
| Manual dispatch | Both jobs (based on branch) | Flavor-specific | Varies | Varies |

### Accessing Dev Builds

**Option 1: From Releases (Recommended)**
1. Go to **Releases** in your private repository
2. Look for pre-releases tagged `dev-v*`
3. Download the MSI from the release assets
4. These releases are permanent (suitable for auto-updater)

**Option 2: From Artifacts (Quick Access)**
1. Go to **Actions** tab in your repository
2. Click on the workflow run for the develop branch
3. Download the artifact under "Artifacts" section
4. Artifacts are retained for 7 days

## Auto-Updater Strategy

When implementing an auto-updater in the future, use this strategy:

### Repository Setup
- **Production builds** → Check public repository: `aleksbalev/L2LootClientAppReleases`
  - Release tags: `v1.2.0`, `v1.3.0`, etc.
  - Publicly accessible
  
- **Development builds** → Check private repository (this repo)
  - Release tags: `dev-v1.2.0-a1b2c3d`, `dev-v1.2.0-e5f6g7h`, etc.
  - Pre-releases only
  - Requires authentication (GitHub token)

### Configuration in Code

```kotlin
object UpdateConfig {
    val updateRepository = when (Config.BUILD_FLAVOR) {
        "prod" -> "aleksbalev/L2LootClientAppReleases" // Public
        "dev" -> "your-org/L2LootClientApp"            // Private
        else -> "aleksbalev/L2LootClientAppReleases"
    }
    
    val checkPrereleases = Config.BUILD_FLAVOR == "dev"
}
```

### Benefits
✅ Test auto-updater with dev builds before releasing to production  
✅ Dev builds check private repo, prod builds check public repo  
✅ Pre-release tags clearly distinguish dev from prod  
✅ Both use proper GitHub releases (not artifacts) for reliability  
✅ Direct MSI download URLs work with auto-updater libraries  

### Release Tag Examples
```
Production:  v1.2.0, v1.2.1, v1.3.0
Development: dev-v1.2.0-a1b2c3d, dev-v1.2.0-e5f6g7h, dev-v1.3.0-x9y8z7
```

## Migration from Old Configuration

The old `generateAppConfig` task has been removed. BuildKonfig now handles all compile-time configuration generation. The generated `BuildKonfig` class is available at:
- `com.l2loot.BuildKonfig`

This is wrapped by `Config` for convenience and maintains backward compatibility with existing code.

