package com.tailspin.apkscanner


public interface ApkScannerListener {
    fun onScanStarted()
    fun onScanError(message: String)
    fun onScanCompleted(countApkFile: Int)
    fun onPermissionDenied()
}