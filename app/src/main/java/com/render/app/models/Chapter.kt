/*
 * File: Chapter.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 20:23:53 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Chapter represents a single chapter in a book.
 * Features:
 * - Reading progress tracking
 * - Bookmark support
 * - Last read position
 * - Reading statistics
 */
@Entity(
    tableName = "chapters",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("bookId"),
        Index("chapterIndex"),
        Index("lastReadTime")
    ]
)
data class Chapter(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "bookId")
    val bookId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "chapterIndex")
    val chapterIndex: Int,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "totalLength")
    val totalLength: Long,

    @ColumnInfo(name = "lastReadPosition")
    val lastReadPosition: Long = 0,

    @ColumnInfo(name = "lastReadTime")
    val lastReadTime: Date? = null,

    @ColumnInfo(name = "isRead")
    val isRead: Boolean = false,

    @ColumnInfo(name = "isBookmarked")
    val isBookmarked: Boolean = false,

    @ColumnInfo(name = "timeSpentReading")
    val timeSpentReading: Long = 0,

    @ColumnInfo(name = "highlights")
    val highlights: List<Highlight>? = null,

    @ColumnInfo(name = "notes")
    val notes: List<Note>? = null,

    @ColumnInfo(name = "bookmarks")
    val bookmarks: List<Bookmark>? = null,

    @ColumnInfo(name = "createdAt")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Date = Date()
) {
    /**
     * Returns reading progress as percentage
     */
    fun getReadingProgress(): Int {
        return if (totalLength > 0) {
            ((lastReadPosition.toDouble() / totalLength) * 100).toInt()
                .coerceIn(0, 100)
        } else {
            0
        }
    }

    /**
     * Checks if chapter is partially read
     */
    fun isPartiallyRead(): Boolean {
        return lastReadPosition > 0 && !isRead
    }

    /**
     * Returns formatted reading time
     */
    fun getFormattedReadingTime(): String {
        return when {
            timeSpentReading < 60_000 -> // Less than 1 minute
                "${timeSpentReading / 1000}s"
            timeSpentReading < 3_600_000 -> // Less than 1 hour
                "${timeSpentReading / 60_000}m"
            else -> // Hours and minutes
                "${timeSpentReading / 3_600_000}h ${(timeSpentReading % 3_600_000) / 60_000}m"
        }
    }

    /**
     * Returns estimated reading time based on average reading speed
     */
    fun getEstimatedReadingTime(wordsPerMinute: Int = 250): Int {
        val wordCount = content.split("\\s+".toRegex()).size
        return (wordCount / wordsPerMinute.toDouble()).toInt()
    }

    /**
     * Creates a copy with updated reading progress
     */
    fun updateProgress(
        newPosition: Long,
        readingTime: Long
    ): Chapter {
        return copy(
            lastReadPosition = newPosition,
            lastReadTime = Date(),
            isRead = newPosition >= totalLength,
            timeSpentReading = timeSpentReading + readingTime,
            updatedAt = Date()
        )
    }

    /**
     * Creates a copy with updated bookmark status
     */
    fun toggleBookmark(): Chapter {
        return copy(
            isBookmarked = !isBookmarked,
            updatedAt = Date()
        )
    }

    /**
     * Add a highlight to the chapter
     */
    fun addHighlight(highlight: Highlight): Chapter {
        val updatedHighlights = (highlights ?: emptyList()) + highlight
        return copy(
            highlights = updatedHighlights,
            updatedAt = Date()
        )
    }

    /**
     * Add a note to the chapter
     */
    fun addNote(note: Note): Chapter {
        val updatedNotes = (notes ?: emptyList()) + note
        return copy(
            notes = updatedNotes,
            updatedAt = Date()
        )
    }

    companion object {
        const val WORDS_PER_MINUTE_SLOW = 150
        const val WORDS_PER_MINUTE_AVERAGE = 250
        const val WORDS_PER_MINUTE_FAST = 350
    }
}

/**
 * Represents a highlighted section in the chapter
 */
data class Highlight(
    val id: String,
    val startPosition: Long,
    val endPosition: Long,
    val color: Int,
    val text: String,
    val createdAt: Date = Date()
)

/**
 * Represents a note attached to a position in the chapter
 */
data class Note(
    val id: String,
    val position: Long,
    val text: String,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

/**
 * Represents a bookmark in the chapter
 */
data class Bookmark(
    val id: String,
    val position: Long,
    val label: String?,
    val createdAt: Date = Date()
)
