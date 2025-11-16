package com.hackathon.shadowsignal.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.hackathon.shadowsignal.ui.theme.NeonGreen
import com.hackathon.shadowsignal.ui.theme.greenGlow

/**
 * Waveform visualizer composable that displays real-time audio waveform
 * 
 * Features:
 * - Canvas-based waveform display
 * - Connected line graph of audio samples
 * - Neon green color with glow effect
 * - Updates at 30 FPS
 * 
 * Requirements: 4.3
 * 
 * @param audioSamples Array of audio sample values (normalized -1.0 to 1.0)
 * @param modifier Optional modifier
 */
@Composable
fun WaveformVisualizer(
    audioSamples: FloatArray,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .greenGlow(blurRadius = 12.dp)
    ) {
        if (audioSamples.isEmpty()) return@Canvas
        
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2
        val amplitude = canvasHeight / 2 * 0.8f // Use 80% of available height
        
        // Calculate step size for samples
        val sampleCount = audioSamples.size.coerceAtMost(200) // Limit to 200 points for performance
        val step = if (audioSamples.size > sampleCount) {
            audioSamples.size / sampleCount
        } else {
            1
        }
        val xStep = canvasWidth / sampleCount
        
        // Build path for waveform
        val path = Path()
        var isFirstPoint = true
        
        for (i in 0 until sampleCount) {
            val sampleIndex = (i * step).coerceAtMost(audioSamples.size - 1)
            val sample = audioSamples[sampleIndex].coerceIn(-1f, 1f)
            val x = i * xStep
            val y = centerY - (sample * amplitude)
            
            if (isFirstPoint) {
                path.moveTo(x, y)
                isFirstPoint = false
            } else {
                path.lineTo(x, y)
            }
        }
        
        // Draw waveform path
        drawPath(
            path = path,
            color = NeonGreen,
            style = Stroke(
                width = 2f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        
        // Draw center line (zero amplitude reference)
        drawLine(
            color = NeonGreen.copy(alpha = 0.3f),
            start = Offset(0f, centerY),
            end = Offset(canvasWidth, centerY),
            strokeWidth = 1f
        )
    }
}
