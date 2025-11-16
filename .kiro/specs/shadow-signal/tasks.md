# Implementation Plan

- [x] 1. Set up Android project structure and build configuration





  - Initialize Gradle project with Kotlin DSL
  - Configure build.gradle.kts with required dependencies (Compose, CameraX, Coroutines, Commons Math)
  - Set up AndroidManifest.xml with permissions and activity declaration
  - Configure gradle.properties for CLI builds
  - Verify project builds with `./gradlew assembleDebug`
  - _Requirements: 5.1, 5.2, 7.1_

- [x] 2. Implement core data models and interfaces





  - Create Anomaly sealed class with VisualAnomaly and AudioAnomaly data classes
  - Define AnomalyType enums (VisualAnomalyType, AudioAnomalyType)
  - Create ThreatAssessment and ThreatLevel models
  - Create ScannerUiState data class
  - Define CameraAnalyzer, AudioAnalyzer, and ThreatFusion interfaces
  - _Requirements: 7.1, 7.2_

- [x] 3. Implement Permission Manager





  - Create PermissionManager class with permission check methods
  - Implement ActivityResultContracts for camera and microphone permissions
  - Create PermissionState data class and StateFlow
  - Add permission request logic with rationale dialogs
  - Handle permission denial scenarios
  - _Requirements: 1.6, 2.6_

- [x] 4. Implement Camera Module with CameraX





  - [x] 4.1 Set up CameraX initialization and lifecycle binding


    - Create CameraAnalyzerImpl class implementing CameraAnalyzer interface
    - Configure Preview and ImageAnalysis use cases
    - Set up camera provider with 640x480 resolution and YUV_420_888 format
    - Bind use cases to lifecycle owner
    - _Requirements: 1.1, 1.2_
  
  - [x] 4.2 Implement image diff detection

    - Convert frames to grayscale
    - Calculate pixel-wise difference between consecutive frames
    - Count changed pixels and calculate percentage
    - Flag anomaly when change exceeds 15% threshold
    - Emit VisualAnomaly with MOTION type
    - _Requirements: 1.2, 1.3_
  
  - [x] 4.3 Implement brightness change detection

    - Calculate mean luminance for each frame
    - Compare with previous frame mean
    - Flag anomaly when brightness change exceeds 30%
    - Emit VisualAnomaly with LIGHT_CHANGE type
    - _Requirements: 1.5_
  
  - [x] 4.4 Optimize camera processing performance

    - Implement frame skipping if needed (process every 2nd frame)
    - Reuse bitmap buffers to reduce GC pressure
    - Run analysis on background executor
    - Ensure 15+ FPS processing rate
    - _Requirements: 6.1, 6.2_

- [ ] 5. Implement Audio Module with AudioRecord
  - [x] 5.1 Set up AudioRecord initialization





    - Create AudioAnalyzerImpl class implementing AudioAnalyzer interface
    - Configure AudioRecord with 44100 Hz sample rate, MONO channel, PCM_16BIT encoding
    - Calculate appropriate buffer size
    - Set up background thread for audio capture
    - _Requirements: 2.1_
  
  - [x] 5.2 Implement FFT analysis





    - Capture audio samples in 2048-sample windows
    - Apply Hamming window function to reduce spectral leakage
    - Perform FFT using Apache Commons Math FastFourierTransformer
    - Calculate magnitude spectrum from complex FFT output
    - Emit spectrum data via StateFlow for visualizers
    - _Requirements: 2.2_
  
  - [x] 5.3 Implement frequency anomaly detection





    - Analyze FFT bins corresponding to <100Hz and >8000Hz ranges
    - Convert magnitude to decibels (20 * log10(magnitude))
    - Flag anomaly when any bin exceeds -40dB threshold
    - Emit AudioAnomaly with FREQUENCY type and detected frequency
    - _Requirements: 2.3_
  
  - [ ] 5.4 Implement audio spike detection
    - Calculate RMS amplitude for each audio window
    - Maintain rolling average of last 10 windows
    - Flag spike when current amplitude exceeds average by 20dB
    - Emit AudioAnomaly with SPIKE type
    - _Requirements: 2.4_
  
  - [ ] 5.5 Optimize audio processing performance
    - Use circular buffer for audio samples
    - Run FFT calculations on Dispatchers.Default coroutine
    - Ensure processing latency under 300ms
    - _Requirements: 2.5, 6.2_

- [ ] 6. Implement Threat Fusion Engine
  - Create ThreatFusionImpl class implementing ThreatFusion interface
  - Implement composite score calculation (visual 60%, audio 40%)
  - Calculate visual score from max of motion, light, and contour intensities
  - Calculate audio score from max of frequency and spike intensities
  - Apply exponential moving average for temporal smoothing (alpha = 0.3)
  - Map composite score to ThreatLevel (LOW <30, MEDIUM 30-70, HIGH >70)
  - Emit ThreatLevel and composite score via StateFlow
  - Ensure updates within 200ms of sensor input
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

- [x] 7. Implement ScannerViewModel





  - Create ScannerViewModel extending ViewModel
  - Inject CameraAnalyzer, AudioAnalyzer, ThreatFusion, and PermissionManager
  - Set up StateFlow for ScannerUiState
  - Combine flows from all modules using Kotlin Flow operators
  - Update UI state reactively when sensor data changes
  - Implement start/stop scanning methods
  - Handle lifecycle events properly
  - _Requirements: 7.1, 7.2, 7.5_

- [ ] 8. Implement Spooky UI theme and styling
  - Create Color.kt with dark theme colors (DarkBackground, NeonGreen, NeonCyan, NeonRed)
  - Define ShadowSignalTheme composable with Material3 dark color scheme
  - Configure typography with monospace fonts for scientific feel
  - Add glow effects using shadow modifiers
  - _Requirements: 4.1, 4.2_

- [ ] 9. Implement UI components
  - [ ] 9.1 Create MainActivity and ScannerScreen composable
    - Set up MainActivity with Compose setContent
    - Create ScannerScreen composable as main UI container
    - Observe ScannerUiState from ViewModel
    - Handle permission requests on launch
    - _Requirements: 1.1, 4.1_
  
  - [ ] 9.2 Implement CameraPreview composable
    - Create AndroidView wrapper for CameraX PreviewView
    - Display full-screen camera feed
    - Add dark overlay for spooky effect
    - _Requirements: 1.1, 4.7_
  
  - [ ] 9.3 Implement ThreatMeter composable
    - Create circular or arc gauge using Canvas API
    - Display current threat level with color coding (Green/Yellow/Red)
    - Add animated transitions using animateFloatAsState
    - Apply neon glow effect with shadows
    - Position prominently on screen
    - _Requirements: 4.5, 4.6_
  
  - [ ] 9.4 Implement WaveformVisualizer composable
    - Create Canvas-based waveform display
    - Draw audio samples as connected line graph
    - Use neon green color with glow effect
    - Update at 30 FPS
    - _Requirements: 4.3_
  
  - [ ] 9.5 Implement SpectrumVisualizer composable
    - Create Canvas-based frequency spectrum bars
    - Use logarithmic frequency scale for x-axis
    - Draw bars with neon cyan color
    - Highlight anomaly frequencies in red
    - Update in real-time from FFT data
    - _Requirements: 4.4_
  
  - [ ] 9.6 Implement AnomalyOverlay composable
    - Display floating indicators for detected anomalies
    - Show anomaly type and intensity as text
    - Add fade in/out animations
    - Position based on camera region if available
    - Overlay on camera preview
    - _Requirements: 4.7_

- [ ] 10. Implement error handling and edge cases
  - Add try-catch blocks around camera initialization
  - Handle AudioRecord initialization failures gracefully
  - Implement error state in ScannerUiState
  - Display user-friendly error messages in UI
  - Add retry mechanisms for recoverable errors
  - Handle low battery warning display
  - Test permission denial scenarios
  - _Requirements: 6.6_

- [ ] 11. Build and test APK
  - Run `./gradlew clean assembleDebug` from CLI
  - Verify APK is generated in app/build/outputs/apk/debug/
  - Check APK size is under 50MB
  - Install APK on Samsung A75 device using adb
  - Verify app launches without crashes
  - _Requirements: 5.2, 5.3, 5.7_

- [ ] 12. Perform integration testing and optimization
  - Test camera feed displays correctly on device
  - Test motion detection by moving in front of camera
  - Test brightness detection by changing lighting
  - Test audio anomaly detection with various sounds
  - Verify threat meter updates in real-time
  - Verify visualizers display correctly
  - Measure and optimize frame processing rate (target 15+ FPS)
  - Measure and optimize audio latency (target <300ms)
  - Profile memory usage and optimize if needed
  - Test app performance over extended session
  - _Requirements: 1.1, 1.2, 1.3, 1.5, 2.2, 2.3, 2.4, 3.6, 4.3, 4.4, 4.5, 6.1, 6.2, 6.5_
