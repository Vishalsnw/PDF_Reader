/*
 * File: BookDao.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 19:54:47 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.database.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.reader.app.models.Book
import com.reader.app.models.BookWithChapters
import com.reader.app.models.BookWithMetadata
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * BookDao provides database operations for Book entities.
 * Features:
 * - CRUD operations
 * - Complex queries
 * - Relationship queries
 * - Flow support
 */
@Dao
interface BookDao {

    // Create Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: Book): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(books: List<Book>): List<Long>

    // Read Operations
    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookById(bookId: String): Flow<Book?>

    @Query("SELECT * FROM books ORDER BY lastReadTime DESC")
    fun getAllBooks(): Flow<List<Book>>

    @Query("""
        SELECT * FROM books 
        WHERE lastReadTime IS NOT NULL 
        ORDER BY lastReadTime DESC 
        LIMIT :limit
    """)
    fun getRecentBooks(limit: Int = 10): Flow<List<Book>>

    @Query("""
        SELECT * FROM books 
        WHERE isFinished = 0 
        AND currentPage > 0 
        ORDER BY lastReadTime DESC
    """)
    fun getCurrentlyReading(): Flow<List<Book>>

    @Query("""
        SELECT * FROM books 
        WHERE isFinished = 1 
        ORDER BY lastReadTime DESC
    """)
    fun getCompletedBooks(): Flow<List<Book>>

    @Query("""
        SELECT * FROM books 
        WHERE title LIKE '%' || :query || '%' 
        OR author LIKE '%' || :query || '%'
    """)
    fun searchBooks(query: String): Flow<List<Book>>

    // Update Operations
    @Update
    suspend fun update(book: Book)

    @Update
    suspend fun updateAll(books: List<Book>)

    @Query("""
        UPDATE books 
        SET currentPage = :page,
            readingProgress = :progress,
            lastReadTime = :timestamp,
            isFinished = CASE WHEN :progress >= 100 THEN 1 ELSE 0 END
        WHERE id = :bookId
    """)
    suspend fun updateProgress(
        bookId: String,
        page: Int,
        progress: Int,
        timestamp: Date = Date()
    )

    @Query("UPDATE books SET isBookmarked = :isBookmarked WHERE id = :bookId")
    suspend fun updateBookmark(bookId: String, isBookmarked: Boolean)

    @Query("UPDATE books SET isFavorite = :isFavorite WHERE id = :bookId")
    suspend fun updateFavorite(bookId: String, isFavorite: Boolean)

    // Delete Operations
    @Delete
    suspend fun delete(book: Book)

    @Delete
    suspend fun deleteAll(books: List<Book>)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteById(bookId: String)

    // Relationship Queries
    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookWithChapters(bookId: String): Flow<BookWithChapters?>

    @Transaction
    @Query("SELECT * FROM books")
    fun getBooksWithChapters(): Flow<List<BookWithChapters>>

    @Transaction
    @Query("SELECT * FROM books WHERE id = :bookId")
    fun getBookWithMetadata(bookId: String): Flow<BookWithMetadata?>

    // Statistics Queries
    @Query("SELECT COUNT(*) FROM books")
    fun getTotalBookCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM books WHERE isFinished = 1")
    fun getCompletedBookCount(): Flow<Int>

    @Query("""
        SELECT SUM(timeSpentReading) 
        FROM books 
        WHERE timeSpentReading > 0
    """)
    fun getTotalReadingTime(): Flow<Long>

    @Query("""
        SELECT AVG(readingProgress) 
        FROM books 
        WHERE currentPage > 0
    """)
    fun getAverageReadingProgress(): Flow<Float>

    // Category Operations
    @Query("""
        SELECT * FROM books 
        WHERE :categoryId IN (
            SELECT categoryId 
            FROM book_categories 
            WHERE bookId = books.id
        )
    """)
    fun getBooksByCategory(categoryId: String): Flow<List<Book>>

    @Query("""
        UPDATE book_categories 
        SET categoryId = :categoryId 
        WHERE bookId = :bookId
    """)
    suspend fun updateBookCategory(bookId: String, categoryId: String)

    // Custom Query Support
    @RawQuery(observedEntities = [Book::class])
    fun getBooksByCustomQuery(query: SupportSQLiteQuery): Flow<List<Book>>

    // Batch Operations
    @Transaction
    suspend fun updateBooksInBatch(books: List<Book>) {
        books.forEach { book ->
            update(book)
        }
    }

    // Cleanup Operations
    @Query("DELETE FROM books WHERE id IN (:bookIds)")
    suspend fun deleteBooksByIds(bookIds: List<String>)

    @Query("DELETE FROM books WHERE isDownloaded = 0")
    suspend fun deleteUndownloadedBooks()

    companion object {
        const val DEFAULT_RECENT_LIMIT = 10
        const val PROGRESS_COMPLETE = 100
    }
}
package com.render.app.database.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.render.app.models.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books")
    fun getAllBooks(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: String): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: Book)

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)

    @Query("DELETE FROM books WHERE id IN (:bookIds)")
    suspend fun deleteBooksByIds(bookIds: List<String>)

    @Query("DELETE FROM books WHERE isDownloaded = 0")
    suspend fun deleteUndownloadedBooks()

    @RawQuery(observedEntities = [Book::class])
    fun getBooksByCustomQuery(query: SupportSQLiteQuery): Flow<List<Book>>

    @Transaction
    suspend fun updateBooksInBatch(books: List<Book>) {
        books.forEach { book ->
            update(book)
        }
    }
}
