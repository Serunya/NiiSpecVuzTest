package com.tailspin.apkscanner

import java.io.File
import java.io.IOException

internal class TemporaryFileProvider {

    fun createTempDir(): File {
        val tempDir = File.createTempFile("temp", System.currentTimeMillis().toString())
        if (!tempDir.delete() || !tempDir.mkdir()) {
            throw IOException("Could not create temporary directory")
        }
        return tempDir
    }

    fun cleanupTempDir(tempDir: File) {
        tempDir.deleteRecursively()
    }
}