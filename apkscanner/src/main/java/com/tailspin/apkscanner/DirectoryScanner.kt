package com.tailspin.apkscanner

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import java.io.File

internal class DirectoryScanner(
    private val apkChecker: ApkChecker,
    private val archiveProcessors: List<ArchiveProcessor>,
    private val temporaryFileProvider: TemporaryFileProvider,
    private val coroutineScope: CoroutineScope
) : FileScanner {

    private val archiveProcessorSemaphore = Semaphore(1)

    override suspend fun scanDirectory(directory: File): Int =
        coroutineScope.async {
            val deferredList = mutableListOf<Deferred<Int>>()
            directory.listFiles()?.forEach { file ->
                deferredList.add(async(Dispatchers.IO) {
                    processFile(file)
                })
            }
            deferredList.awaitAll().sum()
        }.await()


    override suspend fun processFile(file: File): Int {
        return when {
            file.isDirectory -> scanDirectory(file)
            apkChecker.isApk(file) -> 1
            else -> {
                archiveProcessors.firstOrNull { it.canProcess(file) }?.let { processor ->
                    val tempDir = temporaryFileProvider.createTempDir()
                    try {
                        archiveProcessorSemaphore.acquire()
                        try {
                            processor.process(file, tempDir)
                        } finally {
                            archiveProcessorSemaphore.release()
                        }
                        val apkCount = scanDirectory(tempDir)
                        temporaryFileProvider.cleanupTempDir(tempDir)
                        apkCount
                    } finally {
                        temporaryFileProvider.cleanupTempDir(tempDir)
                    }
                } ?: 0
            }
        }
    }


}