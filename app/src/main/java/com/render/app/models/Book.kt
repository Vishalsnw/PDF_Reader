/*
 * File: Book.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 19:16:55 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.reader.app.database.converters.DateConverter
import com.reader.app.database.converters.ListConverter
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * Book entity represents a book in the library with all its metadata and reading progress.
 * Features:
 * - Complete book metadata
 * - Reading progress tracking
 * - Format-specific properties
 * - Category management
 * - Statistics tracking
 */
@Parcelize
@Entity(tableName = "books")
@TypeConverters(DateConverter::class, ListConverter::class)
data class Book(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    // Basic Information
    val title: String,
    val author: String,
    val description: String = "",
    val isbn: String = "",
    val language: String = "en",
    
    // File Information
    val filePath: String,
    val format: BookFormat,
    val fileSize: Long = 0L,
    val coverPath: String? = null,
    
    // Reading Progress
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val readingProgress: Int = 0,
    val lastReadPosition: Long = 0L,
    
    // Reading Statistics
    val timeSpentReading: Long = 0L,
    val wordsRead: Int = 0,
    val averageReadingSpeed: Float = 0f,
    
    // Dates
    val dateAdded: Date = Date(),
    val lastReadTime: Date? = null,
    val lastModified: Date = Date(),
    val publishedDate: Date? = null,
    
    // Categories and Tags
    val categories: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    
    // User Preferences
    val fontSize: Float = 1.0f,
    val fontFamily: String? = null,
    val brightness: Float = 1.0f,
    val backgroundColor: Int? = null,
    val textColor: Int? = null,
    val lineSpacing: Float = 1.0f,
    val marginSize: Float = 1.0f,
    
    // Flags
    val isBookmarked: Boolean = false,
    val isFavorite: Boolean = false,
    val isFinished: Boolean = false,
    val isDownloaded: Boolean = true,
    
    // Additional Metadata
    val publisher: String = "",
    val series: String? = null,
    val seriesIndex: Int? = null,
    val rating: Float = 0f,
    val notes: String = "",
    
    // Format-specific properties
    val properties: Map<String, String> = emptyMap()
) : Parcelable {

    // Computed Properties
    val isStarted: Boolean
        get() = currentPage > 0 || readingProgress > 0

    val displayTitle: String
        get() = when {
            series != null -> "$series #$seriesIndex: $title"
            else -> title
        }

    val readingState: ReadingState
        get() = when {
            isFinished -> ReadingState.COMPLETED
            isStarted -> ReadingState.IN_PROGRESS
            else -> ReadingState.NOT_STARTED
        }

    val formattedSize: String
        get() = when {
            fileSize < 1024 -> "$fileSize B"
            fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
            else -> "${fileSize / (1024 * 1024)} MB"
        }

    val readingTimeFormatted: String
        get() = when {
            timeSpentReading < 60 -> "$timeSpentReading seconds"
            timeSpentReading < 3600 -> "${timeSpentReading / 60} minutes"
            else -> "${timeSpentReading / 3600} hours"
        }

    // Helper Functions
    fun updateProgress(newPage: Int): Book {
        val progress = ((newPage.toFloat() / totalPages) * 100).toInt()
        return copy(
            currentPage = newPage,
            readingProgress = progress,
            lastReadTime = Date(),
            isFinished = progress >= 100
        )
    }

    fun updateReadingTime(additionalTime: Long): Book {
        return copy(
            timeSpentReading = timeSpentReading + additionalTime,
            averageReadingSpeed = calculateNewReadingSpeed(additionalTime)
        )
    }

    fun toggleBookmark(): Book {
        return copy(isBookmarked = !isBookmarked)
    }

    fun toggleFavorite(): Book {
        return copy(isFavorite = !isFavorite)
    }

    fun addCategory(category: String): Book {
        return if (category !in categories) {
            copy(categories = categories + category)
        } else this
    }

    fun removeCategory(category: String): Book {
        return copy(categories = categories - category)
    }

    fun addTag(tag: String): Book {
        return if (tag !in tags) {
            copy(tags = tags + tag)
        } else this
    }

    fun removeTag(tag: String): Book {
        return copy(tags = tags - tag)
    }

    fun updateFontPreferences(
        newSize: Float? = null,
        newFamily: String? = null,
        newLineSpacing: Float? = null,
        newMargin: Float? = null
    ): Book {
        return copy(
            fontSize = newSize ?: fontSize,
            fontFamily = newFamily ?: fontFamily,
            lineSpacing = newLineSpacing ?: lineSpacing,
            marginSize = newMargin ?: marginSize
        )
    }

    fun updateColorPreferences(
        newBackground: Int? = null,
        newText: Int? = null,
        newBrightness: Float? = null
    ): Book {
        return copy(
            backgroundColor = newBackground ?: backgroundColor,
            textColor = newText ?: textColor,
            brightness = newBrightness ?: brightness
        )
    }

    private fun calculateNewReadingSpeed(additionalTime: Long): Float {
        val totalWords = wordsRead.toFloat()
        val totalTime = (timeSpentReading + additionalTime) / 3600f // Convert to hours
        return if (totalTime > 0) totalWords / totalTime else 0f
    }

    companion object {
        const val DEFAULT_FONT_SIZE = 1.0f
        const val DEFAULT_LINE_SPACING = 1.0f
        const val DEFAULT_MARGIN_SIZE = 1.0f
        const val DEFAULT_BRIGHTNESS = 1.0f
    }
}

enum class BookFormat {
    PDF, EPUB, MOBI, CBZ, CBR, TXT, DOC, DOCX;

    companion object {
        fun fromExtension(extension: String): BookFormat? {
            return values().firstOrNull { it.name.equals(extension, ignoreCase = true) }
        }
    }
}

enum class ReadingState {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}
package com.render.app.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "books")
data class Book(
    @PrimaryKey
    val id: String,
    val title: String,
    val author: String = "",
    val filePath: String,
    val coverPath: String? = null,
    val totalPages: Int = 0,
    val currentPage: Int = 0,
    val progress: Int = 0,
    val isDownloaded: Boolean = false,
    val isFavorite: Boolean = false,
    val lastReadTime: Date? = null,
    val addedTime: Date = Date(),
    val fileSize: Long = 0,
    val format: String = "",
    val description: String = "",
    val categories: List<String> = emptyList(),
    @ColumnInfo(name = "reading_time")
    val readingTime: Long = 0
)
