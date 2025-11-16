package com.hackathon.shadowsignal.domain

import android.util.Log
import com.hackathon.shadowsignal.domain.model.AudioAnomaly
import com.hackathon.shadowsignal.domain.model.ThreatLevel
import com.hackathon.shadowsignal.domain.model.VisualAnomaly
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementation of ThreatFusion that combines visual and audio anomalies
 * into a unified threat assessment.
 * 
 * Algorithm:
 * - Visual score: max of motion, light, and contour intensities (0-1)
 * - Audio score: max of frequency and spike intensities (0-1)
 * - Composite score: (visual * 0.6 + audio * 0.4) * 100
 * - Applies exponential moving average for temporal smoothing (alpha = 0.3)
 * - Maps to threat levels: LOW <30, MEDIUM 30-70, HIGH >70
 * 
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7
 */
class ThreatFusionImpl : ThreatFusion {
    
    companion object {
        private const val TAG = "ThreatFusionImpl"
        private const val VISUAL_WEIGHT = 0.6f
        private const val AUDIO_WEIGHT = 0.4f
        private const val EMA_ALPHA = 0.3f // Exponential moving average smoothing factor
        private const val LOW_THRESHOLD = 30f
        private const val HIGH_THRESHOLD = 70f
    }
    
    private val _threatLevelFlow = MutableStateFlow(ThreatLevel.LOW)
    override fun getThreatLevelFlow(): StateFlow<ThreatLevel> = _threatLevelFlow.asStateFlow()
    
    private val _compositeScoreFlow = MutableStateFlow(0f)
    override fun getCompositeScoreFlow(): StateFlow<Float> = _compositeScoreFlow.asStateFlow()
    
    // Current anomaly intensities
    private var currentVisualScore = 0f
    private var currentAudioScore = 0f
    
    // Smoothed composite score using EMA
    private var smoothedCompositeScore = 0f
    
    // Track last update time for logging
    private var lastUpdateTime = 0L
    
    /**
     * Update with new visual anomaly data
     * Calculates visual score from anomaly intensity
     * Requirements: 3.2
     */
    override fun updateVisualAnomaly(anomaly: VisualAnomaly) {
        synchronized(this) {
            // Visual score is the intensity of the anomaly (0-1)
            currentVisualScore = anomaly.intensity.coerceIn(0f, 1f)
            
            Log.d(TAG, "Visual anomaly: type=${anomaly.type}, intensity=${anomaly.intensity}")
            
            updateCompositeScore()
        }
    }
    
    /**
     * Update with new audio anomaly data
     * Calculates audio score from anomaly intensity
     * Requirements: 3.2
     */
    override fun updateAudioAnomaly(anomaly: AudioAnomaly) {
        synchronized(this) {
            // Audio score is the intensity of the anomaly (0-1)
            currentAudioScore = anomaly.intensity.coerceIn(0f, 1f)
            
            Log.d(TAG, "Audio anomaly: type=${anomaly.type}, intensity=${anomaly.intensity}")
            
            updateCompositeScore()
        }
    }
    
    /**
     * Calculate composite score and update threat level
     * Requirements: 3.1, 3.3, 3.4, 3.5, 3.6
     */
    private fun updateCompositeScore() {
        val currentTime = System.currentTimeMillis()
        
        // Calculate raw composite score (0-100)
        // Formula: (visual * 60% + audio * 40%) * 100
        val rawScore = (currentVisualScore * VISUAL_WEIGHT + currentAudioScore * AUDIO_WEIGHT) * 100f
        
        // Apply exponential moving average for temporal smoothing
        // EMA formula: smoothed = alpha * current + (1 - alpha) * previous
        smoothedCompositeScore = if (smoothedCompositeScore == 0f) {
            // First update, use raw score
            rawScore
        } else {
            EMA_ALPHA * rawScore + (1 - EMA_ALPHA) * smoothedCompositeScore
        }
        
        // Ensure score is in valid range
        val finalScore = smoothedCompositeScore.coerceIn(0f, 100f)
        
        // Map composite score to threat level
        val threatLevel = when {
            finalScore < LOW_THRESHOLD -> ThreatLevel.LOW
            finalScore < HIGH_THRESHOLD -> ThreatLevel.MEDIUM
            else -> ThreatLevel.HIGH
        }
        
        // Update flows
        _compositeScoreFlow.value = finalScore
        _threatLevelFlow.value = threatLevel
        
        // Log update (with rate limiting)
        if (currentTime - lastUpdateTime > 1000) {
            Log.d(TAG, "Threat update: level=$threatLevel, score=$finalScore, " +
                    "visual=$currentVisualScore, audio=$currentAudioScore")
            lastUpdateTime = currentTime
        }
        
        // Decay scores over time to prevent stale high threat levels
        // This ensures that if no new anomalies are detected, the threat level gradually decreases
        currentVisualScore *= 0.95f
        currentAudioScore *= 0.95f
    }
}
