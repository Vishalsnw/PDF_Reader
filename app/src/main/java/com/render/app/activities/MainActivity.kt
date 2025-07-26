/*
 * File: MainActivity.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.render.app.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.render.app.R
import com.render.app.adapters.BookListAdapter
import com.render.app.databinding.ActivityMainBinding
import com.render.app.models.Book
import com.render.app.utils.FileUtils
import com.render.app.viewmodels.LibraryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

/**
 * MainActivity serves as the entry point of the application, displaying the user's library
 * of eBooks in a grid layout with material design components and smooth animations.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: LibraryViewModel by viewModels()
    private lateinit var bookAdapter: BookListAdapter
    
    // Permission launcher for storage access
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scanForBooks()
        } else {
            showPermissionDeniedDialog()
        }
    }

    // File picker launcher
    private val pickFile = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importBook(uri) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeViewModelStates()
        checkPermissionsAndInitialize()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = getString(R.string.app_name)
            setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun setupRecyclerView() {
        bookAdapter = BookListAdapter(
            onBookClick = { book -> openReader(book) },
            onBookLongClick = { book -> showBookOptionsDialog(book) }
        )

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 
                resources.getInteger(R.integer.grid_column_count))
            adapter = bookAdapter
            setHasFixedSize(true)
        }

        // Add animation for items
        binding.recyclerView.itemAnimator?.apply {
            addDuration = 300L
            removeDuration = 300L
            moveDuration = 300L
            changeDuration = 300L
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            pickFile.launch("application/*")
        }
    }

    private fun observeViewModelStates() {
        lifecycleScope.launch {
            viewModel.books.collectLatest { books ->
                binding.progressBar.isVisible = false
                binding.emptyView.isVisible = books.isEmpty()
                binding.recyclerView.isVisible = books.isNotEmpty()
                bookAdapter.submitList(books)
            }
        }

        lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is LibraryViewModel.Event.Error -> showError(event.message)
                    is LibraryViewModel.Event.Success -> showSuccess(event.message)
                }
            }
        }
    }

    private fun checkPermissionsAndInitialize() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                scanForBooks()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun scanForBooks() {
        binding.progressBar.isVisible = true
        viewModel.scanForBooks()
    }

    private fun openReader(book: Book) {
        Intent(this, ReaderActivity::class.java).apply {
            putExtra(EXTRA_BOOK_ID, book.id)
            startActivity(this)
        }
    }

    private fun showBookOptionsDialog(book: Book) {
        MaterialAlertDialogBuilder(this)
            .setTitle(book.title)
            .setItems(R.array.book_options) { _, which ->
                when (which) {
                    0 -> viewModel.removeBook(book)
                    1 -> viewModel.bookDetails(book)
                    2 -> shareBook(book)
                }
            }
            .show()
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.permission_required)
            .setMessage(R.string.storage_permission_rationale)
            .setPositiveButton(R.string.grant) { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Retry") { scanForBooks() }
            .show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun shareBook(book: Book) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "application/*"
            putExtra(Intent.EXTRA_STREAM, FileUtils.getUriForFile(this@MainActivity, File(book.filePath)))
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_book)))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        
        // Setup search
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchBooks(newText.orEmpty())
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_sort -> {
                showSortDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSortDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sort_by)
            .setSingleChoiceItems(
                R.array.sort_options,
                viewModel.getCurrentSortOption()
            ) { dialog, which ->
                viewModel.setSortOption(which)
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        const val EXTRA_BOOK_ID = "extra_book_id"
    }
}
