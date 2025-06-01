/*
 * File: Theme.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 20:30:50 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.models

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import com.reader.app.R

/**
 * Theme defines the visual appearance of the application.
 * Features:
 * - Theme types
 * - Color schemes
 * - Typography
 * - Dynamic theming
 */
sealed class Theme(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    val type: ThemeType,
    @StyleRes val styleRes: Int,
    val isDark: Boolean = false,
    val isDynamic: Boolean = false
) {
    /**
     * Light theme
     */
    object Light : Theme(
        titleRes = R.string.theme_light,
        iconRes = R.drawable.ic_theme_light,
        type = ThemeType.LIGHT,
        styleRes = R.style.Theme_Reader_Light
    )

    /**
     * Dark theme
     */
    object Dark : Theme(
        titleRes = R.string.theme_dark,
        iconRes = R.drawable.ic_theme_dark,
        type = ThemeType.DARK,
        styleRes = R.style.Theme_Reader_Dark,
        isDark = true
    )

    /**
     * System default theme
     */
    object System : Theme(
        titleRes = R.string.theme_system,
        iconRes = R.drawable.ic_theme_system,
        type = ThemeType.SYSTEM,
        styleRes = R.style.Theme_Reader_DayNight
    )

    /**
     * Sepia theme for reading
     */
    object Sepia : Theme(
        titleRes = R.string.theme_sepia,
        iconRes = R.drawable.ic_theme_sepia,
        type = ThemeType.SEPIA,
        styleRes = R.style.Theme_Reader_Sepia
    )

    /**
     * OLED black theme
     */
    object Amoled : Theme(
        titleRes = R.string.theme_amoled,
        iconRes = R.drawable.ic_theme_amoled,
        type = ThemeType.AMOLED,
        styleRes = R.style.Theme_Reader_Amoled,
        isDark = true
    )

    /**
     * Material You dynamic theme
     */
    object Dynamic : Theme(
        titleRes = R.string.theme_dynamic,
        iconRes = R.drawable.ic_theme_dynamic,
        type = ThemeType.DYNAMIC,
        styleRes = R.style.Theme_Reader_Dynamic,
        isDynamic = true
    )

    /**
     * Custom theme
     */
    data class Custom(
        val name: String,
        @ColorRes val primaryColor: Int,
        @ColorRes val secondaryColor: Int,
        val config: ThemeConfig
    ) : Theme(
        titleRes = R.string.theme_custom,
        iconRes = R.drawable.ic_theme_custom,
        type = ThemeType.CUSTOM,
        styleRes = R.style.Theme_Reader_Custom,
        isDark = config.isDark
    )

    /**
     * Theme types
     */
    enum class ThemeType {
        LIGHT,
        DARK,
        SYSTEM,
        SEPIA,
        AMOLED,
        DYNAMIC,
        CUSTOM
    }

    /**
     * Theme configuration
     */
    data class ThemeConfig(
        val isDark: Boolean = false,
        val fontSize: Float = 1.0f,
        val fontFamily: String = "sans-serif",
        val lineSpacing: Float = 1.5f,
        val paragraphSpacing: Float = 2.0f,
        val margins: Margins = Margins(),
        val colors: ColorScheme = ColorScheme()
    )

    /**
     * Margin configuration
     */
    data class Margins(
        val horizontal: Int = 16,
        val vertical: Int = 16,
        val paragraph: Int = 8
    )

    /**
     * Color scheme
     */
    data class ColorScheme(
        @ColorRes val primary: Int = R.color.primary,
        @ColorRes val primaryVariant: Int = R.color.primary_variant,
        @ColorRes val secondary: Int = R.color.secondary,
        @ColorRes val secondaryVariant: Int = R.color.secondary_variant,
        @ColorRes val background: Int = R.color.background,
        @ColorRes val surface: Int = R.color.surface,
        @ColorRes val error: Int = R.color.error,
        @ColorRes val onPrimary: Int = R.color.on_primary,
        @ColorRes val onSecondary: Int = R.color.on_secondary,
        @ColorRes val onBackground: Int = R.color.on_background,
        @ColorRes val onSurface: Int = R.color.on_surface,
        @ColorRes val onError: Int = R.color.on_error
    )

    /**
     * Returns reading-specific theme configuration
     */
    fun getReadingTheme(): ReadingTheme {
        return when (type) {
            ThemeType.LIGHT -> ReadingTheme(
                backgroundColor = R.color.reading_background_light,
                textColor = R.color.reading_text_light
            )
            ThemeType.DARK -> ReadingTheme(
                backgroundColor = R.color.reading_background_dark,
                textColor = R.color.reading_text_dark
            )
            ThemeType.SEPIA -> ReadingTheme(
                backgroundColor = R.color.reading_background_sepia,
                textColor = R.color.reading_text_sepia
            )
            ThemeType.AMOLED -> ReadingTheme(
                backgroundColor = R.color.reading_background_amoled,
                textColor = R.color.reading_text_amoled
            )
            else -> ReadingTheme()
        }
    }

    /**
     * Reading-specific theme
     */
    data class ReadingTheme(
        @ColorRes val backgroundColor: Int = R.color.reading_background_default,
        @ColorRes val textColor: Int = R.color.reading_text_default,
        val fontSize: Float = 1.0f,
        val fontFamily: String = "serif",
        val lineSpacing: Float = 1.6f,
        val paragraphSpacing: Float = 2.0f,
        val margins: Margins = Margins(24, 16, 12)
    )

    companion object {
        /**
         * Returns all available themes
         */
        fun getAvailableThemes(): List<Theme> {
            return listOf(Light, Dark, System, Sepia, Amoled, Dynamic)
        }

        /**
         * Returns reading-optimized themes
         */
        fun getReadingThemes(): List<Theme> {
            return listOf(Light, Dark, Sepia, Amoled)
        }

        /**
         * Creates a custom theme
         */
        fun createCustomTheme(
            name: String,
            @ColorRes primaryColor: Int,
            @ColorRes secondaryColor: Int,
            isDark: Boolean = false
        ): Theme {
            return Custom(
                name = name,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                config = ThemeConfig(isDark = isDark)
            )
        }
    }
}
