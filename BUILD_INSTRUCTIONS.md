# Shadow Signal - APK Build Instructions

## Prerequisites
- Java Development Kit (JDK) 17 or higher
- Android SDK with API 35 (Android 15)
- Gradle 8.2 or higher

## Build Issue Fix

The current build error is caused by a Java version parsing issue in Gradle. Here are the solutions:

### Option 1: Use Android Studio (Recommended)
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**
4. The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### Option 2: Fix Gradle Wrapper (Command Line)
If you want to build from command line, try these steps:

1. **Check your Java version:**
   ```cmd
   java -version
   ```
   Make sure you're using JDK 17 or 21 (not JDK 25 which causes parsing issues)

2. **Set JAVA_HOME environment variable:**
   ```cmd
   set JAVA_HOME=C:\Program Files\Java\jdk-17
   set PATH=%JAVA_HOME%\bin;%PATH%
   ```

3. **Clean and build:**
   ```cmd
   gradlew clean
   gradlew assembleDebug
   ```

### Option 3: Use Gradle Daemon with Specific Java Version
1. Stop all Gradle daemons:
   ```cmd
   gradlew --stop
   ```

2. Build with specific Java version:
   ```cmd
   gradlew assembleDebug -Dorg.gradle.java.home="C:\Program Files\Java\jdk-17"
   ```

### Option 4: Disable Configuration Cache Temporarily
If the issue persists, edit `gradle.properties` and comment out:
```properties
# org.gradle.configuration-cache=true
```

Then run:
```cmd
gradlew clean assembleDebug
```

## Build Variants

### Debug APK (for testing)
```cmd
gradlew assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK (for distribution)
```cmd
gradlew assembleRelease
```
Output: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Installation on Device

### Using ADB
1. Enable USB debugging on your Android device
2. Connect device via USB
3. Install the APK:
   ```cmd
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Manual Installation
1. Copy the APK to your device
2. Open the APK file on your device
3. Allow installation from unknown sources if prompted
4. Install the app

## Permissions Required
The app requires the following permissions (will be requested at runtime):
- **Camera**: For visual anomaly detection
- **Microphone**: For audio anomaly detection

## Target Platform
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 15 (API 35)
- **Compile SDK**: Android 15 (API 35)

## Troubleshooting

### "0 was unexpected at this time" Error
This is caused by Gradle having trouble parsing Java version 25.x. Solutions:
1. Downgrade to JDK 17 or JDK 21
2. Use Android Studio to build instead
3. Set JAVA_HOME explicitly to a compatible JDK version

### "SDK location not found" Error
Create a `local.properties` file in the project root:
```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

### Out of Memory Error
Increase Gradle memory in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

## Verification

After building, verify the APK:
```cmd
gradlew assembleDebug --info
```

Check APK size (should be under 50MB):
```cmd
dir app\build\outputs\apk\debug\app-debug.apk
```

## Quick Build Script

Create a `build.bat` file for easy building:
```batch
@echo off
echo Stopping Gradle daemons...
call gradlew --stop

echo Cleaning project...
call gradlew clean

echo Building debug APK...
call gradlew assembleDebug

echo.
echo Build complete!
echo APK location: app\build\outputs\apk\debug\app-debug.apk
pause
```

Run it with:
```cmd
build.bat
```
