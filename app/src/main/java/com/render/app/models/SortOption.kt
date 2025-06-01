/*
 * File: SortOption.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 20:29:03 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.reader.app.R

/**
 * SortOption defines different sorting options for the library.
 * Features:
 * - Sort types
 * - Sort directions
 * - Custom comparators
 * - Section-specific sorting
 */
sealed class SortOption(
    @StringRes val titleRes: Int,
    @DrawableRes val iconRes: Int,
    val type: SortType,
    val direction: SortDirection = SortDirection.ASCENDING,
    val defaultEnabled: Boolean = true
) : Comparable<SortOption> {

    /**
     * Sort by title
     */
    data class Title(
        override val direction: SortDirection = SortDirection.ASCENDING
    ) : SortOption(
        titleRes = R.string.sort_title,
        iconRes = R.drawable.ic_sort_alpha,
        type = SortType.TITLE,
        direction = direction
    )

    /**
     * Sort by author
     */
    data class Author(
        override val direction: SortDirection = SortDirection.ASCENDING
    ) : SortOption(
        titleRes = R.string.sort_author,
        iconRes = R.drawable.ic_sort_author,
        type = SortType.AUTHOR,
        direction = direction
    )

    /**
     * Sort by date added
     */
    data class DateAdded(
        override val direction: SortDirection = SortDirection.DESCENDING
    ) : SortOption(
        titleRes = R.string.sort_date_added,
        iconRes = R.drawable.ic_sort_date_added,
        type = SortType.DATE_ADDED,
        direction = direction
    )

    /**
     * Sort by last read time
     */
    data class LastRead(
        override val direction: SortDirection = SortDirection.DESCENDING
    ) : SortOption(
        titleRes = R.string.sort_last_read,
        iconRes = R.drawable.ic_sort_last_read,
        type = SortType.LAST_READ,
        direction = direction
    )

    /**
     * Sort by reading progress
     */
    data class Progress(
        override val direction: SortDirection = SortDirection.DESCENDING
    ) : SortOption(
        titleRes = R.string.sort_progress,
        iconRes = R.drawable.ic_sort_progress,
        type = SortType.PROGRESS,
        direction = direction
    )

    /**
     * Sort by rating
     */
    data class Rating(
        override val direction: SortDirection = SortDirection.DESCENDING
    ) : SortOption(
        titleRes = R.string.sort_rating,
        iconRes = R.drawable.ic_sort_rating,
        type = SortType.RATING,
        direction = direction
    )

    /**
     * Sort by file size
     */
    data class FileSize(
        override val direction: SortDirection = SortDirection.DESCENDING
    ) : SortOption(
        titleRes = R.string.sort_file_size,
        iconRes = R.drawable.ic_sort_size,
        type = SortType.FILE_SIZE,
        direction = direction
    )

    /**
     * Custom sort option
     */
    data class Custom(
        @StringRes val customTitleRes: Int,
        @DrawableRes val customIconRes: Int,
        val comparator: Comparator<Book>,
        override val direction: SortDirection = SortDirection.ASCENDING
    ) : SortOption(
        titleRes = customTitleRes,
        iconRes = customIconRes,
        type = SortType.CUSTOM,
        direction = direction
    )

    /**
     * Sort types
     */
    enum class SortType {
        TITLE,
        AUTHOR,
        DATE_ADDED,
        LAST_READ,
        PROGRESS,
        RATING,
        FILE_SIZE,
        CUSTOM
    }

    /**
     * Sort directions
     */
    enum class SortDirection {
        ASCENDING,
        DESCENDING
    }

    /**
     * Returns SQL order by clause
     */
    fun toSqlOrderBy(): String {
        val column = when (type) {
            SortType.TITLE -> "title"
            SortType.AUTHOR -> "author"
            SortType.DATE_ADDED -> "createdAt"
            SortType.LAST_READ -> "lastReadTime"
            SortType.PROGRESS -> "readingProgress"
            SortType.RATING -> "rating"
            SortType.FILE_SIZE -> "fileSize"
            SortType.CUSTOM -> throw IllegalStateException("Custom sort doesn't support SQL ordering")
        }
        val direction = if (direction == SortDirection.ASCENDING) "ASC" else "DESC"
        return "$column $direction"
    }

    /**
     * Returns comparator for the sort option
     */
    fun getComparator(): Comparator<Book> {
        val baseComparator = when (type) {
            SortType.TITLE -> compareBy<Book> { it.title }
            SortType.AUTHOR -> compareBy<Book> { it.author }
            SortType.DATE_ADDED -> compareBy<Book> { it.createdAt }
            SortType.LAST_READ -> compareBy<Book> { it.lastReadTime }
            SortType.PROGRESS -> compareBy<Book> { it.readingProgress }
            SortType.RATING -> compareBy<Book> { it.rating }
            SortType.FILE_SIZE -> compareBy<Book> { it.fileSize }
            SortType.CUSTOM -> (this as Custom).comparator
        }

        return if (direction == SortDirection.DESCENDING) {
            baseComparator.reversed()
        } else {
            baseComparator
        }
    }

    /**
     * Toggle sort direction
     */
    fun toggleDirection(): SortOption {
        return when (this) {
            is Title -> copy(direction = direction.toggle())
            is Author -> copy(direction = direction.toggle())
            is DateAdded -> copy(direction = direction.toggle())
            is LastRead -> copy(direction = direction.toggle())
            is Progress -> copy(direction = direction.toggle())
            is Rating -> copy(direction = direction.toggle())
            is FileSize -> copy(direction = direction.toggle())
            is Custom -> copy(direction = direction.toggle())
        }
    }

    override fun compareTo(other: SortOption): Int {
        return if (this.type == other.type) {
            this.direction.compareTo(other.direction)
        } else {
            this.type.compareTo(other.type)
        }
    }

    companion object {
        /**
         * Returns all available sort options
         */
        fun getAvailableOptions(): List<SortOption> {
            return listOf(
                Title(),
                Author(),
                DateAdded(),
                LastRead(),
                Progress(),
                Rating(),
                FileSize()
            )
        }

        /**
         * Returns default sort option for section
         */
        fun getDefaultForSection(section: LibrarySection): SortOption {
            return when (section) {
                is LibrarySection.Recent -> DateAdded()
                is LibrarySection.Reading -> LastRead()
                is LibrarySection.Completed -> Rating()
                else -> Title()
            }
        }
    }
}

/**
 * Toggle sort direction
 */
private fun SortOption.SortDirection.toggle(): SortOption.SortDirection {
    return if (this == SortOption.SortDirection.ASCENDING) {
        SortOption.SortDirection.DESCENDING
    } else {
        SortOption.SortDirection.ASCENDING
    }
}
