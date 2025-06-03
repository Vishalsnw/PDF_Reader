/*
 * File: ReaderTests.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 05:24:09 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.test

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yourapp.readers.domain.models.*
import com.yourapp.readers.domain.repository.IChapterRepository
import com.yourapp.readers.domain.repository.ISettingsRepository
import com.yourapp.readers.ui.reader.ReaderViewModel
import com.yourapp.readers.utils.DispatcherProvider
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ReaderTests {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)

    private lateinit var viewModel: ReaderViewModel
    private lateinit var chapterRepository: IChapterRepository
    private lateinit var settingsRepository: ISettingsRepository
    private lateinit var dispatcherProvider: DispatcherProvider

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        chapterRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        dispatcherProvider = mockk {
            every { main } returns testDispatcher
            every { io } returns testDispatcher
            every { default } returns testDispatcher
        }

        // Mock default settings
        coEvery { settingsRepository.readerSettings } returns flowOf(ReaderSettings())
        coEvery { settingsRepository.fontSettings } returns flowOf(FontSettings())
        coEvery { settingsRepository.colorScheme } returns flowOf(ColorScheme())

        viewModel = ReaderViewModel(
            chapterRepository = chapterRepository,
            settingsRepository = settingsRepository,
            dispatcherProvider = dispatcherProvider
        )
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun `test initial state`() = testScope.runBlockingTest {
        assertNotNull(viewModel.uiState.value)
        assertTrue(viewModel.uiState.value is ReaderUiState.Loading)
    }

    @Test
    fun `test load chapter success`() = testScope.runBlockingTest {
        // Given
        val chapterId = "test_chapter"
        val chapter = Chapter(
            id = chapterId,
            bookId = "test_book",
            title = "Test Chapter",
            number = 1,
            progress = 0f,
            isRead = false,
            isDownloaded = true,
            lastReadTimestamp = System.currentTimeMillis()
        )
        val content = ChapterContent(
            chapterId = chapterId,
            content = "Test content",
            images = emptyList()
        )

        coEvery { chapterRepository.getChapterById(chapterId) } returns Result.Success(chapter)
        coEvery { chapterRepository.getChapterContent(chapterId) } returns Result.Success(content)

        // When
        viewModel.loadChapter(chapterId)
        advanceTimeBy(1000)

        // Then
        assertTrue(viewModel.uiState.value is ReaderUiState.Success)
        val state = viewModel.uiState.value as ReaderUiState.Success
        assertEquals(chapter, state.chapter)
        assertEquals(content, state.content)
    }

    @Test
    fun `test load chapter failure`() = testScope.runBlockingTest {
        // Given
        val chapterId = "test_chapter"
        val error = Exception("Network error")
        coEvery { chapterRepository.getChapterById(chapterId) } returns Result.Error(error)

        // When
        viewModel.loadChapter(chapterId)
        advanceTimeBy(1000)

        // Then
        assertTrue(viewModel.uiState.value is ReaderUiState.Error)
        val state = viewModel.uiState.value as ReaderUiState.Error
        assertEquals(error.message, state.message)
    }

    @Test
    fun `test update progress`() = testScope.runBlockingTest {
        // Given
        val chapterId = "test_chapter"
        val progress = 0.5f
        coEvery { chapterRepository.updateChapterProgress(chapterId, progress) } returns Result.Success(Unit)

        // When
        viewModel.updateProgress(chapterId, progress)
        advanceTimeBy(1000)

        // Then
        coVerify { chapterRepository.updateChapterProgress(chapterId, progress) }
    }

    @Test
    fun `test mark chapter as read`() = testScope.runBlockingTest {
        // Given
        val chapterId = "test_chapter"
        coEvery { chapterRepository.markChapterAsRead(chapterId) } returns Result.Success(Unit)

        // When
        viewModel.markAsRead(chapterId)
        advanceTimeBy(1000)

        // Then
        coVerify { chapterRepository.markChapterAsRead(chapterId) }
    }

    @Test
    fun `test settings update`() = testScope.runBlockingTest {
        // Given
        val newSettings = ReaderSettings(
            pageTransition = PageTransition.CURL,
            readingMode = ReadingMode.PAGED
        )

        // When
        viewModel.updateSettings(newSettings)
        advanceTimeBy(1000)

        // Then
        coVerify { settingsRepository.updateReaderSettings(newSettings) }
    }

    @Test
    fun `test font settings update`() = testScope.runBlockingTest {
        // Given
        val newFontSettings = FontSettings(
            fontSize = 18f,
            fontFamily = "Roboto"
        )

        // When
        viewModel.updateFontSettings(newFontSettings)
        advanceTimeBy(1000)

        // Then
        coVerify { settingsRepository.updateFontSettings(newFontSettings) }
    }
}
