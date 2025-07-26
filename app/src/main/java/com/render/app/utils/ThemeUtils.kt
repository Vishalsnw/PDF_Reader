/*
 * File: ThemeUtils.kt
 * Created: 2025-06-02
 * Author: Vishalsnw
 * Last Modified: 2025-06-02 14:44:32 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.render.app.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.render.app.models.Theme
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Utility class for theme and color management.
 * Features:
 * - Theme management
 * - Color manipulation
 * - UI customization
 * - System theme handling
 */
object ThemeUtils {

    /**
     * Theme Management
     */
    fun setAppTheme(theme: Theme) {
        when (theme.type) {
            Theme.ThemeType.LIGHT -> setLightTheme()
            Theme.ThemeType.DARK -> setDarkTheme()
            Theme.ThemeType.SYSTEM -> setSystemTheme()
            Theme.ThemeType.AMOLED -> setAmoledTheme()
            Theme.ThemeType.SEPIA -> setSepiaTheme()
            Theme.ThemeType.DYNAMIC -> setDynamicTheme()
            Theme.ThemeType.CUSTOM -> setCustomTheme(theme)
        }
    }

    fun isDarkTheme(context: Context): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
    }

    fun applySystemBars(window: Window, lightTheme: Boolean) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = lightTheme
        controller.isAppearanceLightNavigationBars = lightTheme
    }

    /**
     * Color Manipulation
     */
    fun adjustBrightness(color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] = hsv[2] * factor
        return Color.HSVToColor(Color.alpha(color), hsv)
    }

    fun darken(color: Int, factor: Float): Int {
        return adjustBrightness(color, max(0f, 1f - factor))
    }

    fun lighten(color: Int, factor: Float): Int {
        return adjustBrightness(color, min(1f, 1f + factor))
    }

    fun getAlphaColor(color: Int, alpha: Float): Int {
        return Color.argb(
            (alpha * 255).roundToInt(),
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
    }

    /**
     * Color Calculation
     */
    fun calculateContrastColor(backgroundColor: Int): Int {
        val luminance = (0.299 * Color.red(backgroundColor) +
                0.587 * Color.green(backgroundColor) +
                0.114 * Color.blue(backgroundColor)) / 255
        return if (luminance > 0.5f) Color.BLACK else Color.WHITE
    }

    fun calculateComplementaryColor(color: Int): Int {
        return Color.rgb(
            255 - Color.red(color),
            255 - Color.green(color),
            255 - Color.blue(color)
        )
    }

    fun calculateAccentColor(baseColor: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)
        hsv[0] = (hsv[0] + 180) % 360
        return Color.HSVToColor(hsv)
    }

    /**
     * Theme Application
     */
    private fun setLightTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun setDarkTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    private fun setSystemTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    private fun setAmoledTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        // Additional AMOLED-specific settings
    }

    private fun setSepiaTheme() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Additional Sepia-specific settings
    }

    private fun setDynamicTheme() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Apply Material You dynamic colors
        }
    }

    private fun setCustomTheme(theme: Theme) {
        when (theme) {
            is Theme.Custom -> {
                // Apply custom theme settings
            }
            else -> setSystemTheme()
        }
    }

    /**
     * UI Customization
     */
    fun applyThemeToView(view: View, theme: Theme) {
        when (theme.type) {
            Theme.ThemeType.LIGHT -> applyLightThemeToView(view)
            Theme.ThemeType.DARK -> applyDarkThemeToView(view)
            Theme.ThemeType.SEPIA -> applySepiaThemeToView(view)
            Theme.ThemeType.AMOLED -> applyAmoledThemeToView(view)
            Theme.ThemeType.CUSTOM -> applyCustomThemeToView(view, theme)
            else -> applySystemThemeToView(view)
        }
    }

    private fun applyLightThemeToView(view: View) {
        view.setBackgroundColor(Color.WHITE)
        // Additional light theme customizations
    }

    private fun applyDarkThemeToView(view: View) {
        view.setBackgroundColor(Color.parseColor("#121212"))
        // Additional dark theme customizations
    }

    private fun applySepiaThemeToView(view: View) {
        view.setBackgroundColor(Color.parseColor("#F4ECD8"))
        // Additional sepia theme customizations
    }

    private fun applyAmoledThemeToView(view: View) {
        view.setBackgroundColor(Color.BLACK)
        // Additional AMOLED theme customizations
    }

    private fun applySystemThemeToView(view: View) {
        // Apply system theme based on current configuration
    }

    private fun applyCustomThemeToView(view: View, theme: Theme) {
        when (theme) {
            is Theme.Custom -> {
                view.setBackgroundColor(ContextCompat.getColor(
                    view.context,
                    theme.config.colors.background
                ))
                // Additional custom theme settings
            }
            else -> applySystemThemeToView(view)
        }
    }

    /**
     * Constants
     */
    object Colors {
        const val SEPIA_BACKGROUND = "#F4ECD8"
        const val DARK_BACKGROUND = "#121212"
        const val AMOLED_BACKGROUND = "#000000"
        
        const val DEFAULT_ALPHA = 0.87f
        const val SECONDARY_ALPHA = 0.54f
        const val DISABLED_ALPHA = 0.38f
    }

    object Factors {
        const val DARKEN_FACTOR = 0.2f
        const val LIGHTEN_FACTOR = 0.2f
        const val EMPHASIS_HIGH = 0.87f
        const val EMPHASIS_MEDIUM = 0.60f
        const val EMPHASIS_DISABLED = 0.38f
    }
}
