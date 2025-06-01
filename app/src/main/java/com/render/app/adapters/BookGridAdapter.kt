/*
 * File: BookGridAdapter.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 19:47:05 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation
import com.reader.app.R
import com.reader.app.databinding.ItemBookGridBinding
import com.reader.app.models.Book
import com.reader.app.utils.AnimationUtils
import com.reader.app.utils.DateUtils
import com.reader.app.utils.ThemeUtils

/**
 * BookGridAdapter displays books in a grid layout with material design styling.
 * Features:
 * - Cover image display
 * - Reading progress
 * - Status indicators
 * - Smooth animations
 * - Touch feedback
 */
class BookGridAdapter(
    private val onBookClick: (Book, View) -> Unit,
    private val onBookLongClick: (Book) -> Unit,
    private val onBookOptionsClick: (Book, View) -> Unit
) : ListAdapter<Book, BookGridAdapter.BookGridViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookGridViewHolder {
        val binding = ItemBookGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookGridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookGridViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: BookGridViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            holder.updatePayload(payloads.first())
        }
    }

    inner class BookGridViewHolder(
        private val binding: ItemBookGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentBook: Book? = null

        init {
            setupClickListeners()
            setupCardElevation()
        }

        private fun setupClickListeners() {
            binding.root.setOnClickListener {
                currentBook?.let { book ->
                    onBookClick(book, binding.imageCover)
                }
            }

            binding.root.setOnLongClickListener {
                currentBook?.let { book ->
                    onBookLongClick(book)
                }
                true
            }

            binding.buttonOptions.setOnClickListener {
                currentBook?.let { book ->
                    onBookOptionsClick(book, it)
                }
            }
        }

        private fun setupCardElevation() {
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

        fun bind(book: Book) {
            currentBook = book
            setupTransitionName(book)
            bindCover(book)
            bindTitle(book)
            bindProgress(book)
            bindStatus(book)
            bindLastRead(book)
        }

        private fun setupTransitionName(book: Book) {
            ViewCompat.setTransitionName(
                binding.imageCover,
                "book_cover_${book.id}"
            )
        }

        private fun bindCover(book: Book) {
            binding.imageCover.load(book.coverPath) {
                crossfade(true)
                placeholder(R.drawable.placeholder_book)
                error(R.drawable.placeholder_book)
                transformations(RoundedCornersTransformation(8f))
                listener(
                    onSuccess = { _, _ ->
                        binding.shimmerLayout.hideShimmer()
                    },
                    onError = { _, _ ->
                        binding.shimmerLayout.hideShimmer()
                    }
                )
            }
        }

        private fun bindTitle(book: Book) {
            binding.textTitle.apply {
                text = book.title
                isSelected = true  // Enable marquee for long titles
            }

            binding.textAuthor.apply {
                text = book.author
                isSelected = true
            }
        }

        private fun bindProgress(book: Book) {
            binding.progressIndicator.apply {
                progress = book.readingProgress
                isIndeterminate = false
                setProgressCompat(book.readingProgress, true)

                val colorRes = when {
                    book.readingProgress >= 100 -> R.color.progress_complete
                    book.readingProgress > 0 -> R.color.progress_reading
                    else -> R.color.progress_not_started
                }
                setIndicatorColor(context.getColor(colorRes))
            }
        }

        private fun bindStatus(book: Book) {
            binding.apply {
                // Reading status indicator
                imageStatus.setImageResource(
                    when {
                        book.isFinished -> R.drawable.ic_completed
                        book.isStarted -> R.drawable.ic_reading
                        else -> R.drawable.ic_new
                    }
                )

                // Format badge
                chipFormat.text = book.format.name
                
                // Bookmark indicator
                imageBookmark.isVisible = book.isBookmarked
            }
        }

        private fun bindLastRead(book: Book) {
            binding.textLastRead.apply {
                text = book.lastReadTime?.let { 
                    DateUtils.getRelativeTimeSpan(it)
                } ?: context.getString(R.string.not_started)
                
                setTextColor(
                    ThemeUtils.getThemeColor(
                        context,
                        if (book.isStarted) R.attr.colorPrimary else R.attr.colorOnSurface
                    )
                )
            }
        }

        fun updatePayload(payload: Any) {
            when (payload) {
                PAYLOAD_PROGRESS -> currentBook?.let { bindProgress(it) }
                PAYLOAD_STATUS -> currentBook?.let { bindStatus(it) }
                PAYLOAD_LAST_READ -> currentBook?.let { bindLastRead(it) }
            }
        }

        private fun elevateCard() {
            binding.cardView.animate()
                .translationZ(8f)
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

    private class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Book, newItem: Book): Any? {
            return when {
                oldItem.readingProgress != newItem.readingProgress -> PAYLOAD_PROGRESS
                oldItem.isFinished != newItem.isFinished ||
                oldItem.isBookmarked != newItem.isBookmarked -> PAYLOAD_STATUS
                oldItem.lastReadTime != newItem.lastReadTime -> PAYLOAD_LAST_READ
                else -> null
            }
        }
    }

    companion object {
        private const val PAYLOAD_PROGRESS = "payload_progress"
        private const val PAYLOAD_STATUS = "payload_status"
        private const val PAYLOAD_LAST_READ = "payload_last_read"
    }
}
