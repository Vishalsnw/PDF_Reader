/*
 * File: ReaderViewModel.kt
 * Created: 2025-06-02
 * Author: Vishalsnw
 * Last Modified: 2025-06-02 14:51:07 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.viewmodels

import androidx.lifecycle.*
import com.reader.app.data.models.*
import com.reader.app.data.repositories.BookRepository
import com.reader.app.data.repositories.ReadingProgressRepository
import com.reader.app.data.repositories.SettingsRepository
import com.reader.app.reader.BaseReader
import com.reader.app.reader.EpubReader
import com.reader.app.reader.PdfReader
import com.reader.app.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for managing the reader screen and reading operations.
 * Features:
 * - Document handling
 * - Reading progress
 * - View customization
 * - Reading statistics
 */
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val readingProgressRepository: ReadingProgressRepository,
    private val settingsRepository: SettingsRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var reader: BaseReader? = null
    private var currentBook: Book? = null
    private var readingSession: ReadingSession? = null

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Initial)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val _readerConfig = MutableStateFlow(ReaderConfig())
    val readerConfig: StateFlow<ReaderConfig> = _readerConfig.asStateFlow()

    private val _readingMode = MutableStateFlow<ReadingMode>(ReadingMode.Scroll)
    val readingMode: StateFlow<ReadingMode> = _readingMode.asStateFlow()

    init {
        loadReaderSettings()
    }

    /**
     * Document Operations
     */
    fun loadDocument(bookId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = ReaderUiState.Loading
                
                val book = bookRepository.getBook(bookId)
                currentBook = book
                
                val file = File(book.path)
                if (!file.exists()) {
                    throw Exception("File not found")
                }

                reader = createReader(file)
                reader?.loadDocument(file)?.onSuccess {
                    _totalPages.value = reader?.getPageCount() ?: 0
                    loadLastReadPosition(book)
                    startReadingSession()
                    _uiState.value = ReaderUiState.Success
                }?.onFailure { error ->
                    _uiState.value = ReaderUiState.Error(error.message ?: "Failed to load document")
                }
            } catch (e: Exception) {
                _uiState.value = ReaderUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun createReader(file: File): BaseReader {
        return when (FileUtils.getMimeType(file)) {
            FileUtils.MimeTypes.PDF -> PdfReader()
            FileUtils.MimeTypes.EPUB -> EpubReader()
            else -> throw UnsupportedOperationException("Unsupported file type")
        }
    }

    /**
     * Navigation
     */
    fun goToPage(pageNumber: Int) {
        viewModelScope.launch {
            reader?.goToPage(pageNumber)?.let { success ->
                if (success) {
                    _currentPage.value = pageNumber
                    saveReadingProgress()
                }
            }
        }
    }

    fun goToNextPage() {
        viewModelScope.launch {
            reader?.goToNextPage()?.let { success ->
                if (success) {
                    _currentPage.value = reader?.getCurrentPage() ?: 0
                    saveReadingProgress()
                }
            }
        }
    }

    fun goToPreviousPage() {
        viewModelScope.launch {
            reader?.goToPreviousPage()?.let { success ->
                if (success) {
                    _currentPage.value = reader?.getCurrentPage() ?: 0
                    saveReadingProgress()
                }
            }
        }
    }

    /**
     * Reading Progress
     */
    private fun loadLastReadPosition(book: Book) {
        viewModelScope.launch {
            val progress = readingProgressRepository.getReadingProgress(book.id)
            progress?.let {
                goToPage(it.lastPage)
            }
        }
    }

    private fun saveReadingProgress() {
        viewModelScope.launch {
            currentBook?.let { book ->
                val progress = ReadingProgress(
                    bookId = book.id,
                    lastPage = _currentPage.value,
                    totalPages = _totalPages.value,
                    timestamp = System.currentTimeMillis()
                )
                readingProgressRepository.saveReadingProgress(progress)
            }
        }
    }

    /**
     * Reading Session
     */
    private fun startReadingSession() {
        readingSession = ReadingSession(
            bookId = currentBook?.id ?: 0,
            startTime = System.currentTimeMillis(),
            initialPage = _currentPage.value
        )
    }

    private fun endReadingSession() {
        viewModelScope.launch {
            readingSession?.let { session ->
                val endTime = System.currentTimeMillis()
                val duration = endTime - session.startTime
                val pagesRead = _currentPage.value - session.initialPage

                val completedSession = session.copy(
                    endTime = endTime,
                    duration = duration,
                    pagesRead = pagesRead
                )
                readingProgressRepository.saveReadingSession(completedSession)
            }
            readingSession = null
        }
    }

    /**
     * Settings Management
     */
    private fun loadReaderSettings() {
        viewModelScope.launch {
            settingsRepository.getReaderConfig().collect { config ->
                _readerConfig.value = config
                reader?.onConfigUpdated(config)
            }
        }
    }

    fun updateReaderConfig(config: ReaderConfig) {
        viewModelScope.launch {
            settingsRepository.saveReaderConfig(config)
            reader?.onConfigUpdated(config)
        }
    }

    fun setReadingMode(mode: ReadingMode) {
        viewModelScope.launch {
            _readingMode.value = mode
            reader?.onReadingModeChanged(mode)
        }
    }

    /**
     * Cleanup
     */
    override fun onCleared() {
        super.onCleared()
        endReadingSession()
        reader?.closeDocument()
        reader = null
    }

    /**
     * UI State
     */
    sealed class ReaderUiState {
        object Initial : ReaderUiState()
        object Loading : ReaderUiState()
        object Success : ReaderUiState()
        data class Error(val message: String) : ReaderUiState()
    }

    /**
     * Data Classes
     */
    data class ReadingSession(
        val bookId: Long,
        val startTime: Long,
        val endTime: Long = 0,
        val duration: Long = 0,
        val initialPage: Int,
        val pagesRead: Int = 0
    )
}
