package com.hackathon.shadowsignal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
                    
                    // Initialize CameraProvider and ViewModel
                    LaunchedEffect(Unit) {
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(this@MainActivity)
                        cameraProviderFuture.addListener({
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
                        }, cameraExecutor)
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
    
    override fun onResume() {
        super.onResume()
        // Update permission state when activity resumes (e.g., returning from Settings)
        permissionManager.updatePermissionState()
    }
}
