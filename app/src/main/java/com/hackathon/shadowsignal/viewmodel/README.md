# ScannerViewModel Implementation

## Overview

The `ScannerViewModel` is the central coordinator for the Shadow Signal app, managing the reactive data flow between sensor modules and the UI layer.

## Architecture

The ViewModel follows the MVVM pattern and implements these key responsibilities:

### 1. Dependency Injection
- **CameraAnalyzer**: Provides visual anomaly detection
- **AudioAnalyzer**: Provides audio anomaly detection and spectrum data
- **ThreatFusion**: Combines sensor data into threat assessments
- **PermissionManager**: Manages runtime permissions

### 2. Reactive State Management
- Uses Kotlin `StateFlow` for reactive UI updates
- Combines multiple flows using `combine` operator
- Maintains immutable UI state with `ScannerUiState`

### 3. Lifecycle Management
- Properly handles start/stop scanning
- Cleans up resources in `onCleared()`
- Respects Android lifecycle events

## Key Features

### Flow Combination (Requirement 7.2)
```kotlin
combine(
    threatFusion.getThreatLevelFlow(),
    threatFusion.getCompositeScoreFlow(),
    audioAnalyzer.getSpectrumFlow(),
    cameraAnalyzer.getAnomalyFlow(),
    audioAnalyzer.getAnomalyFlow()
) { ... }
```

The ViewModel combines 5 separate flows into a single reactive stream that updates the UI state.

### Anomaly Tracking
- Maintains a list of the 10 most recent anomalies
- Filters out zero-intensity anomalies
- Provides historical context for the UI

### Permission Handling
- Observes permission state changes
- Updates UI state when permissions change
- Gracefully handles partial permissions (camera-only or audio-only)

### Error Handling
- Catches exceptions from sensor modules
- Provides error messages to UI
- Allows error dismissal

## Usage

```kotlin
// In MainActivity or Composable
val viewModel = ScannerViewModel(
    cameraAnalyzer = cameraAnalyzerImpl,
    audioAnalyzer = audioAnalyzerImpl,
    threatFusion = threatFusionImpl,
    permissionManager = permissionManagerImpl
)

// Observe UI state
val uiState by viewModel.uiState.collectAsState()

// Start scanning
viewModel.startScanning()

// Stop scanning
viewModel.stopScanning()
```

## Requirements Satisfied

- **7.1**: Creates ScannerViewModel extending ViewModel with proper dependency injection
- **7.2**: Combines flows from all modules using Kotlin Flow operators
- **7.5**: Implements start/stop scanning methods and handles lifecycle events properly

## Notes

- The ViewModel does not directly start camera analysis; this is handled in the UI layer when the lifecycle owner is available
- Audio analysis starts immediately when `startScanning()` is called (if permission granted)
- All sensor operations are wrapped in try-catch blocks for robustness
- The ViewModel uses `viewModelScope` for coroutine management, ensuring proper cancellation
