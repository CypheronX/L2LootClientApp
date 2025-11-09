# Updater Executable

This directory should contain the `L2LootUpdater.zip` file, which contains the complete native app folder structure (executable, config, runtime, etc.).

## Building the Updater

To build the updater native executable and copy it to this location, run:

```bash
./gradlew copyUpdaterToResources
```

Or on Windows PowerShell:

```powershell
.\gradlew.bat copyUpdaterToResources
```

The updater will be built from the `updater/` module as a standalone native executable and automatically copied here.

## Why Native EXE?

The updater is built as a standalone native executable because:
- **No Java dependency**: Runs independently without requiring Java to be installed
- **Self-contained**: Includes its own JVM runtime, so it doesn't depend on the main app's JVM
- **Easier deployment**: Single executable file that can be run directly
- **Better user experience**: No need to locate or bundle Java executables

## Manual Build

If you need to build manually:

1. Build the native distribution: `./gradlew :updater:createReleaseDistributable`
2. Create a ZIP of the app folder: `./gradlew zipUpdaterForResources`
3. Copy the ZIP from `build/updater-resources/L2LootUpdater.zip` to this directory

## CI/CD

The GitHub Actions workflow builds the updater executable before packaging the main app to ensure it's included in the distribution.

