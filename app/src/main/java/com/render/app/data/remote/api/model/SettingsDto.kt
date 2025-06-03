/*
 * File: SettingsDto.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 14:13:55 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.data.remote.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class SettingsDto(
    @SerializedName("user_id")
    val userId: String,

    @SerializedName("device_id")
    val deviceId: String? = null,

    @SerializedName("theme")
    val theme: ThemeDto,

    @SerializedName("font_settings")
    val fontSettings: FontSettingsDto,

    @SerializedName("reading_settings")
    val readingSettings: ReadingSettingsDto,

    @SerializedName("notification_settings")
    val notificationSettings: NotificationSettingsDto,

    @SerializedName("sync_settings")
    val syncSettings: SyncSettingsDto,

    @SerializedName("accessibility_settings")
    val accessibilitySettings: AccessibilitySettingsDto,

    @SerializedName("last_modified")
    val lastModified: Long = System.currentTimeMillis(),

    @SerializedName("version")
    val version: Int = CURRENT_VERSION,

    @SerializedName("created_at")
    val createdAt: Date = Date(),

    @SerializedName("updated_at")
    val updatedAt: Date = Date()
) {
    companion object {
        const val CURRENT_VERSION = 2
        const val MIN_SUPPORTED_VERSION = 1
    }
}

data class ThemeDto(
    @SerializedName("name")
    val name: String = "light",

    @SerializedName("is_dark")
    val isDark: Boolean = false,

    @SerializedName("primary_color")
    val primaryColor: String = "#FF6200EE",

    @SerializedName("secondary_color")
    val secondaryColor: String = "#FF03DAC5",

    @SerializedName("background_color")
    val backgroundColor: String = "#FFFFFFFF",

    @SerializedName("text_color")
    val textColor: String = "#FF000000",

    @SerializedName("custom_colors")
    val customColors: Map<String, String> = emptyMap(),

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

data class FontSettingsDto(
    @SerializedName("family")
    val family: String = "roboto",

    @SerializedName("size")
    val size: Float = 16f,

    @SerializedName("weight")
    val weight: Int = 400,

    @SerializedName("line_spacing")
    val lineSpacing: Float = 1.5f,

    @SerializedName("letter_spacing")
    val letterSpacing: Float = 0f,

    @SerializedName("alignment")
    val alignment: TextAlignment = TextAlignment.LEFT,

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

data class ReadingSettingsDto(
    @SerializedName("page_transition")
    val pageTransition: PageTransition = PageTransition.SLIDE,

    @SerializedName("reading_mode")
    val readingMode: ReadingMode = ReadingMode.PAGED,

    @SerializedName("orientation")
    val orientation: ScreenOrientation = ScreenOrientation.AUTO,

    @SerializedName("keep_screen_on")
    val keepScreenOn: Boolean = false,

    @SerializedName("fullscreen")
    val fullscreen: Boolean = false,

    @SerializedName("margins")
    val margins: MarginsDto = MarginsDto(),

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

data class NotificationSettingsDto(
    @SerializedName("enabled")
    val enabled: Boolean = true,

    @SerializedName("chapter_updates")
    val chapterUpdates: Boolean = true,

    @SerializedName("reading_reminders")
    val readingReminders: Boolean = false,

    @SerializedName("reminder_time")
    val reminderTime: String? = null,

    @SerializedName("vibration")
    val vibration: Boolean = true,

    @SerializedName("sound")
    val sound: Boolean = true,

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

data class SyncSettingsDto(
    @SerializedName("auto_sync")
    val autoSync: Boolean = true,

    @SerializedName("sync_over_cellular")
    val syncOverCellular: Boolean = false,

    @SerializedName("sync_interval")
    val syncInterval: Int = 30, // minutes

    @SerializedName("last_sync")
    val lastSync: Long = 0,

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

data class AccessibilitySettingsDto(
    @SerializedName("high_contrast")
    val highContrast: Boolean = false,

    @SerializedName("text_to_speech")
    val textToSpeech: Boolean = false,

    @SerializedName("speech_rate")
    val speechRate: Float = 1.0f,

    @SerializedName("speech_pitch")
    val speechPitch: Float = 1.0f,

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

data class MarginsDto(
    @SerializedName("left")
    val left: Int = 16,

    @SerializedName("right")
    val right: Int = 16,

    @SerializedName("top")
    val top: Int = 16,

    @SerializedName("bottom")
    val bottom: Int = 16
)

enum class TextAlignment {
    @SerializedName("left")
    LEFT,

    @SerializedName("center")
    CENTER,

    @SerializedName("right")
    RIGHT,

    @SerializedName("justify")
    JUSTIFY
}

enum class PageTransition {
    @SerializedName("none")
    NONE,

    @SerializedName("slide")
    SLIDE,

    @SerializedName("curl")
    CURL,

    @SerializedName("fade")
    FADE
}

enum class ReadingMode {
    @SerializedName("paged")
    PAGED,

    @SerializedName("continuous")
    CONTINUOUS,

    @SerializedName("webtoon")
    WEBTOON
}

enum class ScreenOrientation {
    @SerializedName("auto")
    AUTO,

    @SerializedName("portrait")
    PORTRAIT,

    @SerializedName("landscape")
    LANDSCAPE
}

/**
 * Extension functions for domain model conversion
 */
fun SettingsDto.toDomainModel() = Settings(
    userId = userId,
    deviceId = deviceId,
    theme = theme.toDomainModel(),
    fontSettings = fontSettings.toDomainModel(),
    readingSettings = readingSettings.toDomainModel(),
    notificationSettings = notificationSettings.toDomainModel(),
    syncSettings = syncSettings.toDomainModel(),
    accessibilitySettings = accessibilitySettings.toDomainModel(),
    lastModified = lastModified,
    version = version
)

fun ThemeDto.toDomainModel() = Theme(
    name = name,
    isDark = isDark,
    primaryColor = primaryColor,
    secondaryColor = secondaryColor,
    backgroundColor = backgroundColor,
    textColor = textColor,
    customColors = customColors
)

fun FontSettingsDto.toDomainModel() = FontSettings(
    family = family,
    size = size,
    weight = weight,
    lineSpacing = lineSpacing,
    letterSpacing = letterSpacing,
    alignment = alignment.name
)
