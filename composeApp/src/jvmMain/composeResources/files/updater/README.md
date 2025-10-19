# Updater JAR

This directory should contain the `L2LootUpdater.jar` file.

## Building the Updater

To build the updater JAR and copy it to this location, run:

```bash
./gradlew :updater:packageReleaseUberJarForCurrentOS copyUpdaterToResources
```

Or on Windows PowerShell:

```powershell
.\gradlew.bat :updater:packageReleaseUberJarForCurrentOS copyUpdaterToResources
```

The updater will be built from the `updater/` module and automatically copied here.

## Why JAR Instead of EXE?

The updater uses an UberJar (single JAR file) instead of a native EXE because:
- **Much smaller size**: ~5-10MB vs 50-100MB for bundled EXE
- **Uses main app's JVM**: Runs using the JVM bundled with the main L2Loot app
- **No extra dependencies**: No need to bundle another JVM runtime

## Manual Build

If you need to build manually:

1. Navigate to the updater module: `cd updater`
2. Build the JAR: `../gradlew packageReleaseUberJarForCurrentOS`
3. Copy the JAR from `updater/build/compose/jars/*.jar` to this directory as `L2LootUpdater.jar`

## CI/CD

The GitHub Actions workflow builds the updater JAR before packaging the main app to ensure it's included in the distribution.

