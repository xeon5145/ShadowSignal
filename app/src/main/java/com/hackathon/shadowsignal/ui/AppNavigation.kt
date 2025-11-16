package com.hackathon.shadowsignal.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hackathon.shadowsignal.viewmodel.ScannerViewModel

/**
 * Main navigation composable for the app
 * Handles permission checking and navigation to scanner screen
 */
@Composable
fun AppNavigation(
    viewModel: ScannerViewModel,
    modifier: Modifier = Modifier
) {
    PermissionHandler(
        viewModel = viewModel
    ) {
        ScannerScreen(
            viewModel = viewModel,
            modifier = modifier.fillMaxSize()
        )
    }
}
