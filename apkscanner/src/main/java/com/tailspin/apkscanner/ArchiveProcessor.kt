package com.tailspin.apkscanner

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipFile

internal interface ArchiveProcessor {
    fun canProcess(file: File): Boolean
    fun process(sourceFile: File, tempDir: File)
}

internal class ZipProcessor : ArchiveProcessor {
    override fun canProcess(file: File): Boolean = file.extension == "zip"

    override fun process(sourceFile: File, tempDir: File) {
        try {
            ZipFile(sourceFile).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    val entryFile = File(tempDir, entry.name)
                    if (entry.isDirectory) {
                        entryFile.mkdirs()
                    } else {
                        entryFile.parentFile?.mkdirs()
                        zip.getInputStream(entry).use { input ->
                            entryFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}

internal class TarProcessor : ArchiveProcessor {
    override fun canProcess(file: File): Boolean = file.extension == "tar"

    override fun process(sourceFile: File, tempDir: File) {
        try {
            FileInputStream(sourceFile).use { fis ->
                TarArchiveInputStream(fis).use { tarInput ->
                    var entry: TarArchiveEntry? = tarInput.nextTarEntry
                    while (entry != null) {
                        val entryFile = File(tempDir, entry.name)
                        if (entry.isDirectory) {
                            entryFile.mkdirs()
                        } else {
                            entryFile.parentFile?.mkdirs()
                            FileOutputStream(entryFile).use { fos ->
                                tarInput.copyTo(fos)
                            }
                        }
                        entry = tarInput.nextTarEntry
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}