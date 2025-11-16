package com.hackathon.shadowsignal.ui

import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.hackathon.shadowsignal.data.CameraAnalyzerImpl

/**
 * Camera preview composable that displays the live camera feed
 * 
 * Uses AndroidView to wrap CameraX PreviewView and adds a dark overlay
 * for the spooky aesthetic effect.
 * 
 * Requirements: 1.1, 4.7
 * 
 * @param cameraAnalyzer The camera analyzer implementation that manages CameraX
 * @param lifecycleOwner Lifecycle owner for camera lifecycle binding
 * @param modifier Optional modifier for the preview
 */
@Composable
fun CameraPreview(
    cameraAnalyzer: CameraAnalyzerImpl,
    lifecycleOwner: LifecycleOwner,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Box(modifier = modifier.fillMaxSize()) {
        // CameraX PreviewView wrapped in AndroidView
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    // Set scale type to fill the view
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            update = { previewView ->
                // Start camera analysis and bind preview surface
                cameraAnalyzer.startAnalysis(lifecycleOwner)
                
                // Get the Preview use case and set the surface provider
                cameraAnalyzer.getPreview()?.setSurfaceProvider(previewView.surfaceProvider)
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Dark overlay for spooky effect (semi-transparent black)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
    }
}
