/*
 * File: ChangelogDialog.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 20:18:47 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.reader.app.R
import com.reader.app.databinding.DialogChangelogBinding
import com.reader.app.models.ChangelogItem
import com.reader.app.adapters.ChangelogAdapter
import com.reader.app.utils.AppVersionUtils
import com.reader.app.utils.PreferenceUtils

/**
 * ChangelogDialog displays app version changes in a material design dialog.
 * Features:
 * - Version history
 * - Change categories
 * - Rich formatting
 * - Auto-display on updates
 */
class ChangelogDialog private constructor(
    context: Context,
    private val showOnlyNewChanges: Boolean = true
) : Dialog(context) {

    private lateinit var binding: DialogChangelogBinding
    private lateinit var adapter: ChangelogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = DialogChangelogBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        setupDialog()
        setupRecyclerView()
        loadChangelog()
    }

    private fun setupDialog() {
        window?.apply {
            setBackgroundDrawableResource(R.drawable.bg_rounded_dialog)
            setLayout(
                context.resources.getDimensionPixelSize(R.dimen.changelog_dialog_width),
                context.resources.getDimensionPixelSize(R.dimen.changelog_dialog_height)
            )
        }

        binding.apply {
            buttonClose.setOnClickListener { 
                dismiss()
                PreferenceUtils.setLastSeenVersion(context, AppVersionUtils.getCurrentVersion())
            }

            textVersion.text = context.getString(
                R.string.version_name,
                AppVersionUtils.getCurrentVersion()
            )
        }
    }

    private fun setupRecyclerView() {
        adapter = ChangelogAdapter()
        
        binding.recyclerChangelog.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ChangelogDialog.adapter
            setHasFixedSize(true)
        }
    }

    private fun loadChangelog() {
        val changelog = if (showOnlyNewChanges) {
            getNewChanges()
        } else {
            getAllChanges()
        }
        
        adapter.submitList(changelog)
    }

    private fun getNewChanges(): List<ChangelogItem> {
        val lastSeenVersion = PreferenceUtils.getLastSeenVersion(context)
        return getAllChanges().filter { 
            it.version > lastSeenVersion 
        }
    }

    private fun getAllChanges(): List<ChangelogItem> {
        return listOf(
            ChangelogItem(
                version = "2.0.0",
                changes = listOf(
                    Change(
                        type = ChangeType.NEW,
                        description = "Added support for PDF files"
                    ),
                    Change(
                        type = ChangeType.IMPROVED,
                        description = "Enhanced reading experience with new text rendering"
                    ),
                    Change(
                        type = ChangeType.FIXED,
                        description = "Fixed crash when opening large files"
                    )
                )
            ),
            ChangelogItem(
                version = "1.9.0",
                changes = listOf(
                    Change(
                        type = ChangeType.NEW,
                        description = "Dark mode support"
                    ),
                    Change(
                        type = ChangeType.IMPROVED,
                        description = "Better memory management"
                    )
                )
            )
            // Add more versions here
        )
    }

    companion object {
        fun show(context: Context, showOnlyNewChanges: Boolean = true) {
            if (!shouldShow(context, showOnlyNewChanges)) return

            ChangelogDialog(context, showOnlyNewChanges).show()
        }

        private fun shouldShow(context: Context, showOnlyNewChanges: Boolean): Boolean {
            if (!showOnlyNewChanges) return true

            val lastSeenVersion = PreferenceUtils.getLastSeenVersion(context)
            val currentVersion = AppVersionUtils.getCurrentVersion()
            return currentVersion > lastSeenVersion
        }
    }

    data class Change(
        val type: ChangeType,
        val description: String
    )

    enum class ChangeType {
        NEW,
        IMPROVED,
        FIXED,
        REMOVED
    }
}
