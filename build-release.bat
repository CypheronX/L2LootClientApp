@echo off
echo ========================================
echo L2Loot Release Builder
echo ========================================
echo.
echo Select build type:
echo 1. MSI Installer (Recommended)
echo 2. EXE Portable
echo 3. Both MSI and EXE
echo 4. Test Release Build (No Installer)
echo.
set /p choice="Enter your choice (1-4): "

if "%choice%"=="1" (
    echo.
    echo Building MSI Installer...
    echo.
    call gradlew.bat packageReleaseMsi
    echo.
    echo ========================================
    echo MSI Installer created at:
    echo composeApp\build\compose\binaries\main-release\msi\
    echo ========================================
)

if "%choice%"=="2" (
    echo.
    echo Building EXE Portable...
    echo.
    call gradlew.bat packageReleaseExe
    echo.
    echo ========================================
    echo EXE created at:
    echo composeApp\build\compose\binaries\main-release\exe\
    echo ========================================
)

if "%choice%"=="3" (
    echo.
    echo Building Both MSI and EXE...
    echo.
    call gradlew.bat packageReleaseDistributionForCurrentOS
    echo.
    echo ========================================
    echo Installers created at:
    echo composeApp\build\compose\binaries\main-release\
    echo ========================================
)

if "%choice%"=="4" (
    echo.
    echo Running Release Build for Testing...
    echo.
    call gradlew.bat runReleaseDistributable
)

echo.
pause
