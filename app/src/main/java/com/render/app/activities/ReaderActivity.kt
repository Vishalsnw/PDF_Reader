/*
 * File: ReaderActivity.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.render.app.activities

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.render.app.R
import com.render.app.databinding.ActivityReaderBinding
import com.render.app.models.BookProgress
import com.render.app.models.ReadingMode
import com.render.app.models.ReaderSettings
import com.render.app.services.readers.BaseReader
import com.render.app.services.readers.EpubReader
import com.render.app.services.readers.PdfReader
import com.render.app.utils.AnimationUtils
import com.render.app.utils.SystemUiUtil
import com.render.app.viewmodels.ReaderViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/**
 * ReaderActivity provides a polished reading experience similar to Kindle.
 * Supports multiple formats (PDF, EPUB) with features like:
 * - Page animations
 * - Night mode
 * - Text customization
 * - Reading progress tracking
 * - Gesture navigation
 */
@AndroidEntryPoint
class ReaderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReaderBinding
    private val viewModel: ReaderViewModel by viewModels()
    private lateinit var reader: BaseReader
    private var currentPage = 0
    private var totalPages = 0
    private var isControlsVisible = true
    private var lastTapTime = 0L

    // Gesture detection
    private val gestureDetector by lazy {
        GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                handleTap(e.x)
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                e1 ?: return false
                return handleFling(e1.x, e2.x, velocityX)
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                e1 ?: return false
                return handleScroll(distanceX)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowBehavior()
        setupToolbar()
        initializeReader()
        setupGestures()
        observeViewModelStates()
        setupBottomControls()
    }

    private fun setupWindowBehavior() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
    }

    private fun initializeReader() {
        lifecycleScope.launch {
            viewModel.bookState.collectLatest { state ->
                when (state) {
                    is ReaderViewModel.BookState.Loading -> showLoading()
                    is ReaderViewModel.BookState.Success -> {
                        reader = when (state.book.format) {
                            "pdf" -> PdfReader(state.book.filePath)
                            "epub" -> EpubReader(state.book.filePath)
                            else -> throw IllegalStateException("Unsupported format")
                        }
                        totalPages = reader.getPageCount()
                        currentPage = state.book.lastReadPage
                        loadCurrentPage()
                    }
                    is ReaderViewModel.BookState.Error -> showError(state.message)
                }
            }
        }
    }

    private fun setupGestures() {
        binding.readerView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun handleTap(x: Float) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTapTime < DOUBLE_TAP_TIMEOUT) {
            handleDoubleTap(x)
        } else {
            val screenWidth = binding.readerView.width
            when {
                x < screenWidth * 0.3 -> navigatePage(-1)
                x > screenWidth * 0.7 -> navigatePage(1)
                else -> toggleControls()
            }
        }
        lastTapTime = currentTime
    }

    private fun handleFling(startX: Float, endX: Float, velocityX: Float): Boolean {
        if (abs(velocityX) > FLING_THRESHOLD) {
            if (velocityX > 0) navigatePage(-1) else navigatePage(1)
            return true
        }
        return false
    }

    private fun handleScroll(distanceX: Float): Boolean {
        // Implement smooth page scrolling animation
        return true
    }

    private fun handleDoubleTap(x: Float) {
        when (viewModel.currentReadingMode) {
            ReadingMode.CONTINUOUS -> viewModel.setReadingMode(ReadingMode.PAGE)
            ReadingMode.PAGE -> viewModel.setReadingMode(ReadingMode.CONTINUOUS)
        }
    }

    private fun navigatePage(direction: Int) {
        val nextPage = currentPage + direction
        if (nextPage in 0..totalPages) {
            AnimationUtils.animatePageTurn(binding.readerView, direction) {
                currentPage = nextPage
                loadCurrentPage()
            }
        }
    }

    private fun loadCurrentPage() {
        lifecycleScope.launch {
            try {
                val pageContent = reader.getPage(currentPage)
                binding.readerView.setPageContent(pageContent)
                updateProgress()
            } catch (e: Exception) {
                showError(getString(R.string.error_loading_page))
            }
        }
    }

    private fun updateProgress() {
        viewModel.updateProgress(
            BookProgress(
                pageNumber = currentPage,
                totalPages = totalPages,
                percentage = (currentPage.toFloat() / totalPages * 100).toInt()
            )
        )
        binding.pageProgress.text = getString(
            R.string.page_progress,
            currentPage + 1,
            totalPages
        )
    }

    private fun toggleControls() {
        isControlsVisible = !isControlsVisible
        val controller = WindowInsetsControllerCompat(window, binding.root)
        
        if (isControlsVisible) {
            controller.show(WindowInsetsCompat.Type.systemBars())
            AnimationUtils.fadeIn(binding.controlsContainer)
        } else {
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = 
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            AnimationUtils.fadeOut(binding.controlsContainer)
        }
    }

    private fun setupBottomControls() {
        binding.brightnessSlider.apply {
            value = viewModel.getCurrentBrightness()
            addOnChangeListener { _, value, fromUser ->
                if (fromUser) viewModel.setBrightness(value)
            }
        }

        binding.fontSizeSlider.apply {
            value = viewModel.getCurrentFontSize()
            addOnChangeListener { _, value, fromUser ->
                if (fromUser) viewModel.setFontSize(value)
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.readerView.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.retry)) { loadCurrentPage() }
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_reader, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_toc -> {
                showTableOfContents()
                true
            }
            R.id.action_settings -> {
                showReaderSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showTableOfContents() {
        lifecycleScope.launch {
            val chapters = reader.getTableOfContents()
            MaterialAlertDialogBuilder(this@ReaderActivity)
                .setTitle(R.string.table_of_contents)
                .setItems(chapters.map { it.title }.toTypedArray()) { _, position ->
                    navigateToChapter(chapters[position].pageNumber)
                }
                .show()
        }
    }

    private fun showReaderSettings() {
        ReaderSettingsDialog.show(supportFragmentManager) { settings ->
            viewModel.updateSettings(settings)
        }
    }

    private fun navigateToChapter(pageNumber: Int) {
        currentPage = pageNumber
        loadCurrentPage()
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveReadingProgress(currentPage)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Handle orientation changes
        loadCurrentPage()
    }

    companion object {
        private const val DOUBLE_TAP_TIMEOUT = 300L
        private const val FLING_THRESHOLD = 1000f
    }
}
