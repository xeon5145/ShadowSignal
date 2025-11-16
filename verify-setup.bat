@echo off
echo ========================================
echo Shadow Signal - Setup Verification
echo ========================================
echo.

echo Checking Java installation...
java -version 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install JDK 17 or higher from https://adoptium.net/
    echo.
) else (
    echo [OK] Java is installed
    echo.
)

echo Checking JAVA_HOME...
if "%JAVA_HOME%"=="" (
    echo [WARNING] JAVA_HOME is not set
    echo Please set JAVA_HOME to your JDK installation directory
    echo.
) else (
    echo [OK] JAVA_HOME is set to: %JAVA_HOME%
    echo.
)

echo Checking Gradle wrapper...
if exist "gradle\wrapper\gradle-wrapper.jar" (
    echo [OK] Gradle wrapper is present
) else (
    echo [ERROR] Gradle wrapper jar is missing
)
echo.

echo Checking project structure...
if exist "app\build.gradle.kts" (
    echo [OK] App build configuration exists
) else (
    echo [ERROR] App build configuration missing
)

if exist "app\src\main\AndroidManifest.xml" (
    echo [OK] AndroidManifest.xml exists
) else (
    echo [ERROR] AndroidManifest.xml missing
)
echo.

echo ========================================
echo Setup verification complete
echo ========================================
pause
