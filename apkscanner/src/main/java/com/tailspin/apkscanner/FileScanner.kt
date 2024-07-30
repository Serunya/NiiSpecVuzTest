package com.tailspin.apkscanner

import java.io.File

internal interface FileScanner {
    suspend fun scanDirectory(directory: File): Int
    suspend fun processFile(file: File): Int
}