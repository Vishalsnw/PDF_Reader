/*
 * File: CategoryAdapter.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 19:49:27 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.reader.app.R
import com.reader.app.databinding.ItemCategoryBinding
import com.reader.app.models.Category
import com.reader.app.utils.AnimationUtils
import com.reader.app.utils.ThemeUtils

/**
 * CategoryAdapter displays categories with material design styling.
 * Features:
 * - Selection handling
 * - Book count display
 * - Color coding
 * - Animations
 */
class CategoryAdapter(
    private val onCategorySelected: (Category) -> Unit,
    private val onCategoryLongClick: (Category, MaterialCardView) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var selectedCategoryId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun selectCategory(categoryId: String?) {
        val oldSelection = selectedCategoryId
        selectedCategoryId = categoryId
        
        oldSelection?.let { notifyItemChanged(getItemPosition(it)) }
        categoryId?.let { notifyItemChanged(getItemPosition(it)) }
    }

    private fun getItemPosition(categoryId: String): Int {
        return currentList.indexOfFirst { it.id == categoryId }
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentCategory: Category? = null

        init {
            setupClickListeners()
            setupCardBehavior()
        }

        private fun setupClickListeners() {
            binding.root.setOnClickListener {
                currentCategory?.let { category ->
                    selectCategory(category.id)
                    onCategorySelected(category)
                }
            }

            binding.root.setOnLongClickListener {
                currentCategory?.let { category ->
                    onCategoryLongClick(category, binding.cardView)
                }
                true
            }
        }

        private fun setupCardBehavior() {
            binding.cardView.apply {
                setOnTouchListener { view, event ->
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN -> {
                            elevateCard()
                            false
                        }
                        android.view.MotionEvent.ACTION_UP,
                        android.view.MotionEvent.ACTION_CANCEL -> {
                            resetCardElevation()
                            false
                        }
                        else -> false
                    }
                }
            }
        }

        fun bind(category: Category) {
            currentCategory = category
            bindCategoryInfo(category)
            updateSelectionState(category)
            setupCategoryColor(category)
        }

        private fun bindCategoryInfo(category: Category) {
            binding.apply {
                // Category name
                textCategoryName.text = category.name

                // Book count
                textBookCount.text = category.bookCount.toString()
                
                // Recent indicator
                imageRecent.isVisible = category.hasRecentBooks

                // Progress indicator
                progressIndicator.apply {
                    progress = category.readingProgress
                    isIndeterminate = false
                    setProgressCompat(category.readingProgress, true)
                }

                // Unread badge
                chipUnread.apply {
                    isVisible = category.unreadCount > 0
                    text = category.unreadCount.toString()
                }
            }
        }

        private fun updateSelectionState(category: Category) {
            val isSelected = category.id == selectedCategoryId
            
            binding.cardView.apply {
                strokeWidth = if (isSelected) 2.dp else 0
                strokeColor = if (isSelected) {
                    ThemeUtils.getThemeColor(context, R.attr.colorPrimary)
                } else {
                    ContextCompat.getColor(context, android.R.color.transparent)
                }
            }

            if (isSelected) {
                AnimationUtils.pulseAnimation(binding.cardView)
            }
        }

        private fun setupCategoryColor(category: Category) {
            binding.viewColor.setBackgroundColor(
                category.color ?: ThemeUtils.getThemeColor(
                    binding.root.context,
                    R.attr.colorPrimary
                )
            )
        }

        private fun elevateCard() {
            binding.cardView.animate()
                .translationZ(4f)
                .setDuration(150)
                .start()
        }

        private fun resetCardElevation() {
            binding.cardView.animate()
                .translationZ(0f)
                .setDuration(150)
                .start()
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem &&
                   oldItem.bookCount == newItem.bookCount &&
                   oldItem.unreadCount == newItem.unreadCount &&
                   oldItem.readingProgress == newItem.readingProgress
        }

        override fun getChangePayload(oldItem: Category, newItem: Category): Any? {
            return when {
                oldItem.bookCount != newItem.bookCount ||
                oldItem.unreadCount != newItem.unreadCount ||
                oldItem.readingProgress != newItem.readingProgress -> PAYLOAD_STATS
                else -> null
            }
        }
    }

    companion object {
        private const val PAYLOAD_STATS = "payload_stats"
    }

    private val Int.dp: Int
        get() = (this * binding.root.resources.displayMetrics.density).toInt()
}
