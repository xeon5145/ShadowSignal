package com.hackathon.shadowsignal.domain

import com.hackathon.shadowsignal.domain.model.AudioAnomaly
import com.hackathon.shadowsignal.domain.model.ThreatLevel
import com.hackathon.shadowsignal.domain.model.VisualAnomaly
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for threat fusion engine
 * Combines camera and audio signals into unified threat assessment
 * Requirements: 7.1
 */
interface ThreatFusion {
    /**
     * Update with new visual anomaly data
     */
    fun updateVisualAnomaly(anomaly: VisualAnomaly)
    
    /**
     * Update with new audio anomaly data
     */
    fun updateAudioAnomaly(anomaly: AudioAnomaly)
    
    /**
     * Get flow of current threat level
     */
    fun getThreatLevelFlow(): StateFlow<ThreatLevel>
    
    /**
     * Get flow of composite score (0-100)
     */
    fun getCompositeScoreFlow(): StateFlow<Float>
}
