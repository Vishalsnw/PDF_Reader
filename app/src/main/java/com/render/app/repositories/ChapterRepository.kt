/*
 * File: ChapterRepository.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 04:44:43 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.data.repository

import com.yourapp.readers.data.local.ChapterDao
import com.yourapp.readers.data.local.entity.ChapterEntity
import com.yourapp.readers.data.remote.ChapterApi
import com.yourapp.readers.data.remote.model.ChapterDto
import com.yourapp.readers.domain.models.Chapter
import com.yourapp.readers.domain.models.ChapterContent
import com.yourapp.readers.domain.models.Result
import com.yourapp.readers.domain.repository.IChapterRepository
import com.yourapp.readers.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChapterRepository @Inject constructor(
    private val chapterApi: ChapterApi,
    private val chapterDao: ChapterDao,
    private val networkUtils: NetworkUtils
) : IChapterRepository {

    override suspend fun getChapterById(chapterId: String): Result<Chapter> = withContext(Dispatchers.IO) {
        try {
            val localChapter = chapterDao.getChapterById(chapterId)
            
            if (localChapter != null) {
                return@withContext Result.Success(localChapter.toDomain())
            }

            if (!networkUtils.isNetworkAvailable()) {
                return@withContext Result.Error(Exception("No internet connection"))
            }

            val remoteChapter = chapterApi.getChapter(chapterId)
            chapterDao.insertChapter(remoteChapter.toEntity())
            Result.Success(remoteChapter.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun getChapterContent(chapterId: String): Result<ChapterContent> = withContext(Dispatchers.IO) {
        try {
            val localContent = chapterDao.getChapterContent(chapterId)
            
            if (localContent != null) {
                return@withContext Result.Success(localContent.toDomain())
            }

            if (!networkUtils.isNetworkAvailable()) {
                return@withContext Result.Error(Exception("No internet connection"))
            }

            val remoteContent = chapterApi.getChapterContent(chapterId)
            chapterDao.insertChapterContent(remoteContent.toEntity())
            Result.Success(remoteContent.toDomain())
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun getChaptersByBookId(bookId: String): Flow<List<Chapter>> =
        chapterDao.getChaptersByBookId(bookId)
            .map { chapters -> chapters.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    override suspend fun updateChapterProgress(chapterId: String, progress: Float): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            chapterDao.updateProgress(chapterId, progress)
            
            if (networkUtils.isNetworkAvailable()) {
                chapterApi.updateProgress(chapterId, progress)
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun markChapterAsRead(chapterId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            chapterDao.markAsRead(chapterId)
            
            if (networkUtils.isNetworkAvailable()) {
                chapterApi.markAsRead(chapterId)
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun downloadChapter(chapterId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!networkUtils.isNetworkAvailable()) {
                return@withContext Result.Error(Exception("No internet connection"))
            }

            val remoteChapter = chapterApi.getChapter(chapterId)
            val remoteContent = chapterApi.getChapterContent(chapterId)
            
            chapterDao.insertChapter(remoteChapter.toEntity())
            chapterDao.insertChapterContent(remoteContent.toEntity())
            chapterDao.markAsDownloaded(chapterId)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun deleteChapter(chapterId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            chapterDao.deleteChapter(chapterId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun syncChapters(bookId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!networkUtils.isNetworkAvailable()) {
                return@withContext Result.Error(Exception("No internet connection"))
            }

            val remoteChapters = chapterApi.getChapters(bookId)
            chapterDao.insertChapters(remoteChapters.map { it.toEntity() })
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun ChapterEntity.toDomain(): Chapter = Chapter(
        id = id,
        bookId = bookId,
        title = title,
        number = number,
        progress = progress,
        isRead = isRead,
        isDownloaded = isDownloaded,
        lastReadTimestamp = lastReadTimestamp
    )

    private fun ChapterDto.toEntity(): ChapterEntity = ChapterEntity(
        id = id,
        bookId = bookId,
        title = title,
        number = number,
        progress = progress,
        isRead = isRead,
        isDownloaded = false,
        lastReadTimestamp = System.currentTimeMillis()
    )

    private fun ChapterDto.toDomain(): Chapter = Chapter(
        id = id,
        bookId = bookId,
        title = title,
        number = number,
        progress = progress,
        isRead = isRead,
        isDownloaded = false,
        lastReadTimestamp = System.currentTimeMillis()
    )
}
