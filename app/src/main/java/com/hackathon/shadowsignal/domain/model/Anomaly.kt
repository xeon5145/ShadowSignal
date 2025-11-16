package com.hackathon.shadowsignal.domain.model

import android.graphics.Rect

/**
 * Base sealed class for all anomaly types
 * Requirements: 7.1, 7.2
 */
sealed class Anomaly {
    abstract val intensity: Float
    abstract val timestamp: Long
}

/**
 * Visual anomaly detected by camera analysis
 */
data class VisualAnomaly(
    override val intensity: Float,
    override val timestamp: Long,
    val type: VisualAnomalyType,
    val affectedRegion: Rect? = null
) : Anomaly()

/**
 * Types of visual anomalies
 */
enum class VisualAnomalyType {
    MOTION,
    LIGHT_CHANGE,
    CONTOUR
}

/**
 * Audio anomaly detected by audio analysis
 */
data class AudioAnomaly(
    override val intensity: Float,
    override val timestamp: Long,
    val type: AudioAnomalyType,
    val frequency: Float? = null,
    val amplitude: Float? = null
) : Anomaly()

/**
 * Types of audio anomalies
 */
enum class AudioAnomalyType {
    FREQUENCY,
    SPIKE
}
