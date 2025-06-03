/*
 * File: ReaderSettingsDialog.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 04:42:14 UTC
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
import com.yourapp.readers.databinding.DialogReaderSettingsBinding
import com.yourapp.readers.domain.models.ReaderSettings
import com.yourapp.readers.utils.setOnSafeClickListener
import com.yourapp.readers.utils.viewBinding

class ReaderSettingsDialog : DialogFragment() {

    private val binding by viewBinding(DialogReaderSettingsBinding::bind)
    private var currentSettings = ReaderSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle)
        
        arguments?.let {
            currentSettings = it.getParcelable(ARG_CURRENT_SETTINGS) ?: ReaderSettings()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_reader_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupInitialState()
        setupListeners()
        updatePreview()
    }

    private fun setupInitialState() {
        binding.apply {
            // Page transition
            when (currentSettings.pageTransition) {
                PageTransition.NONE -> rbTransitionNone
                PageTransition.SLIDE -> rbTransitionSlide
                PageTransition.CURL -> rbTransitionCurl
                PageTransition.FADE -> rbTransitionFade
            }.isChecked = true

            // Reading mode
            when (currentSettings.readingMode) {
                ReadingMode.CONTINUOUS -> rbModeContinuous
                ReadingMode.PAGED -> rbModePaged
            }.isChecked = true

            // Screen orientation
            when (currentSettings.screenOrientation) {
                ScreenOrientation.AUTO -> rbOrientationAuto
                ScreenOrientation.PORTRAIT -> rbOrientationPortrait
                ScreenOrientation.LANDSCAPE -> rbOrientationLandscape
            }.isChecked = true

            // Switches
            switchKeepScreenOn.isChecked = currentSettings.keepScreenOn
            switchFullscreen.isChecked = currentSettings.fullscreen
            switchShowProgress.isChecked = currentSettings.showProgress
            switchPageNumbers.isChecked = currentSettings.showPageNumbers

            // Brightness
            sliderBrightness.value = currentSettings.brightness
            tvBrightnessValue.text = getString(R.string.brightness_value, currentSettings.brightness)

            // Margins
            sliderMargins.value = currentSettings.margins.toFloat()
            tvMarginsValue.text = getString(R.string.margins_value, currentSettings.margins)
        }
    }

    private fun setupListeners() {
        binding.apply {
            // Page transition radio group
            rgTransition.setOnCheckedChangeListener { _, checkedId ->
                currentSettings = currentSettings.copy(
                    pageTransition = when (checkedId) {
                        R.id.rbTransitionNone -> PageTransition.NONE
                        R.id.rbTransitionSlide -> PageTransition.SLIDE
                        R.id.rbTransitionCurl -> PageTransition.CURL
                        R.id.rbTransitionFade -> PageTransition.FADE
                        else -> PageTransition.NONE
                    }
                )
                updatePreview()
            }

            // Reading mode radio group
            rgMode.setOnCheckedChangeListener { _, checkedId ->
                currentSettings = currentSettings.copy(
                    readingMode = when (checkedId) {
                        R.id.rbModeContinuous -> ReadingMode.CONTINUOUS
                        R.id.rbModePaged -> ReadingMode.PAGED
                        else -> ReadingMode.CONTINUOUS
                    }
                )
                updatePreview()
            }

            // Screen orientation radio group
            rgOrientation.setOnCheckedChangeListener { _, checkedId ->
                currentSettings = currentSettings.copy(
                    screenOrientation = when (checkedId) {
                        R.id.rbOrientationAuto -> ScreenOrientation.AUTO
                        R.id.rbOrientationPortrait -> ScreenOrientation.PORTRAIT
                        R.id.rbOrientationLandscape -> ScreenOrientation.LANDSCAPE
                        else -> ScreenOrientation.AUTO
                    }
                )
            }

            // Switches
            switchKeepScreenOn.setOnCheckedChangeListener { _, isChecked ->
                currentSettings = currentSettings.copy(keepScreenOn = isChecked)
            }

            switchFullscreen.setOnCheckedChangeListener { _, isChecked ->
                currentSettings = currentSettings.copy(fullscreen = isChecked)
                updatePreview()
            }

            switchShowProgress.setOnCheckedChangeListener { _, isChecked ->
                currentSettings = currentSettings.copy(showProgress = isChecked)
                updatePreview()
            }

            switchPageNumbers.setOnCheckedChangeListener { _, isChecked ->
                currentSettings = currentSettings.copy(showPageNumbers = isChecked)
                updatePreview()
            }

            // Sliders
            sliderBrightness.addOnChangeListener { _, value, _ ->
                currentSettings = currentSettings.copy(brightness = value)
                tvBrightnessValue.text = getString(R.string.brightness_value, value)
            }

            sliderMargins.addOnChangeListener { _, value, _ ->
                currentSettings = currentSettings.copy(margins = value.toInt())
                tvMarginsValue.text = getString(R.string.margins_value, value.toInt())
                updatePreview()
            }

            // Button actions
            btnApply.setOnSafeClickListener {
                setFragmentResult(REQUEST_KEY, createResultBundle())
                dismiss()
            }

            btnReset.setOnSafeClickListener {
                resetToDefaults()
            }

            btnCancel.setOnSafeClickListener {
                dismiss()
            }
        }
    }

    private fun updatePreview() {
        binding.previewContainer.apply {
            setPadding(
                currentSettings.margins,
                currentSettings.margins,
                currentSettings.margins,
                currentSettings.margins
            )
        }
    }

    private fun resetToDefaults() {
        currentSettings = ReaderSettings()
        setupInitialState()
        updatePreview()
    }

    private fun createResultBundle() = bundleOf(
        RESULT_SETTINGS to currentSettings
    )

    companion object {
        const val TAG = "ReaderSettingsDialog"
        const val REQUEST_KEY = "reader_settings_request"
        const val RESULT_SETTINGS = "result_settings"
        private const val ARG_CURRENT_SETTINGS = "current_settings"

        fun newInstance(currentSettings: ReaderSettings) = ReaderSettingsDialog().apply {
            arguments = bundleOf(ARG_CURRENT_SETTINGS to currentSettings)
        }
    }
}

enum class PageTransition {
    NONE,
    SLIDE,
    CURL,
    FADE
}

enum class ReadingMode {
    CONTINUOUS,
    PAGED
}

enum class ScreenOrientation {
    AUTO,
    PORTRAIT,
    LANDSCAPE
}

data class ReaderSettings(
    val pageTransition: PageTransition = PageTransition.SLIDE,
    val readingMode: ReadingMode = ReadingMode.CONTINUOUS,
    val screenOrientation: ScreenOrientation = ScreenOrientation.AUTO,
    val keepScreenOn: Boolean = false,
    val fullscreen: Boolean = false,
    val showProgress: Boolean = true,
    val showPageNumbers: Boolean = true,
    val brightness: Float = 1f,
    val margins: Int = DEFAULT_MARGINS
) : android.os.Parcelable {
    companion object {
        const val DEFAULT_MARGINS = 16
    }

    // Parcelable implementation
    constructor(parcel: android.os.Parcel) : this(
        PageTransition.valueOf(parcel.readString() ?: PageTransition.SLIDE.name),
        ReadingMode.valueOf(parcel.readString() ?: ReadingMode.CONTINUOUS.name),
        ScreenOrientation.valueOf(parcel.readString() ?: ScreenOrientation.AUTO.name),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readFloat(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeString(pageTransition.name)
        parcel.writeString(readingMode.name)
        parcel.writeString(screenOrientation.name)
        parcel.writeByte(if (keepScreenOn) 1 else 0)
        parcel.writeByte(if (fullscreen) 1 else 0)
        parcel.writeByte(if (showProgress) 1 else 0)
        parcel.writeByte(if (showPageNumbers) 1 else 0)
        parcel.writeFloat(brightness)
        parcel.writeInt(margins)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : android.os.Parcelable.Creator<ReaderSettings> {
        override fun createFromParcel(parcel: android.os.Parcel): ReaderSettings = ReaderSettings(parcel)
        override fun newArray(size: Int): Array<ReaderSettings?> = arrayOfNulls(size)
    }
}
