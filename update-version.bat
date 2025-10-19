@echo off
REM Helper script to update version.properties
REM Version is now managed through BuildKonfig and automatically synced from version.properties

setlocal

REM Check if version argument is provided
if "%~1"=="" (
    echo Usage: update-version.bat ^<version^>
    echo Example: update-version.bat 1.2.3
    exit /b 1
)

REM Parse version components
for /f "tokens=1,2,3 delims=." %%a in ("%~1") do (
    set MAJOR=%%a
    set MINOR=%%b
    set PATCH=%%c
)

REM Validate version components
if "%MAJOR%"=="" (
    echo Error: Invalid version format. Use: major.minor.patch
    exit /b 1
)
if "%MINOR%"=="" (
    echo Error: Invalid version format. Use: major.minor.patch
    exit /b 1
)
if "%PATCH%"=="" (
    echo Error: Invalid version format. Use: major.minor.patch
    exit /b 1
)

echo Updating version.properties to %~1

REM Update version.properties
(
    echo versionMajor=%MAJOR%
    echo versionMinor=%MINOR%
    echo versionPatch=%PATCH%
) > version.properties

echo.
echo Version updated successfully!
echo Next steps:
echo 1. Build your flavor: gradlew packageMsiDev or gradlew packageMsiProd
echo 2. Version %~1 will be automatically used by BuildKonfig
echo.

endlocal
