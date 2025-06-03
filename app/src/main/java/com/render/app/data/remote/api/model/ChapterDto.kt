/*
 * File: ChapterDto.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 14:11:51 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.data.remote.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ChapterDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("book_id")
    val bookId: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("number")
    val number: Int,

    @SerializedName("volume")
    val volume: Int? = null,

    @SerializedName("language")
    val language: String,

    @SerializedName("status")
    val status: ChapterStatus,

    @SerializedName("progress")
    val progress: Float = 0f,

    @SerializedName("is_read")
    val isRead: Boolean = false,

    @SerializedName("is_downloaded")
    val isDownloaded: Boolean = false,

    @SerializedName("is_bookmarked")
    val isBookmarked: Boolean = false,

    @SerializedName("word_count")
    val wordCount: Int? = null,

    @SerializedName("estimated_read_time")
    val estimatedReadTime: Int? = null,

    @SerializedName("last_read_timestamp")
    val lastReadTimestamp: Long? = null,

    @SerializedName("created_at")
    val createdAt: Date,

    @SerializedName("updated_at")
    val updatedAt: Date,

    @SerializedName("metadata")
    val metadata: ChapterMetadataDto? = null,

    @SerializedName("next_chapter")
    val nextChapter: ChapterRefDto? = null,

    @SerializedName("previous_chapter")
    val previousChapter: ChapterRefDto? = null
) {
    companion object {
        const val DEFAULT_LANGUAGE = "en"
        const val MIN_WORD_COUNT = 100
        const val MAX_WORD_COUNT = 10000
        const val WORDS_PER_MINUTE = 200
    }
}

data class ChapterMetadataDto(
    @SerializedName("translator")
    val translator: String? = null,

    @SerializedName("source")
    val source: String? = null,

    @SerializedName("revision")
    val revision: Int = 1,

    @SerializedName("tags")
    val tags: List<String> = emptyList(),

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("version")
    val version: String? = null
)

data class ChapterRefDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("number")
    val number: Int,

    @SerializedName("title")
    val title: String
)

data class ChapterProgressDto(
    @SerializedName("chapter_id")
    val chapterId: String,

    @SerializedName("progress")
    val progress: Float,

    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @SerializedName("device_id")
    val deviceId: String? = null,

    @SerializedName("reading_session_id")
    val readingSessionId: String? = null
)

enum class ChapterStatus {
    @SerializedName("draft")
    DRAFT,

    @SerializedName("published")
    PUBLISHED,

    @SerializedName("locked")
    LOCKED,

    @SerializedName("hidden")
    HIDDEN,

    @SerializedName("deleted")
    DELETED
}

/**
 * Extension functions for data manipulation
 */
fun ChapterDto.calculateReadingTime(): Int {
    return wordCount?.let { words ->
        (words / WORDS_PER_MINUTE).coerceAtLeast(1)
    } ?: estimatedReadTime ?: 5 // Default 5 minutes if no data available
}

fun ChapterDto.isAccessible(): Boolean {
    return status == ChapterStatus.PUBLISHED && !isDeleted()
}

fun ChapterDto.isDeleted(): Boolean {
    return status == ChapterStatus.DELETED
}

fun ChapterDto.toContentUri(): String {
    return "content://com.yourapp.readers/chapters/$id"
}

/**
 * Mapper functions for domain model conversion
 */
fun ChapterDto.toDomainModel(): Chapter {
    return Chapter(
        id = id,
        bookId = bookId,
        title = title,
        number = number,
        volume = volume,
        language = language,
        status = status.name,
        progress = progress,
        isRead = isRead,
        isDownloaded = isDownloaded,
        isBookmarked = isBookmarked,
        wordCount = wordCount,
        estimatedReadTime = calculateReadingTime(),
        lastReadTimestamp = lastReadTimestamp,
        createdAt = createdAt,
        updatedAt = updatedAt,
        metadata = metadata?.toDomainModel(),
        nextChapter = nextChapter?.toDomainModel(),
        previousChapter = previousChapter?.toDomainModel()
    )
}

fun ChapterMetadataDto.toDomainModel(): ChapterMetadata {
    return ChapterMetadata(
        translator = translator,
        source = source,
        revision = revision,
        tags = tags,
        notes = notes,
        version = version
    )
}

fun ChapterRefDto.toDomainModel(): ChapterRef {
    return ChapterRef(
        id = id,
        number = number,
        title = title
    )
}

private const val WORDS_PER_MINUTE = 200
