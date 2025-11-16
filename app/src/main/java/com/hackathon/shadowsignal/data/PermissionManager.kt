package com.hackathon.shadowsignal.data

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for permission management
 * Handles runtime permissions for camera and microphone
 * Requirements: 7.1
 */
interface PermissionManager {
    /**
     * Check if camera permission is granted
     */
    fun checkCameraPermission(): Boolean
    
    /**
     * Check if microphone permission is granted
     */
    fun checkMicrophonePermission(): Boolean
    
    /**
     * Request necessary permissions from the user
     */
    fun requestPermissions(activity: Activity)
    
    /**
     * Get flow of permission state changes
     */
    fun getPermissionStateFlow(): StateFlow<PermissionState>
}

/**
 * State of runtime permissions
 */
data class PermissionState(
    val cameraGranted: Boolean,
    val microphoneGranted: Boolean
)
