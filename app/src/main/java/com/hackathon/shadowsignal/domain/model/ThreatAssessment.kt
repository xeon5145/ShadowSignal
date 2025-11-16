package com.hackathon.shadowsignal.domain.model

/**
 * Threat assessment combining visual and audio anomaly scores
 * Requirements: 7.1, 7.2
 */
data class ThreatAssessment(
    val level: ThreatLevel,
    val compositeScore: Float,
    val visualScore: Float,
    val audioScore: Float,
    val timestamp: Long
)

/**
 * Threat level categories
 * LOW: Score < 30
 * MEDIUM: Score 30-70
 * HIGH: Score > 70
 */
enum class ThreatLevel {
    LOW,
    MEDIUM,
    HIGH
}
