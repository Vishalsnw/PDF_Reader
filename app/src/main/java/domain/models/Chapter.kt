/*
 * File: Chapter.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 14:16:37 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.domain.models

import java.util.Date

data class Chapter(
    val id: String,
    val bookId: String,
    val title: String,
    val number: Int,
    val volume: Int? = null,
    val language: String,
    val status: String,
    val progress: Float = 0f,
    val isRead: Boolean = false,
    val isDownloaded: Boolean = false,
    val isBookmarked: Boolean = false,
    val wordCount: Int? = null,
    val estimatedReadTime: Int? = null,
    val lastReadTimestamp: Long? = null,
    val createdAt: Date,
    val updatedAt: Date,
    val metadata: ChapterMetadata? = null,
    val nextChapter: ChapterRef? = null,
    val previousChapter: ChapterRef? = null
) {
    companion object {
        const val MIN_PROGRESS = 0f
        const val MAX_PROGRESS = 1f
        const val COMPLETION_THRESHOLD = 0.95f
        const val DEFAULT_LANGUAGE = "en"
    }

    fun isCompleted(): Boolean = progress >= COMPLETION_THRESHOLD

    fun isInProgress(): Boolean = progress > MIN_PROGRESS && progress < COMPLETION_THRESHOLD

    fun canContinueReading(): Boolean = isDownloaded && !isCompleted()

    fun needsDownload(): Boolean = !isDownloaded && (isInProgress() || !isRead)

    fun hasNextChapter(): Boolean = nextChapter != null

    fun hasPreviousChapter(): Boolean = previousChapter != null

    fun isAccessible(): Boolean = status == "published" && !isDeleted()

    fun isDeleted(): Boolean = status == "deleted"

    fun getReadableProgress(): String {
        return when {
            progress >= COMPLETION_THRESHOLD -> "Completed"
            progress > MIN_PROGRESS -> "${(progress * 100).toInt()}%"
            else -> "Not Started"
        }
    }

    fun getTimeToRead(): String {
        return estimatedReadTime?.let { time ->
            when {
                time < 60 -> "$time min"
                else -> "${time / 60}h ${time % 60}m"
            }
        } ?: "Unknown"
    }
}

data class ChapterMetadata(
    val translator: String? = null,
    val source: String? = null,
    val revision: Int = 1,
    val tags: List<String> = emptyList(),
    val notes: String? = null,
    val version: String? = null
) {
    fun hasTranslator(): Boolean = !translator.isNullOrBlank()
    fun hasSource(): Boolean = !source.isNullOrBlank()
    fun hasNotes(): Boolean = !notes.isNullOrBlank()
    fun hasTags(): Boolean = tags.isNotEmpty()
}

data class ChapterRef(
    val id: String,
    val number: Int,
    val title: String
)

data class ChapterContent(
    val chapterId: String,
    val content: String,
    val images: List<ChapterImage> = emptyList(),
    val footnotes: Map<String, String> = emptyMap(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
    fun hasImages(): Boolean = images.isNotEmpty()
    fun hasFootnotes(): Boolean = footnotes.isNotEmpty()
    fun isEmpty(): Boolean = content.isEmpty() && !hasImages()
    fun estimateReadingTime(wordsPerMinute: Int = 200): Int {
        val wordCount = content.split("\\s+".toRegex()).size
        return (wordCount / wordsPerMinute).coerceAtLeast(1)
    }
}

data class ChapterImage(
    val url: String,
    val width: Int,
    val height: Int,
    val caption: String? = null,
    val isDownloaded: Boolean = false,
    val localPath: String? = null
) {
    fun aspectRatio(): Float = width.toFloat() / height.toFloat()
    fun isLocallyAvailable(): Boolean = isDownloaded && !localPath.isNullOrBlank()
    fun getDisplayUrl(): String = localPath ?: url
}

/**
 * Extension functions for List<Chapter>
 */
fun List<Chapter>.sortedByNumber(): List<Chapter> =
    sortedBy { it.number }

fun List<Chapter>.filterDownloaded(): List<Chapter> =
    filter { it.isDownloaded }

fun List<Chapter>.filterUnread(): List<Chapter> =
    filter { !it.isRead }

fun List<Chapter>.filterAccessible(): List<Chapter> =
    filter { it.isAccessible() }

fun List<Chapter>.getReadProgress(): Float =
    count { it.isRead }.toFloat() / size.coerceAtLeast(1)

/**
 * Factory methods
 */
fun Chapter.Companion.createEmpty(id: String = "", bookId: String = "") = Chapter(
    id = id,
    bookId = bookId,
    title = "",
    number = 0,
    language = DEFAULT_LANGUAGE,
    status = "draft",
    createdAt = Date(),
    updatedAt = Date()
)

/**
 * Utility functions
 */
fun Chapter.toReadingState(): ReadingState = ReadingState(
    chapterId = id,
    bookId = bookId,
    progress = progress,
    lastReadTimestamp = lastReadTimestamp ?: System.currentTimeMillis()
)

data class ReadingState(
    val chapterId: String,
    val bookId: String,
    val progress: Float,
    val lastReadTimestamp: Long
)
