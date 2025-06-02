/*
 * File: BookRepository.kt
 * Created: 2025-06-02
 * Author: Vishalsnw
 * Last Modified: 2025-06-02 05:23:53 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.repositories

import android.content.Context
import com.reader.app.database.AppDatabase
import com.reader.app.database.dao.BookDao
import com.reader.app.models.*
import com.reader.app.utils.FileUtils
import com.reader.app.utils.CoroutineDispatcherProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing book-related operations.
 * Features:
 * - CRUD operations
 * - Book importing
 * - Library management
 * - Reading progress tracking
 */
@Singleton
class BookRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val bookDao: BookDao,
    private val dispatchers: CoroutineDispatcherProvider
) {
    /**
     * Book Operations
     */
    suspend fun addBook(book: Book): Long = withContext(dispatchers.io) {
        bookDao.insert(book)
    }

    suspend fun addBooks(books: List<Book>) = withContext(dispatchers.io) {
        bookDao.insertAll(books)
    }

    suspend fun updateBook(book: Book) = withContext(dispatchers.io) {
        bookDao.update(book)
    }

    suspend fun deleteBook(book: Book) = withContext(dispatchers.io) {
        // Delete associated file
        book.filePath?.let { path ->
            FileUtils.deleteFile(context, path)
        }
        bookDao.delete(book)
    }

    fun getBook(id: String): Flow<Book?> = bookDao.getBookById(id)

    fun getAllBooks(): Flow<List<Book>> = bookDao.getAllBooks()

    /**
     * Library Operations
     */
    fun getBooksBySection(section: LibrarySection): Flow<List<Book>> {
        return when (section) {
            is LibrarySection.AllBooks -> bookDao.getAllBooks()
            is LibrarySection.Reading -> bookDao.getInProgressBooks()
            is LibrarySection.Completed -> bookDao.getCompletedBooks()
            is LibrarySection.Recent -> bookDao.getRecentBooks()
            is LibrarySection.Favorites -> bookDao.getFavoriteBooks()
            is LibrarySection.Category -> bookDao.getBooksByCategory(section.categoryId)
            is LibrarySection.Custom -> getCustomSectionBooks(section)
        }
    }

    fun getBooksBySort(sortOption: SortOption): Flow<List<Book>> {
        return bookDao.getAllBooks().map { books ->
            books.sortedWith(sortOption.getComparator())
        }
    }

    private fun getCustomSectionBooks(section: LibrarySection.Custom): Flow<List<Book>> {
        return when (section.customFilter) {
            LibrarySection.FilterType.IN_PROGRESS -> bookDao.getInProgressBooks()
            LibrarySection.FilterType.COMPLETED -> bookDao.getCompletedBooks()
            LibrarySection.FilterType.FAVORITES -> bookDao.getFavoriteBooks()
            LibrarySection.FilterType.DOWNLOADED -> bookDao.getDownloadedBooks()
            LibrarySection.FilterType.UNREAD -> bookDao.getUnreadBooks()
            else -> bookDao.getAllBooks()
        }
    }

    /**
     * Reading Progress Operations
     */
    suspend fun updateReadingProgress(
        bookId: String,
        progress: ReadingProgress
    ) = withContext(dispatchers.io) {
        bookDao.updateReadingProgress(
            bookId = bookId,
            currentPage = progress.currentPage,
            totalPages = progress.totalPages,
            readingTime = progress.readingTime,
            lastReadTime = Date()
        )
    }

    suspend fun markBookAsRead(
        bookId: String,
        completed: Boolean = true
    ) = withContext(dispatchers.io) {
        bookDao.markBookAsRead(bookId, completed)
    }

    /**
     * Book Import Operations
     */
    suspend fun importBook(file: File): Result<Book> = withContext(dispatchers.io) {
        try {
            val importedFile = FileUtils.importFile(context, file)
            val book = Book(
                id = UUID.randomUUID().toString(),
                title = FileUtils.getFileName(file),
                filePath = importedFile.absolutePath,
                fileSize = file.length(),
                mimeType = FileUtils.getMimeType(file),
                createdAt = Date()
            )
            addBook(book)
            Result.success(book)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBooks(files: List<File>): Result<List<Book>> = withContext(dispatchers.io) {
        try {
            val books = files.map { file ->
                val importedFile = FileUtils.importFile(context, file)
                Book(
                    id = UUID.randomUUID().toString(),
                    title = FileUtils.getFileName(file),
                    filePath = importedFile.absolutePath,
                    fileSize = file.length(),
                    mimeType = FileUtils.getMimeType(file),
                    createdAt = Date()
                )
            }
            addBooks(books)
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Book Management Operations
     */
    suspend fun toggleFavorite(bookId: String) = withContext(dispatchers.io) {
        bookDao.toggleFavorite(bookId)
    }

    suspend fun updateBookmark(
        bookId: String,
        bookmark: Bookmark
    ) = withContext(dispatchers.io) {
        bookDao.updateBookmark(bookId, bookmark)
    }

    suspend fun addToCategory(
        bookId: String,
        categoryId: String
    ) = withContext(dispatchers.io) {
        bookDao.addToCategory(bookId, categoryId)
    }

    /**
     * Statistics Operations
     */
    fun getReadingStats(): Flow<ReadingStats> = bookDao.getReadingStats()

    fun getBookProgress(bookId: String): Flow<Int> = bookDao.getBookProgress(bookId)

    fun getTotalReadingTime(): Flow<Long> = bookDao.getTotalReadingTime()

    data class ReadingProgress(
        val currentPage: Int,
        val totalPages: Int,
        val readingTime: Long
    )

    companion object {
        const val MIN_SEARCH_LENGTH = 3
        const val MAX_RECENT_BOOKS = 10
    }
}
