/*
 * File: PdfReader.kt
 * Created: 2025-06-02
 * Author: Vishalsnw
 * Last Modified: 2025-06-02 05:28:43 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.reader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.reader.app.models.ReadingMode
import com.reader.app.utils.FileUtils
import com.reader.app.utils.PdfUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min

/**
 * PdfReader implements PDF document reading functionality.
 * Features:
 * - PDF rendering
 * - Text extraction
 * - Page navigation
 * - Zoom support
 */
class PdfReader : BaseReader() {
    private var pdfRenderer: PdfRenderer? = null
    private var currentPageNumber: Int = 0
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var renderScale: Float = 1.0f
    private var documentPassword: String? = null

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
            documentPassword = password

            closeDocument()

            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            fileDescriptor = fd
            pdfRenderer = PdfRenderer(fd)

            _readerState.value = ReaderState.Ready(pdfRenderer?.pageCount ?: 0)
            Result.success(Unit)
        } catch (e: Exception) {
            _readerState.value = ReaderState.Error(e)
            Result.failure(e)
        }
    }

    override suspend fun closeDocument() = withContext(Dispatchers.IO) {
        try {
            pdfRenderer?.close()
            fileDescriptor?.close()
            pdfRenderer = null
            fileDescriptor = null
            currentPageNumber = 0
            _readerState.value = ReaderState.Initial
        } catch (e: Exception) {
            _readerState.value = ReaderState.Error(e)
        }
    }

    override fun isDocumentLoaded(): Boolean {
        return pdfRenderer != null
    }

    override fun getPageCount(): Int {
        return pdfRenderer?.pageCount ?: 0
    }

    override fun getCurrentPage(): Int {
        return currentPageNumber
    }

    /**
     * Navigation Operations
     */
    override suspend fun goToPage(pageNumber: Int): Boolean = withContext(Dispatchers.IO) {
        if (!isDocumentLoaded()) return@withContext false

        val targetPage = pageNumber.coerceIn(0, getPageCount() - 1)
        if (targetPage != currentPageNumber) {
            currentPageNumber = targetPage
            true
        } else {
            false
        }
    }

    override suspend fun goToNextPage(): Boolean {
        return goToPage(currentPageNumber + 1)
    }

    override suspend fun goToPreviousPage(): Boolean {
        return goToPage(currentPageNumber - 1)
    }

    override suspend fun goToPosition(position: Float) {
        val targetPage = (position * (getPageCount() - 1)).toInt()
        goToPage(targetPage)
    }

    override fun canGoToNextPage(): Boolean {
        return currentPageNumber < (getPageCount() - 1)
    }

    override fun canGoToPreviousPage(): Boolean {
        return currentPageNumber > 0
    }

    /**
     * Rendering Operations
     */
    override suspend fun renderPage(pageNumber: Int): Result<ReaderPage> = withContext(Dispatchers.IO) {
        try {
            val renderer = pdfRenderer ?: throw IllegalStateException("PDF not loaded")
            val page = renderer.openPage(pageNumber)

            val width = (page.width * renderScale).toInt()
            val height = (page.height * renderScale).toInt()

            val bitmap = Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            )

            page.render(
                bitmap,
                null,
                null,
                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
            )
            page.close()

            Result.success(
                ReaderPage(
                    pageNumber = pageNumber,
                    width = width,
                    height = height,
                    renderResult = bitmap
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPageText(pageNumber: Int): String = withContext(Dispatchers.IO) {
        return@withContext PdfUtils.extractPageText(
            fileDescriptor?.fileDescriptor ?: return@withContext "",
            pageNumber,
            documentPassword
        )
    }

    override suspend fun searchText(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<SearchResult>()
        val pageCount = getPageCount()

        for (pageNumber in 0 until pageCount) {
            val pageText = getPageText(pageNumber)
            val matches = PdfUtils.findTextLocations(
                fileDescriptor?.fileDescriptor ?: return@withContext emptyList(),
                pageNumber,
                query,
                documentPassword
            )

            matches.forEach { match ->
                results.add(
                    SearchResult(
                        pageNumber = pageNumber,
                        text = pageText.substring(match.startIndex, match.endIndex),
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
            val renderer = pdfRenderer ?: throw IllegalStateException("PDF not loaded")
            val page = renderer.openPage(pageNumber)

            val scale = min(
                width.toFloat() / page.width,
                height.toFloat() / page.height
            )

            val targetWidth = (page.width * scale).toInt()
            val targetHeight = (page.height * scale).toInt()

            val bitmap = Bitmap.createBitmap(
                targetWidth,
                targetHeight,
                Bitmap.Config.ARGB_8888
            )

            page.render(
                bitmap,
                null,
                null,
                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
            )
            page.close()

            Result.success(
                ReaderThumbnail(
                    pageNumber = pageNumber,
                    width = targetWidth,
                    height = targetHeight,
                    thumbnail = bitmap
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Selection Operations
     */
    override suspend fun getTextInRect(rect: RectF): String = withContext(Dispatchers.IO) {
        return@withContext PdfUtils.getTextInRect(
            fileDescriptor?.fileDescriptor ?: return@withContext "",
            currentPageNumber,
            rect,
            documentPassword
        )
    }

    override suspend fun getWordAtPosition(x: Float, y: Float): TextSelection? = withContext(Dispatchers.IO) {
        val scaledX = x / renderScale
        val scaledY = y / renderScale

        return@withContext PdfUtils.getWordAtPosition(
            fileDescriptor?.fileDescriptor ?: return@withContext null,
            currentPageNumber,
            scaledX,
            scaledY,
            documentPassword
        )?.let { word ->
            TextSelection(
                text = word.text,
                rect = word.bounds,
                pageNumber = currentPageNumber
            )
        }
    }

    override suspend fun getLinkAtPosition(x: Float, y: Float): ReaderLink? = withContext(Dispatchers.IO) {
        val scaledX = x / renderScale
        val scaledY = y / renderScale

        return@withContext PdfUtils.getLinkAtPosition(
            fileDescriptor?.fileDescriptor ?: return@withContext null,
            currentPageNumber,
            scaledX,
            scaledY,
            documentPassword
        )?.let { link ->
            ReaderLink(
                rect = link.bounds,
                targetPage = link.targetPage,
                uri = link.uri
            )
        }
    }

    /**
     * Configuration Operations
     */
    override fun onConfigUpdated(config: ReaderConfig) {
        renderScale = max(
            MIN_RENDER_SCALE,
            min(config.fontSize, MAX_RENDER_SCALE)
        )
    }

    override fun onReadingModeChanged(mode: ReadingMode) {
        // Handle reading mode changes if needed
    }

    companion object {
        private const val MIN_RENDER_SCALE = 0.5f
        private const val MAX_RENDER_SCALE = 3.0f
        private const val DEFAULT_RENDER_SCALE = 1.0f
    }
}
