/*
 * File: LibraryViewModel.kt
 * Created: 2025-06-02
 * Author: Vishalsnw
 * Last Modified: 2025-06-02 14:49:01 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.viewmodels

import androidx.lifecycle.*
import com.reader.app.data.models.*
import com.reader.app.data.repositories.LibraryRepository
import com.reader.app.utils.FileUtils
import com.reader.app.utils.StorageUtils
import com.reader.app.utils.TimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for managing the library screen and book operations.
 * Features:
 * - Book management
 * - Library organization
 * - Reading progress
 * - Book metadata
 */
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _collections = MutableStateFlow<List<Collection>>(emptyList())
    val collections: StateFlow<List<Collection>> = _collections.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.TITLE)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _filterOptions = MutableStateFlow(FilterOptions())
    val filterOptions: StateFlow<FilterOptions> = _filterOptions.asStateFlow()

    init {
        loadLibrary()
        observeCollections()
    }

    /**
     * Library Management
     */
    private fun loadLibrary() {
        viewModelScope.launch {
            _uiState.value = LibraryUiState.Loading
            try {
                libraryRepository.getBooks()
                    .combine(sortOrder) { books, order ->
                        sortBooks(books, order)
                    }
                    .combine(filterOptions) { books, filters ->
                        filterBooks(books, filters)
                    }
                    .collect { books ->
                        _books.value = books
                        _uiState.value = LibraryUiState.Success
                    }
            } catch (e: Exception) {
                _uiState.value = LibraryUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun observeCollections() {
        viewModelScope.launch {
            libraryRepository.getCollections().collect { collections ->
                _collections.value = collections
            }
        }
    }

    /**
     * Book Operations
     */
    fun addBook(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = LibraryUiState.Loading
                val metadata = FileUtils.getFileMetadata(file)
                val book = Book(
                    id = 0,
                    title = metadata.name,
                    path = file.absolutePath,
                    size = metadata.size,
                    lastModified = metadata.lastModified,
                    mimeType = metadata.mimeType,
                    addedDate = System.currentTimeMillis()
                )
                libraryRepository.addBook(book)
                _uiState.value = LibraryUiState.Success
            } catch (e: Exception) {
                _uiState.value = LibraryUiState.Error(e.message ?: "Failed to add book")
            }
        }
    }

    fun removeBook(book: Book) {
        viewModelScope.launch {
            try {
                libraryRepository.removeBook(book)
                // Optionally delete the file
                File(book.path).delete()
            } catch (e: Exception) {
                _uiState.value = LibraryUiState.Error("Failed to remove book")
            }
        }
    }

    fun updateBook(book: Book) {
        viewModelScope.launch {
            try {
                libraryRepository.updateBook(book)
            } catch (e: Exception) {
                _uiState.value = LibraryUiState.Error("Failed to update book")
            }
        }
    }

    /**
     * Collection Operations
     */
    fun createCollection(name: String) {
        viewModelScope.launch {
            try {
                val collection = Collection(
                    id = 0,
                    name = name,
                    createdDate = System.currentTimeMillis()
                )
                libraryRepository.addCollection(collection)
            } catch (e: Exception) {
                _uiState.value = LibraryUiState.Error("Failed to create collection")
            }
        }
    }

    fun addToCollection(book: Book, collectionId: Long) {
        viewModelScope.launch {
            try {
                libraryRepository.addBookToCollection(book.id, collectionId)
            } catch (e: Exception) {
                _uiState.value = LibraryUiState.Error("Failed to add to collection")
            }
        }
    }

    fun removeFromCollection(book: Book, collectionId: Long) {
        viewModelScope.launch {
            try {
                libraryRepository.removeBookFromCollection(book.id, collectionId)
            } catch (e: Exception) {
                _uiState.value = LibraryUiState.Error("Failed to remove from collection")
            }
        }
    }

    /**
     * Sorting and Filtering
     */
    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            _sortOrder.value = order
        }
    }

    fun updateFilterOptions(options: FilterOptions) {
        viewModelScope.launch {
            _filterOptions.value = options
        }
    }

    private fun sortBooks(books: List<Book>, order: SortOrder): List<Book> {
        return when (order) {
            SortOrder.TITLE -> books.sortedBy { it.title }
            SortOrder.AUTHOR -> books.sortedBy { it.author }
            SortOrder.ADDED_DATE -> books.sortedByDescending { it.addedDate }
            SortOrder.LAST_READ -> books.sortedByDescending { it.lastReadDate }
            SortOrder.PROGRESS -> books.sortedByDescending { it.readProgress }
        }
    }

    private fun filterBooks(books: List<Book>, filters: FilterOptions): List<Book> {
        return books.filter { book ->
            (filters.format == null || book.mimeType == filters.format) &&
            (filters.author == null || book.author == filters.author) &&
            (filters.collection == null || book.collections.contains(filters.collection))
        }
    }

    /**
     * UI State
     */
    sealed class LibraryUiState {
        object Loading : LibraryUiState()
        object Success : LibraryUiState()
        data class Error(val message: String) : LibraryUiState()
    }

    /**
     * Data Classes
     */
    data class FilterOptions(
        val format: String? = null,
        val author: String? = null,
        val collection: Long? = null
    )

    enum class SortOrder {
        TITLE,
        AUTHOR,
        ADDED_DATE,
        LAST_READ,
        PROGRESS
    }
}
