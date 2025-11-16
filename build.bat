@echo off
echo ========================================
echo Shadow Signal - APK Build Script
echo ========================================
echo.

echo [1/4] Stopping Gradle daemons...
call gradlew --stop
echo.

echo [2/4] Cleaning project...
call gradlew clean
echo.

echo [3/4] Building debug APK for Android 15...
call gradlew assembleDebug --warning-mode all
echo.

echo [4/4] Checking build output...
if exist app\build\outputs\apk\debug\app-debug.apk (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo APK Location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    dir app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo To install on device:
    echo   adb install app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo.
    echo Please check the error messages above.
    echo.
    echo Common solutions:
    echo 1. Make sure you have JDK 17 installed
    echo 2. Try building with Android Studio instead
    echo 3. Check BUILD_INSTRUCTIONS.md for more help
    echo.
)

pause
