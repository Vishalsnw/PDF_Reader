/*
 * File: LibrarySection.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 20:25:24 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.reader.app.R

/**
 * LibrarySection represents different sections in the library view.
 * Features:
 * - Section types
 * - Sorting options
 * - Filter criteria
 * - Display configuration
 */
sealed class LibrarySection(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    val type: SectionType,
    val sortType: SortType = SortType.LAST_READ,
    val filterType: FilterType = FilterType.NONE
) {
    /**
     * All books in the library
     */
    object AllBooks : LibrarySection(
        titleRes = R.string.section_all_books,
        iconRes = R.drawable.ic_library,
        type = SectionType.ALL
    )

    /**
     * Currently reading books
     */
    object Reading : LibrarySection(
        titleRes = R.string.section_reading,
        iconRes = R.drawable.ic_reading,
        type = SectionType.READING,
        filterType = FilterType.IN_PROGRESS
    )

    /**
     * Completed books
     */
    object Completed : LibrarySection(
        titleRes = R.string.section_completed,
        iconRes = R.drawable.ic_completed,
        type = SectionType.COMPLETED,
        filterType = FilterType.COMPLETED
    )

    /**
     * Recently added books
     */
    object Recent : LibrarySection(
        titleRes = R.string.section_recent,
        iconRes = R.drawable.ic_recent,
        type = SectionType.RECENT,
        sortType = SortType.DATE_ADDED
    )

    /**
     * Favorite books
     */
    object Favorites : LibrarySection(
        titleRes = R.string.section_favorites,
        iconRes = R.drawable.ic_favorite,
        type = SectionType.FAVORITES,
        filterType = FilterType.FAVORITES
    )

    /**
     * Books in a specific category
     */
    data class Category(
        val categoryId: String,
        val categoryName: String
    ) : LibrarySection(
        titleRes = R.string.section_category,
        iconRes = R.drawable.ic_category,
        type = SectionType.CATEGORY
    )

    /**
     * Custom dynamic section
     */
    data class Custom(
        @StringRes val customTitleRes: Int,
        @DrawableRes val customIconRes: Int,
        val customFilter: FilterType,
        val customSort: SortType
    ) : LibrarySection(
        titleRes = customTitleRes,
        iconRes = customIconRes,
        type = SectionType.CUSTOM,
        sortType = customSort,
        filterType = customFilter
    )

    /**
     * Section types
     */
    enum class SectionType {
        ALL,
        READING,
        COMPLETED,
        RECENT,
        FAVORITES,
        CATEGORY,
        CUSTOM
    }

    /**
     * Sort types for sections
     */
    enum class SortType {
        TITLE,
        AUTHOR,
        LAST_READ,
        DATE_ADDED,
        PROGRESS,
        RATING,
        CUSTOM
    }

    /**
     * Filter types for sections
     */
    enum class FilterType {
        NONE,
        IN_PROGRESS,
        COMPLETED,
        FAVORITES,
        DOWNLOADED,
        UNREAD,
        CUSTOM
    }

    /**
     * Returns query parameters for the section
     */
    fun getQueryParams(): QueryParams {
        return QueryParams(
            sectionType = type,
            sortType = sortType,
            filterType = filterType,
            categoryId = (this as? Category)?.categoryId
        )
    }

    /**
     * Checks if section can be customized
     */
    fun isCustomizable(): Boolean {
        return type == SectionType.CUSTOM || type == SectionType.CATEGORY
    }

    /**
     * Returns section display configuration
     */
    fun getDisplayConfig(): DisplayConfig {
        return when (type) {
            SectionType.RECENT -> DisplayConfig(
                showDates = true,
                showProgress = false,
                maxItems = 10
            )
            SectionType.READING -> DisplayConfig(
                showDates = true,
                showProgress = true,
                showLastRead = true
            )
            SectionType.COMPLETED -> DisplayConfig(
                showDates = false,
                showProgress = false,
                showRating = true
            )
            else -> DisplayConfig()
        }
    }

    companion object {
        /**
         * Returns all default sections
         */
        fun getDefaultSections(): List<LibrarySection> {
            return listOf(
                AllBooks,
                Reading,
                Recent,
                Completed,
                Favorites
            )
        }

        /**
         * Creates a custom section
         */
        fun createCustomSection(
            @StringRes titleRes: Int,
            @DrawableRes iconRes: Int,
            filter: FilterType = FilterType.NONE,
            sort: SortType = SortType.LAST_READ
        ): LibrarySection {
            return Custom(
                customTitleRes = titleRes,
                customIconRes = iconRes,
                customFilter = filter,
                customSort = sort
            )
        }
    }

    /**
     * Query parameters for section
     */
    data class QueryParams(
        val sectionType: SectionType,
        val sortType: SortType,
        val filterType: FilterType,
        val categoryId: String? = null
    )

    /**
     * Display configuration for section
     */
    data class DisplayConfig(
        val showDates: Boolean = false,
        val showProgress: Boolean = false,
        val showLastRead: Boolean = false,
        val showRating: Boolean = false,
        val maxItems: Int = -1,
        val gridColumns: Int = 2
    )
}
