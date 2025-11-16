package com.hackathon.shadowsignal.data

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementation of PermissionManager
 * Handles runtime permissions for camera and microphone using ActivityResultContracts
 * Requirements: 1.6, 2.6
 */
class PermissionManagerImpl(
    private val context: Context
) : PermissionManager {
    
    private val _permissionState = MutableStateFlow(
        PermissionState(
            cameraGranted = checkCameraPermission(),
            microphoneGranted = checkMicrophonePermission()
        )
    )
    
    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null
    
    /**
     * Initialize the permission launcher with the activity
     * Must be called during activity creation before onResume
     */
    fun initialize(activity: ComponentActivity) {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
            val microphoneGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
            
            _permissionState.value = PermissionState(
                cameraGranted = cameraGranted,
                microphoneGranted = microphoneGranted
            )
        }
    }
    
    override fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun checkMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    override fun requestPermissions(activity: Activity) {
        val permissionsToRequest = mutableListOf<String>()
        
        if (!checkCameraPermission()) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        
        if (!checkMicrophonePermission()) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher?.launch(permissionsToRequest.toTypedArray())
        } else {
            // All permissions already granted, update state
            _permissionState.value = PermissionState(
                cameraGranted = true,
                microphoneGranted = true
            )
        }
    }
    
    override fun getPermissionStateFlow(): StateFlow<PermissionState> {
        return _permissionState.asStateFlow()
    }
    
    /**
     * Check if we should show rationale for camera permission
     */
    fun shouldShowCameraRationale(activity: Activity): Boolean {
        return activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
    }
    
    /**
     * Check if we should show rationale for microphone permission
     */
    fun shouldShowMicrophoneRationale(activity: Activity): Boolean {
        return activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
    }
    
    /**
     * Update permission state manually (useful for checking on resume)
     */
    fun updatePermissionState() {
        _permissionState.value = PermissionState(
            cameraGranted = checkCameraPermission(),
            microphoneGranted = checkMicrophonePermission()
        )
    }
}
