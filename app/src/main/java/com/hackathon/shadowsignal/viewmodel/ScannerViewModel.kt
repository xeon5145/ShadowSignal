package com.hackathon.shadowsignal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.shadowsignal.data.AudioAnalyzer
import com.hackathon.shadowsignal.data.CameraAnalyzer
import com.hackathon.shadowsignal.data.PermissionManager
import com.hackathon.shadowsignal.domain.ThreatFusion
import com.hackathon.shadowsignal.domain.model.AudioAnomaly
import com.hackathon.shadowsignal.domain.model.ScannerUiState
import com.hackathon.shadowsignal.domain.model.VisualAnomaly
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Scanner screen that coordinates all sensor modules and manages UI state.
 * 
 * This ViewModel:
 * - Combines flows from Camera, Audio, and Threat Fusion modules
 * - Manages scanning lifecycle (start/stop)
 * - Provides reactive UI state updates
 * - Handles permission state changes
 * 
 * Requirements: 7.1, 7.2, 7.5
 */
class ScannerViewModel(
    private val cameraAnalyzer: CameraAnalyzer,
    private val audioAnalyzer: AudioAnalyzer,
    private val threatFusion: ThreatFusion,
    private val permissionManager: PermissionManager
) : ViewModel() {
    
    /**
     * Get the camera analyzer implementation for UI binding
     * Returns null if not a CameraAnalyzerImpl instance
     */
    fun getCameraAnalyzer(): com.hackathon.shadowsignal.data.CameraAnalyzerImpl? {
        return cameraAnalyzer as? com.hackathon.shadowsignal.data.CameraAnalyzerImpl
    }

    private val _uiState = MutableStateFlow(ScannerUiState.default())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    // Track recent anomalies for display
    private val recentAnomalies = mutableListOf<com.hackathon.shadowsignal.domain.model.Anomaly>()
    private val maxAnomalies = 10

    init {
        observePermissions()
        observeSensorData()
    }

    /**
     * Observe permission state changes and update UI state accordingly
     */
    private fun observePermissions() {
        viewModelScope.launch {
            permissionManager.getPermissionStateFlow().collect { permissionState ->
                _uiState.update { currentState ->
                    currentState.copy(
                        permissionsGranted = permissionState.cameraGranted && permissionState.microphoneGranted
                    )
                }
            }
        }
    }

    /**
     * Combine flows from all sensor modules and update UI state reactively
     * Requirements: 7.1, 7.2
     */
    private fun observeSensorData() {
        // Observe camera anomalies and feed to threat fusion
        viewModelScope.launch {
            cameraAnalyzer.getAnomalyFlow().collect { visualAnomaly ->
                visualAnomaly?.let {
                    threatFusion.updateVisualAnomaly(it)
                }
            }
        }
        
        // Observe audio anomalies and feed to threat fusion
        viewModelScope.launch {
            audioAnalyzer.getAnomalyFlow().collect { audioAnomaly ->
                audioAnomaly?.let {
                    threatFusion.updateAudioAnomaly(it)
                }
            }
        }
        
        // Combine flows from all modules for UI state
        viewModelScope.launch {
            combine(
                threatFusion.getThreatLevelFlow(),
                threatFusion.getCompositeScoreFlow(),
                audioAnalyzer.getSpectrumFlow(),
                cameraAnalyzer.getAnomalyFlow(),
                audioAnalyzer.getAnomalyFlow()
            ) { threatLevel, compositeScore, spectrum, visualAnomaly, audioAnomaly ->
                // Create threat assessment from fusion engine data
                val threatAssessment = com.hackathon.shadowsignal.domain.model.ThreatAssessment(
                    level = threatLevel,
                    compositeScore = compositeScore,
                    visualScore = 0f, // Calculated internally by fusion engine
                    audioScore = 0f,   // Calculated internally by fusion engine
                    timestamp = System.currentTimeMillis()
                )

                // Update recent anomalies list
                updateRecentAnomalies(visualAnomaly, audioAnomaly)

                Triple(threatAssessment, spectrum, recentAnomalies.toList())
            }.collect { (threatAssessment, spectrum, anomalies) ->
                _uiState.update { currentState ->
                    currentState.copy(
                        threatAssessment = threatAssessment,
                        audioSpectrum = spectrum,
                        recentAnomalies = anomalies
                    )
                }
            }
        }
    }

    /**
     * Update the list of recent anomalies, maintaining a maximum size
     */
    private fun updateRecentAnomalies(
        visualAnomaly: VisualAnomaly?,
        audioAnomaly: AudioAnomaly?
    ) {
        visualAnomaly?.let {
            if (it.intensity > 0f) {
                recentAnomalies.add(0, it)
                if (recentAnomalies.size > maxAnomalies) {
                    recentAnomalies.removeAt(recentAnomalies.size - 1)
                }
            }
        }
        
        audioAnomaly?.let {
            if (it.intensity > 0f) {
                recentAnomalies.add(0, it)
                if (recentAnomalies.size > maxAnomalies) {
                    recentAnomalies.removeAt(recentAnomalies.size - 1)
                }
            }
        }
    }

    /**
     * Start scanning for anomalies
     * Initializes camera and audio analysis if permissions are granted
     * Requirements: 7.5
     */
    fun startScanning() {
        viewModelScope.launch {
            val permissionState = permissionManager.getPermissionStateFlow().value
            
            _uiState.update { it.copy(isScanning = true) }

            // Start camera analysis if permission granted
            if (permissionState.cameraGranted) {
                try {
                    // Camera will be started when lifecycle owner is provided
                    // This is handled in the UI layer
                } catch (e: Exception) {
                    handleError("Camera initialization failed: ${e.message}")
                }
            }

            // Start audio analysis if permission granted
            if (permissionState.microphoneGranted) {
                try {
                    audioAnalyzer.startRecording()
                } catch (e: Exception) {
                    handleError("Audio initialization failed: ${e.message}")
                }
            }
        }
    }

    /**
     * Stop scanning for anomalies
     * Stops camera and audio analysis
     * Requirements: 7.5
     */
    fun stopScanning() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = false) }

            try {
                cameraAnalyzer.stopAnalysis()
            } catch (e: Exception) {
                handleError("Camera stop failed: ${e.message}")
            }

            try {
                audioAnalyzer.stopRecording()
            } catch (e: Exception) {
                handleError("Audio stop failed: ${e.message}")
            }

            // Clear recent anomalies when stopping
            recentAnomalies.clear()
        }
    }

    /**
     * Handle errors by updating UI state with error message
     */
    private fun handleError(message: String) {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = message
            )
        }
    }

    /**
     * Clear error message from UI state
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Handle lifecycle events properly
     * Clean up resources when ViewModel is cleared
     * Requirements: 7.5
     */
    override fun onCleared() {
        super.onCleared()
        // Stop scanning to release resources
        viewModelScope.launch {
            stopScanning()
        }
    }
}
