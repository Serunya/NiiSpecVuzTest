package com.tailspin.niispecvuz

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tailspin.apkscanner.ApkScannerHelper
import com.tailspin.apkscanner.ApkScannerListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class MainViewModel(private val storageManager: StorageManager) : ViewModel() {

    private val mutableScanState =
        MutableStateFlow<ScanApkState>(ScanApkState.ApkScanningEmptyState)
    val scanState = mutableScanState.asStateFlow()


    private val apkScannerHelper: ApkScannerHelper

    init {
        val apkScannerListener = object : ApkScannerListener {
            override fun onScanStarted() {
                mutableScanState.value = ScanApkState.ApkScanningState
            }

            override fun onScanError(message: String) {
                mutableScanState.value = ScanApkState.ApkScanningErrorState(message)
            }

            override fun onScanCompleted(countApkFile: Int) {
                mutableScanState.value = ScanApkState.ApkScanningCompleteState(countApkFile)
            }

            override fun onPermissionDenied() {
                mutableScanState.value = ScanApkState.StoragePermissionDeniedState
            }
        }
        apkScannerHelper = ApkScannerHelper(apkScannerListener, viewModelScope)
    }

    fun scanningExternalStorage(context: Context) {
        val directories = getDirectories().filter { !it.absolutePath.contains("emulated") }
        apkScannerHelper.startScan(context, directories)
    }

    private fun getDirectories() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) getStorageDirectories()
        else listOf(getLegacyStorageDirectory())


    @RequiresApi(Build.VERSION_CODES.R)
    private fun getStorageDirectories(): List<File> {
        val directories = mutableListOf<File>()
        storageManager.storageVolumes.forEach { volume ->
            volume.directory?.let {
                directories.add(it)
            }
        }
        return directories
    }

    @Suppress("DEPRECATION")
    private fun getLegacyStorageDirectory(): File {
        return File("/sdcard");
    }

    companion object {
        fun factory(storageManager: StorageManager) = viewModelFactory {
            initializer {
                MainViewModel(storageManager)
            }
        }
    }
}