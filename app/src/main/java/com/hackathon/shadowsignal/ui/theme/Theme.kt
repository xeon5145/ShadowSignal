package com.hackathon.shadowsignal.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Dark color scheme for Shadow Signal app
 * Uses neon green, cyan, and red accents on dark background
 */
private val ShadowSignalColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = DarkBackground,
    primaryContainer = NeonGreenDim,
    onPrimaryContainer = TextPrimary,
    
    secondary = NeonCyan,
    onSecondary = DarkBackground,
    secondaryContainer = NeonCyanDim,
    onSecondaryContainer = TextPrimary,
    
    tertiary = NeonRed,
    onTertiary = DarkBackground,
    tertiaryContainer = NeonRedDim,
    onTertiaryContainer = TextPrimary,
    
    error = NeonRed,
    onError = DarkBackground,
    errorContainer = NeonRedDim,
    onErrorContainer = TextPrimary,
    
    background = DarkBackground,
    onBackground = TextPrimary,
    
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    
    outline = TextTertiary,
    outlineVariant = DarkSurfaceVariant,
    
    scrim = Color.Black.copy(alpha = 0.8f)
)

/**
 * Main theme composable for Shadow Signal app
 * Applies dark theme with neon accents and monospace typography
 * 
 * @param darkTheme Whether to use dark theme (always true for this app)
 * @param content The composable content to wrap with the theme
 */
@Composable
fun ShadowSignalTheme(
    darkTheme: Boolean = true, // Always dark for spooky aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ShadowSignalColorScheme,
        typography = ShadowSignalTypography,
        content = content
    )
}

/**
 * Modifier extension to add neon glow effect to composables
 * Creates a shadow with the specified color and blur radius
 * 
 * @param color The glow color
 * @param blurRadius The blur radius for the glow effect
 * @param alpha The alpha transparency of the glow
 * @return Modified Modifier with glow effect
 */
fun Modifier.neonGlow(
    color: Color,
    blurRadius: Dp = 16.dp,
    alpha: Float = GlowAlpha
): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            this.color = color.copy(alpha = alpha)
        }
        
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.setShadowLayer(
            blurRadius.toPx(),
            0f,
            0f,
            color.copy(alpha = alpha).toArgb()
        )
        
        canvas.drawRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            paint = paint
        )
    }
}

/**
 * Modifier extension to add green neon glow effect
 * Convenience function for the most common glow color
 * 
 * @param blurRadius The blur radius for the glow effect
 * @param alpha The alpha transparency of the glow
 * @return Modified Modifier with green glow effect
 */
fun Modifier.greenGlow(
    blurRadius: Dp = 16.dp,
    alpha: Float = GlowAlpha
): Modifier = this.neonGlow(NeonGreen, blurRadius, alpha)

/**
 * Modifier extension to add cyan neon glow effect
 * 
 * @param blurRadius The blur radius for the glow effect
 * @param alpha The alpha transparency of the glow
 * @return Modified Modifier with cyan glow effect
 */
fun Modifier.cyanGlow(
    blurRadius: Dp = 16.dp,
    alpha: Float = GlowAlpha
): Modifier = this.neonGlow(NeonCyan, blurRadius, alpha)

/**
 * Modifier extension to add red neon glow effect
 * 
 * @param blurRadius The blur radius for the glow effect
 * @param alpha The alpha transparency of the glow
 * @return Modified Modifier with red glow effect
 */
fun Modifier.redGlow(
    blurRadius: Dp = 16.dp,
    alpha: Float = GlowAlpha
): Modifier = this.neonGlow(NeonRed, blurRadius, alpha)

/**
 * Modifier extension to add threat-level-based glow effect
 * Automatically selects color based on threat level
 * 
 * @param threatLevel The current threat level (LOW, MEDIUM, HIGH)
 * @param blurRadius The blur radius for the glow effect
 * @param alpha The alpha transparency of the glow
 * @return Modified Modifier with threat-appropriate glow effect
 */
fun Modifier.threatGlow(
    threatLevel: String,
    blurRadius: Dp = 16.dp,
    alpha: Float = GlowAlpha
): Modifier {
    val color = when (threatLevel.uppercase()) {
        "LOW" -> ThreatLow
        "MEDIUM" -> ThreatMedium
        "HIGH" -> ThreatHigh
        else -> NeonGreen
    }
    return this.neonGlow(color, blurRadius, alpha)
}
