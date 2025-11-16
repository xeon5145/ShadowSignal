package com.hackathon.shadowsignal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hackathon.shadowsignal.data.PermissionManagerImpl
import com.hackathon.shadowsignal.ui.PermissionHandler
import com.hackathon.shadowsignal.ui.rememberPermissionRequestWithRationale

class MainActivity : ComponentActivity() {
    
    private lateinit var permissionManager: PermissionManagerImpl
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize permission manager
        permissionManager = PermissionManagerImpl(this)
        permissionManager.initialize(this)
        
        setContent {
            ShadowSignalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
                    
                    PermissionHandler(
                        permissionState = permissionState,
                        onRequestPermissions = permissionRequestState.requestPermissions
                    ) {
                        Greeting("Shadow Signal")
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

@Composable
fun ShadowSignalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        content = content
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Welcome to $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ShadowSignalTheme {
        Greeting("Shadow Signal")
    }
}
