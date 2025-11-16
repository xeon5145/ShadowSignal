# Permission Manager Implementation

## Overview

The Permission Manager handles runtime permissions for camera and microphone access in Shadow Signal. It provides a clean, reactive API using Kotlin StateFlow and integrates with Jetpack Compose for UI handling.

## Components

### 1. PermissionManager Interface
- Defines the contract for permission management
- Located in: `PermissionManager.kt`

### 2. PermissionManagerImpl
- Concrete implementation using ActivityResultContracts
- Manages permission state with StateFlow
- Handles permission checks and requests
- Located in: `PermissionManagerImpl.kt`

### 3. PermissionHandler Composable
- UI component that responds to permission state
- Shows appropriate screens for denied permissions
- Supports graceful degradation (camera-only or audio-only modes)
- Located in: `ui/PermissionHandler.kt`

### 4. Permission Extensions
- Helper functions for rationale dialogs
- Simplifies permission request flow
- Located in: `ui/PermissionExtensions.kt`

## Usage

### Basic Setup in Activity

```kotlin
class MainActivity : ComponentActivity() {
    private lateinit var permissionManager: PermissionManagerImpl
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize permission manager
        permissionManager = PermissionManagerImpl(this)
        permissionManager.initialize(this)
        
        setContent {
            val permissionState by permissionManager.getPermissionStateFlow().collectAsState()
            
            PermissionHandler(
                permissionState = permissionState,
                onRequestPermissions = {
                    permissionManager.requestPermissions(this)
                }
            ) {
                // Your main app content here
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        permissionManager.updatePermissionState()
    }
}
```

### With Rationale Dialogs

```kotlin
val permissionRequestState = rememberPermissionRequestWithRationale(
    permissionManager = permissionManager,
    activity = this
)

// Request permissions with automatic rationale handling
permissionRequestState.requestPermissions()
```

## Permission States

The `PermissionState` data class tracks both permissions:

```kotlin
data class PermissionState(
    val cameraGranted: Boolean,
    val microphoneGranted: Boolean
)
```

## Handling Permission Denial

The implementation supports three scenarios:

1. **Both permissions denied**: Shows full permission request screen
2. **Camera only denied**: Allows audio-only mode with warning
3. **Microphone only denied**: Allows camera-only mode with warning

## Rationale Dialogs

Rationale dialogs are shown when:
- User previously denied permission
- `shouldShowRequestPermissionRationale()` returns true
- Explains why the permission is needed before requesting again

## Requirements Satisfied

- **Requirement 1.6**: Camera permission handling with explanatory messages
- **Requirement 2.6**: Microphone permission handling with explanatory messages

## Key Features

✅ Reactive permission state using StateFlow
✅ ActivityResultContracts for modern permission handling
✅ Automatic rationale dialogs
✅ Graceful degradation for partial permissions
✅ Lifecycle-aware updates
✅ Compose-friendly API
✅ Clean separation of concerns

## Testing Scenarios

1. **First launch**: Permissions requested automatically
2. **Permission denied**: Rationale shown on next request
3. **Permission permanently denied**: User directed to settings
4. **Partial permissions**: App continues with limited features
5. **Resume from settings**: State updates automatically
