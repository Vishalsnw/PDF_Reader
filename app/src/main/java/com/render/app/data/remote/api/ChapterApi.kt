/*
 * File: ChapterApi.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 06:36:53 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.data.remote.api

import com.yourapp.readers.data.remote.model.ChapterDto
import com.yourapp.readers.data.remote.model.ChapterContentDto
import com.yourapp.readers.data.remote.model.ChapterProgressDto
import com.yourapp.readers.data.remote.model.ApiResponse
import retrofit2.http.*

interface ChapterApi {
    @GET("books/{bookId}/chapters")
    suspend fun getChaptersByBook(
        @Path("bookId") bookId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sort") sort: String = "number"
    ): ApiResponse<List<ChapterDto>>

    @GET("chapters/{chapterId}")
    suspend fun getChapterById(
        @Path("chapterId") chapterId: String
    ): ApiResponse<ChapterDto>

    @GET("chapters/{chapterId}/content")
    suspend fun getChapterContent(
        @Path("chapterId") chapterId: String,
        @Query("format") format: String = "html"
    ): ApiResponse<ChapterContentDto>

    @POST("chapters/{chapterId}/progress")
    suspend fun updateChapterProgress(
        @Path("chapterId") chapterId: String,
        @Body progress: ChapterProgressDto
    ): ApiResponse<Unit>

    @POST("chapters/{chapterId}/read")
    suspend fun markChapterAsRead(
        @Path("chapterId") chapterId: String
    ): ApiResponse<Unit>

    @DELETE("chapters/{chapterId}/read")
    suspend fun markChapterAsUnread(
        @Path("chapterId") chapterId: String
    ): ApiResponse<Unit>

    @GET("books/{bookId}/chapters/sync")
    suspend fun syncChapters(
        @Path("bookId") bookId: String,
        @Query("lastSync") lastSync: Long
    ): ApiResponse<List<ChapterDto>>

    @GET("chapters/search")
    suspend fun searchChapters(
        @Query("bookId") bookId: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<ChapterDto>>

    @GET("chapters/recent")
    suspend fun getRecentChapters(
        @Query("limit") limit: Int = 10,
        @Query("userId") userId: String
    ): ApiResponse<List<ChapterDto>>

    @GET("chapters/{chapterId}/download")
    suspend fun getChapterDownloadUrl(
        @Path("chapterId") chapterId: String
    ): ApiResponse<String>

    @POST("chapters/batch-progress")
    suspend fun updateBatchProgress(
        @Body progressList: List<ChapterProgressDto>
    ): ApiResponse<Unit>

    @GET("books/{bookId}/chapters/stats")
    suspend fun getChapterStats(
        @Path("bookId") bookId: String
    ): ApiResponse<ChapterStatsDto>

    @GET("chapters/{chapterId}/related")
    suspend fun getRelatedChapters(
        @Path("chapterId") chapterId: String,
        @Query("limit") limit: Int = 5
    ): ApiResponse<List<ChapterDto>>

    @POST("chapters/{chapterId}/bookmark")
    suspend fun bookmarkChapter(
        @Path("chapterId") chapterId: String
    ): ApiResponse<Unit>

    @DELETE("chapters/{chapterId}/bookmark")
    suspend fun removeBookmark(
        @Path("chapterId") chapterId: String
    ): ApiResponse<Unit>

    @GET("chapters/bookmarks")
    suspend fun getBookmarkedChapters(
        @Query("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<ChapterDto>>

    companion object {
        const val BASE_URL = "https://api.yourapp.com/v1/"
        const val TIMEOUT_SECONDS = 30L
    }
}

data class ChapterStatsDto(
    val totalChapters: Int,
    val readChapters: Int,
    val downloadedChapters: Int,
    val bookmarkedChapters: Int,
    val averageReadingTime: Long,
    val lastReadTimestamp: Long
)

/**
 * Extension functions for convenience
 */
suspend fun ChapterApi.markChapterProgress(
    chapterId: String,
    progress: Float,
    timestamp: Long = System.currentTimeMillis()
) {
    updateChapterProgress(
        chapterId,
        ChapterProgressDto(
            chapterId = chapterId,
            progress = progress,
            timestamp = timestamp
        )
    )
}

suspend fun ChapterApi.syncChapterProgress(
    bookId: String,
    progressList: List<Pair<String, Float>>
) {
    val dtoList = progressList.map { (chapterId, progress) ->
        ChapterProgressDto(
            chapterId = chapterId,
            progress = progress,
            timestamp = System.currentTimeMillis()
        )
    }
    updateBatchProgress(dtoList)
}
