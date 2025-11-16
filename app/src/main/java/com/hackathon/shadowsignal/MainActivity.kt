package com.hackathon.shadowsignal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.hackathon.shadowsignal.data.AudioAnalyzerImpl
import com.hackathon.shadowsignal.data.CameraAnalyzerImpl
import com.hackathon.shadowsignal.data.PermissionManagerImpl
import com.hackathon.shadowsignal.domain.ThreatFusionImpl
import com.hackathon.shadowsignal.ui.PermissionHandler
import com.hackathon.shadowsignal.ui.ScannerScreen
import com.hackathon.shadowsignal.ui.rememberPermissionRequestWithRationale
import com.hackathon.shadowsignal.ui.theme.ShadowSignalTheme
import com.hackathon.shadowsignal.viewmodel.ScannerViewModel
import java.util.concurrent.Executor

/**
 * Main activity for Shadow Signal app
 * 
 * Sets up Compose UI with:
 * - Permission handling
 * - CameraX initialization
 * - ViewModel initialization with all dependencies
 * - ScannerScreen as main UI
 * 
 * Requirements: 1.1, 4.1
 */
class MainActivity : ComponentActivity() {
    
    private lateinit var permissionManager: PermissionManagerImpl
    private lateinit var cameraExecutor: Executor
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize permission manager
        permissionManager = PermissionManagerImpl(this)
        permissionManager.initialize(this)
        
        // Initialize camera executor
        cameraExecutor = ContextCompat.getMainExecutor(this)
        
        setContent {
            ShadowSignalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var viewModel by remember { mutableStateOf<ScannerViewModel?>(null) }
                    var initError by remember { mutableStateOf<String?>(null) }
                    
                    // Initialize CameraProvider and ViewModel with error handling
                    LaunchedEffect(Unit) {
                        try {
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(this@MainActivity)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    
                                    // Initialize all modules
                                    val cameraAnalyzer = CameraAnalyzerImpl(cameraProvider)
                                    val audioAnalyzer = AudioAnalyzerImpl()
                                    val threatFusion = ThreatFusionImpl()
                                    
                                    // Initialize ViewModel with dependencies
                                    viewModel = ScannerViewModel(
                                        cameraAnalyzer = cameraAnalyzer,
                                        audioAnalyzer = audioAnalyzer,
                                        threatFusion = threatFusion,
                                        permissionManager = permissionManager
                                    )
                                } catch (e: Exception) {
                                    initError = "Camera initialization failed: ${e.message}"
                                    android.util.Log.e("MainActivity", "Camera init error", e)
                                }
                            }, cameraExecutor)
                        } catch (e: Exception) {
                            initError = "Failed to get camera provider: ${e.message}"
                            android.util.Log.e("MainActivity", "Camera provider error", e)
                        }
                    }
                    
                    val permissionState by permissionManager.getPermissionStateFlow().collectAsState()
                    val permissionRequestState = rememberPermissionRequestWithRationale(
                        permissionManager = permissionManager,
                        activity = this
                    )
                    
                    // Request permissions on first launch
                    LaunchedEffect(Unit) {
                        if (!permissionState.cameraGranted || !permissionState.microphoneGranted) {
                            permissionRequestState.requestPermissions()
                        }
                    }
                    
                    // Show error message if initialization failed
                    if (initError != null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Initialization Error",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = initError ?: "Unknown error",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    } else {
                        // Show scanner screen when permissions are granted and ViewModel is ready
                        PermissionHandler(
                            permissionState = permissionState,
                            onRequestPermissions = permissionRequestState.requestPermissions
                        ) {
                            viewModel?.let { vm ->
                                ScannerScreen(
                                    viewModel = vm,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Update permission state when activity resumes (e.g., returning from Settings)
        permissionManager.updatePermissionState()
    }
}
