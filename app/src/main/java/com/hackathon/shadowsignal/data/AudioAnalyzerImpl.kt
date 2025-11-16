package com.hackathon.shadowsignal.data

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.hackathon.shadowsignal.domain.model.AudioAnomaly
import com.hackathon.shadowsignal.domain.model.AudioAnomalyType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.PI
import kotlin.math.sqrt

/**
 * Implementation of AudioAnalyzer interface
 * Captures audio using AudioRecord and performs real-time analysis
 * Requirements: 2.1
 */
class AudioAnalyzerImpl : AudioAnalyzer {
    
    // Audio configuration constants
    private companion object {
        const val SAMPLE_RATE = 44100 // Hz
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        const val BUFFER_SIZE_MULTIPLIER = 2
        const val FFT_WINDOW_SIZE = 2048 // Must be power of 2 for FFT
        
        // Frequency anomaly detection thresholds (Requirements: 2.3)
        const val LOW_FREQ_THRESHOLD = 100f // Hz
        const val HIGH_FREQ_THRESHOLD = 8000f // Hz
        const val ANOMALY_DB_THRESHOLD = -40f // dB
        
        // Spike detection thresholds (Requirements: 2.4)
        const val AMPLITUDE_HISTORY_SIZE = 10 // Number of windows to track
        const val SPIKE_THRESHOLD_DB = 20f // dB above rolling average
    }
    
    // AudioRecord instance
    private var audioRecord: AudioRecord? = null
    
    // Buffer size for audio capture
    private var bufferSize: Int = 0
    
    // Background thread for audio capture
    private var captureThread: Thread? = null
    
    // Flag to control recording state
    private val isRecording = AtomicBoolean(false)
    
    // StateFlows for anomaly and spectrum data
    private val _anomalyFlow = MutableStateFlow<AudioAnomaly?>(null)
    private val _spectrumFlow = MutableStateFlow(FloatArray(0))
    
    // FFT transformer instance
    private val fftTransformer = FastFourierTransformer(DftNormalization.STANDARD)
    
    // Hamming window coefficients (pre-calculated for performance)
    private val hammingWindow = FloatArray(FFT_WINDOW_SIZE) { i ->
        (0.54 - 0.46 * cos(2.0 * PI * i / (FFT_WINDOW_SIZE - 1))).toFloat()
    }
    
    // Circular buffer for accumulating audio samples for FFT window (Requirements: 6.2)
    // Using circular buffer instead of mutableList for O(1) operations
    private val sampleBuffer = CircularBuffer(FFT_WINDOW_SIZE * 2) // 2x window size for buffering
    
    // Rolling buffer for amplitude history (Requirements: 2.4)
    private val amplitudeHistory = CircularBuffer(AMPLITUDE_HISTORY_SIZE)
    
    // Coroutine scope for FFT processing on Dispatchers.Default (Requirements: 2.5, 6.2)
    private val processingScope = CoroutineScope(Dispatchers.Default + Job())
    
    init {
        // Calculate appropriate buffer size
        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )
        
        // Use 2x minimum buffer size for stability
        bufferSize = minBufferSize * BUFFER_SIZE_MULTIPLIER
    }
    
    override fun startRecording() {
        if (isRecording.get()) {
            return // Already recording
        }
        
        try {
            // Initialize AudioRecord
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )
            
            // Check if AudioRecord was initialized successfully
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                throw IllegalStateException("AudioRecord initialization failed")
            }
            
            // Start recording
            audioRecord?.startRecording()
            isRecording.set(true)
            
            // Start background thread for audio capture
            captureThread = Thread(audioCaptureRunnable, "AudioCaptureThread").apply {
                start()
            }
            
        } catch (e: Exception) {
            // Handle initialization errors
            isRecording.set(false)
            audioRecord?.release()
            audioRecord = null
            throw e
        }
    }
    
    override fun stopRecording() {
        if (!isRecording.get()) {
            return // Not recording
        }
        
        // Signal thread to stop
        isRecording.set(false)
        
        // Wait for capture thread to finish
        captureThread?.join(1000)
        captureThread = null
        
        // Cancel processing coroutines
        processingScope.cancel()
        
        // Stop and release AudioRecord
        audioRecord?.apply {
            if (state == AudioRecord.STATE_INITIALIZED) {
                stop()
            }
            release()
        }
        audioRecord = null
        
        // Clear flows and buffers
        _anomalyFlow.value = null
        _spectrumFlow.value = FloatArray(0)
        sampleBuffer.clear()
        amplitudeHistory.clear()
    }
    
    override fun getAnomalyFlow(): StateFlow<AudioAnomaly?> = _anomalyFlow.asStateFlow()
    
    override fun getSpectrumFlow(): StateFlow<FloatArray> = _spectrumFlow.asStateFlow()
    
    /**
     * Runnable for audio capture on background thread
     */
    private val audioCaptureRunnable = Runnable {
        val audioBuffer = ShortArray(bufferSize / 2) // 16-bit samples
        
        while (isRecording.get()) {
            val readResult = audioRecord?.read(audioBuffer, 0, audioBuffer.size) ?: -1
            
            if (readResult > 0) {
                // Audio data captured successfully
                // Add samples to circular buffer for FFT processing
                for (i in 0 until readResult) {
                    sampleBuffer.add(audioBuffer[i])
                }
                
                // Process FFT when we have enough samples
                // Launch FFT processing on Dispatchers.Default coroutine (Requirements: 2.5, 6.2)
                while (sampleBuffer.size >= FFT_WINDOW_SIZE) {
                    // Extract samples for processing
                    val windowSamples = ShortArray(FFT_WINDOW_SIZE) { i ->
                        sampleBuffer.get(i)
                    }
                    
                    // Remove processed samples from circular buffer
                    repeat(FFT_WINDOW_SIZE) {
                        sampleBuffer.removeFirst()
                    }
                    
                    // Process FFT on background coroutine to avoid blocking audio capture
                    processingScope.launch {
                        processFFTWindow(windowSamples)
                    }
                }
            } else if (readResult == AudioRecord.ERROR_INVALID_OPERATION) {
                // Recording stopped or error occurred
                break
            }
        }
    }
    
    /**
     * Process a single FFT window of audio samples
     * Requirements: 2.2
     * Runs on Dispatchers.Default coroutine for optimal performance (Requirements: 2.5, 6.2)
     */
    private fun processFFTWindow(windowSamples: ShortArray) {
        try {
            // Track processing start time to ensure latency under 300ms (Requirements: 2.5)
            val startTime = System.currentTimeMillis()
            
            // Convert samples to double array and apply Hamming window
            val processedSamples = DoubleArray(FFT_WINDOW_SIZE) { i ->
                val sample = windowSamples[i].toDouble()
                // Normalize from 16-bit range (-32768 to 32767) to (-1.0 to 1.0)
                val normalized = sample / 32768.0
                // Apply Hamming window to reduce spectral leakage
                normalized * hammingWindow[i]
            }
            
            // Perform FFT using Apache Commons Math
            val fftResult = fftTransformer.transform(processedSamples, TransformType.FORWARD)
            
            // Calculate magnitude spectrum from complex FFT output
            val magnitudeSpectrum = calculateMagnitudeSpectrum(fftResult)
            
            // Emit spectrum data via StateFlow for visualizers
            _spectrumFlow.value = magnitudeSpectrum
            
            // Calculate RMS amplitude for spike detection (Requirements: 2.4)
            val rmsAmplitude = calculateRMSAmplitude(processedSamples)
            
            // Detect frequency anomalies (Requirements: 2.3)
            detectFrequencyAnomalies(magnitudeSpectrum)
            
            // Detect audio spikes (Requirements: 2.4)
            detectAudioSpike(rmsAmplitude)
            
            // Calculate processing latency
            val processingTime = System.currentTimeMillis() - startTime
            
            // Log warning if processing exceeds 300ms threshold (Requirements: 2.5)
            if (processingTime > 300) {
                System.err.println("WARNING: Audio processing latency exceeded 300ms: ${processingTime}ms")
            }
            
        } catch (e: Exception) {
            // Log error but continue processing
            // In production, would use proper logging framework
            e.printStackTrace()
        }
    }
    
    /**
     * Calculate magnitude spectrum from complex FFT output
     * Returns array of magnitude values for each frequency bin
     */
    private fun calculateMagnitudeSpectrum(fftResult: Array<Complex>): FloatArray {
        // Only use first half of FFT result (positive frequencies)
        // FFT output is symmetric for real input
        val halfSize = fftResult.size / 2
        
        return FloatArray(halfSize) { i ->
            val complex = fftResult[i]
            val real = complex.real
            val imaginary = complex.imaginary
            
            // Calculate magnitude: sqrt(re^2 + im^2)
            sqrt(real * real + imaginary * imaginary).toFloat()
        }
    }
    
    /**
     * Detect frequency anomalies in the magnitude spectrum
     * Analyzes FFT bins corresponding to <100Hz and >8000Hz ranges
     * Requirements: 2.3
     */
    private fun detectFrequencyAnomalies(magnitudeSpectrum: FloatArray) {
        try {
            // Calculate frequency resolution (Hz per bin)
            val frequencyResolution = SAMPLE_RATE.toFloat() / FFT_WINDOW_SIZE
            
            var maxAnomalyDb = Float.NEGATIVE_INFINITY
            var anomalyFrequency = 0f
            
            // Analyze each frequency bin
            for (i in magnitudeSpectrum.indices) {
                // Calculate frequency for this bin
                val frequency = i * frequencyResolution
                
                // Check if frequency is in anomaly ranges (<100Hz or >8000Hz)
                if (frequency < LOW_FREQ_THRESHOLD || frequency > HIGH_FREQ_THRESHOLD) {
                    val magnitude = magnitudeSpectrum[i]
                    
                    // Skip zero or very small magnitudes to avoid log issues
                    if (magnitude > 1e-10f) {
                        // Convert magnitude to decibels: 20 * log10(magnitude)
                        val db = 20f * log10(magnitude)
                        
                        // Check if this bin exceeds the -40dB threshold
                        if (db > ANOMALY_DB_THRESHOLD) {
                            // Track the highest anomaly
                            if (db > maxAnomalyDb) {
                                maxAnomalyDb = db
                                anomalyFrequency = frequency
                            }
                        }
                    }
                }
            }
            
            // If an anomaly was detected, emit it
            if (maxAnomalyDb > Float.NEGATIVE_INFINITY) {
                // Calculate intensity (0.0 to 1.0) based on how far above threshold
                // Map -40dB (threshold) to 0.0 and 0dB to 1.0
                val intensity = ((maxAnomalyDb - ANOMALY_DB_THRESHOLD) / (0f - ANOMALY_DB_THRESHOLD))
                    .coerceIn(0f, 1f)
                
                // Emit AudioAnomaly with FREQUENCY type
                val anomaly = AudioAnomaly(
                    intensity = intensity,
                    timestamp = System.currentTimeMillis(),
                    type = AudioAnomalyType.FREQUENCY,
                    frequency = anomalyFrequency,
                    amplitude = maxAnomalyDb
                )
                
                _anomalyFlow.value = anomaly
            }
            
        } catch (e: Exception) {
            // Log error but continue processing
            e.printStackTrace()
        }
    }
    
    /**
     * Calculate RMS (Root Mean Square) amplitude for audio window
     * Requirements: 2.4
     */
    private fun calculateRMSAmplitude(windowSamples: DoubleArray): Float {
        // Calculate sum of squares
        var sumOfSquares = 0.0
        for (sample in windowSamples) {
            sumOfSquares += sample * sample
        }
        
        // Calculate mean of squares
        val meanSquare = sumOfSquares / windowSamples.size
        
        // Return root mean square
        return sqrt(meanSquare).toFloat()
    }
    
    /**
     * Detect audio spikes by comparing current amplitude to rolling average
     * Requirements: 2.4
     */
    private fun detectAudioSpike(rmsAmplitude: Float) {
        try {
            // Add current amplitude to circular buffer history
            amplitudeHistory.add(rmsAmplitude)
            
            // Need at least 2 samples to calculate average (current + at least 1 historical)
            if (amplitudeHistory.size < 2) {
                return
            }
            
            // Calculate rolling average (excluding current sample)
            var sum = 0.0
            val count = amplitudeHistory.size - 1
            for (i in 0 until count) {
                sum += amplitudeHistory.get(i, asFloat = true)
            }
            val averageAmplitude = (sum / count).toFloat()
            
            // Skip if average is too small to avoid log issues
            if (averageAmplitude < 1e-10f || rmsAmplitude < 1e-10f) {
                return
            }
            
            // Convert amplitudes to decibels
            val currentDb = 20f * log10(rmsAmplitude)
            val averageDb = 20f * log10(averageAmplitude)
            
            // Calculate difference in dB
            val dbDifference = currentDb - averageDb
            
            // Check if current amplitude exceeds average by 20dB threshold
            if (dbDifference > SPIKE_THRESHOLD_DB) {
                // Calculate intensity (0.0 to 1.0) based on how far above threshold
                // Map 20dB (threshold) to 0.0 and 60dB to 1.0
                val intensity = ((dbDifference - SPIKE_THRESHOLD_DB) / 40f)
                    .coerceIn(0f, 1f)
                
                // Emit AudioAnomaly with SPIKE type
                val anomaly = AudioAnomaly(
                    intensity = intensity,
                    timestamp = System.currentTimeMillis(),
                    type = AudioAnomalyType.SPIKE,
                    frequency = null,
                    amplitude = currentDb
                )
                
                _anomalyFlow.value = anomaly
            }
            
        } catch (e: Exception) {
            // Log error but continue processing
            e.printStackTrace()
        }
    }
    
    /**
     * Generic circular buffer implementation for efficient data storage
     * Provides O(1) add and remove operations (Requirements: 6.2)
     */
    private class CircularBuffer(private val capacity: Int) {
        private val shortBuffer = ShortArray(capacity)
        private val floatBuffer = FloatArray(capacity)
        private var head = 0
        private var tail = 0
        private var count = 0
        private var isFloatMode = false
        
        fun add(value: Short) {
            isFloatMode = false
            shortBuffer[tail] = value
            tail = (tail + 1) % capacity
            if (count < capacity) {
                count++
            } else {
                // Buffer is full, overwrite oldest element
                head = (head + 1) % capacity
            }
        }
        
        fun add(value: Float) {
            isFloatMode = true
            floatBuffer[tail] = value
            tail = (tail + 1) % capacity
            if (count < capacity) {
                count++
            } else {
                // Buffer is full, overwrite oldest element
                head = (head + 1) % capacity
            }
        }
        
        fun get(index: Int): Short {
            if (index >= count) {
                throw IndexOutOfBoundsException("Index $index out of bounds for size $count")
            }
            val actualIndex = (head + index) % capacity
            return if (isFloatMode) {
                floatBuffer[actualIndex].toInt().toShort()
            } else {
                shortBuffer[actualIndex]
            }
        }
        
        fun get(index: Int, asFloat: Boolean): Float {
            if (index >= count) {
                throw IndexOutOfBoundsException("Index $index out of bounds for size $count")
            }
            val actualIndex = (head + index) % capacity
            return if (isFloatMode) {
                floatBuffer[actualIndex]
            } else {
                shortBuffer[actualIndex].toFloat()
            }
        }
        
        fun removeFirst() {
            if (count == 0) {
                throw NoSuchElementException("Buffer is empty")
            }
            head = (head + 1) % capacity
            count--
        }
        
        fun clear() {
            head = 0
            tail = 0
            count = 0
        }
        
        val size: Int
            get() = count
    }
}
