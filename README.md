# Shadow Signal - Paranormal Scanner App

Android application for detecting and visualizing "anomalies" from camera and microphone input.

## Prerequisites

To build this project, you need:

1. **Java Development Kit (JDK) 17 or higher**
   - Download from: https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/
   - Set JAVA_HOME environment variable to JDK installation path
   - Add JDK bin directory to PATH

2. **Android SDK** (optional for CLI builds, required for device deployment)
   - Download Android Studio or standalone SDK tools
   - Set ANDROID_HOME environment variable

## Project Structure

```
shadow-signal/
├── app/                          # Main application module
│   ├── src/main/
│   │   ├── java/com/hackathon/shadowsignal/
│   │   │   └── MainActivity.kt   # Main activity
│   │   ├── res/                  # Resources
│   │   └── AndroidManifest.xml   # App manifest with permissions
│   ├── build.gradle.kts          # App-level build configuration
│   └── proguard-rules.pro        # ProGuard rules
├── gradle/                       # Gradle wrapper files
├── build.gradle.kts              # Project-level build configuration
├── settings.gradle.kts           # Project settings
├── gradle.properties             # Gradle properties
└── gradlew.bat                   # Gradle wrapper script (Windows)
```

## Building the Project

### From Command Line (Windows)

```cmd
gradlew.bat assembleDebug
```


The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### Installing on Device

```cmd
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Dependencies

- **Kotlin**: 1.9.22
- **Jetpack Compose**: 2024.01.00 BOM
- **CameraX**: 1.3.1
- **Coroutines**: 1.7.3
- **Apache Commons Math**: 3.6.1 (for FFT)
- **AndroidX Core**: 1.12.0
- **Lifecycle**: 2.7.0

## Permissions

The app requires the following permissions:
- `CAMERA` - For visual anomaly detection
- `RECORD_AUDIO` - For audio anomaly detection

## Configuration

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Compile SDK**: 34

## Next Steps

After Java is installed and the project builds successfully:
1. Implement core data models and interfaces (Task 2)
2. Implement Permission Manager (Task 3)
3. Implement Camera Module with CameraX (Task 4)
4. Implement Audio Module with AudioRecord (Task 5)
5. Continue with remaining tasks as per implementation plan
