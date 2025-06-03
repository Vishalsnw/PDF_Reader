/*
 * File: SettingsApi.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 06:38:48 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.data.remote.api

import com.yourapp.readers.data.remote.model.SettingsDto
import com.yourapp.readers.data.remote.model.ThemeDto
import com.yourapp.readers.data.remote.model.FontSettingsDto
import com.yourapp.readers.data.remote.model.ApiResponse
import retrofit2.http.*

interface SettingsApi {
    @GET("settings/{userId}")
    suspend fun getUserSettings(
        @Path("userId") userId: String
    ): ApiResponse<SettingsDto>

    @PUT("settings/{userId}")
    suspend fun updateUserSettings(
        @Path("userId") userId: String,
        @Body settings: SettingsDto
    ): ApiResponse<SettingsDto>

    @GET("settings/{userId}/sync")
    suspend fun syncSettings(
        @Path("userId") userId: String,
        @Query("lastSync") lastSync: Long
    ): ApiResponse<SettingsDto>

    @PATCH("settings/{userId}/theme")
    suspend fun updateTheme(
        @Path("userId") userId: String,
        @Body theme: ThemeDto
    ): ApiResponse<ThemeDto>

    @PATCH("settings/{userId}/font")
    suspend fun updateFontSettings(
        @Path("userId") userId: String,
        @Body fontSettings: FontSettingsDto
    ): ApiResponse<FontSettingsDto>

    @POST("settings/{userId}/reset")
    suspend fun resetToDefaults(
        @Path("userId") userId: String
    ): ApiResponse<SettingsDto>

    @GET("settings/themes")
    suspend fun getAvailableThemes(): ApiResponse<List<ThemeDto>>

    @GET("settings/fonts")
    suspend fun getAvailableFonts(): ApiResponse<List<String>>

    @POST("settings/{userId}/backup")
    suspend fun backupSettings(
        @Path("userId") userId: String
    ): ApiResponse<String> // Returns backup ID

    @POST("settings/{userId}/restore")
    suspend fun restoreSettings(
        @Path("userId") userId: String,
        @Query("backupId") backupId: String
    ): ApiResponse<SettingsDto>

    @GET("settings/{userId}/devices")
    suspend fun getDeviceSettings(
        @Path("userId") userId: String
    ): ApiResponse<List<DeviceSettingsDto>>

    @POST("settings/{userId}/devices/sync")
    suspend fun syncDeviceSettings(
        @Path("userId") userId: String,
        @Body deviceSettings: DeviceSettingsDto
    ): ApiResponse<Unit>

    @DELETE("settings/{userId}/devices/{deviceId}")
    suspend fun removeDevice(
        @Path("userId") userId: String,
        @Path("deviceId") deviceId: String
    ): ApiResponse<Unit>

    @GET("settings/defaults")
    suspend fun getDefaultSettings(): ApiResponse<SettingsDto>

    @GET("settings/version")
    suspend fun getSettingsVersion(): ApiResponse<SettingsVersionDto>

    companion object {
        const val BASE_URL = "https://api.yourapp.com/v1/"
        const val TIMEOUT_SECONDS = 20L
        const val CACHE_SIZE_MB = 10L
    }
}

data class DeviceSettingsDto(
    val deviceId: String,
    val deviceName: String,
    val settings: SettingsDto,
    val lastSync: Long = System.currentTimeMillis()
)

data class SettingsVersionDto(
    val version: Int,
    val minSupportedVersion: Int,
    val lastUpdate: Long,
    val changeLog: List<String>
)

/**
 * Extension functions for convenience
 */
suspend fun SettingsApi.quickUpdateTheme(
    userId: String,
    themeName: String,
    isDark: Boolean = false
) {
    updateTheme(
        userId,
        ThemeDto(
            name = themeName,
            isDark = isDark,
            timestamp = System.currentTimeMillis()
        )
    )
}

suspend fun SettingsApi.quickUpdateFont(
    userId: String,
    fontFamily: String,
    fontSize: Float
) {
    updateFontSettings(
        userId,
        FontSettingsDto(
            family = fontFamily,
            size = fontSize,
            timestamp = System.currentTimeMillis()
        )
    )
}

suspend fun SettingsApi.syncSettingsIfNeeded(
    userId: String,
    lastSync: Long
): ApiResponse<SettingsDto> {
    val version = getSettingsVersion()
    return if (version.data.lastUpdate > lastSync) {
        syncSettings(userId, lastSync)
    } else {
        getUserSettings(userId)
    }
}

/**
 * Utility class for handling settings sync conflicts
 */
object SettingsSyncHelper {
    fun resolveConflict(
        localSettings: SettingsDto,
        remoteSettings: SettingsDto
    ): SettingsDto {
        return if (localSettings.lastModified > remoteSettings.lastModified) {
            localSettings
        } else {
            remoteSettings
        }
    }

    fun mergeSettings(
        localSettings: SettingsDto,
        remoteSettings: SettingsDto
    ): SettingsDto {
        return localSettings.copy(
            theme = remoteSettings.theme,
            fontSettings = remoteSettings.fontSettings.copy(
                size = localSettings.fontSettings.size
            ),
            lastModified = System.currentTimeMillis()
        )
    }
}
