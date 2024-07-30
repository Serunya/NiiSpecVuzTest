package com.tailspin.apkscanner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File


class ApkScannerHelper(
    private val listener: ApkScannerListener,
    private val coroutineScope: CoroutineScope,
) {

    private val apkChecker = ApkCheckerImpl()
    private val tempFileProvider = TemporaryFileProvider()
    private val archiveProcessors = listOf(ZipProcessor(), TarProcessor())
    private val directoryScanner =
        DirectoryScanner(apkChecker, archiveProcessors, tempFileProvider, coroutineScope)


    fun startScan(context: Context, directories: List<File>) {
        if (requestExternalStoragePermission(context)) {
            listener.onScanStarted()
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    var apkCount = 0
                    directories.forEach { directory ->
                        apkCount += async(Dispatchers.IO) { directoryScanner.scanDirectory(directory) }.await()
                    }
                    listener.onScanCompleted(apkCount)
                } catch (e: Exception) {
                    listener.onScanError(e.message ?: "Unknown Error")
                }
            }
        } else {
            listener.onPermissionDenied()
        }
    }


    private fun requestExternalStoragePermission(context: Context): Boolean {
        if (!checkStoragePermissions(context)) {
            requestForStoragePermissions(context)
            return false
        }
        return true
    }

    private fun checkStoragePermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

        }
    }


    private fun requestForStoragePermissions(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent()
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.setData(uri)
                context.startActivity(intent)
            } catch (e: java.lang.Exception) {
                val intent = Intent()
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                context.startActivity(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
    }


}