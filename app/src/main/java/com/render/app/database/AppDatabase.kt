/*
 * File: AppDatabase.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 19:52:51 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.render.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.render.app.database.converters.DateConverter
import com.render.app.database.converters.ListConverter
import com.render.app.database.dao.BookDao
import com.render.app.database.dao.CategoryDao
import com.render.app.database.dao.ChapterDao
import com.render.app.models.Book
import com.render.app.models.Category
import com.render.app.models.Chapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AppDatabase manages the local SQLite database using Room.
 * Features:
 * - Database migrations
 * - Type converters
 * - Data access objects
 * - Singleton instance
 */
@Database(
    entities = [
        Book::class,
        Chapter::class,
        Category::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    DateConverter::class,
    ListConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        private const val DATABASE_NAME = "reader_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(*MIGRATIONS)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate database on creation
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                prepopulateDatabase(database)
                            }
                        }
                    }

                    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                        super.onDestructiveMigration(db)
                        // Handle data recovery after destructive migration
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                handleDestructiveMigration(database)
                            }
                        }
                    }
                })
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .enableMultiInstanceInvalidation()
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
        }

        private suspend fun prepopulateDatabase(database: AppDatabase) {
            // Add default categories
            database.categoryDao().insertAll(
                listOf(
                    Category(
                        name = "Favorites",
                        color = null,
                        bookCount = 0,
                        unreadCount = 0,
                        readingProgress = 0,
                        hasRecentBooks = false
                    ),
                    Category(
                        name = "Reading Now",
                        color = null,
                        bookCount = 0,
                        unreadCount = 0,
                        readingProgress = 0,
                        hasRecentBooks = false
                    )
                )
            )
        }

        private suspend fun handleDestructiveMigration(database: AppDatabase) {
            // Implement data recovery logic here
        }

        // Database Migrations
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example migration:
                // database.execSQL("""
                //     ALTER TABLE Book 
                //     ADD COLUMN customFont TEXT DEFAULT NULL
                // """)
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Future migration placeholder
            }
        }

        private val MIGRATIONS = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3
        )
    }

    enum class JournalMode {
        // SQLite journal modes
        WRITE_AHEAD_LOGGING,
        TRUNCATE,
        PERSIST,
        MEMORY,
        DELETE
    }

    /**
     * Database operations that need to be atomic
     */
    suspend fun runInTransaction(block: suspend () -> Unit) {
        withTransaction {
            block()
        }
    }

    /**
     * Clear all tables in the database
     */
    suspend fun clearAllTables() {
        if (isOpen) {
            clearAllTables()
            // Recreate default data
            prepopulateDatabase(this)
        }
    }

    /**
     * Check if the database is created and valid
     */
    fun isDatabaseCreated(): Boolean {
        val dbFile = context?.getDatabasePath(DATABASE_NAME)
        return dbFile?.exists() == true && isOpen
    }

    /**
     * Get the current database version
     */
    fun getCurrentVersion(): Int {
        var version = 0
        query("PRAGMA user_version", null).use { cursor ->
            if (cursor.moveToFirst()) {
                version = cursor.getInt(0)
            }
        }
        return version
    }
}
