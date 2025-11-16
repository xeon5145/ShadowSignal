package com.hackathon.shadowsignal.ui

import android.app.Activity
import androidx.compose.runtime.*
import com.hackathon.shadowsignal.data.PermissionManagerImpl

/**
 * Extension functions and composables for handling permissions with rationale
 * Requirements: 1.6, 2.6
 */

/**
 * Composable that manages permission request flow with rationale dialogs
 */
@Composable
fun rememberPermissionRequestWithRationale(
    permissionManager: PermissionManagerImpl,
    activity: Activity
): PermissionRequestState {
    var showCameraRationale by remember { mutableStateOf(false) }
    var showMicrophoneRationale by remember { mutableStateOf(false) }
    
    // Show camera rationale dialog
    PermissionRationaleDialog(
        showDialog = showCameraRationale,
        title = "Camera Permission Required",
        message = "Shadow Signal uses your camera to detect visual anomalies like motion, " +
                "light changes, and unusual shapes. This is essential for the paranormal scanner experience.",
        onDismiss = { showCameraRationale = false },
        onConfirm = {
            showCameraRationale = false
            permissionManager.requestPermissions(activity)
        }
    )
    
    // Show microphone rationale dialog
    PermissionRationaleDialog(
        showDialog = showMicrophoneRationale,
        title = "Microphone Permission Required",
        message = "Shadow Signal analyzes audio frequencies to detect acoustic anomalies " +
                "and unusual sounds. This helps identify paranormal activity through sound.",
        onDismiss = { showMicrophoneRationale = false },
        onConfirm = {
            showMicrophoneRationale = false
            permissionManager.requestPermissions(activity)
        }
    )
    
    return PermissionRequestState(
        requestPermissions = {
            // Check if we should show rationale
            val shouldShowCameraRationale = !permissionManager.checkCameraPermission() &&
                    permissionManager.shouldShowCameraRationale(activity)
            val shouldShowMicrophoneRationale = !permissionManager.checkMicrophonePermission() &&
                    permissionManager.shouldShowMicrophoneRationale(activity)
            
            when {
                shouldShowCameraRationale -> showCameraRationale = true
                shouldShowMicrophoneRationale -> showMicrophoneRationale = true
                else -> permissionManager.requestPermissions(activity)
            }
        }
    )
}

/**
 * State holder for permission requests
 */
data class PermissionRequestState(
    val requestPermissions: () -> Unit
)
