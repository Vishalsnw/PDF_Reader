/*
 * File: ChapterDao.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 19:56:31 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.database.dao

import androidx.room.*
import com.reader.app.models.Chapter
import com.reader.app.models.ChapterWithBookmarks
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * ChapterDao provides database operations for Chapter entities.
 * Features:
 * - CRUD operations
 * - Progress tracking
 * - Bookmark management
 * - Reading statistics
 */
@Dao
interface ChapterDao {

    // Create Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chapter: Chapter): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chapters: List<Chapter>): List<Long>

    // Read Operations
    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    fun getChapterById(chapterId: String): Flow<Chapter?>

    @Query("""
        SELECT * FROM chapters 
        WHERE bookId = :bookId 
        ORDER BY chapterIndex ASC
    """)
    fun getChaptersByBookId(bookId: String): Flow<List<Chapter>>

    @Query("""
        SELECT * FROM chapters 
        WHERE bookId = :bookId 
        AND isRead = 0 
        ORDER BY chapterIndex ASC 
        LIMIT 1
    """)
    fun getNextUnreadChapter(bookId: String): Flow<Chapter?>

    @Query("""
        SELECT * FROM chapters 
        WHERE bookId = :bookId 
        AND lastReadPosition > 0 
        ORDER BY lastReadTime DESC 
        LIMIT 1
    """)
    fun getLastReadChapter(bookId: String): Flow<Chapter?>

    // Update Operations
    @Update
    suspend fun update(chapter: Chapter)

    @Update
    suspend fun updateAll(chapters: List<Chapter>)

    @Query("""
        UPDATE chapters 
        SET lastReadPosition = :position,
            lastReadTime = :timestamp,
            isRead = CASE 
                WHEN :position >= totalLength THEN 1 
                ELSE isRead 
            END
        WHERE id = :chapterId
    """)
    suspend fun updateReadingProgress(
        chapterId: String,
        position: Long,
        timestamp: Date = Date()
    )

    @Query("""
        UPDATE chapters 
        SET isRead = :isRead,
            lastReadTime = CASE 
                WHEN :isRead = 1 THEN :timestamp 
                ELSE lastReadTime 
            END
        WHERE id = :chapterId
    """)
    suspend fun markChapterRead(
        chapterId: String,
        isRead: Boolean,
        timestamp: Date = Date()
    )

    @Query("UPDATE chapters SET isBookmarked = :isBookmarked WHERE id = :chapterId")
    suspend fun updateBookmark(chapterId: String, isBookmarked: Boolean)

    // Delete Operations
    @Delete
    suspend fun delete(chapter: Chapter)

    @Delete
    suspend fun deleteAll(chapters: List<Chapter>)

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChaptersByBookId(bookId: String)

    // Statistics Queries
    @Query("""
        SELECT COUNT(*) FROM chapters 
        WHERE bookId = :bookId AND isRead = 1
    """)
    fun getReadChapterCount(bookId: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) FROM chapters 
        WHERE bookId = :bookId
    """)
    fun getTotalChapterCount(bookId: String): Flow<Int>

    @Query("""
        SELECT ROUND(
            (SELECT COUNT(*) 
             FROM chapters 
             WHERE bookId = :bookId AND isRead = 1) * 100.0 / 
            (SELECT COUNT(*) 
             FROM chapters 
             WHERE bookId = :bookId)
        ) 
        AS progress
    """)
    fun getReadingProgress(bookId: String): Flow<Int>

    @Query("""
        SELECT SUM(timeSpentReading) 
        FROM chapters 
        WHERE bookId = :bookId
    """)
    fun getTotalReadingTime(bookId: String): Flow<Long>

    // Bookmark Operations
    @Transaction
    @Query("""
        SELECT * FROM chapters 
        WHERE bookId = :bookId 
        AND isBookmarked = 1 
        ORDER BY chapterIndex ASC
    """)
    fun getBookmarkedChapters(bookId: String): Flow<List<ChapterWithBookmarks>>

    @Query("""
        SELECT * FROM chapters 
        WHERE bookId = :bookId 
        AND lastReadTime IS NOT NULL 
        ORDER BY lastReadTime DESC 
        LIMIT :limit
    """)
    fun getRecentChapters(bookId: String, limit: Int = 5): Flow<List<Chapter>>

    // Batch Operations
    @Transaction
    suspend fun updateChaptersInBatch(chapters: List<Chapter>) {
        chapters.forEach { chapter ->
            update(chapter)
        }
    }

    @Query("""
        UPDATE chapters 
        SET isRead = 1,
            lastReadTime = :timestamp 
        WHERE bookId = :bookId 
        AND chapterIndex <= :upToIndex
    """)
    suspend fun markPreviousChaptersRead(
        bookId: String,
        upToIndex: Int,
        timestamp: Date = Date()
    )

    // Search Operations
    @Query("""
        SELECT * FROM chapters 
        WHERE bookId = :bookId 
        AND (title LIKE '%' || :query || '%' 
             OR content LIKE '%' || :query || '%')
        ORDER BY chapterIndex ASC
    """)
    fun searchChapters(bookId: String, query: String): Flow<List<Chapter>>

    companion object {
        const val DEFAULT_RECENT_LIMIT = 5
        const val PROGRESS_COMPLETE = 100
    }
}
package com.render.app.database.dao

import androidx.room.*
import com.render.app.models.Chapter
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE bookId = :bookId")
    fun getChaptersByBookId(bookId: String): Flow<List<Chapter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chapter: Chapter)

    @Update
    suspend fun update(chapter: Chapter)

    @Delete
    suspend fun delete(chapter: Chapter)
}
