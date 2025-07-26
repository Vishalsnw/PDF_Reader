
package com.render.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: String? = null,
    val bookCount: Int = 0,
    val unreadCount: Int = 0,
    val readingProgress: Int = 0,
    val hasRecentBooks: Boolean = false
)
