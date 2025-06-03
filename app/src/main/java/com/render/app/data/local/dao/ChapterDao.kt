/*
 * File: ChapterDao.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 06:31:45 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.data.local.dao

import androidx.room.*
import com.yourapp.readers.data.local.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY number ASC")
    fun getChaptersByBook(bookId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getChapterById(chapterId: String): ChapterEntity?

    @Query("""
        SELECT * FROM chapters 
        WHERE bookId = :bookId 
        AND number > :currentNumber 
        ORDER BY number ASC 
        LIMIT 1
    """)
    suspend fun getNextChapter(bookId: String, currentNumber: Int): ChapterEntity?

    @Query("""
        SELECT * FROM chapters 
        WHERE bookId = :bookId 
        AND number < :currentNumber 
        ORDER BY number DESC 
        LIMIT 1
    """)
    suspend fun getPreviousChapter(bookId: String, currentNumber: Int): ChapterEntity?

    @Query("""
        SELECT * FROM chapters 
        WHERE bookId = :bookId 
        AND isRead = 0 
        ORDER BY number ASC 
        LIMIT 1
    """)
    suspend fun getFirstUnreadChapter(bookId: String): ChapterEntity?

    @Query("SELECT COUNT(*) FROM chapters WHERE bookId = :bookId AND isRead = 1")
    suspend fun getReadChapterCount(bookId: String): Int

    @Query("SELECT COUNT(*) FROM chapters WHERE bookId = :bookId")
    suspend fun getTotalChapterCount(bookId: String): Int

    @Query("""
        SELECT * FROM chapters 
        WHERE lastReadTimestamp > 0 
        ORDER BY lastReadTimestamp DESC 
        LIMIT :limit
    """)
    fun getRecentlyReadChapters(limit: Int = 10): Flow<List<ChapterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Update
    suspend fun updateChapter(chapter: ChapterEntity)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChaptersByBook(bookId: String)

    @Query("UPDATE chapters SET isRead = :isRead WHERE id = :chapterId")
    suspend fun updateChapterReadStatus(chapterId: String, isRead: Boolean)

    @Query("UPDATE chapters SET progress = :progress WHERE id = :chapterId")
    suspend fun updateChapterProgress(chapterId: String, progress: Float)

    @Query("UPDATE chapters SET lastReadTimestamp = :timestamp WHERE id = :chapterId")
    suspend fun updateLastReadTimestamp(chapterId: String, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE chapters 
        SET isDownloaded = :isDownloaded 
        WHERE id = :chapterId
    """)
    suspend fun updateDownloadStatus(chapterId: String, isDownloaded: Boolean)

    @Transaction
    @Query("""
        SELECT * FROM chapters 
        WHERE bookId = :bookId 
        AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
        ORDER BY number ASC
    """)
    suspend fun searchChapters(bookId: String, query: String): List<ChapterEntity>

    @Query("""
        SELECT * FROM chapters 
        WHERE bookId = :bookId 
        AND isDownloaded = 1 
        ORDER BY number ASC
    """)
    fun getDownloadedChapters(bookId: String): Flow<List<ChapterEntity>>

    @Query("""
        SELECT * FROM chapters 
        WHERE isDownloaded = 0 
        AND progress > 0 
        ORDER BY lastReadTimestamp DESC
    """)
    fun getUndownloadedReadChapters(): Flow<List<ChapterEntity>>

    @Query("UPDATE chapters SET content = :content WHERE id = :chapterId")
    suspend fun updateChapterContent(chapterId: String, content: String)

    @Query("""
        UPDATE chapters 
        SET progress = :progress, 
            isRead = CASE WHEN :progress >= 0.99 THEN 1 ELSE isRead END,
            lastReadTimestamp = :timestamp
        WHERE id = :chapterId
    """)
    suspend fun updateReadingProgress(
        chapterId: String,
        progress: Float,
        timestamp: Long = System.currentTimeMillis()
    )

    @Transaction
    suspend fun markPreviousChaptersAsRead(bookId: String, chapterNumber: Int) {
        val chapters = getChaptersByBookAndMaxNumber(bookId, chapterNumber)
        chapters.forEach { chapter ->
            updateChapterReadStatus(chapter.id, true)
        }
    }

    @Query("""
        SELECT * FROM chapters 
        WHERE bookId = :bookId 
        AND number <= :maxNumber
    """)
    suspend fun getChaptersByBookAndMaxNumber(
        bookId: String,
        maxNumber: Int
    ): List<ChapterEntity>
}
