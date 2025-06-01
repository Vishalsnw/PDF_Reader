/*
 * File: LibraryAdapter.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 18:58:39 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.chip.Chip
import com.reader.app.R
import com.reader.app.databinding.ItemLibrarySectionBinding
import com.reader.app.models.Book
import com.reader.app.models.LibrarySection
import com.reader.app.utils.AnimationUtils
import com.reader.app.utils.ThemeUtils

/**
 * LibraryAdapter manages the display of book collections organized by sections.
 * Features:
 * - Section headers with expandable content
 * - Category filtering
 * - Grid/List view switching
 * - Drag and drop support
 * - Section statistics
 */
class LibraryAdapter(
    private val onBookClick: (Book, View) -> Unit,
    private val onBookLongClick: (Book) -> Unit,
    private val onSectionClick: (LibrarySection) -> Unit,
    private val onCategorySelected: (String) -> Unit
) : ListAdapter<LibrarySection, LibraryAdapter.LibrarySectionViewHolder>(SectionDiffCallback()) {

    private var expandedSectionId: String? = null
    private var currentViewMode: ViewMode = ViewMode.GRID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LibrarySectionViewHolder {
        val binding = ItemLibrarySectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LibrarySectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LibrarySectionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewAttachedToWindow(holder: LibrarySectionViewHolder) {
        super.onViewAttachedToWindow(holder)
        val layoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
        layoutParams.isFullSpan = true
    }

    inner class LibrarySectionViewHolder(
        private val binding: ItemLibrarySectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val bookAdapter = BookListAdapter(
            onBookClick = onBookClick,
            onBookLongClick = onBookLongClick,
            onBookOptionsClick = { book, view -> showBookOptions(book, view) }
        )

        init {
            setupRecyclerView()
            setupExpandCollapse()
            setupCategoryChips()
        }

        private fun setupRecyclerView() {
            binding.recyclerViewBooks.apply {
                adapter = bookAdapter
                setHasFixedSize(true)
                itemAnimator = AnimationUtils.getItemAnimator()
            }
        }

        private fun setupExpandCollapse() {
            binding.layoutHeader.setOnClickListener {
                val section = getItem(bindingAdapterPosition)
                toggleSection(section)
            }
        }

        private fun setupCategoryChips() {
            binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
                checkedIds.firstOrNull()?.let { id ->
                    val chip = group.findViewById<Chip>(id)
                    onCategorySelected(chip.text.toString())
                }
            }
        }

        fun bind(section: LibrarySection) {
            bindHeader(section)
            bindStatistics(section)
            bindCategories(section)
            bindBooks(section)
            updateExpandedState(section)
        }

        private fun bindHeader(section: LibrarySection) {
            binding.apply {
                textSectionTitle.text = section.title
                textSectionDescription.text = section.description
                imageIcon.setImageResource(getSectionIcon(section.type))
                
                buttonSort.setOnClickListener {
                    showSortOptions(section)
                }

                buttonViewMode.setOnClickListener {
                    toggleViewMode()
                }
            }
        }

        private fun bindStatistics(section: LibrarySection) {
            binding.apply {
                textBookCount.text = section.books.size.toString()
                textTotalPages.text = section.getTotalPages().toString()
                
                progressReading.apply {
                    progress = section.getReadingProgress()
                    isIndeterminate = false
                    setProgressCompat(section.getReadingProgress(), true)
                }

                // Animate statistics changes
                AnimationUtils.animateNumericChange(
                    textBookCount,
                    section.books.size
                )
            }
        }

        private fun bindCategories(section: LibrarySection) {
            binding.chipGroupCategories.removeAllViews()
            section.categories.forEach { category ->
                addCategoryChip(category)
            }
        }

        private fun addCategoryChip(category: String) {
            val chip = LayoutInflater.from(binding.root.context)
                .inflate(R.layout.item_category_chip, binding.chipGroupCategories, false) as Chip
            
            chip.apply {
                text = category
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        onCategorySelected(category)
                    }
                }
            }

            binding.chipGroupCategories.addView(chip)
        }

        private fun bindBooks(section: LibrarySection) {
            bookAdapter.submitList(section.books) {
                // Scroll to top when list changes
                binding.recyclerViewBooks.scrollToPosition(0)
            }
        }

        private fun updateExpandedState(section: LibrarySection) {
            val isExpanded = section.id == expandedSectionId
            binding.apply {
                recyclerViewBooks.isVisible = isExpanded
                chipGroupCategories.isVisible = isExpanded
                imageExpand.rotation = if (isExpanded) 180f else 0f

                if (isExpanded) {
                    AnimationUtils.expandSection(layoutContent)
                } else {
                    AnimationUtils.collapseSection(layoutContent)
                }
            }
        }

        private fun toggleSection(section: LibrarySection) {
            expandedSectionId = if (expandedSectionId == section.id) null else section.id
            onSectionClick(section)
            updateExpandedState(section)
        }

        private fun toggleViewMode() {
            currentViewMode = when (currentViewMode) {
                ViewMode.GRID -> ViewMode.LIST
                ViewMode.LIST -> ViewMode.GRID
            }
            updateViewMode()
        }

        private fun updateViewMode() {
            val layoutManager = when (currentViewMode) {
                ViewMode.GRID -> createGridLayoutManager()
                ViewMode.LIST -> createListLayoutManager()
            }
            binding.recyclerViewBooks.layoutManager = layoutManager
            binding.buttonViewMode.setImageResource(
                when (currentViewMode) {
                    ViewMode.GRID -> R.drawable.ic_view_list
                    ViewMode.LIST -> R.drawable.ic_view_grid
                }
            )
        }

        private fun showSortOptions(section: LibrarySection) {
            SortOptionsDialog.show(
                context = binding.root.context,
                currentSort = section.sortOption
            ) { newSort ->
                // Handle sort option change
            }
        }

        private fun showBookOptions(book: Book, anchor: View) {
            BookOptionsPopup.show(
                context = binding.root.context,
                anchor = anchor,
                book = book
            )
        }

        private fun getSectionIcon(type: LibrarySection.Type): Int {
            return when (type) {
                LibrarySection.Type.RECENT -> R.drawable.ic_recent
                LibrarySection.Type.READING -> R.drawable.ic_reading
                LibrarySection.Type.COMPLETED -> R.drawable.ic_completed
                LibrarySection.Type.COLLECTION -> R.drawable.ic_collection
            }
        }
    }

    private class SectionDiffCallback : DiffUtil.ItemCallback<LibrarySection>() {
        override fun areItemsTheSame(oldItem: LibrarySection, newItem: LibrarySection): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LibrarySection, newItem: LibrarySection): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: LibrarySection, newItem: LibrarySection): Any? {
            return when {
                oldItem.books != newItem.books -> PAYLOAD_BOOKS
                oldItem.categories != newItem.categories -> PAYLOAD_CATEGORIES
                else -> null
            }
        }
    }

    enum class ViewMode {
        GRID, LIST
    }

    companion object {
        private const val PAYLOAD_BOOKS = "payload_books"
        private const val PAYLOAD_CATEGORIES = "payload_categories"
    }
}
