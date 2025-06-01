/*
 * File: ChapterAdapter.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 19:01:15 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.adapters

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.reader.app.R
import com.reader.app.databinding.ItemChapterBinding
import com.reader.app.models.Chapter
import com.reader.app.utils.AnimationUtils
import com.reader.app.utils.TextUtils
import com.reader.app.utils.TimeUtils

/**
 * ChapterAdapter manages the display of book chapters with rich features.
 * Features:
 * - Chapter progress tracking
 * - Nested subsections
 * - Reading time estimates
 * - Progress indicators
 * - Bookmarking support
 */
class ChapterAdapter(
    private val onChapterClick: (Chapter) -> Unit,
    private val onChapterLongClick: (Chapter, View) -> Unit,
    private val onBookmarkClick: (Chapter, Boolean) -> Unit
) : ListAdapter<Chapter, ChapterAdapter.ChapterViewHolder>(ChapterDiffCallback()) {

    private var currentChapterId: String? = null
    private var expandedChapterId: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val binding = ItemChapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(
        holder: ChapterViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            holder.updatePayload(payloads.first())
        }
    }

    inner class ChapterViewHolder(
        private val binding: ItemChapterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentChapter: Chapter? = null
        private var progressAnimator: ValueAnimator? = null

        init {
            setupClickListeners()
            setupCardBehavior()
        }

        private fun setupClickListeners() {
            binding.root.setOnClickListener {
                currentChapter?.let { chapter ->
                    if (chapter.hasSubchapters) {
                        toggleExpansion(chapter)
                    } else {
                        onChapterClick(chapter)
                    }
                }
            }

            binding.root.setOnLongClickListener {
                currentChapter?.let { chapter ->
                    onChapterLongClick(chapter, binding.cardChapter)
                }
                true
            }

            binding.buttonBookmark.setOnClickListener {
                currentChapter?.let { chapter ->
                    val newState = !chapter.isBookmarked
                    onBookmarkClick(chapter, newState)
                    updateBookmarkState(newState)
                }
            }
        }

        private fun setupCardBehavior() {
            binding.cardChapter.apply {
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

        fun bind(chapter: Chapter) {
            currentChapter = chapter
            bindChapterInfo(chapter)
            bindProgress(chapter)
            bindSubchapters(chapter)
            updateExpandedState(chapter)
            updateCurrentChapterState(chapter)
        }

        private fun bindChapterInfo(chapter: Chapter) {
            binding.apply {
                // Chapter title with proper indentation
                textChapterTitle.apply {
                    text = chapter.title
                    setPadding(chapter.level * 16, paddingTop, paddingRight, paddingBottom)
                }

                // Chapter number
                textChapterNumber.text = chapter.number

                // Reading time estimate
                textReadingTime.text = TimeUtils.formatReadingTime(
                    chapter.getEstimatedReadingTime()
                )

                // Progress text
                textProgress.text = "${chapter.progress}%"

                // Bookmark state
                buttonBookmark.isSelected = chapter.isBookmarked

                // Subsection indicator
                imageExpand.isVisible = chapter.hasSubchapters
                imageExpand.rotation = if (chapter.id == expandedChapterId) 180f else 0f

                // Word count
                textWordCount.text = TextUtils.formatWordCount(chapter.wordCount)
            }
        }

        private fun bindProgress(chapter: Chapter) {
            progressAnimator?.cancel()
            
            progressAnimator = ValueAnimator.ofInt(0, chapter.progress).apply {
                duration = 500
                addUpdateListener { animator ->
                    binding.progressIndicator.progress = animator.animatedValue as Int
                }
                start()
            }

            // Update progress bar color based on completion
            val colorRes = when {
                chapter.progress >= 100 -> R.color.progress_complete
                chapter.progress > 0 -> R.color.progress_reading
                else -> R.color.progress_not_started
            }
            
            binding.progressIndicator.setIndicatorColor(
                ContextCompat.getColor(binding.root.context, colorRes)
            )
        }

        private fun bindSubchapters(chapter: Chapter) {
            binding.recyclerViewSubchapters.apply {
                isVisible = chapter.id == expandedChapterId && chapter.hasSubchapters
                if (isVisible && adapter == null) {
                    adapter = ChapterAdapter(onChapterClick, onChapterLongClick, onBookmarkClick)
                }
                (adapter as? ChapterAdapter)?.submitList(chapter.subchapters)
            }
        }

        private fun updateExpandedState(chapter: Chapter) {
            val isExpanded = chapter.id == expandedChapterId
            binding.apply {
                recyclerViewSubchapters.isVisible = isExpanded && chapter.hasSubchapters
                if (isExpanded) {
                    AnimationUtils.expandView(recyclerViewSubchapters)
                } else {
                    AnimationUtils.collapseView(recyclerViewSubchapters)
                }
                imageExpand.animate()
                    .rotation(if (isExpanded) 180f else 0f)
                    .setDuration(200)
                    .start()
            }
        }

        private fun updateCurrentChapterState(chapter: Chapter) {
            val isCurrentChapter = chapter.id == currentChapterId
            binding.cardChapter.strokeWidth = if (isCurrentChapter) 2.dp else 0
        }

        private fun toggleExpansion(chapter: Chapter) {
            expandedChapterId = if (expandedChapterId == chapter.id) null else chapter.id
            updateExpandedState(chapter)
        }

        private fun updateBookmarkState(isBookmarked: Boolean) {
            binding.buttonBookmark.apply {
                isSelected = isBookmarked
                AnimationUtils.pulseAnimation(this)
            }
        }

        fun updatePayload(payload: Any) {
            when (payload) {
                PAYLOAD_PROGRESS -> currentChapter?.let { bindProgress(it) }
                PAYLOAD_BOOKMARK -> currentChapter?.let { 
                    updateBookmarkState(it.isBookmarked)
                }
            }
        }

        private fun elevateCard() {
            binding.cardChapter.animate()
                .translationZ(4f)
                .setDuration(150)
                .start()
        }

        private fun resetCardElevation() {
            binding.cardChapter.animate()
                .translationZ(0f)
                .setDuration(150)
                .start()
        }
    }

    private class ChapterDiffCallback : DiffUtil.ItemCallback<Chapter>() {
        override fun areItemsTheSame(oldItem: Chapter, newItem: Chapter): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Chapter, newItem: Chapter): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Chapter, newItem: Chapter): Any? {
            return when {
                oldItem.progress != newItem.progress -> PAYLOAD_PROGRESS
                oldItem.isBookmarked != newItem.isBookmarked -> PAYLOAD_BOOKMARK
                else -> null
            }
        }
    }

    companion object {
        private const val PAYLOAD_PROGRESS = "payload_progress"
        private const val PAYLOAD_BOOKMARK = "payload_bookmark"
    }

    private val Int.dp: Int
        get() = (this * binding.root.resources.displayMetrics.density).toInt()
}
