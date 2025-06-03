/*
 * File: SettingsRepository.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 04:46:43 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.yourapp.readers.domain.models.*
import com.yourapp.readers.domain.repository.ISettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val preferences: SharedPreferences,
    private val gson: Gson
) : ISettingsRepository {

    private val _readerSettings = MutableStateFlow(loadReaderSettings())
    private val _fontSettings = MutableStateFlow(loadFontSettings())
    private val _colorScheme = MutableStateFlow(loadColorScheme())
    private val _appSettings = MutableStateFlow(loadAppSettings())

    override val readerSettings: Flow<ReaderSettings> = _readerSettings.asStateFlow()
    override val fontSettings: Flow<FontSettings> = _fontSettings.asStateFlow()
    override val colorScheme: Flow<ColorScheme> = _colorScheme.asStateFlow()
    override val appSettings: Flow<AppSettings> = _appSettings.asStateFlow()

    override suspend fun updateReaderSettings(settings: ReaderSettings) = withContext(Dispatchers.IO) {
        preferences.edit {
            putString(KEY_READER_SETTINGS, gson.toJson(settings))
        }
        _readerSettings.value = settings
    }

    override suspend fun updateFontSettings(settings: FontSettings) = withContext(Dispatchers.IO) {
        preferences.edit {
            putString(KEY_FONT_SETTINGS, gson.toJson(settings))
        }
        _fontSettings.value = settings
    }

    override suspend fun updateColorScheme(scheme: ColorScheme) = withContext(Dispatchers.IO) {
        preferences.edit {
            putString(KEY_COLOR_SCHEME, gson.toJson(scheme))
        }
        _colorScheme.value = scheme
    }

    override suspend fun updateAppSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        preferences.edit {
            putString(KEY_APP_SETTINGS, gson.toJson(settings))
        }
        _appSettings.value = settings
    }

    override suspend fun resetToDefaults() = withContext(Dispatchers.IO) {
        preferences.edit {
            remove(KEY_READER_SETTINGS)
            remove(KEY_FONT_SETTINGS)
            remove(KEY_COLOR_SCHEME)
            remove(KEY_APP_SETTINGS)
        }
        
        _readerSettings.value = ReaderSettings()
        _fontSettings.value = FontSettings()
        _colorScheme.value = ColorScheme()
        _appSettings.value = AppSettings()
    }

    override suspend fun exportSettings(): String = withContext(Dispatchers.IO) {
        val settings = Settings(
            readerSettings = _readerSettings.value,
            fontSettings = _fontSettings.value,
            colorScheme = _colorScheme.value,
            appSettings = _appSettings.value
        )
        gson.toJson(settings)
    }

    override suspend fun importSettings(json: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val settings = gson.fromJson(json, Settings::class.java)
            
            preferences.edit {
                putString(KEY_READER_SETTINGS, gson.toJson(settings.readerSettings))
                putString(KEY_FONT_SETTINGS, gson.toJson(settings.fontSettings))
                putString(KEY_COLOR_SCHEME, gson.toJson(settings.colorScheme))
                putString(KEY_APP_SETTINGS, gson.toJson(settings.appSettings))
            }

            _readerSettings.value = settings.readerSettings
            _fontSettings.value = settings.fontSettings
            _colorScheme.value = settings.colorScheme
            _appSettings.value = settings.appSettings

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun loadReaderSettings(): ReaderSettings {
        val json = preferences.getString(KEY_READER_SETTINGS, null)
        return try {
            gson.fromJson(json, ReaderSettings::class.java) ?: ReaderSettings()
        } catch (e: Exception) {
            ReaderSettings()
        }
    }

    private fun loadFontSettings(): FontSettings {
        val json = preferences.getString(KEY_FONT_SETTINGS, null)
        return try {
            gson.fromJson(json, FontSettings::class.java) ?: FontSettings()
        } catch (e: Exception) {
            FontSettings()
        }
    }

    private fun loadColorScheme(): ColorScheme {
        val json = preferences.getString(KEY_COLOR_SCHEME, null)
        return try {
            gson.fromJson(json, ColorScheme::class.java) ?: ColorScheme()
        } catch (e: Exception) {
            ColorScheme()
        }
    }

    private fun loadAppSettings(): AppSettings {
        val json = preferences.getString(KEY_APP_SETTINGS, null)
        return try {
            gson.fromJson(json, AppSettings::class.java) ?: AppSettings()
        } catch (e: Exception) {
            AppSettings()
        }
    }

    companion object {
        private const val KEY_READER_SETTINGS = "reader_settings"
        private const val KEY_FONT_SETTINGS = "font_settings"
        private const val KEY_COLOR_SCHEME = "color_scheme"
        private const val KEY_APP_SETTINGS = "app_settings"
    }
}

data class Settings(
    val readerSettings: ReaderSettings,
    val fontSettings: FontSettings,
    val colorScheme: ColorScheme,
    val appSettings: AppSettings
)

data class AppSettings(
    val viewMode: ViewMode = ViewMode.GRID,
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val imageQuality: ImageQuality = ImageQuality.AUTO,
    val enableNotifications: Boolean = true,
    val updateNotifications: Boolean = true,
    val keepScreenOn: Boolean = false
)

enum class ViewMode {
    GRID,
    LIST
}

enum class DarkMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class ImageQuality {
    LOW,
    MEDIUM,
    HIGH,
    AUTO
}
