/*
 * File: BaseReader.kt
 * Created: 2025-06-02
 * Author: Vishalsnw
 * Last Modified: 2025-06-02 05:26:22 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.reader

import android.content.Context
import android.graphics.RectF
import com.reader.app.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

/**
 * BaseReader defines the base interface for different reader implementations.
 * Features:
 * - Document handling
 * - Page navigation
 * - Content rendering
 * - Reading state management
 */
abstract class BaseReader {

    protected val _readerState = MutableStateFlow<ReaderState>(ReaderState.Initial)
    val readerState: Flow<ReaderState> = _readerState

    /**
     * Reader configuration
     */
    protected var config: ReaderConfig = ReaderConfig()
        private set

    /**
     * Current reading mode
     */
    protected var readingMode: ReadingMode = ReadingMode.Scroll
        private set

    /**
     * Document Operations
     */
    abstract suspend fun loadDocument(
        context: Context,
        file: File,
        password: String? = null
    ): Result<Unit>

    abstract suspend fun closeDocument()

    abstract fun isDocumentLoaded(): Boolean

    abstract fun getPageCount(): Int

    abstract fun getCurrentPage(): Int

    /**
     * Navigation Operations
     */
    abstract suspend fun goToPage(pageNumber: Int): Boolean

    abstract suspend fun goToNextPage(): Boolean

    abstract suspend fun goToPreviousPage(): Boolean

    abstract suspend fun goToPosition(position: Float)

    abstract fun canGoToNextPage(): Boolean

    abstract fun canGoToPreviousPage(): Boolean

    /**
     * Content Operations
     */
    abstract suspend fun renderPage(pageNumber: Int): Result<ReaderPage>

    abstract suspend fun getPageText(pageNumber: Int): String

    abstract suspend fun searchText(query: String): List<SearchResult>

    abstract suspend fun extractPageThumbnail(
        pageNumber: Int,
        width: Int,
        height: Int
    ): Result<ReaderThumbnail>

    /**
     * Selection Operations
     */
    abstract suspend fun getTextInRect(rect: RectF): String

    abstract suspend fun getWordAtPosition(x: Float, y: Float): TextSelection?

    abstract suspend fun getLinkAtPosition(x: Float, y: Float): ReaderLink?

    /**
     * Configuration Operations
     */
    fun updateConfig(newConfig: ReaderConfig) {
        config = newConfig
        onConfigUpdated(newConfig)
    }

    fun updateReadingMode(mode: ReadingMode) {
        readingMode = mode
        onReadingModeChanged(mode)
    }

    protected abstract fun onConfigUpdated(config: ReaderConfig)

    protected abstract fun onReadingModeChanged(mode: ReadingMode)

    /**
     * Reader configuration
     */
    data class ReaderConfig(
        val theme: Theme = Theme.Light,
        val fontSize: Float = 1.0f,
        val lineSpacing: Float = 1.5f,
        val margins: Margins = Margins(),
        val pageTransition: PageTransition = PageTransition.SLIDE,
        val scrollSensitivity: Float = 1.0f,
        val allowScreenshots: Boolean = false,
        val keepScreenOn: Boolean = true
    ) {
        data class Margins(
            val left: Int = 16,
            val top: Int = 16,
            val right: Int = 16,
            val bottom: Int = 16
        )

        enum class PageTransition {
            NONE,
            SLIDE,
            CURL,
            FADE
        }
    }

    /**
     * Reader state
     */
    sealed class ReaderState {
        object Initial : ReaderState()
        object Loading : ReaderState()
        data class Ready(val pageCount: Int) : ReaderState()
        data class Error(val exception: Exception) : ReaderState()
    }

    /**
     * Page representation
     */
    data class ReaderPage(
        val pageNumber: Int,
        val width: Int,
        val height: Int,
        val renderResult: Any
    )

    /**
     * Thumbnail representation
     */
    data class ReaderThumbnail(
        val pageNumber: Int,
        val width: Int,
        val height: Int,
        val thumbnail: Any
    )

    /**
     * Link representation
     */
    data class ReaderLink(
        val rect: RectF,
        val targetPage: Int?,
        val uri: String?
    )

    /**
     * Search result
     */
    data class SearchResult(
        val pageNumber: Int,
        val text: String,
        val rect: RectF
    )

    /**
     * Text selection
     */
    data class TextSelection(
        val text: String,
        val rect: RectF,
        val pageNumber: Int
    )

    companion object {
        const val DEFAULT_THUMBNAIL_SIZE = 128
        const val DEFAULT_RENDER_DPI = 72
        const val MAX_ZOOM_SCALE = 3.0f
        const val MIN_ZOOM_SCALE = 0.5f
    }
}
