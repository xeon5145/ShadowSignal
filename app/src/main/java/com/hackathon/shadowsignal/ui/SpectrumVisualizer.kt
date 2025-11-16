package com.hackathon.shadowsignal.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.hackathon.shadowsignal.ui.theme.NeonCyan
import com.hackathon.shadowsignal.ui.theme.NeonRed
import com.hackathon.shadowsignal.ui.theme.cyanGlow
import kotlin.math.log10
import kotlin.math.pow

/**
 * Spectrum visualizer composable that displays frequency spectrum as bars
 * 
 * Features:
 * - Canvas-based frequency spectrum bars
 * - Logarithmic frequency scale for x-axis
 * - Cyan bars with glow effect
 * - Highlights anomaly frequencies in red
 * - Updates in real-time from FFT data
 * 
 * Requirements: 4.4
 * 
 * @param spectrum Array of FFT magnitude values
 * @param anomalyFrequencies Set of frequencies to highlight as anomalies (in Hz)
 * @param sampleRate Sample rate of audio (default 44100 Hz)
 * @param modifier Optional modifier
 */
@Composable
fun SpectrumVisualizer(
    spectrum: FloatArray,
    anomalyFrequencies: Set<Float> = emptySet(),
    sampleRate: Int = 44100,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .cyanGlow(blurRadius = 12.dp)
    ) {
        if (spectrum.isEmpty()) return@Canvas
        
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Number of bars to display
        val barCount = 64.coerceAtMost(spectrum.size / 2)
        val barWidth = (canvasWidth / barCount) * 0.8f
        val barSpacing = canvasWidth / barCount
        
        // Frequency range: 20 Hz to 20 kHz (human hearing range)
        val minFreq = 20f
        val maxFreq = 20000f
        val freqPerBin = sampleRate.toFloat() / spectrum.size
        
        for (i in 0 until barCount) {
            // Logarithmic frequency mapping
            val logMin = log10(minFreq)
            val logMax = log10(maxFreq)
            val logFreq = logMin + (i.toFloat() / barCount) * (logMax - logMin)
            val freq = 10f.pow(logFreq)
            
            // Map frequency to spectrum bin
            val binIndex = (freq / freqPerBin).toInt().coerceIn(0, spectrum.size - 1)
            
            // Get magnitude and normalize (assuming spectrum values are in dB or normalized)
            val magnitude = spectrum[binIndex].coerceIn(0f, 1f)
            val barHeight = magnitude * canvasHeight * 0.9f
            
            // Check if this frequency is an anomaly
            val isAnomaly = anomalyFrequencies.any { anomalyFreq ->
                freq >= anomalyFreq * 0.9f && freq <= anomalyFreq * 1.1f // 10% tolerance
            }
            
            val barColor = if (isAnomaly) NeonRed else NeonCyan
            
            // Draw bar
            val x = i * barSpacing + (barSpacing - barWidth) / 2
            val y = canvasHeight - barHeight
            
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
        
        // Draw baseline
        drawLine(
            color = NeonCyan.copy(alpha = 0.3f),
            start = Offset(0f, canvasHeight),
            end = Offset(canvasWidth, canvasHeight),
            strokeWidth = 1f
        )
    }
}
