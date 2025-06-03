/*
 * File: Settings.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 14:22:43 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.domain.models

data class Settings(
    val userId: String,
    val deviceId: String? = null,
    val theme: Theme,
    val fontSettings: FontSettings,
    val readingSettings: ReadingSettings,
    val notificationSettings: NotificationSettings,
    val syncSettings: SyncSettings,
    val accessibilitySettings: AccessibilitySettings,
    val lastModified: Long = System.currentTimeMillis(),
    val version: Int = CURRENT_VERSION
) {
    companion object {
        const val CURRENT_VERSION = 2
        const val MIN_SUPPORTED_VERSION = 1

        fun createDefault(userId: String) = Settings(
            userId = userId,
            theme = Theme.createDefault(),
            fontSettings = FontSettings.createDefault(),
            readingSettings = ReadingSettings.createDefault(),
            notificationSettings = NotificationSettings.createDefault(),
            syncSettings = SyncSettings.createDefault(),
            accessibilitySettings = AccessibilitySettings.createDefault()
        )
    }

    fun isOutdated(): Boolean = version < MIN_SUPPORTED_VERSION
    
    fun requiresSync(lastSyncTime: Long): Boolean = 
        lastModified > lastSyncTime || syncSettings.requiresSync(lastSyncTime)
}

data class Theme(
    val name: String = "light",
    val isDark: Boolean = false,
    val primaryColor: String = "#FF6200EE",
    val secondaryColor: String = "#FF03DAC5",
    val backgroundColor: String = "#FFFFFFFF",
    val textColor: String = "#FF000000",
    val customColors: Map<String, String> = emptyMap()
) {
    companion object {
        fun createDefault() = Theme()
    }

    fun isCustom(): Boolean = name == "custom"
    
    fun requiresDarkMode(): Boolean = isDark || name == "dark"
}

data class FontSettings(
    val family: String = "roboto",
    val size: Float = 16f,
    val weight: Int = 400,
    val lineSpacing: Float = 1.5f,
    val letterSpacing: Float = 0f,
    val alignment: TextAlignment = TextAlignment.LEFT
) {
    companion object {
        const val MIN_SIZE = 12f
        const val MAX_SIZE = 32f
        const val DEFAULT_SIZE = 16f

        fun createDefault() = FontSettings()
    }

    fun validateSize(): Float = size.coerceIn(MIN_SIZE, MAX_SIZE)
}

data class ReadingSettings(
    val pageTransition: PageTransition = PageTransition.SLIDE,
    val readingMode: ReadingMode = ReadingMode.PAGED,
    val orientation: ScreenOrientation = ScreenOrientation.AUTO,
    val keepScreenOn: Boolean = false,
    val fullscreen: Boolean = false,
    val margins: Margins = Margins()
) {
    companion object {
        fun createDefault() = ReadingSettings()
    }
}

data class NotificationSettings(
    val enabled: Boolean = true,
    val chapterUpdates: Boolean = true,
    val readingReminders: Boolean = false,
    val reminderTime: String? = null,
    val vibration: Boolean = true,
    val sound: Boolean = true
) {
    companion object {
        fun createDefault() = NotificationSettings()
    }

    fun hasReminder(): Boolean = readingReminders && !reminderTime.isNullOrBlank()
}

data class SyncSettings(
    val autoSync: Boolean = true,
    val syncOverCellular: Boolean = false,
    val syncInterval: Int = 30, // minutes
    val lastSync: Long = 0
) {
    companion object {
        const val MIN_SYNC_INTERVAL = 15
        const val MAX_SYNC_INTERVAL = 1440 // 24 hours
        
        fun createDefault() = SyncSettings()
    }

    fun requiresSync(currentTime: Long): Boolean {
        if (!autoSync) return false
        val timeSinceLastSync = currentTime - lastSync
        return timeSinceLastSync >= syncInterval * 60 * 1000
    }

    fun validateSyncInterval(): Int = syncInterval.coerceIn(MIN_SYNC_INTERVAL, MAX_SYNC_INTERVAL)
}

data class AccessibilitySettings(
    val highContrast: Boolean = false,
    val textToSpeech: Boolean = false,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f
) {
    companion object {
        const val MIN_SPEECH_RATE = 0.5f
        const val MAX_SPEECH_RATE = 2.0f
        const val MIN_SPEECH_PITCH = 0.5f
        const val MAX_SPEECH_PITCH = 2.0f

        fun createDefault() = AccessibilitySettings()
    }

    fun validateSpeechRate(): Float = speechRate.coerceIn(MIN_SPEECH_RATE, MAX_SPEECH_RATE)
    fun validateSpeechPitch(): Float = speechPitch.coerceIn(MIN_SPEECH_PITCH, MAX_SPEECH_PITCH)
}

data class Margins(
    val left: Int = 16,
    val right: Int = 16,
    val top: Int = 16,
    val bottom: Int = 16
) {
    companion object {
        const val MIN_MARGIN = 0
        const val MAX_MARGIN = 64
    }

    fun validate(): Margins = copy(
        left = left.coerceIn(MIN_MARGIN, MAX_MARGIN),
        right = right.coerceIn(MIN_MARGIN, MAX_MARGIN),
        top = top.coerceIn(MIN_MARGIN, MAX_MARGIN),
        bottom = bottom.coerceIn(MIN_MARGIN, MAX_MARGIN)
    )
}

enum class TextAlignment {
    LEFT, CENTER, RIGHT, JUSTIFY
}

enum class PageTransition {
    NONE, SLIDE, CURL, FADE
}

enum class ReadingMode {
    PAGED, CONTINUOUS, WEBTOON
}

enum class ScreenOrientation {
    AUTO, PORTRAIT, LANDSCAPE
}

/**
 * Extension functions for settings validation and manipulation
 */
fun Settings.validate(): Settings = copy(
    fontSettings = fontSettings.copy(size = fontSettings.validateSize()),
    syncSettings = syncSettings.copy(syncInterval = syncSettings.validateSyncInterval()),
    accessibilitySettings = accessibilitySettings.copy(
        speechRate = accessibilitySettings.validateSpeechRate(),
        speechPitch = accessibilitySettings.validateSpeechPitch()
    ),
    readingSettings = readingSettings.copy(margins = readingSettings.margins.validate())
)

fun Settings.requiresRestart(): Boolean = 
    version != CURRENT_VERSION || accessibilitySettings.highContrast

fun Settings.canAutoSync(): Boolean =
    syncSettings.autoSync && (syncSettings.syncOverCellular || isWifiConnected())

private fun isWifiConnected(): Boolean {
    // Implementation would depend on Android connectivity manager
    // This is just a placeholder
    return true
}
