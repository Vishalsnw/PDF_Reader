/*
 * File: ReadingMode.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 20:27:25 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.reader.app.R

/**
 * ReadingMode defines different reading modes and their configurations.
 * Features:
 * - Reading modes
 * - Display settings
 * - Navigation options
 * - Gesture controls
 */
sealed class ReadingMode(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    val type: ReadingType,
    val defaultConfig: ReadingConfig = ReadingConfig()
) {
    /**
     * Continuous vertical scrolling mode
     */
    object Scroll : ReadingMode(
        titleRes = R.string.mode_scroll,
        iconRes = R.drawable.ic_scroll,
        type = ReadingType.SCROLL,
        defaultConfig = ReadingConfig(
            isPaginated = false,
            isScrollEnabled = true,
            allowZoom = true,
            snapToPage = false
        )
    )

    /**
     * Page-by-page mode with horizontal swipe
     */
    object Paged : ReadingMode(
        titleRes = R.string.mode_paged,
        iconRes = R.drawable.ic_paged,
        type = ReadingType.PAGED,
        defaultConfig = ReadingConfig(
            isPaginated = true,
            isScrollEnabled = false,
            snapToPage = true,
            pageTransitionAnimation = PageTransition.CURL
        )
    )

    /**
     * Webtoon-style continuous strip mode
     */
    object Webtoon : ReadingMode(
        titleRes = R.string.mode_webtoon,
        iconRes = R.drawable.ic_webtoon,
        type = ReadingType.WEBTOON,
        defaultConfig = ReadingConfig(
            isPaginated = false,
            isScrollEnabled = true,
            fitToWidth = true,
            allowZoom = false
        )
    )

    /**
     * Two-page spread mode for landscape
     */
    object Spread : ReadingMode(
        titleRes = R.string.mode_spread,
        iconRes = R.drawable.ic_spread,
        type = ReadingType.SPREAD,
        defaultConfig = ReadingConfig(
            isPaginated = true,
            isScrollEnabled = false,
            snapToPage = true,
            pagesPerView = 2,
            pageTransitionAnimation = PageTransition.SLIDE
        )
    )

    /**
     * Reading types
     */
    enum class ReadingType {
        SCROLL,
        PAGED,
        WEBTOON,
        SPREAD
    }

    /**
     * Page transition animations
     */
    enum class PageTransition {
        NONE,
        SLIDE,
        FADE,
        CURL,
        FLIP
    }

    /**
     * Reading configuration
     */
    data class ReadingConfig(
        val isPaginated: Boolean = false,
        val isScrollEnabled: Boolean = true,
        val snapToPage: Boolean = false,
        val allowZoom: Boolean = true,
        val fitToWidth: Boolean = false,
        val pagesPerView: Int = 1,
        val pageTransitionAnimation: PageTransition = PageTransition.NONE,
        val gestureConfig: GestureConfig = GestureConfig(),
        val displayConfig: DisplayConfig = DisplayConfig()
    )

    /**
     * Gesture configuration
     */
    data class GestureConfig(
        val tapToTurnPage: Boolean = true,
        val doubleTapToZoom: Boolean = true,
        val volumeKeysNavigation: Boolean = false,
        val tapZones: Map<TapZone, TapAction> = defaultTapZones
    ) {
        enum class TapZone {
            TOP_LEFT, TOP_CENTER, TOP_RIGHT,
            CENTER_LEFT, CENTER, CENTER_RIGHT,
            BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
        }

        enum class TapAction {
            PREVIOUS_PAGE,
            NEXT_PAGE,
            SHOW_MENU,
            NONE
        }

        companion object {
            private val defaultTapZones = mapOf(
                TapZone.LEFT to TapAction.PREVIOUS_PAGE,
                TapZone.RIGHT to TapAction.NEXT_PAGE,
                TapZone.CENTER to TapAction.SHOW_MENU
            )
        }
    }

    /**
     * Display configuration
     */
    data class DisplayConfig(
        val backgroundColor: Int = android.graphics.Color.WHITE,
        val keepScreenOn: Boolean = true,
        val fullscreen: Boolean = true,
        val showPageNumber: Boolean = true,
        val showProgress: Boolean = true,
        val showClock: Boolean = false,
        val showBatteryStatus: Boolean = false
    )

    /**
     * Returns mode-specific settings
     */
    fun getModeSettings(): ModeSettings {
        return when (type) {
            ReadingType.SCROLL -> ModeSettings(
                allowTextSelection = true,
                allowBookmarks = true,
                allowHighlights = true
            )
            ReadingType.PAGED -> ModeSettings(
                allowTextSelection = true,
                allowBookmarks = true,
                allowHighlights = true
            )
            ReadingType.WEBTOON -> ModeSettings(
                allowTextSelection = false,
                allowBookmarks = true,
                allowHighlights = false
            )
            ReadingType.SPREAD -> ModeSettings(
                allowTextSelection = false,
                allowBookmarks = true,
                allowHighlights = false
            )
        }
    }

    /**
     * Mode-specific settings
     */
    data class ModeSettings(
        val allowTextSelection: Boolean,
        val allowBookmarks: Boolean,
        val allowHighlights: Boolean
    )

    companion object {
        /**
         * Returns all available reading modes
         */
        fun getAvailableModes(): List<ReadingMode> {
            return listOf(Scroll, Paged, Webtoon, Spread)
        }

        /**
         * Returns default mode for file type
         */
        fun getDefaultModeForFile(mimeType: String): ReadingMode {
            return when {
                mimeType.startsWith("image/") -> Webtoon
                mimeType == "application/pdf" -> Paged
                mimeType == "application/epub+zip" -> Scroll
                else -> Scroll
            }
        }
    }
}
