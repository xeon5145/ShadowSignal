# Build Instructions for Shadow Signal

## Environment Setup

### 1. Install Java Development Kit (JDK)

The project requires JDK 17 or higher.

**Option A: Using Chocolatey (Windows Package Manager)**
```cmd
choco install openjdk17
```

**Option B: Manual Installation**
1. Download JDK 17 from https://adoptium.net/
2. Run the installer
3. Set environment variables:
   - `JAVA_HOME` = `C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot`
   - Add to `PATH`: `%JAVA_HOME%\bin`

**Verify Installation:**
```cmd
java -version
```

### 2. Verify Project Setup

Run the verification script:
```cmd
verify-setup.bat
```

This will check:
- Java installation
- JAVA_HOME environment variable
- Gradle wrapper presence
- Project structure integrity

## Building the Project

### Build Debug APK

```cmd
gradlew.bat assembleDebug
```

**Output Location:** `app\build\outputs\apk\debug\app-debug.apk`

### Build Release APK

```cmd
gradlew.bat assembleRelease
```

**Output Location:** `app\build\outputs\apk\release\app-release-unsigned.apk`

### Clean Build

```cmd
gradlew.bat clean assembleDebug
```


## Installing on Device

### Prerequisites
- Enable Developer Options on your Android device
- Enable USB Debugging
- Install ADB (Android Debug Bridge)

### Install APK
```cmd
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Reinstall (if already installed)
```cmd
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Uninstall
```cmd
adb uninstall com.hackathon.shadowsignal
```

## Troubleshooting

### "JAVA_HOME is not set"
Set the JAVA_HOME environment variable:
```cmd
setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot"
```
Then restart your command prompt.

### "Gradle sync failed"
Try cleaning the project:
```cmd
gradlew.bat clean
```

### "SDK location not found"
Create `local.properties` in the project root:
```
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

### Build is slow
Add to `gradle.properties`:
```
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
```

## Project Configuration Summary

- **Package Name:** com.hackathon.shadowsignal
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Build Tools:** Gradle 8.2
- **Kotlin Version:** 1.9.22
- **AGP Version:** 8.2.2
