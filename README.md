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

### Quick Build (Recommended)

Run the build script:
```cmd
build.bat
```

### From Command Line (Windows)

**Option 1: Using Gradle Wrapper**
```cmd
gradlew.bat clean assembleDebug
```

**Option 2: If you encounter Java version errors**
```cmd
gradlew --stop
gradlew clean assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### Using Android Studio (Alternative)

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Go to **Build > Build Bundle(s) / APK(s) > Build APK(s)**
4. APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### Installing on Device

**Via ADB:**
```cmd
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Manual Installation:**
1. Copy APK to your Android device
2. Open the APK file on your device
3. Allow installation from unknown sources if prompted
4. Install the app

### Troubleshooting Build Issues

If you encounter "0 was unexpected at this time" error:
1. Make sure you're using JDK 17 or JDK 21 (not JDK 25)
2. Check your JAVA_HOME environment variable
3. See `BUILD_INSTRUCTIONS.md` for detailed solutions

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
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35

## Features Implemented

✅ **Core Data Models** - Anomaly types, threat assessment, UI state
✅ **Permission Management** - Runtime camera and microphone permissions
✅ **Camera Module** - Real-time visual anomaly detection (motion, brightness)
✅ **Audio Module** - FFT analysis, frequency and spike detection
✅ **Threat Fusion Engine** - Combines visual and audio signals
✅ **ViewModel** - Reactive state management with Kotlin Flow
✅ **Spooky UI Theme** - Dark theme with neon green/cyan/red accents
✅ **UI Components**:
  - Camera preview with dark overlay
  - Threat meter with animated arc gauge
  - Waveform visualizer
  - Frequency spectrum visualizer
  - Floating anomaly indicators
✅ **Error Handling** - Comprehensive error handling and user feedback

## App Architecture

```
┌─────────────────────────────────────────┐
│           MainActivity                   │
│  (Compose UI + ViewModel Setup)         │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│        ScannerViewModel                  │
│  (Coordinates all modules)              │
└──┬────────────┬────────────┬────────────┘
   │            │            │
   ▼            ▼            ▼
┌──────┐   ┌──────┐   ┌──────────┐
│Camera│   │Audio │   │  Threat  │
│Module│   │Module│   │  Fusion  │
└──────┘   └──────┘   └──────────┘
```

## Testing the App

1. **Grant Permissions**: Allow camera and microphone access
2. **Motion Detection**: Move in front of the camera
3. **Light Detection**: Change lighting conditions
4. **Audio Detection**: Make sounds (especially low/high frequencies)
5. **Threat Meter**: Watch the gauge respond to anomalies
6. **Visualizers**: See real-time audio waveform and spectrum
