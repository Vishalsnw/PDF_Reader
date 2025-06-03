/*
 * File: BookOptionsPopup.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 04:33:18 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.ui.components

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.yourapp.readers.R
import com.yourapp.readers.databinding.PopupBookOptionsBinding
import com.yourapp.readers.domain.models.Book
import com.yourapp.readers.utils.dpToPx
import com.yourapp.readers.utils.setOnSafeClickListener

class BookOptionsPopup(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val book: Book,
    private val onMarkAsReadClick: (Book) -> Unit,
    private val onAddToCollectionClick: (Book) -> Unit,
    private val onShareClick: (Book) -> Unit,
    private val onDeleteClick: (Book) -> Unit,
    private val onDownloadClick: (Book) -> Unit
) {
    private var popupWindow: PopupWindow? = null
    private var binding: PopupBookOptionsBinding? = null

    fun show(anchorView: View) {
        if (popupWindow?.isShowing == true) return

        binding = PopupBookOptionsBinding.inflate(LayoutInflater.from(context))
        
        setupPopupWindow()
        setupUI()
        setupClickListeners()
        
        showPopupWithAnimation(anchorView)
    }

    private fun setupPopupWindow() {
        binding?.let { binding ->
            popupWindow = PopupWindow(
                binding.root,
                context.dpToPx(200f).toInt(),
                PopupWindow.WRAP_CONTENT,
                true
            ).apply {
                elevation = context.dpToPx(8f)
                setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_popup))
                animationStyle = R.style.PopupAnimation
            }
        }
    }

    private fun setupUI() {
        binding?.apply {
            // Configure visibility based on book state
            btnMarkAsRead.visibility = if (book.isRead) View.GONE else View.VISIBLE
            btnDownload.visibility = if (book.isDownloaded) View.GONE else View.VISIBLE
            
            // Update UI states
            btnMarkAsRead.isEnabled = !book.isProcessing
            btnAddToCollection.isEnabled = !book.isProcessing
            btnShare.isEnabled = !book.isProcessing
            btnDelete.isEnabled = !book.isProcessing && book.isDownloaded
            btnDownload.isEnabled = !book.isProcessing
        }
    }

    private fun setupClickListeners() {
        binding?.apply {
            btnMarkAsRead.setOnSafeClickListener {
                onMarkAsReadClick(book)
                dismiss()
            }

            btnAddToCollection.setOnSafeClickListener {
                onAddToCollectionClick(book)
                dismiss()
            }

            btnShare.setOnSafeClickListener {
                onShareClick(book)
                dismiss()
            }

            btnDelete.setOnSafeClickListener {
                onDeleteClick(book)
                dismiss()
            }

            btnDownload.setOnSafeClickListener {
                onDownloadClick(book)
                dismiss()
            }
        }
    }

    private fun showPopupWithAnimation(anchorView: View) {
        popupWindow?.showAsDropDown(
            anchorView,
            0,
            -anchorView.height,
            Gravity.END
        )
    }

    fun dismiss() {
        popupWindow?.dismiss()
        binding = null
        popupWindow = null
    }

    fun isShowing(): Boolean = popupWindow?.isShowing == true

    companion object {
        private const val ANIMATION_DURATION = 200L
    }
}
