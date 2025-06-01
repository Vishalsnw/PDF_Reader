/*
 * File: BookListAdapter.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 18:56:32 UTC
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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.reader.app.R
import com.reader.app.databinding.ItemBookListBinding
import com.reader.app.models.Book
import com.reader.app.utils.DateUtils
import com.reader.app.utils.FileUtils
import com.reader.app.utils.ThemeUtils
import java.io.File

/**
 * BookListAdapter displays books in a list format with material design styling.
 * Features:
 * - Smooth animations
 * - Progress indicators
 * - Format badges
 * - Reading status
 * - Last read time
 */
class BookListAdapter(
    private val onBookClick: (Book, View) -> Unit,
    private val onBookLongClick: (Book) -> Unit,
    private val onBookOptionsClick: (Book, View) -> Unit
) : ListAdapter<Book, BookListAdapter.BookViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: BookViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    inner class BookViewHolder(
        private val binding: ItemBookListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentBook: Book? = null

        init {
            setupClickListeners()
            setupCardElevation()
        }

        private fun setupClickListeners() {
            binding.root.setOnClickListener {
                currentBook?.let { book ->
                    onBookClick(book, binding.cardView)
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
            bindBookInfo(book)
            bindCoverImage(book)
            bindProgress(book)
            bindFormatBadge(book)
            bindLastRead(book)
            setupSwipeActions(book)
        }

        private fun setupTransitionName(book: Book) {
            ViewCompat.setTransitionName(
                binding.cardView,
                "book_transition_${book.id}"
            )
        }

        private fun bindBookInfo(book: Book) {
            binding.apply {
                textTitle.text = book.title
                textAuthor.text = book.author
                textFormat.text = book.format.uppercase()
                
                // Set text colors based on reading status
                val textColor = if (book.isFinished) {
                    ThemeUtils.getThemeColor(root.context, R.attr.colorPrimary)
                } else {
                    ThemeUtils.getThemeColor(root.context, R.attr.colorOnSurface)
                }
                textTitle.setTextColor(textColor)
            }
        }

        private fun bindCoverImage(book: Book) {
            binding.imageCover.load(File(book.coverPath)) {
                crossfade(true)
                placeholder(R.drawable.placeholder_book)
                error(R.drawable.placeholder_book)
                transformations(RoundedCornersTransformation(8f))
                listener(
                    onSuccess = { _, _ ->
                        binding.imageCover.setTag(R.id.tag_image_loaded, true)
                    },
                    onError = { _, _ ->
                        binding.imageCover.setTag(R.id.tag_image_loaded, false)
                    }
                )
            }
        }

        private fun bindProgress(book: Book) {
            binding.progressIndicator.apply {
                progress = book.readingProgress
                isIndeterminate = false
                
                // Animate progress changes
                setProgressCompat(book.readingProgress, true)
                
                // Update progress color based on completion
                val colorRes = when {
                    book.readingProgress >= 100 -> R.color.progress_complete
                    book.readingProgress > 0 -> R.color.progress_reading
                    else -> R.color.progress_not_started
                }
                setIndicatorColor(context.getColor(colorRes))
            }
        }

        private fun bindFormatBadge(book: Book) {
            binding.chipFormat.apply {
                text = book.format.uppercase()
                setChipBackgroundColorResource(
                    when (book.format.lowercase()) {
                        "pdf" -> R.color.format_pdf
                        "epub" -> R.color.format_epub
                        else -> R.color.format_other
                    }
                )
            }
        }

        private fun bindLastRead(book: Book) {
            binding.textLastRead.text = DateUtils.getRelativeTimeSpan(book.lastReadTime)
        }

        private fun setupSwipeActions(book: Book) {
            // Implement swipe-to-archive or other actions if needed
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

        fun unbind() {
            binding.imageCover.setTag(R.id.tag_image_loaded, null)
            currentBook = null
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
                oldItem.lastReadTime != newItem.lastReadTime -> PAYLOAD_LAST_READ
                else -> null
            }
        }
    }

    companion object {
        private const val PAYLOAD_PROGRESS = "payload_progress"
        private const val PAYLOAD_LAST_READ = "payload_last_read"
    }
}
