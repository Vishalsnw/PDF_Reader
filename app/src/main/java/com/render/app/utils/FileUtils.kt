/*
 * File: FileUtils.kt
 * Created: 2025-06-02
 * Author: Vishalsnw
 * Last Modified: 2025-06-02 14:36:50 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import java.io.*
import java.security.MessageDigest
import java.util.*
import kotlin.math.log10
import kotlin.math.pow

/**
 * Utility class for handling file operations.
 * Features:
 * - File management
 * - MIME type handling
 * - File operations
 * - File metadata
 */
object FileUtils {

    private const val BUFFER_SIZE = 8192
    private const val MAX_FILENAME_LENGTH = 255

    /**
     * File Management
     */
    fun importFile(context: Context, sourceFile: File): File {
        val destinationFile = createUniqueFile(
            context.getExternalFilesDir(null)!!,
            sourceFile.name
        )

        sourceFile.inputStream().use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output, BUFFER_SIZE)
            }
        }

        return destinationFile
    }

    fun importFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileNameFromUri(context, uri)
            val destinationFile = createUniqueFile(
                context.getExternalFilesDir(null)!!,
                fileName
            )

            inputStream.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output, BUFFER_SIZE)
                }
            }

            destinationFile
        } catch (e: Exception) {
            null
        }
    }

    fun deleteFile(context: Context, filePath: String): Boolean {
        return try {
            File(filePath).delete()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * File Operations
     */
    fun copyFile(source: File, destination: File): Boolean {
        return try {
            source.inputStream().use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output, BUFFER_SIZE)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun moveFile(source: File, destination: File): Boolean {
        return try {
            if (copyFile(source, destination)) {
                source.delete()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun createTempFile(context: Context, prefix: String, suffix: String): File {
        return File.createTempFile(
            prefix,
            suffix,
            context.cacheDir
        )
    }

    /**
     * File Information
     */
    fun getMimeType(file: File): String {
        val extension = getFileExtension(file.name)
        return MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT))
            ?: "application/octet-stream"
    }

    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "")
    }

    fun getFileName(file: File): String {
        return file.nameWithoutExtension
    }

    fun getFileSize(file: File): Long {
        return file.length()
    }

    fun getFormattedFileSize(size: Long): String {
        if (size <= 0) return "0 B"

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        val value = size / 1024.0.pow(digitGroups.toDouble())

        return String.format("%.1f %s", value, units[digitGroups])
    }

    /**
     * File Validation
     */
    fun isValidFileType(fileName: String, allowedExtensions: List<String>): Boolean {
        val extension = getFileExtension(fileName).toLowerCase(Locale.ROOT)
        return allowedExtensions.contains(extension)
    }

    fun isValidFileName(fileName: String): Boolean {
        return fileName.isNotBlank() &&
                fileName.length <= MAX_FILENAME_LENGTH &&
                !fileName.contains(File.separatorChar) &&
                !fileName.contains("\\") &&
                !fileName.matches(Regex("[\\\\/:*?\"<>|]"))
    }

    fun sanitizeFileName(fileName: String): String {
        var sanitized = fileName
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .trim()

        if (sanitized.length > MAX_FILENAME_LENGTH) {
            val extension = getFileExtension(sanitized)
            val nameWithoutExtension = sanitized.substringBeforeLast('.')
            sanitized = nameWithoutExtension
                .take(MAX_FILENAME_LENGTH - extension.length - 1)
                .plus(".$extension")
        }

        return sanitized
    }

    /**
     * File Metadata
     */
    fun calculateMD5(file: File): String {
        val digest = MessageDigest.getInstance("MD5")
        file.inputStream().use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            var bytes = input.read(buffer)
            while (bytes >= 0) {
                digest.update(buffer, 0, bytes)
                bytes = input.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun getFileMetadata(file: File): FileMetadata {
        return FileMetadata(
            name = file.name,
            size = file.length(),
            lastModified = Date(file.lastModified()),
            mimeType = getMimeType(file),
            md5 = calculateMD5(file)
        )
    }

    /**
     * Directory Operations
     */
    fun createDirectory(parent: File, directoryName: String): File? {
        val directory = File(parent, directoryName)
        return if (directory.mkdir()) directory else null
    }

    fun listFiles(
        directory: File,
        extensions: List<String>? = null,
        recursive: Boolean = false
    ): List<File> {
        val files = mutableListOf<File>()
        
        if (!directory.isDirectory) return files

        directory.listFiles()?.forEach { file ->
            if (file.isFile) {
                if (extensions == null || extensions.contains(getFileExtension(file.name))) {
                    files.add(file)
                }
            } else if (recursive && file.isDirectory) {
                files.addAll(listFiles(file, extensions, true))
            }
        }

        return files
    }

    /**
     * Helper Functions
     */
    private fun createUniqueFile(directory: File, fileName: String): File {
        val sanitizedName = sanitizeFileName(fileName)
        val nameWithoutExtension = sanitizedName.substringBeforeLast('.')
        val extension = getFileExtension(sanitizedName)
        var file = File(directory, sanitizedName)
        var counter = 1

        while (file.exists()) {
            val newName = if (extension.isNotEmpty()) {
                "${nameWithoutExtension}_${counter++}.$extension"
            } else {
                "${nameWithoutExtension}_${counter++}"
            }
            file = File(directory, newName)
        }

        return file
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        val documentFile = DocumentFile.fromSingleUri(context, uri)
        return documentFile?.name ?: UUID.randomUUID().toString()
    }

    /**
     * Data Classes
     */
    data class FileMetadata(
        val name: String,
        val size: Long,
        val lastModified: Date,
        val mimeType: String,
        val md5: String
    )

    /**
     * Constants
     */
    object MimeTypes {
        const val PDF = "application/pdf"
        const val EPUB = "application/epub+zip"
        const val CBZ = "application/x-cbz"
        const val CBR = "application/x-cbr"
        const val MOBI = "application/x-mobipocket-ebook"
        const val TXT = "text/plain"
    }
}
