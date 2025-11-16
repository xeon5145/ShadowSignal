package com.hackathon.shadowsignal.viewmodel

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hackathon.shadowsignal.data.AudioAnalyzerImpl
import com.hackathon.shadowsignal.data.CameraAnalyzerImpl
import com.hackathon.shadowsignal.data.PermissionManagerImpl
import com.hackathon.shadowsignal.domain.ThreatFusionImpl

class ScannerViewModelFactory(
    private val context: Context,
    private val cameraProvider: ProcessCameraProvider,
    private val permissionManager: PermissionManagerImpl
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScannerViewModel::class.java)) {
            val cameraAnalyzer = CameraAnalyzerImpl(cameraProvider)
            val audioAnalyzer = AudioAnalyzerImpl()
            val threatFusion = ThreatFusionImpl()
            @Suppress("UNCHECKED_CAST")
            return ScannerViewModel(cameraAnalyzer, audioAnalyzer, threatFusion, permissionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}