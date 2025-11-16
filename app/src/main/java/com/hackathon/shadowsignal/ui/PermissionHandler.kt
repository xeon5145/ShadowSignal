package com.hackathon.shadowsignal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hackathon.shadowsignal.data.PermissionState

/**
 * Composable that handles permission state and displays appropriate UI
 * Shows rationale dialogs and error messages for denied permissions
 * Requirements: 1.6, 2.6
 */
@Composable
fun PermissionHandler(
    permissionState: PermissionState,
    onRequestPermissions: () -> Unit,
    content: @Composable () -> Unit
) {
    when {
        permissionState.cameraGranted && permissionState.microphoneGranted -> {
            // All permissions granted, show main content
            content()
        }
        !permissionState.cameraGranted && !permissionState.microphoneGranted -> {
            // Both permissions denied
            PermissionDeniedScreen(
                title = "Camera and Microphone Access Required",
                message = "Shadow Signal needs access to your camera and microphone to detect anomalies. " +
                        "Please grant these permissions to continue.",
                onRequestPermissions = onRequestPermissions
            )
        }
        !permissionState.cameraGranted -> {
            // Only camera denied
            PermissionDeniedScreen(
                title = "Camera Access Required",
                message = "Shadow Signal needs camera access to detect visual anomalies. " +
                        "The app will continue with audio-only detection, but visual features will be disabled.",
                onRequestPermissions = onRequestPermissions,
                canContinue = true
            )
        }
        !permissionState.microphoneGranted -> {
            // Only microphone denied
            PermissionDeniedScreen(
                title = "Microphone Access Required",
                message = "Shadow Signal needs microphone access to detect audio anomalies. " +
                        "The app will continue with camera-only detection, but audio features will be disabled.",
                onRequestPermissions = onRequestPermissions,
                canContinue = true
            )
        }
    }
}

/**
 * Screen displayed when permissions are denied
 */
@Composable
private fun PermissionDeniedScreen(
    title: String,
    message: String,
    onRequestPermissions: () -> Unit,
    canContinue: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permissions")
                }
                
                if (canContinue) {
                    TextButton(
                        onClick = { /* Continue with limited functionality */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continue with Limited Features")
                    }
                }
            }
        }
    }
}

/**
 * Dialog for showing permission rationale before requesting
 */
@Composable
fun PermissionRationaleDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = title)
            },
            text = {
                Text(text = message)
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Not Now")
                }
            }
        )
    }
}
