package com.hackathon.shadowsignal.data

import android.graphics.Rect
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.hackathon.shadowsignal.domain.model.VisualAnomaly
import com.hackathon.shadowsignal.domain.model.VisualAnomalyType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

/**
 * Implementation of CameraAnalyzer using CameraX
 * Detects visual anomalies through frame analysis
 * Requirements: 1.1, 1.2, 1.3, 1.5, 6.1, 6.2
 */
class CameraAnalyzerImpl(
    private val cameraProvider: ProcessCameraProvider
) : CameraAnalyzer {
    
    companion object {
        private const val TAG = "CameraAnalyzerImpl"
        private const val TARGET_WIDTH = 640
        private const val TARGET_HEIGHT = 480
        private const val MOTION_THRESHOLD = 0.15f // 15% pixel change
        private const val BRIGHTNESS_THRESHOLD = 0.30f // 30% brightness change
        private const val FRAME_SKIP = 1 // Process every 2nd frame (0 = every frame, 1 = every 2nd)
    }
    
    private val _anomalyFlow = MutableStateFlow<VisualAnomaly?>(null)
    override fun getAnomalyFlow(): StateFlow<VisualAnomaly?> = _anomalyFlow.asStateFlow()
    
    private var camera: Camera? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var preview: Preview? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    // Frame processing state
    private var previousGrayscale: ByteArray? = null
    private var previousBrightness: Float = 0f
    private var frameCounter = 0
    
    // Buffer reuse for performance
    private var grayscaleBuffer: ByteArray? = null
    
    override fun startAnalysis(lifecycleOwner: LifecycleOwner) {
        try {
            Log.d(TAG, "Starting camera analysis")
            
            // Unbind any existing use cases
            cameraProvider.unbindAll()
            
            // Set up Preview use case
            preview = Preview.Builder()
                .build()
            
            // Set up ImageAnalysis use case
            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(TARGET_WIDTH, TARGET_HEIGHT))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        processFrame(imageProxy)
                    }
                }
            
            // Select back camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            // Bind use cases to lifecycle
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            
            Log.d(TAG, "Camera analysis started successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start camera analysis", e)
        }
    }
    
    override fun stopAnalysis() {
        try {
            Log.d(TAG, "Stopping camera analysis")
            cameraProvider.unbindAll()
            camera = null
            imageAnalysis = null
            preview = null
            previousGrayscale = null
            grayscaleBuffer = null
            frameCounter = 0
            _anomalyFlow.value = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping camera analysis", e)
        }
    }
    
    /**
     * Get the Preview use case for UI binding
     */
    fun getPreview(): Preview? = preview
    
    /**
     * Process a single camera frame
     * Performs motion detection and brightness change detection
     */
    private fun processFrame(imageProxy: ImageProxy) {
        try {
            // Frame skipping for performance optimization
            frameCounter++
            if (frameCounter % (FRAME_SKIP + 1) != 0) {
                imageProxy.close()
                return
            }
            
            val width = imageProxy.width
            val height = imageProxy.height
            val timestamp = System.currentTimeMillis()
            
            // Convert frame to grayscale
            val grayscale = convertToGrayscale(imageProxy, width, height)
            
            // Calculate brightness
            val currentBrightness = calculateMeanLuminance(grayscale)
            
            // Detect anomalies
            var detectedAnomaly: VisualAnomaly? = null
            
            // Motion detection (image diff)
            if (previousGrayscale != null) {
                val motionIntensity = detectMotion(grayscale, previousGrayscale!!, width, height)
                if (motionIntensity > MOTION_THRESHOLD) {
                    detectedAnomaly = VisualAnomaly(
                        intensity = motionIntensity,
                        timestamp = timestamp,
                        type = VisualAnomalyType.MOTION,
                        affectedRegion = null
                    )
                    Log.d(TAG, "Motion detected: intensity=$motionIntensity")
                }
            }
            
            // Brightness change detection
            if (previousBrightness > 0f && detectedAnomaly == null) {
                val brightnessChange = abs(currentBrightness - previousBrightness) / previousBrightness
                if (brightnessChange > BRIGHTNESS_THRESHOLD) {
                    detectedAnomaly = VisualAnomaly(
                        intensity = brightnessChange.coerceAtMost(1.0f),
                        timestamp = timestamp,
                        type = VisualAnomalyType.LIGHT_CHANGE,
                        affectedRegion = null
                    )
                    Log.d(TAG, "Brightness change detected: change=$brightnessChange")
                }
            }
            
            // Update state
            previousGrayscale = grayscale
            previousBrightness = currentBrightness
            
            // Emit anomaly or null
            _anomalyFlow.value = detectedAnomaly
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing frame", e)
        } finally {
            imageProxy.close()
        }
    }
    
    /**
     * Convert YUV_420_888 image to grayscale
     * Reuses buffer to reduce GC pressure
     */
    private fun convertToGrayscale(imageProxy: ImageProxy, width: Int, height: Int): ByteArray {
        // Reuse buffer if possible
        val size = width * height
        if (grayscaleBuffer == null || grayscaleBuffer!!.size != size) {
            grayscaleBuffer = ByteArray(size)
        }
        val grayscale = grayscaleBuffer!!
        
        // YUV_420_888 format: Y plane contains luminance (grayscale)
        val yBuffer = imageProxy.planes[0].buffer
        val yRowStride = imageProxy.planes[0].rowStride
        val yPixelStride = imageProxy.planes[0].pixelStride
        
        var outputIndex = 0
        for (y in 0 until height) {
            var bufferIndex = y * yRowStride
            for (x in 0 until width) {
                grayscale[outputIndex++] = yBuffer[bufferIndex].toByte()
                bufferIndex += yPixelStride
            }
        }
        
        return grayscale
    }
    
    /**
     * Calculate mean luminance of grayscale image
     */
    private fun calculateMeanLuminance(grayscale: ByteArray): Float {
        var sum = 0L
        for (pixel in grayscale) {
            sum += (pixel.toInt() and 0xFF)
        }
        return sum.toFloat() / grayscale.size
    }
    
    /**
     * Detect motion by comparing current and previous frames
     * Returns intensity as percentage of changed pixels
     */
    private fun detectMotion(
        current: ByteArray,
        previous: ByteArray,
        width: Int,
        height: Int
    ): Float {
        val threshold = 30 // Pixel difference threshold
        var changedPixels = 0
        val totalPixels = width * height
        
        for (i in current.indices) {
            val diff = abs((current[i].toInt() and 0xFF) - (previous[i].toInt() and 0xFF))
            if (diff > threshold) {
                changedPixels++
            }
        }
        
        return changedPixels.toFloat() / totalPixels
    }
    
    /**
     * Clean up resources
     */
    fun release() {
        stopAnalysis()
        cameraExecutor.shutdown()
    }
}
