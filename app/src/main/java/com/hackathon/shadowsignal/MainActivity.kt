package com.hackathon.shadowsignal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.hackathon.shadowsignal.data.PermissionManagerImpl
import com.hackathon.shadowsignal.ui.AppNavigation
import com.hackathon.shadowsignal.ui.theme.ShadowSignalTheme
import com.hackathon.shadowsignal.viewmodel.ScannerViewModel
import com.hackathon.shadowsignal.viewmodel.ScannerViewModelFactory

class MainActivity : ComponentActivity() {
    
    private lateinit var permissionManager: PermissionManagerImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize permission manager first
        permissionManager = PermissionManagerImpl(this)
        permissionManager.initialize(this)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val viewModel: ScannerViewModel by viewModels {
                ScannerViewModelFactory(this, cameraProvider, permissionManager)
            }

            setContent {
                ShadowSignalTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Request permissions on first launch
                        LaunchedEffect(Unit) {
                            if (!permissionManager.checkCameraPermission() || 
                                !permissionManager.checkMicrophonePermission()) {
                                viewModel.requestPermissions(this@MainActivity)
                            }
                        }
                        
                        AppNavigation(viewModel = viewModel)
                    }
                }
            }
        }, ContextCompat.getMainExecutor(this))
    }
    
    override fun onResume() {
        super.onResume()
        // Update permission state when activity resumes
        if (::permissionManager.isInitialized) {
            permissionManager.updatePermissionState()
        }
    }
}