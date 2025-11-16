# Requirements Document

## Introduction

Shadow Signal is an Android application designed for a Halloween-themed hackathon that detects and visualizes "anomalies" from camera and microphone input in real-time. The app creates an immersive, spooky "paranormal scanner" experience by analyzing environmental data and presenting findings through a scientific-horror themed interface. This project exemplifies the "Frankenstein" category by stitching together multiple technologies: CameraX for video processing, AudioRecord for audio analysis, FFT-based spectral analysis, and real-time data visualization with a neon-accented dark UI.

The app must be buildable entirely via CLI using Gradle, produce an installable APK for Samsung A75, and operate completely offline with lightweight processing suitable for a 15-day development timeline.

## Glossary

- **Shadow Signal App**: The Android application that performs anomaly detection and visualization
- **Anomaly**: An unusual pattern, movement, sound frequency, or environmental change detected by the app's sensors
- **Camera Module**: The component using CameraX to capture and analyze live video feed
- **Audio Module**: The component using AudioRecord to capture and analyze microphone input
- **Threat Fusion Engine**: The logic that combines camera and audio signals to calculate threat levels
- **Threat Score**: A categorical value (LOW, MEDIUM, HIGH) representing detected anomaly intensity
- **Spooky UI**: The dark-themed user interface with neon green/cyan highlights
- **FFT**: Fast Fourier Transform used for audio spectral analysis
- **Image Diff**: Frame-to-frame comparison technique for detecting visual changes
- **Contour Detection**: Computer vision technique for identifying shape changes
- **Visualizer**: UI component displaying waveforms or frequency spectrums
- **Threat Meter**: UI component showing the current Threat Score
- **CLI Build**: Command-line interface build process using Gradle without Android Studio

## Requirements

### Requirement 1: Camera Feed and Visual Anomaly Detection

**User Story:** As a user, I want to see a live camera feed that detects visual anomalies like motion, light changes, or unusual shapes, so that I can identify paranormal-like visual disturbances

#### Acceptance Criteria

1. WHEN the user launches Shadow Signal App, THE Camera Module SHALL initialize CameraX and display live camera feed within 2 seconds
2. WHEN the camera feed is active, THE Camera Module SHALL perform Image Diff analysis between consecutive frames
3. WHEN frame differences exceed 15% of pixels changed, THE Camera Module SHALL flag a visual anomaly
4. THE Camera Module SHALL detect contour changes indicating shape or object movement
5. WHEN brightness changes exceed 30% between frames, THE Camera Module SHALL register a light anomaly
6. WHEN camera permission is not granted, THE Shadow Signal App SHALL request permission and display an explanatory message

### Requirement 2: Audio Capture and Spectral Anomaly Detection

**User Story:** As a user, I want the app to analyze microphone input for unusual frequencies and audio spikes, so that I can detect acoustic anomalies in my environment

#### Acceptance Criteria

1. WHEN the user grants microphone permission, THE Audio Module SHALL initialize AudioRecord and begin capturing audio
2. THE Audio Module SHALL perform FFT analysis on audio samples in real-time
3. WHEN frequencies below 100Hz or above 8000Hz exceed amplitude threshold of -40dB, THE Audio Module SHALL flag a frequency anomaly
4. WHEN audio amplitude increases by more than 20dB within 500ms, THE Audio Module SHALL detect a spike anomaly
5. THE Audio Module SHALL process audio with latency under 300ms
6. WHEN microphone permission is not granted, THE Shadow Signal App SHALL request permission and display an explanatory message

### Requirement 3: Threat Fusion and Scoring

**User Story:** As a user, I want the app to combine camera and audio signals into a single threat level, so that I get a clear understanding of overall anomaly intensity

#### Acceptance Criteria

1. WHEN both Camera Module and Audio Module are active, THE Threat Fusion Engine SHALL combine their signals
2. THE Threat Fusion Engine SHALL calculate a composite score weighting visual anomalies at 60% and audio anomalies at 40%
3. WHEN the composite score is below 30, THE Threat Fusion Engine SHALL output LOW Threat Score
4. WHEN the composite score is between 30 and 70, THE Threat Fusion Engine SHALL output MEDIUM Threat Score
5. WHEN the composite score exceeds 70, THE Threat Fusion Engine SHALL output HIGH Threat Score
6. THE Threat Fusion Engine SHALL update the Threat Score within 200ms of receiving new sensor data
7. THE Threat Fusion Engine SHALL use modular, simple logic without complex ML models

### Requirement 4: Spooky UI with Visualizers

**User Story:** As a user, I want to see a dark, neon-accented interface with real-time visualizers and a threat meter, so that the experience feels immersive and scientifically spooky

#### Acceptance Criteria

1. THE Spooky UI SHALL use a dark mode color scheme as the base theme
2. THE Spooky UI SHALL highlight interactive elements and data with neon green or cyan colors
3. WHEN audio data is available, THE Spooky UI SHALL display a real-time waveform Visualizer
4. THE Spooky UI SHALL display a frequency spectrum Visualizer showing FFT output
5. THE Spooky UI SHALL prominently display the Threat Meter showing current Threat Score
6. WHEN the Threat Score changes, THE Threat Meter SHALL update with smooth animations
7. THE Spooky UI SHALL display the camera feed with visual overlay indicators for detected anomalies

### Requirement 5: Build and Deployment

**User Story:** As a developer, I want to build the app entirely from the command line and generate an installable APK, so that I can automate builds and deploy to Samsung A75 devices

#### Acceptance Criteria

1. THE Shadow Signal App SHALL be written in Kotlin with Gradle build system
2. WHEN executing "./gradlew assembleDebug" from CLI, THE Shadow Signal App SHALL build successfully without Android Studio
3. THE Shadow Signal App SHALL generate an APK file installable on Samsung A75 devices
4. THE Shadow Signal App SHALL target Android API level 24 (Android 7.0) or higher
5. THE Shadow Signal App SHALL include all dependencies in the APK for offline operation
6. THE Shadow Signal App SHALL not require server calls or internet connectivity
7. THE Shadow Signal App SHALL be under 50MB in size

### Requirement 6: Performance and Constraints

**User Story:** As a user, I want the app to run smoothly on my device without lag or excessive battery drain, so that I can use it throughout the hackathon event

#### Acceptance Criteria

1. THE Shadow Signal App SHALL process camera frames at minimum 15 frames per second
2. THE Shadow Signal App SHALL use lightweight processing algorithms suitable for mobile devices
3. WHEN using OpenCV or similar libraries, THE Shadow Signal App SHALL limit operations to simple, optimized functions
4. THE Shadow Signal App SHALL not implement complex ML models that require significant processing time
5. THE Shadow Signal App SHALL maintain responsive UI with frame rates above 30 FPS
6. WHEN device battery drops below 15%, THE Shadow Signal App SHALL display a warning but continue operating

### Requirement 7: Modular Architecture

**User Story:** As a developer, I want the codebase to be modular and maintainable, so that I can iterate quickly and add polish within the 15-day timeline

#### Acceptance Criteria

1. THE Shadow Signal App SHALL separate concerns into distinct modules: Camera Module, Audio Module, Threat Fusion Engine, and Spooky UI
2. THE Shadow Signal App SHALL use interfaces or abstractions between modules for loose coupling
3. THE Shadow Signal App SHALL implement the Camera Module independently testable from other components
4. THE Shadow Signal App SHALL implement the Audio Module independently testable from other components
5. THE Threat Fusion Engine SHALL accept inputs from camera and audio modules through defined interfaces
6. THE Shadow Signal App SHALL follow MVP-first development approach focusing on core functionality before polish

## Project Constraints

- **Timeline**: 15-day development deadline
- **Development Approach**: MVP first, polish later
- **Processing**: Lightweight algorithms only, avoid heavyweight ML models
- **Build System**: Must build via CLI with `./gradlew assembleDebug`
- **Target Device**: Samsung A75 (Android 7.0+)
- **Connectivity**: Offline-only, no server dependencies
- **Optional Technologies**: Simple ML or OpenCV allowed if lightweight

## Success Criteria

The Shadow Signal app will be considered successful when it:
1. Builds successfully from CLI and installs on Samsung A75
2. Displays live camera feed with visual anomaly detection
3. Captures and analyzes audio for frequency anomalies
4. Combines signals into LOW/MEDIUM/HIGH threat levels
5. Presents data through a polished, spooky dark UI with neon accents
6. Runs smoothly without lag or crashes
7. Creates a believable "paranormal scanner" experience
