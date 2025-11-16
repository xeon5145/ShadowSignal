package com.hackathon.shadowsignal.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hackathon.shadowsignal.MainActivity
import com.hackathon.shadowsignal.viewmodel.ScannerViewModel

/**
 * Composable that handles permission state and displays appropriate UI
 * Shows rationale dialogs and error messages for denied permissions
 * Requirements: 1.6, 2.6
 */
@Composable
fun PermissionHandler(
    viewModel: ScannerViewModel,
    content: @Composable () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    when {
        uiState.permissionsGranted -> {
            // All permissions granted, show main content
            content()
        }
        else -> {
            // Permissions not granted
            PermissionDeniedScreen(
                title = "Camera and Microphone Access Required",
                message = "Shadow Signal needs access to your camera and microphone to detect anomalies. " +
                        "Please grant these permissions to continue.",
                onRequestPermissions = { viewModel.requestPermissions(context as MainActivity) },
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
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
    onOpenSettings: () -> Unit,
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
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Button(
                    onClick = onRequestPermissions,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Grant Permissions")
                }
                
                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open App Settings")
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
