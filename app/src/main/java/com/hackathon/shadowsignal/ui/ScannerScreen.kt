package com.hackathon.shadowsignal.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import com.hackathon.shadowsignal.data.CameraAnalyzerImpl
import com.hackathon.shadowsignal.domain.model.ScannerUiState
import com.hackathon.shadowsignal.viewmodel.ScannerViewModel

/**
 * Main scanner screen composable that displays the camera feed and all UI components
 * 
 * This screen:
 * - Observes ScannerUiState from ViewModel
 * - Manages scanning lifecycle
 * - Displays camera preview, threat meter, visualizers, and anomaly overlays
 * 
 * Requirements: 1.1, 4.1
 * 
 * @param viewModel The ScannerViewModel that manages state and coordinates modules
 * @param modifier Optional modifier for the screen
 */
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // Start scanning when screen is displayed, stop when disposed
    DisposableEffect(lifecycleOwner) {
        viewModel.startScanning()
        
        onDispose {
            viewModel.stopScanning()
        }
    }

    ScannerScreenContent(
        viewModel = viewModel,
        uiState = uiState,
        lifecycleOwner = lifecycleOwner,
        modifier = modifier
    )
}

/**
 * Content composable for the scanner screen
 * Separated for easier testing and preview
 * 
 * @param viewModel The ViewModel to access camera analyzer
 * @param uiState Current UI state
 * @param lifecycleOwner Lifecycle owner for camera
 * @param modifier Optional modifier
 */
@Composable
private fun ScannerScreenContent(
    viewModel: ScannerViewModel,
    uiState: ScannerUiState,
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Camera preview as background layer
        viewModel.getCameraAnalyzer()?.let { cameraAnalyzer ->
            CameraPreview(
                cameraAnalyzer = cameraAnalyzer,
                lifecycleOwner = lifecycleOwner,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Threat meter - positioned at top center
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))
            ThreatMeter(
                threatLevel = uiState.threatAssessment.level,
                compositeScore = uiState.threatAssessment.compositeScore
            )
        }
        
        // TODO: Add WaveformVisualizer composable (sub-task 9.4)
        // TODO: Add SpectrumVisualizer composable (sub-task 9.5)
        // TODO: Add AnomalyOverlay composable (sub-task 9.6)
    }
}
