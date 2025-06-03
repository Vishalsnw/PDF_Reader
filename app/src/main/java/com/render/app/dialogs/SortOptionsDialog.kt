/*
 * File: SortOptionsDialog.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 04:35:06 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.yourapp.readers.R
import com.yourapp.readers.databinding.DialogSortOptionsBinding
import com.yourapp.readers.domain.models.SortOption
import com.yourapp.readers.domain.models.SortOrder
import com.yourapp.readers.utils.setOnSafeClickListener
import com.yourapp.readers.utils.viewBinding

class SortOptionsDialog : DialogFragment() {

    private val binding by viewBinding(DialogSortOptionsBinding::bind)
    private var currentSortOption: SortOption = SortOption.TITLE
    private var currentSortOrder: SortOrder = SortOrder.ASCENDING

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle)
        
        arguments?.let {
            currentSortOption = it.getParcelable(ARG_CURRENT_SORT_OPTION) ?: SortOption.TITLE
            currentSortOrder = it.getParcelable(ARG_CURRENT_SORT_ORDER) ?: SortOrder.ASCENDING
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_sort_options, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupInitialState()
        setupClickListeners()
    }

    private fun setupInitialState() {
        binding.apply {
            // Set sort option radio buttons
            when (currentSortOption) {
                SortOption.TITLE -> rbTitle
                SortOption.AUTHOR -> rbAuthor
                SortOption.DATE_ADDED -> rbDateAdded
                SortOption.LAST_READ -> rbLastRead
            }.isChecked = true

            // Set sort order radio buttons
            when (currentSortOrder) {
                SortOrder.ASCENDING -> rbAscending
                SortOrder.DESCENDING -> rbDescending
            }.isChecked = true
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Sort option selection
            rgSortOption.setOnCheckedChangeListener { _, checkedId ->
                currentSortOption = when (checkedId) {
                    R.id.rbTitle -> SortOption.TITLE
                    R.id.rbAuthor -> SortOption.AUTHOR
                    R.id.rbDateAdded -> SortOption.DATE_ADDED
                    R.id.rbLastRead -> SortOption.LAST_READ
                    else -> SortOption.TITLE
                }
            }

            // Sort order selection
            rgSortOrder.setOnCheckedChangeListener { _, checkedId ->
                currentSortOrder = when (checkedId) {
                    R.id.rbAscending -> SortOrder.ASCENDING
                    R.id.rbDescending -> SortOrder.DESCENDING
                    else -> SortOrder.ASCENDING
                }
            }

            // Button actions
            btnApply.setOnSafeClickListener {
                setFragmentResult(REQUEST_KEY, createResultBundle())
                dismiss()
            }

            btnCancel.setOnSafeClickListener {
                dismiss()
            }
        }
    }

    private fun createResultBundle() = bundleOf(
        RESULT_SORT_OPTION to currentSortOption,
        RESULT_SORT_ORDER to currentSortOrder
    )

    companion object {
        const val TAG = "SortOptionsDialog"
        const val REQUEST_KEY = "sort_options_request"
        const val RESULT_SORT_OPTION = "result_sort_option"
        const val RESULT_SORT_ORDER = "result_sort_order"
        private const val ARG_CURRENT_SORT_OPTION = "current_sort_option"
        private const val ARG_CURRENT_SORT_ORDER = "current_sort_order"

        fun newInstance(
            currentSortOption: SortOption,
            currentSortOrder: SortOrder
        ) = SortOptionsDialog().apply {
            arguments = bundleOf(
                ARG_CURRENT_SORT_OPTION to currentSortOption,
                ARG_CURRENT_SORT_ORDER to currentSortOrder
            )
        }
    }
}

// SortOption.kt
enum class SortOption {
    TITLE,
    AUTHOR,
    DATE_ADDED,
    LAST_READ
}

// SortOrder.kt
enum class SortOrder {
    ASCENDING,
    DESCENDING
}
