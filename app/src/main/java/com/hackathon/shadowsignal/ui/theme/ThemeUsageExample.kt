package com.hackathon.shadowsignal.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Example composable demonstrating theme usage and glow effects
 * This file serves as a reference for how to use the Shadow Signal theme
 */
@Composable
fun ThemeUsageExample() {
    ShadowSignalTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Example 1: Text with green glow
            Text(
                text = "ANOMALY DETECTED",
                style = MaterialTheme.typography.headlineLarge,
                color = NeonGreen,
                modifier = Modifier.greenGlow()
            )
            
            // Example 2: Box with cyan glow
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(DarkSurface)
                    .cyanGlow(blurRadius = 20.dp)
            )
            
            // Example 3: Text with threat-based glow
            Text(
                text = "THREAT LEVEL: HIGH",
                style = MaterialTheme.typography.titleLarge,
                color = ThreatHigh,
                modifier = Modifier.threatGlow("HIGH")
            )
            
            // Example 4: Custom neon glow
            Text(
                text = "SCANNING...",
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                modifier = Modifier.neonGlow(
                    color = NeonCyan,
                    blurRadius = 12.dp,
                    alpha = 0.8f
                )
            )
        }
    }
}
