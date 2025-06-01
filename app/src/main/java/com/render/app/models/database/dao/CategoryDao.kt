/*
 * File: CategoryDao.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 19:58:43 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.database.dao

import androidx.room.*
import com.reader.app.models.Category
import com.reader.app.models.CategoryWithBooks
import com.reader.app.models.CategoryStats
import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * CategoryDao provides database operations for Category entities.
 * Features:
 * - CRUD operations
 * - Book associations
 * - Statistics tracking
 * - Custom sorting
 */
@Dao
interface CategoryDao {

    // Create Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>): List<Long>

    // Read Operations
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoryById(categoryId: String): Flow<Category?>

    @Query("""
        SELECT * FROM categories 
        ORDER BY 
            CASE 
                WHEN name = 'Favorites' THEN 0 
                WHEN name = 'Reading Now' THEN 1 
                ELSE 2 
            END, 
            name ASC
    """)
    fun getAllCategories(): Flow<List<Category>>

    @Query("""
        SELECT * FROM categories 
        WHERE bookCount > 0 
        ORDER BY lastUsedTime DESC
    """)
    fun getActiveCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE isDefault = 1")
    fun getDefaultCategories(): Flow<List<Category>>

    // Update Operations
    @Update
    suspend fun update(category: Category)

    @Update
    suspend fun updateAll(categories: List<Category>)

    @Query("""
        UPDATE categories 
        SET name = :newName,
            color = :newColor,
            lastModifiedTime = :timestamp 
        WHERE id = :categoryId
    """)
    suspend fun updateCategory(
        categoryId: String,
        newName: String,
        newColor: Int?,
        timestamp: Date = Date()
    )

    @Query("""
        UPDATE categories 
        SET lastUsedTime = :timestamp 
        WHERE id = :categoryId
    """)
    suspend fun updateLastUsed(
        categoryId: String,
        timestamp: Date = Date()
    )

    // Delete Operations
    @Delete
    suspend fun delete(category: Category)

    @Delete
    suspend fun deleteAll(categories: List<Category>)

    @Query("DELETE FROM categories WHERE id = :categoryId AND isDefault = 0")
    suspend fun deleteCategoryById(categoryId: String)

    // Book Association Operations
    @Transaction
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoryWithBooks(categoryId: String): Flow<CategoryWithBooks?>

    @Transaction
    @Query("SELECT * FROM categories")
    fun getCategoriesWithBooks(): Flow<List<CategoryWithBooks>>

    @Query("""
        SELECT c.* FROM categories c
        INNER JOIN book_categories bc ON c.id = bc.categoryId
        WHERE bc.bookId = :bookId
    """)
    fun getCategoriesForBook(bookId: String): Flow<List<Category>>

    @Transaction
    suspend fun addBookToCategory(bookId: String, categoryId: String) {
        insertBookCategory(bookId, categoryId)
        updateCategoryStats(categoryId)
    }

    @Transaction
    suspend fun removeBookFromCategory(bookId: String, categoryId: String) {
        deleteBookCategory(bookId, categoryId)
        updateCategoryStats(categoryId)
    }

    // Statistics Operations
    @Query("""
        SELECT 
            c.id as categoryId,
            COUNT(b.id) as totalBooks,
            SUM(CASE WHEN b.isFinished = 1 THEN 1 ELSE 0 END) as completedBooks,
            SUM(CASE WHEN b.currentPage > 0 AND b.isFinished = 0 THEN 1 ELSE 0 END) as inProgressBooks,
            AVG(b.readingProgress) as averageProgress,
            SUM(b.timeSpentReading) as totalReadingTime
        FROM categories c
        LEFT JOIN book_categories bc ON c.id = bc.categoryId
        LEFT JOIN books b ON bc.bookId = b.id
        WHERE c.id = :categoryId
        GROUP BY c.id
    """)
    fun getCategoryStats(categoryId: String): Flow<CategoryStats>

    @Query("""
        UPDATE categories
        SET bookCount = (
            SELECT COUNT(*)
            FROM book_categories
            WHERE categoryId = :categoryId
        ),
        unreadCount = (
            SELECT COUNT(*)
            FROM book_categories bc
            INNER JOIN books b ON bc.bookId = b.id
            WHERE bc.categoryId = :categoryId
            AND b.currentPage = 0
        ),
        readingProgress = (
            SELECT ROUND(AVG(b.readingProgress))
            FROM book_categories bc
            INNER JOIN books b ON bc.bookId = b.id
            WHERE bc.categoryId = :categoryId
            AND b.currentPage > 0
        ),
        hasRecentBooks = (
            SELECT EXISTS(
                SELECT 1
                FROM book_categories bc
                INNER JOIN books b ON bc.bookId = b.id
                WHERE bc.categoryId = :categoryId
                AND b.lastReadTime >= datetime('now', '-7 days')
            )
        )
        WHERE id = :categoryId
    """)
    suspend fun updateCategoryStats(categoryId: String)

    // Search Operations
    @Query("""
        SELECT * FROM categories 
        WHERE name LIKE '%' || :query || '%' 
        ORDER BY 
            CASE 
                WHEN name LIKE :query || '%' THEN 0 
                ELSE 1 
            END,
            name ASC
    """)
    fun searchCategories(query: String): Flow<List<Category>>

    // Internal Operations
    @Query("""
        INSERT INTO book_categories (bookId, categoryId) 
        VALUES (:bookId, :categoryId)
    """)
    suspend fun insertBookCategory(bookId: String, categoryId: String)

    @Query("""
        DELETE FROM book_categories 
        WHERE bookId = :bookId 
        AND categoryId = :categoryId
    """)
    suspend fun deleteBookCategory(bookId: String, categoryId: String)

    companion object {
        const val CATEGORY_FAVORITES = "Favorites"
        const val CATEGORY_READING_NOW = "Reading Now"
        const val DEFAULT_COLOR = 0xFF2196F3.toInt() // Material Blue
    }
}
