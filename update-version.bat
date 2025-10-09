@echo off
REM Helper script to update BuildConfig.kt with version from version.properties

for /f "tokens=2 delims==" %%a in ('findstr "versionMajor" version.properties') do set MAJOR=%%a
for /f "tokens=2 delims==" %%a in ('findstr "versionMinor" version.properties') do set MINOR=%%a
for /f "tokens=2 delims==" %%a in ('findstr "versionPatch" version.properties') do set PATCH=%%a

set VERSION=%MAJOR%.%MINOR%.%PATCH%

echo Updating BuildConfig.kt to version %VERSION%

powershell -Command "(Get-Content shared\src\commonMain\kotlin\com\l2loot\BuildConfig.kt) -replace 'const val VERSION_NAME = \".*\"', 'const val VERSION_NAME = \"%VERSION%\"' | Set-Content shared\src\commonMain\kotlin\com\l2loot\BuildConfig.kt"

echo Done!

