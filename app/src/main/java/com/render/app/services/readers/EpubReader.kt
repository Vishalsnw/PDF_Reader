/*
 * File: EpubReader.kt
 * Created: 2025-06-02
 * Author: Vishalsnw
 * Last Modified: 2025-06-02 05:30:48 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.reader

import android.content.Context
import android.graphics.RectF
import android.webkit.WebView
import android.webkit.WebViewClient
import com.reader.app.models.ReadingMode
import com.reader.app.utils.EpubUtils
import com.reader.app.utils.FileUtils
import com.reader.app.utils.HtmlUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.epub.EpubReader
import java.io.File
import java.io.FileInputStream
import kotlin.coroutines.resume

/**
 * EpubReader implements EPUB document reading functionality.
 * Features:
 * - EPUB parsing
 * - HTML rendering
 * - Chapter navigation
 * - Content extraction
 */
class EpubReader : BaseReader() {
    private var epubBook: Book? = null
    private var currentSpineIndex: Int = 0
    private var webView: WebView? = null
    private var currentChapterContent: String = ""
    private var chapterPositions: List<Float> = emptyList()
    private val cssInjector = MutableStateFlow<String>("")

    /**
     * Document Operations
     */
    override suspend fun loadDocument(
        context: Context,
        file: File,
        password: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _readerState.value = ReaderState.Loading

            FileInputStream(file).use { inputStream ->
                epubBook = EpubReader().readEpub(inputStream)
            }

            setupWebView(context)
            loadChapterPositions()

            _readerState.value = ReaderState.Ready(getPageCount())
            Result.success(Unit)
        } catch (e: Exception) {
            _readerState.value = ReaderState.Error(e)
            Result.failure(e)
        }
    }

    override suspend fun closeDocument() = withContext(Dispatchers.IO) {
        try {
            epubBook = null
            currentSpineIndex = 0
            currentChapterContent = ""
            chapterPositions = emptyList()
            webView = null
            _readerState.value = ReaderState.Initial
        } catch (e: Exception) {
            _readerState.value = ReaderState.Error(e)
        }
    }

    override fun isDocumentLoaded(): Boolean {
        return epubBook != null
    }

    override fun getPageCount(): Int {
        return epubBook?.spine?.spineReferences?.size ?: 0
    }

    override fun getCurrentPage(): Int {
        return currentSpineIndex
    }

    /**
     * Navigation Operations
     */
    override suspend fun goToPage(pageNumber: Int): Boolean = withContext(Dispatchers.Main) {
        if (!isDocumentLoaded() || pageNumber == currentSpineIndex) return@withContext false

        try {
            val targetIndex = pageNumber.coerceIn(0, getPageCount() - 1)
            loadChapter(targetIndex)
            currentSpineIndex = targetIndex
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun goToNextPage(): Boolean {
        return goToPage(currentSpineIndex + 1)
    }

    override suspend fun goToPreviousPage(): Boolean {
        return goToPage(currentSpineIndex - 1)
    }

    override suspend fun goToPosition(position: Float) {
        val targetIndex = (position * (getPageCount() - 1)).toInt()
        goToPage(targetIndex)
    }

    override fun canGoToNextPage(): Boolean {
        return currentSpineIndex < (getPageCount() - 1)
    }

    override fun canGoToPreviousPage(): Boolean {
        return currentSpineIndex > 0
    }

    /**
     * Content Operations
     */
    override suspend fun renderPage(pageNumber: Int): Result<ReaderPage> = withContext(Dispatchers.IO) {
        try {
            val content = loadChapterContent(pageNumber)
            val width = webView?.width ?: DEFAULT_WIDTH
            val height = webView?.height ?: DEFAULT_HEIGHT

            Result.success(
                ReaderPage(
                    pageNumber = pageNumber,
                    width = width,
                    height = height,
                    renderResult = content
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPageText(pageNumber: Int): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val content = loadChapterContent(pageNumber)
            HtmlUtils.extractText(content)
        } catch (e: Exception) {
            ""
        }
    }

    override suspend fun searchText(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<SearchResult>()
        val book = epubBook ?: return@withContext emptyList()

        book.spine.spineReferences.forEachIndexed { index, spineReference ->
            val content = loadChapterContent(index)
            val matches = HtmlUtils.findTextLocations(content, query)

            matches.forEach { match ->
                results.add(
                    SearchResult(
                        pageNumber = index,
                        text = match.text,
                        rect = match.bounds
                    )
                )
            }
        }

        results
    }

    override suspend fun extractPageThumbnail(
        pageNumber: Int,
        width: Int,
        height: Int
    ): Result<ReaderThumbnail> = withContext(Dispatchers.IO) {
        try {
            val content = loadChapterContent(pageNumber)
            val thumbnail = EpubUtils.generateThumbnail(content, width, height)

            Result.success(
                ReaderThumbnail(
                    pageNumber = pageNumber,
                    width = width,
                    height = height,
                    thumbnail = thumbnail
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Selection Operations
     */
    override suspend fun getTextInRect(rect: RectF): String = withContext(Dispatchers.Main) {
        return@withContext suspendCancellableCoroutine { continuation ->
            webView?.evaluateJavascript(
                "javascript:getTextInRect(${rect.left},${rect.top},${rect.right},${rect.bottom})"
            ) { result ->
                continuation.resume(result?.removeSurrounding("\"") ?: "")
            }
        }
    }

    override suspend fun getWordAtPosition(x: Float, y: Float): TextSelection? = withContext(Dispatchers.Main) {
        return@withContext suspendCancellableCoroutine { continuation ->
            webView?.evaluateJavascript(
                "javascript:getWordAtPosition($x,$y)"
            ) { result ->
                val selection = HtmlUtils.parseTextSelection(result, currentSpineIndex)
                continuation.resume(selection)
            }
        }
    }

    override suspend fun getLinkAtPosition(x: Float, y: Float): ReaderLink? = withContext(Dispatchers.Main) {
        return@withContext suspendCancellableCoroutine { continuation ->
            webView?.evaluateJavascript(
                "javascript:getLinkAtPosition($x,$y)"
            ) { result ->
                val link = HtmlUtils.parseLink(result)
                continuation.resume(link)
            }
        }
    }

    /**
     * Configuration Operations
     */
    override fun onConfigUpdated(config: ReaderConfig) {
        val css = buildCustomCss(config)
        cssInjector.value = css
        webView?.evaluateJavascript("javascript:updateStyles('$css')", null)
    }

    override fun onReadingModeChanged(mode: ReadingMode) {
        when (mode) {
            is ReadingMode.Scroll -> setupScrollMode()
            is ReadingMode.Paged -> setupPagedMode()
            else -> { /* Handle other modes */ }
        }
    }

    /**
     * Private Helper Functions
     */
    private suspend fun loadChapter(index: Int) {
        val content = loadChapterContent(index)
        currentChapterContent = content
        webView?.loadDataWithBaseURL(
            "file:///android_asset/",
            wrapContentWithHtml(content),
            "text/html",
            "UTF-8",
            null
        )
    }

    private suspend fun loadChapterContent(index: Int): String = withContext(Dispatchers.IO) {
        val book = epubBook ?: return@withContext ""
        val resource = book.spine.getResource(index)
        return@withContext String(resource.data)
    }

    private fun setupWebView(context: Context) {
        webView = WebView(context).apply {
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            addJavascriptInterface(WebViewJsInterface(), "Android")
        }
    }

    private fun loadChapterPositions() {
        chapterPositions = epubBook?.spine?.spineReferences?.mapIndexed { index, _ ->
            index.toFloat() / (getPageCount() - 1)
        } ?: emptyList()
    }

    private fun buildCustomCss(config: ReaderConfig): String {
        return """
            body {
                font-size: ${config.fontSize}em;
                line-height: ${config.lineSpacing};
                padding: ${config.margins.top}px ${config.margins.right}px 
                        ${config.margins.bottom}px ${config.margins.left}px;
                background-color: ${(config.theme as? Theme.Custom)?.config?.colors?.background 
                    ?: "#FFFFFF"};
                color: ${(config.theme as? Theme.Custom)?.config?.colors?.onBackground 
                    ?: "#000000"};
            }
        """.trimIndent()
    }

    private fun wrapContentWithHtml(content: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>${cssInjector.value}</style>
            </head>
            <body>
                $content
                <script src="reader.js"></script>
            </body>
            </html>
        """.trimIndent()
    }

    private fun setupScrollMode() {
        webView?.settings?.apply {
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
        }
    }

    private fun setupPagedMode() {
        webView?.settings?.apply {
            setSupportZoom(false)
            builtInZoomControls = false
        }
    }

    companion object {
        private const val DEFAULT_WIDTH = 800
        private const val DEFAULT_HEIGHT = 1200
    }

    private inner class WebViewJsInterface {
        // JavaScript interface methods
    }
}
