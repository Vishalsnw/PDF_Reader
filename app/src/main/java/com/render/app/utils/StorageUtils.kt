/*
 * File: StorageUtils.kt
 * Created: 2025-06-02
 * Author: Vishalsnw
 * Last Modified: 2025-06-02 14:39:21 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.render.app.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Utility class for handling storage operations.
 * Features:
 * - Storage access
 * - Space management
 * - Cache handling
 * - Path resolution
 */
object StorageUtils {

    private const val CACHE_EXPIRY_DAYS = 7L
    private const val MIN_FREE_SPACE = 100L * 1024 * 1024 // 100MB
    private const val DEFAULT_BUFFER_SIZE = 8192

    /**
     * Storage Access
     */
    fun getExternalStorageDirectory(context: Context): File? {
        return try {
            ContextCompat.getExternalFilesDirs(context, null).firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    fun getInternalStorageDirectory(context: Context): File {
        return context.filesDir
    }

    fun getCacheDirectory(context: Context): File {
        return context.cacheDir
    }

    fun getExternalCacheDirectory(context: Context): File? {
        return context.externalCacheDir
    }

    /**
     * Space Management
     */
    fun getAvailableSpace(directory: File): Long {
        return try {
            StatFs(directory.path).let {
                it.availableBlocksLong * it.blockSizeLong
            }
        } catch (e: Exception) {
            0L
        }
    }

    fun getTotalSpace(directory: File): Long {
        return try {
            StatFs(directory.path).let {
                it.blockCountLong * it.blockSizeLong
            }
        } catch (e: Exception) {
            0L
        }
    }

    fun hasEnoughSpace(directory: File, requiredSpace: Long): Boolean {
        return getAvailableSpace(directory) >= requiredSpace
    }

    fun getStorageStatus(context: Context): StorageStatus {
        val internal = getInternalStorageDirectory(context)
        val external = getExternalStorageDirectory(context)

        return StorageStatus(
            internalAvailable = getAvailableSpace(internal),
            internalTotal = getTotalSpace(internal),
            externalAvailable = external?.let { getAvailableSpace(it) } ?: 0L,
            externalTotal = external?.let { getTotalSpace(it) } ?: 0L
        )
    }

    /**
     * Cache Management
     */
    fun clearCache(context: Context) {
        try {
            clearDirectory(context.cacheDir)
            context.externalCacheDir?.let { clearDirectory(it) }
        } catch (e: Exception) {
            // Handle exception
        }
    }

    fun cleanupOldCache(context: Context) {
        try {
            val expiryTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(CACHE_EXPIRY_DAYS)
            cleanupDirectory(context.cacheDir, expiryTime)
            context.externalCacheDir?.let { cleanupDirectory(it, expiryTime) }
        } catch (e: Exception) {
            // Handle exception
        }
    }

    fun ensureCacheSpace(context: Context, requiredSpace: Long): Boolean {
        val cacheDir = context.cacheDir
        if (hasEnoughSpace(cacheDir, requiredSpace)) return true

        // Try to free up space
        cleanupOldCache(context)
        return hasEnoughSpace(cacheDir, requiredSpace)
    }

    /**
     * Path Management
     */
    fun getAbsolutePath(context: Context, relativePath: String): String {
        return File(context.filesDir, relativePath).absolutePath
    }

    fun getRelativePath(context: Context, absolutePath: String): String? {
        val basePath = context.filesDir.absolutePath
        return if (absolutePath.startsWith(basePath)) {
            absolutePath.substring(basePath.length + 1)
        } else {
            null
        }
    }

    fun createDirectory(context: Context, directoryName: String): File? {
        return try {
            File(context.filesDir, directoryName).apply {
                if (!exists()) {
                    mkdirs()
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Storage Utilities
     */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in setOf(
            Environment.MEDIA_MOUNTED,
            Environment.MEDIA_MOUNTED_READ_ONLY
        )
    }

    fun isStorageLow(context: Context): Boolean {
        val internal = getInternalStorageDirectory(context)
        return getAvailableSpace(internal) < MIN_FREE_SPACE
    }

    /**
     * Helper Functions
     */
    private fun clearDirectory(directory: File) {
        try {
            directory.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    clearDirectory(file)
                }
                file.delete()
            }
        } catch (e: Exception) {
            // Handle exception
        }
    }

    private fun cleanupDirectory(directory: File, expiryTime: Long) {
        try {
            directory.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    cleanupDirectory(file, expiryTime)
                } else if (file.lastModified() < expiryTime) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // Handle exception
        }
    }

    /**
     * Data Classes
     */
    data class StorageStatus(
        val internalAvailable: Long,
        val internalTotal: Long,
        val externalAvailable: Long,
        val externalTotal: Long
    ) {
        val internalUsed: Long
            get() = internalTotal - internalAvailable

        val externalUsed: Long
            get() = externalTotal - externalAvailable

        val internalUsagePercentage: Float
            get() = if (internalTotal > 0) {
                (internalUsed.toFloat() / internalTotal) * 100
            } else {
                0f
            }

        val externalUsagePercentage: Float
            get() = if (externalTotal > 0) {
                (externalUsed.toFloat() / externalTotal) * 100
            } else {
                0f
            }
    }

    /**
     * Storage Types
     */
    enum class StorageType {
        INTERNAL,
        EXTERNAL,
        CACHE
    }

    /**
     * Constants
     */
    object Paths {
        const val BOOKS_DIRECTORY = "books"
        const val COVERS_DIRECTORY = "covers"
        const val TEMP_DIRECTORY = "temp"
        const val BACKUP_DIRECTORY = "backup"
    }
}
