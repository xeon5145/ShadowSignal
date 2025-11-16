package com.hackathon.shadowsignal.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hackathon.shadowsignal.domain.model.ThreatLevel
import com.hackathon.shadowsignal.ui.theme.NeonGreen
import com.hackathon.shadowsignal.ui.theme.NeonRed
import com.hackathon.shadowsignal.ui.theme.ThreatHigh
import com.hackathon.shadowsignal.ui.theme.ThreatLow
import com.hackathon.shadowsignal.ui.theme.ThreatMedium
import com.hackathon.shadowsignal.ui.theme.neonGlow
import kotlin.math.cos
import kotlin.math.sin

/**
 * Threat meter composable that displays current threat level as an arc gauge
 * 
 * Features:
 * - Circular arc gauge showing threat score (0-100)
 * - Color-coded by threat level (Green/Yellow/Red)
 * - Animated transitions
 * - Neon glow effect
 * 
 * Requirements: 4.5, 4.6
 * 
 * @param threatLevel Current threat level
 * @param compositeScore Current composite score (0-100)
 * @param modifier Optional modifier
 */
@Composable
fun ThreatMeter(
    threatLevel: ThreatLevel,
    compositeScore: Float,
    modifier: Modifier = Modifier
) {
    // Animate score changes
    val animatedScore by animateFloatAsState(
        targetValue = compositeScore.coerceIn(0f, 100f),
        animationSpec = tween(durationMillis = 500),
        label = "threat_score_animation"
    )
    
    // Get color based on threat level
    val meterColor = when (threatLevel) {
        ThreatLevel.LOW -> ThreatLow
        ThreatLevel.MEDIUM -> ThreatMedium
        ThreatLevel.HIGH -> ThreatHigh
    }
    
    Box(
        modifier = modifier
            .size(200.dp)
            .neonGlow(meterColor, blurRadius = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Arc gauge
        Canvas(modifier = Modifier.size(180.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2
            val centerY = canvasHeight / 2
            val radius = (canvasWidth / 2) * 0.8f
            
            // Background arc (dark gray)
            drawArc(
                color = Color.DarkGray.copy(alpha = 0.3f),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 20f, cap = StrokeCap.Round)
            )
            
            // Foreground arc (threat level color)
            val sweepAngle = (animatedScore / 100f) * 270f
            drawArc(
                color = meterColor,
                startAngle = 135f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 20f, cap = StrokeCap.Round)
            )
            
            // Draw needle indicator
            val needleAngle = Math.toRadians((135 + sweepAngle).toDouble())
            val needleLength = radius * 0.9f
            val needleEndX = centerX + (needleLength * cos(needleAngle)).toFloat()
            val needleEndY = centerY + (needleLength * sin(needleAngle)).toFloat()
            
            drawLine(
                color = meterColor,
                start = Offset(centerX, centerY),
                end = Offset(needleEndX, needleEndY),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            
            // Center dot
            drawCircle(
                color = meterColor,
                radius = 8f,
                center = Offset(centerX, centerY)
            )
        }
        
        // Text overlay
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Text(
                text = "${animatedScore.toInt()}",
                style = MaterialTheme.typography.displayMedium,
                color = meterColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = threatLevel.name,
                style = MaterialTheme.typography.titleMedium,
                color = meterColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
