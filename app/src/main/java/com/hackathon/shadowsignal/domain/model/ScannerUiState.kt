package com.hackathon.shadowsignal.domain.model

/**
 * UI state for the Scanner screen
 * Requirements: 7.1, 7.2
 */
data class ScannerUiState(
    val threatAssessment: ThreatAssessment,
    val recentAnomalies: List<Anomaly>,
    val audioSpectrum: FloatArray,
    val permissionsGranted: Boolean,
    val isScanning: Boolean,
    val errorMessage: String? = null
) {
    companion object {
        /**
         * Default UI state with safe initial values
         */
        fun default() = ScannerUiState(
            threatAssessment = ThreatAssessment(
                level = ThreatLevel.LOW,
                compositeScore = 0f,
                visualScore = 0f,
                audioScore = 0f,
                timestamp = System.currentTimeMillis()
            ),
            recentAnomalies = emptyList(),
            audioSpectrum = FloatArray(0),
            permissionsGranted = false,
            isScanning = false,
            errorMessage = null
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScannerUiState

        if (threatAssessment != other.threatAssessment) return false
        if (recentAnomalies != other.recentAnomalies) return false
        if (!audioSpectrum.contentEquals(other.audioSpectrum)) return false
        if (permissionsGranted != other.permissionsGranted) return false
        if (isScanning != other.isScanning) return false
        if (errorMessage != other.errorMessage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = threatAssessment.hashCode()
        result = 31 * result + recentAnomalies.hashCode()
        result = 31 * result + audioSpectrum.contentHashCode()
        result = 31 * result + permissionsGranted.hashCode()
        result = 31 * result + isScanning.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        return result
    }
}
