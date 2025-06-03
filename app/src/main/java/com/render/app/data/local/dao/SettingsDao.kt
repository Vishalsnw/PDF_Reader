/*
 * File: SettingsDao.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 06:33:35 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.data.local.dao

import androidx.room.*
import com.yourapp.readers.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE userId = :userId")
    fun getSettings(userId: String): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE userId = :userId")
    suspend fun getSettingsSync(userId: String): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)

    @Update
    suspend fun updateSettings(settings: SettingsEntity)

    @Delete
    suspend fun deleteSettings(settings: SettingsEntity)

    @Query("DELETE FROM settings WHERE userId = :userId")
    suspend fun deleteSettingsByUserId(userId: String)

    @Query("""
        UPDATE settings 
        SET readerTheme = :theme 
        WHERE userId = :userId
    """)
    suspend fun updateReaderTheme(userId: String, theme: String)

    @Query("""
        UPDATE settings 
        SET fontSize = :fontSize 
        WHERE userId = :userId
    """)
    suspend fun updateFontSize(userId: String, fontSize: Float)

    @Query("""
        UPDATE settings 
        SET fontFamily = :fontFamily 
        WHERE userId = :userId
    """)
    suspend fun updateFontFamily(userId: String, fontFamily: String)

    @Query("""
        UPDATE settings 
        SET lineSpacing = :lineSpacing 
        WHERE userId = :userId
    """)
    suspend fun updateLineSpacing(userId: String, lineSpacing: Float)

    @Query("""
        UPDATE settings 
        SET margins = :margins 
        WHERE userId = :userId
    """)
    suspend fun updateMargins(userId: String, margins: Int)

    @Query("""
        UPDATE settings 
        SET colorScheme = :colorScheme 
        WHERE userId = :userId
    """)
    suspend fun updateColorScheme(userId: String, colorScheme: String)

    @Query("""
        UPDATE settings 
        SET orientation = :orientation 
        WHERE userId = :userId
    """)
    suspend fun updateOrientation(userId: String, orientation: Int)

    @Query("""
        UPDATE settings 
        SET pageTransition = :transition 
        WHERE userId = :userId
    """)
    suspend fun updatePageTransition(userId: String, transition: String)

    @Query("""
        UPDATE settings 
        SET keepScreenOn = :keepScreenOn 
        WHERE userId = :userId
    """)
    suspend fun updateKeepScreenOn(userId: String, keepScreenOn: Boolean)

    @Query("""
        UPDATE settings 
        SET fullscreen = :fullscreen 
        WHERE userId = :userId
    """)
    suspend fun updateFullscreen(userId: String, fullscreen: Boolean)

    @Transaction
    suspend fun resetToDefaults(userId: String) {
        val defaultSettings = SettingsEntity(
            userId = userId,
            readerTheme = "light",
            fontSize = 16f,
            fontFamily = "roboto",
            lineSpacing = 1.5f,
            margins = 16,
            colorScheme = "default",
            orientation = 0, // auto
            pageTransition = "slide",
            keepScreenOn = false,
            fullscreen = false,
            lastModified = System.currentTimeMillis()
        )
        insertSettings(defaultSettings)
    }

    @Transaction
    suspend fun updateLastModified(userId: String) {
        getSettingsSync(userId)?.let { settings ->
            updateSettings(settings.copy(lastModified = System.currentTimeMillis()))
        }
    }

    @Query("""
        SELECT * FROM settings 
        WHERE lastModified > :timestamp 
        AND userId = :userId
    """)
    suspend fun getSettingsChanges(userId: String, timestamp: Long): SettingsEntity?

    @Transaction
    suspend fun syncSettings(userId: String, remoteSettings: SettingsEntity) {
        val localSettings = getSettingsSync(userId)
        if (localSettings == null || remoteSettings.lastModified > localSettings.lastModified) {
            insertSettings(remoteSettings)
        }
    }

    @Query("SELECT COUNT(*) FROM settings WHERE userId = :userId")
    suspend fun hasSettings(userId: String): Int

    @Transaction
    suspend fun initializeDefaultSettings(userId: String) {
        if (hasSettings(userId) == 0) {
            resetToDefaults(userId)
        }
    }

    @Query("""
        UPDATE settings 
        SET 
            readerTheme = :theme,
            fontSize = :fontSize,
            fontFamily = :fontFamily,
            lineSpacing = :lineSpacing,
            margins = :margins,
            colorScheme = :colorScheme
        WHERE userId = :userId
    """)
    suspend fun updateDisplaySettings(
        userId: String,
        theme: String,
        fontSize: Float,
        fontFamily: String,
        lineSpacing: Float,
        margins: Int,
        colorScheme: String
    )
}
