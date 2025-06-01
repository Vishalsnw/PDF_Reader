/*
 * File: SettingsActivity.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 18:53:19 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ShareCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.reader.app.BuildConfig
import com.reader.app.R
import com.reader.app.databinding.ActivitySettingsBinding
import com.reader.app.models.Theme
import com.reader.app.utils.StorageUtils
import com.reader.app.utils.ThemeUtils
import com.reader.app.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat

/**
 * SettingsActivity handles all app configurations and user preferences.
 * Features include:
 * - Reading preferences
 * - Theme customization
 * - Storage management
 * - Backup & Restore
 * - App information
 */
@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.settings)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @AndroidEntryPoint
    class SettingsFragment : PreferenceFragmentCompat() {
        private val viewModel: SettingsViewModel by viewModels()
        
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            setupPreferences()
        }

        private fun setupPreferences() {
            // Reading Preferences
            setupReadingPreferences()
            
            // Theme Settings
            setupThemeSettings()
            
            // Storage Management
            setupStoragePreferences()
            
            // Backup & Restore
            setupBackupPreferences()
            
            // About Section
            setupAboutPreferences()
        }

        private fun setupReadingPreferences() {
            // Default Reading Mode
            findPreference<ListPreference>("reading_mode")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    viewModel.setDefaultReadingMode(newValue.toString())
                    true
                }
            }

            // Page Turn Animation
            findPreference<SwitchPreferenceCompat>("page_animation")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    viewModel.setPageAnimation(newValue as Boolean)
                    true
                }
            }

            // Keep Screen On
            findPreference<SwitchPreferenceCompat>("keep_screen_on")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    viewModel.setKeepScreenOn(newValue as Boolean)
                    true
                }
            }

            // Font Settings
            findPreference<Preference>("font_settings")?.apply {
                setOnPreferenceClickListener {
                    showFontSettingsDialog()
                    true
                }
            }
        }

        private fun setupThemeSettings() {
            // App Theme
            findPreference<ListPreference>("app_theme")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    val theme = Theme.valueOf(newValue.toString())
                    updateTheme(theme)
                    true
                }
            }

            // Reading Theme
            findPreference<ListPreference>("reading_theme")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    viewModel.setReadingTheme(newValue.toString())
                    true
                }
            }

            // Custom Colors
            findPreference<Preference>("custom_colors")?.apply {
                setOnPreferenceClickListener {
                    showColorCustomizationDialog()
                    true
                }
            }
        }

        private fun setupStoragePreferences() {
            // Cache Management
            findPreference<Preference>("clear_cache")?.apply {
                lifecycleScope.launch {
                    summary = getString(R.string.cache_size, 
                        formatFileSize(StorageUtils.getCacheSize(requireContext())))
                    
                    setOnPreferenceClickListener {
                        showClearCacheDialog()
                        true
                    }
                }
            }

            // Storage Location
            findPreference<ListPreference>("storage_location")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    viewModel.setStorageLocation(newValue.toString())
                    true
                }
            }
        }

        private fun setupBackupPreferences() {
            // Backup Library
            findPreference<Preference>("backup_library")?.apply {
                setOnPreferenceClickListener {
                    backupLibrary()
                    true
                }
            }

            // Restore Library
            findPreference<Preference>("restore_library")?.apply {
                setOnPreferenceClickListener {
                    showRestoreDialog()
                    true
                }
            }

            // Auto Backup
            findPreference<SwitchPreferenceCompat>("auto_backup")?.apply {
                setOnPreferenceChangeListener { _, newValue ->
                    viewModel.setAutoBackup(newValue as Boolean)
                    true
                }
            }
        }

        private fun setupAboutPreferences() {
            // Version Info
            findPreference<Preference>("app_version")?.apply {
                summary = BuildConfig.VERSION_NAME
                setOnPreferenceClickListener {
                    showChangelog()
                    true
                }
            }

            // Rate App
            findPreference<Preference>("rate_app")?.apply {
                setOnPreferenceClickListener {
                    openPlayStore()
                    true
                }
            }

            // Feedback
            findPreference<Preference>("feedback")?.apply {
                setOnPreferenceClickListener {
                    sendFeedback()
                    true
                }
            }
        }

        private fun showFontSettingsDialog() {
            FontSettingsDialog.show(parentFragmentManager)
        }

        private fun showColorCustomizationDialog() {
            ColorCustomizationDialog.show(parentFragmentManager)
        }

        private fun showClearCacheDialog() {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_cache)
                .setMessage(R.string.clear_cache_confirmation)
                .setPositiveButton(R.string.clear) { _, _ ->
                    clearCache()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        private fun clearCache() {
            lifecycleScope.launch {
                viewModel.clearCache()
                updateCacheSize()
                showSnackbar(getString(R.string.cache_cleared))
            }
        }

        private fun updateCacheSize() {
            lifecycleScope.launch {
                findPreference<Preference>("clear_cache")?.summary = 
                    getString(R.string.cache_size, 
                        formatFileSize(StorageUtils.getCacheSize(requireContext())))
            }
        }

        private fun backupLibrary() {
            lifecycleScope.launch {
                try {
                    val backupFile = viewModel.backupLibrary()
                    shareBackup(backupFile)
                } catch (e: Exception) {
                    showSnackbar(getString(R.string.backup_failed))
                }
            }
        }

        private fun shareBackup(backupFile: File) {
            ShareCompat.IntentBuilder(requireActivity())
                .setStream(Uri.fromFile(backupFile))
                .setType("application/zip")
                .setChooserTitle(R.string.save_backup)
                .startChooser()
        }

        private fun showRestoreDialog() {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.restore_library)
                .setMessage(R.string.restore_confirmation)
                .setPositiveButton(R.string.restore) { _, _ ->
                    restoreLibrary()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        private fun restoreLibrary() {
            lifecycleScope.launch {
                try {
                    viewModel.restoreLibrary()
                    showSnackbar(getString(R.string.restore_successful))
                } catch (e: Exception) {
                    showSnackbar(getString(R.string.restore_failed))
                }
            }
        }

        private fun updateTheme(theme: Theme) {
            when (theme) {
                Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO)
                Theme.DARK -> AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES)
                Theme.SYSTEM -> AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        private fun showChangelog() {
            ChangelogDialog.show(parentFragmentManager)
        }

        private fun openPlayStore() {
            try {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=${BuildConfig.APPLICATION_ID}")))
            } catch (e: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")))
            }
        }

        private fun sendFeedback() {
            ShareCompat.IntentBuilder(requireActivity())
                .setType("message/rfc822")
                .addEmailTo(getString(R.string.feedback_email))
                .setSubject(getString(R.string.feedback_subject))
                .setText(getString(R.string.feedback_body))
                .startChooser()
        }

        private fun formatFileSize(size: Long): String {
            val units = arrayOf("B", "KB", "MB", "GB")
            var fileSize = size.toDouble()
            var unitIndex = 0
            while (fileSize > 1024 && unitIndex < units.size - 1) {
                fileSize /= 1024
                unitIndex++
            }
            return DecimalFormat("#.##").format(fileSize) + " " + units[unitIndex]
        }

        private fun showSnackbar(message: String) {
            view?.let {
                Snackbar.make(it, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
