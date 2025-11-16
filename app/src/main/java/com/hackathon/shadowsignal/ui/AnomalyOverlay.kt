package com.hackathon.shadowsignal.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hackathon.shadowsignal.domain.model.Anomaly
import com.hackathon.shadowsignal.domain.model.AudioAnomaly
import com.hackathon.shadowsignal.domain.model.AudioAnomalyType
import com.hackathon.shadowsignal.domain.model.VisualAnomaly
import com.hackathon.shadowsignal.domain.model.VisualAnomalyType
import com.hackathon.shadowsignal.ui.theme.NeonCyan
import com.hackathon.shadowsignal.ui.theme.NeonGreen
import com.hackathon.shadowsignal.ui.theme.NeonRed
import com.hackathon.shadowsignal.ui.theme.neonGlow
import kotlinx.coroutines.delay

/**
 * Anomaly overlay composable that displays floating indicators for detected anomalies
 * 
 * Features:
 * - Floating indicators for each anomaly
 * - Shows anomaly type and intensity
 * - Fade in/out animations
 * - Positioned based on camera region if available
 * - Overlays on camera preview
 * 
 * Requirements: 4.7
 * 
 * @param anomalies List of recent anomalies to display
 * @param modifier Optional modifier
 */
@Composable
fun AnomalyOverlay(
    anomalies: List<Anomaly>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Display up to 5 most recent anomalies
        anomalies.take(5).forEachIndexed { index, anomaly ->
            AnomalyIndicator(
                anomaly = anomaly,
                index = index,
                modifier = Modifier.align(Alignment.TopStart)
            )
        }
    }
}

/**
 * Individual anomaly indicator
 * 
 * @param anomaly The anomaly to display
 * @param index Index for positioning multiple indicators
 * @param modifier Optional modifier
 */
@Composable
private fun AnomalyIndicator(
    anomaly: Anomaly,
    index: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember(anomaly.timestamp) { mutableStateOf(true) }

    // Auto-hide after 3 seconds
    LaunchedEffect(anomaly.timestamp) {
        delay(3000)
        isVisible = false
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(500)),
        modifier = modifier
    ) {
        val (color, typeText) = when (anomaly) {
            is VisualAnomaly -> {
                val text = when (anomaly.type) {
                    VisualAnomalyType.MOTION -> "MOTION"
                    VisualAnomalyType.LIGHT_CHANGE -> "LIGHT"
                    VisualAnomalyType.CONTOUR -> "SHAPE"
                }
                NeonGreen to text
            }
            is AudioAnomaly -> {
                val text = when (anomaly.type) {
                    AudioAnomalyType.FREQUENCY -> "FREQ ${anomaly.frequency?.toInt() ?: ""}Hz"
                    AudioAnomalyType.SPIKE -> "SPIKE"
                }
                NeonCyan to text
            }
        }

        // Position based on anomaly region or stack vertically
        val offsetY = if (anomaly is VisualAnomaly && anomaly.affectedRegion != null) {
            anomaly.affectedRegion.top
        } else {
            80 + (index * 60) // Stack indicators vertically
        }

        val offsetX = if (anomaly is VisualAnomaly && anomaly.affectedRegion != null) {
            anomaly.affectedRegion.left
        } else {
            16 // Default left margin
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX, offsetY) }
                .background(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 2.dp,
                    color = color,
                    shape = RoundedCornerShape(8.dp)
                )
                .neonGlow(color, blurRadius = 8.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                Text(
                    text = typeText,
                    style = MaterialTheme.typography.labelLarge,
                    color = color
                )
                Text(
                    text = "Intensity: ${(anomaly.intensity * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color.copy(alpha = 0.8f)
                )
            }
        }
    }
}
