package com.hackathon.shadowsignal.data

import com.hackathon.shadowsignal.domain.model.AudioAnomaly
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for audio analysis module
 * Captures audio and detects spectral anomalies
 * Requirements: 7.1
 */
interface AudioAnalyzer {
    /**
     * Start audio recording and analysis
     */
    fun startRecording()
    
    /**
     * Stop audio recording and release resources
     */
    fun stopRecording()
    
    /**
     * Get flow of audio anomalies detected
     * Emits null when no anomaly is detected
     */
    fun getAnomalyFlow(): StateFlow<AudioAnomaly?>
    
    /**
     * Get flow of frequency spectrum data for visualization
     * Returns array of magnitude values for each frequency bin
     */
    fun getSpectrumFlow(): StateFlow<FloatArray>
}
