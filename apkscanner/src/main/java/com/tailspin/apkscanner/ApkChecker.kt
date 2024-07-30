package com.tailspin.apkscanner

import java.io.File

internal interface ApkChecker {
    fun isApk(file: File): Boolean
}