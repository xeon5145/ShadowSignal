package com.hackathon.shadowsignal.data

import androidx.lifecycle.LifecycleOwner
import com.hackathon.shadowsignal.domain.model.VisualAnomaly
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for camera analysis module
 * Captures video frames and detects visual anomalies
 * Requirements: 7.1
 */
interface CameraAnalyzer {
    /**
     * Start camera analysis bound to the given lifecycle
     */
    fun startAnalysis(lifecycleOwner: LifecycleOwner)
    
    /**
     * Stop camera analysis and release resources
     */
    fun stopAnalysis()
    
    /**
     * Get flow of visual anomalies detected by camera
     * Emits null when no anomaly is detected
     */
    fun getAnomalyFlow(): StateFlow<VisualAnomaly?>
}
