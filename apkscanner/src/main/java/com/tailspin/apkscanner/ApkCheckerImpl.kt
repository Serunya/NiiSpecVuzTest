package com.tailspin.apkscanner

import java.io.File
import java.util.zip.ZipFile

internal class ApkCheckerImpl : ApkChecker {


    override fun isApk(file: File): Boolean {
        if (file.extension == "tar") return false
        return try {
            ZipFile(file).use { zip ->
                zip.getEntry("AndroidManifest.xml") != null
            }
        } catch (e: Exception) {
            false
        }
    }

}