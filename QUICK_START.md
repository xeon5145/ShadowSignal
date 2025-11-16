# Shadow Signal - Quick Start Guide

## 🚀 Fastest Way to Build APK

### Method 1: Use the Build Script (Easiest)
```cmd
build.bat
```
This will automatically clean, build, and show you where the APK is located.

### Method 2: Android Studio (Most Reliable)
1. Open Android Studio
2. Open this project folder
3. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)**
4. Done! APK is at `app/build/outputs/apk/debug/app-debug.apk`

### Method 3: Command Line
```cmd
gradlew clean assembleDebug
```

## 📱 Install on Your Android Device

### Option A: USB Cable + ADB
```cmd
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Option B: Manual Transfer
1. Copy `app/build/outputs/apk/debug/app-debug.apk` to your phone
2. Open the file on your phone
3. Tap "Install"

## ⚠️ If Build Fails

### Error: "0 was unexpected at this time"
**Cause**: Java version issue (you might have JDK 25 which has parsing bugs)

**Solution 1** - Use Android Studio (recommended)

**Solution 2** - Install JDK 17:
1. Download JDK 17 from https://adoptium.net/
2. Install it
3. Set JAVA_HOME:
   ```cmd
   set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot
   set PATH=%JAVA_HOME%\bin;%PATH%
   ```
4. Try building again

**Solution 3** - Stop Gradle daemon and retry:
```cmd
gradlew --stop
gradlew clean assembleDebug
```

### Error: "SDK location not found"
Create `local.properties` file with:
```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

## 🎯 What the App Does

**Shadow Signal** is a "paranormal scanner" that:
- 📷 Analyzes camera feed for visual anomalies (motion, light changes)
- 🎤 Analyzes audio for frequency anomalies and spikes
- 🎯 Combines signals into a threat level (LOW/MEDIUM/HIGH)
- 📊 Shows real-time visualizers (waveform, spectrum)
- 👻 Has a spooky dark theme with neon effects

## 🔑 Permissions Needed

When you first run the app, it will ask for:
- **Camera** - to detect visual anomalies
- **Microphone** - to detect audio anomalies

Both are required for the app to work.

## 📋 System Requirements

- **Your Device**: Android 7.0 (API 24) or higher
- **Target**: Android 15 (API 35)
- **Build Machine**: Windows with JDK 17+

## 🎮 How to Use

1. Launch the app
2. Grant camera and microphone permissions
3. Point camera at something
4. Make sounds or move around
5. Watch the threat meter and visualizers react!

## 📚 More Help

- Detailed build instructions: `BUILD_INSTRUCTIONS.md`
- Full documentation: `README.md`
- Implementation tasks: `.kiro/specs/shadow-signal/tasks.md`

## 🐛 Common Issues

| Issue | Solution |
|-------|----------|
| Build fails with Java error | Use Android Studio or install JDK 17 |
| App crashes on launch | Check if permissions are granted |
| Camera not working | Ensure device has camera permission |
| Audio not working | Ensure device has microphone permission |
| APK too large | Normal, includes CameraX and FFT libraries |

## ✅ Build Checklist

- [ ] JDK 17 or higher installed
- [ ] JAVA_HOME environment variable set
- [ ] Android SDK installed (if using command line)
- [ ] Project opened in Android Studio OR
- [ ] Gradle wrapper works from command line
- [ ] Build completes successfully
- [ ] APK file exists at `app/build/outputs/apk/debug/app-debug.apk`
- [ ] APK installs on device
- [ ] App launches without crashes
- [ ] Permissions granted
- [ ] Camera preview shows
- [ ] Threat meter responds to movement

---

**Need more help?** Check `BUILD_INSTRUCTIONS.md` for detailed troubleshooting.
