package com.hackathon.shadowsignal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            ThreatMeter(
                threatLevel = uiState.threatAssessment.level,
                compositeScore = uiState.threatAssessment.compositeScore
            )
        }
        
        // Anomaly overlay - displays floating anomaly indicators
        AnomalyOverlay(
            anomalies = uiState.recentAnomalies,
            modifier = Modifier.fillMaxSize()
        )
        
        // Visualizers - positioned at bottom (only show if scanning and have data)
        if (uiState.isScanning && uiState.audioSpectrum.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Spectrum visualizer
                val anomalyFrequencies = uiState.recentAnomalies
                    .filterIsInstance<com.hackathon.shadowsignal.domain.model.AudioAnomaly>()
                    .mapNotNull { it.frequency }
                    .toSet()
                
                SpectrumVisualizer(
                    spectrum = uiState.audioSpectrum,
                    anomalyFrequencies = anomalyFrequencies
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Waveform visualizer
                WaveformVisualizer(
                    audioSamples = uiState.audioSpectrum
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Error message overlay - displayed at bottom center
        uiState.errorMessage?.let { errorMsg ->
            ErrorMessageOverlay(
                message = errorMsg,
                onDismiss = { viewModel.clearError() }
            )
        }
    }
}

/**
 * Error message overlay composable
 * Displays error messages with dismiss button
 * 
 * @param message Error message to display
 * @param onDismiss Callback when dismiss button is clicked
 */
@Composable
private fun ErrorMessageOverlay(
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 200.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .background(
                    Color.Black.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = onDismiss
            ) {
                Text("Dismiss")
            }
        }
    }
}
