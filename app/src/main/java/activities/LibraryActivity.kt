/*
 * File: LibraryActivity.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 18:50:29 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.activities

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.reader.app.R
import com.reader.app.adapters.BookGridAdapter
import com.reader.app.adapters.BookListAdapter
import com.reader.app.databinding.ActivityLibraryBinding
import com.reader.app.models.Book
import com.reader.app.models.BookshelfLayout
import com.reader.app.models.SortOption
import com.reader.app.utils.FileUtils
import com.reader.app.utils.ThemeUtils
import com.reader.app.viewmodels.LibraryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

/**
 * LibraryActivity manages the user's book collection with a modern material design interface.
 * Features include:
 * - Grid/List view switching
 * - Advanced sorting and filtering
 * - Book import and management
 * - Search functionality
 * - Category organization
 */
@AndroidEntryPoint
class LibraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLibraryBinding
    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var gridAdapter: BookGridAdapter
    private lateinit var listAdapter: BookListAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    
    private var currentLayout = BookshelfLayout.GRID
    private var isFilterPanelExpanded = false

    // File picker launcher
    private val pickFiles = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris?.forEach { uri ->
            viewModel.importBook(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupTransitions()
        binding = ActivityLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerViews()
        setupFilterPanel()
        setupFabMenu()
        observeViewModelStates()
        setupSwipeRefresh()
        initializeBottomSheet()
    }

    private fun setupTransitions() {
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            duration = 300L
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.library_title)
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRecyclerViews() {
        // Grid Adapter
        gridAdapter = BookGridAdapter(
            onBookClick = { book, view -> openBookDetail(book, view) },
            onBookLongClick = { book -> showBookOptionsDialog(book) }
        )

        // List Adapter
        listAdapter = BookListAdapter(
            onBookClick = { book, view -> openBookDetail(book, view) },
            onBookLongClick = { book -> showBookOptionsDialog(book) }
        )

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@LibraryActivity, 
                resources.getInteger(R.integer.grid_column_count))
            adapter = gridAdapter
            setHasFixedSize(true)

            // Add scroll listener for FAB visibility
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) binding.fabMenu.shrink() else binding.fabMenu.extend()
                }
            })
        }
    }

    private fun setupFilterPanel() {
        binding.chipGroupCategories.apply {
            viewModel.getCategories().forEach { category ->
                addView(createCategoryChip(category))
            }
        }

        binding.buttonApplyFilters.setOnClickListener {
            applyFilters()
            toggleFilterPanel()
        }
    }

    private fun createCategoryChip(category: String): Chip {
        return Chip(this).apply {
            text = category
            isCheckable = true
            setOnCheckedChangeListener { _, isChecked ->
                viewModel.toggleCategoryFilter(category, isChecked)
            }
        }
    }

    private fun setupFabMenu() {
        binding.fabMenu.setOnClickListener {
            showImportOptions()
        }

        binding.fabScan.setOnClickListener {
            viewModel.scanForBooks()
        }

        binding.fabImport.setOnClickListener {
            pickFiles.launch("application/*")
        }
    }

    private fun observeViewModelStates() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.books.collectLatest { books ->
                        updateBookList(books)
                    }
                }

                launch {
                    viewModel.loadingState.collectLatest { isLoading ->
                        binding.progressBar.visibility = 
                            if (isLoading) View.VISIBLE else View.GONE
                    }
                }

                launch {
                    viewModel.events.collectLatest { event ->
                        handleEvent(event)
                    }
                }
            }
        }
    }

    private fun updateBookList(books: List<Book>) {
        binding.emptyView.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
        when (currentLayout) {
            BookshelfLayout.GRID -> gridAdapter.submitList(books)
            BookshelfLayout.LIST -> listAdapter.submitList(books)
        }
    }

    private fun handleEvent(event: LibraryViewModel.Event) {
        when (event) {
            is LibraryViewModel.Event.Error -> showError(event.message)
            is LibraryViewModel.Event.Success -> showSuccess(event.message)
            is LibraryViewModel.Event.ImportComplete -> handleImportComplete(event.count)
        }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.retry)) {
                viewModel.refreshLibrary()
            }
            .show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun handleImportComplete(count: Int) {
        Snackbar.make(
            binding.root,
            resources.getQuantityString(R.plurals.books_imported, count, count),
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun openBookDetail(book: Book, view: View) {
        Intent(this, ReaderActivity::class.java).apply {
            putExtra(EXTRA_BOOK_ID, book.id)
            putExtra(EXTRA_TRANSITION_NAME, view.transitionName)
            startActivity(this)
        }
    }

    private fun showBookOptionsDialog(book: Book) {
        MaterialAlertDialogBuilder(this)
            .setTitle(book.title)
            .setItems(R.array.book_options) { _, which ->
                when (which) {
                    0 -> viewModel.removeBook(book)
                    1 -> showBookDetails(book)
                    2 -> shareBook(book)
                }
            }
            .show()
    }

    private fun showBookDetails(book: Book) {
        BookDetailsBottomSheet.show(supportFragmentManager, book)
    }

    private fun shareBook(book: Book) {
        val uri = FileUtils.getUriForFile(this, File(book.filePath))
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "application/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_book)))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_library, menu)
        setupSearchView(menu)
        return true
    }

    private fun setupSearchView(menu: Menu) {
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchBooks(newText.orEmpty())
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_view_mode -> {
                toggleViewMode()
                true
            }
            R.id.action_sort -> {
                showSortDialog()
                true
            }
            R.id.action_filter -> {
                toggleFilterPanel()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toggleViewMode() {
        currentLayout = if (currentLayout == BookshelfLayout.GRID) {
            binding.recyclerView.layoutManager = LinearLayoutManager(this)
            binding.recyclerView.adapter = listAdapter
            BookshelfLayout.LIST
        } else {
            binding.recyclerView.layoutManager = GridLayoutManager(this, 
                resources.getInteger(R.integer.grid_column_count))
            binding.recyclerView.adapter = gridAdapter
            BookshelfLayout.GRID
        }
        viewModel.saveLayoutPreference(currentLayout)
    }

    companion object {
        const val EXTRA_BOOK_ID = "extra_book_id"
        const val EXTRA_TRANSITION_NAME = "extra_transition_name"
    }
}
