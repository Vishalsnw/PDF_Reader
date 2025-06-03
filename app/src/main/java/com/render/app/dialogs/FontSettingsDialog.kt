/*
 * File: FontSettingsDialog.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 04:37:08 UTC
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
import com.yourapp.readers.databinding.DialogFontSettingsBinding
import com.yourapp.readers.domain.models.FontSettings
import com.yourapp.readers.utils.setOnSafeClickListener
import com.yourapp.readers.utils.viewBinding

class FontSettingsDialog : DialogFragment() {

    private val binding by viewBinding(DialogFontSettingsBinding::bind)
    private var currentSettings = FontSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle)
        
        arguments?.let {
            currentSettings = it.getParcelable(ARG_CURRENT_SETTINGS) ?: FontSettings()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_font_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupInitialState()
        setupListeners()
        updatePreview()
    }

    private fun setupInitialState() {
        binding.apply {
            // Font size
            sliderFontSize.value = currentSettings.fontSize.toFloat()
            tvFontSizeValue.text = getString(R.string.font_size_value, currentSettings.fontSize)

            // Line spacing
            sliderLineSpacing.value = currentSettings.lineSpacing.toFloat()
            tvLineSpacingValue.text = getString(R.string.line_spacing_value, currentSettings.lineSpacing)

            // Font family
            when (currentSettings.fontFamily) {
                FontFamily.ROBOTO -> rbRoboto
                FontFamily.OPEN_SANS -> rbOpenSans
                FontFamily.LORA -> rbLora
                FontFamily.MERRIWEATHER -> rbMerriweather
            }.isChecked = true

            // Font weight
            switchBoldText.isChecked = currentSettings.isBold
        }
    }

    private fun setupListeners() {
        binding.apply {
            // Font size slider
            sliderFontSize.addOnChangeListener { _, value, _ ->
                currentSettings = currentSettings.copy(fontSize = value.toInt())
                tvFontSizeValue.text = getString(R.string.font_size_value, value.toInt())
                updatePreview()
            }

            // Line spacing slider
            sliderLineSpacing.addOnChangeListener { _, value, _ ->
                currentSettings = currentSettings.copy(lineSpacing = value.toInt())
                tvLineSpacingValue.text = getString(R.string.line_spacing_value, value.toInt())
                updatePreview()
            }

            // Font family radio group
            rgFontFamily.setOnCheckedChangeListener { _, checkedId ->
                currentSettings = currentSettings.copy(
                    fontFamily = when (checkedId) {
                        R.id.rbRoboto -> FontFamily.ROBOTO
                        R.id.rbOpenSans -> FontFamily.OPEN_SANS
                        R.id.rbLora -> FontFamily.LORA
                        R.id.rbMerriweather -> FontFamily.MERRIWEATHER
                        else -> FontFamily.ROBOTO
                    }
                )
                updatePreview()
            }

            // Bold text switch
            switchBoldText.setOnCheckedChangeListener { _, isChecked ->
                currentSettings = currentSettings.copy(isBold = isChecked)
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
        binding.tvPreview.apply {
            textSize = currentSettings.fontSize.toFloat()
            lineSpacing = currentSettings.lineSpacing.toFloat()
            typeface = resources.getFont(
                when (currentSettings.fontFamily) {
                    FontFamily.ROBOTO -> R.font.roboto
                    FontFamily.OPEN_SANS -> R.font.open_sans
                    FontFamily.LORA -> R.font.lora
                    FontFamily.MERRIWEATHER -> R.font.merriweather
                }
            )
            setTypeface(typeface, if (currentSettings.isBold) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        }
    }

    private fun resetToDefaults() {
        currentSettings = FontSettings()
        setupInitialState()
        updatePreview()
    }

    private fun createResultBundle() = bundleOf(
        RESULT_SETTINGS to currentSettings
    )

    companion object {
        const val TAG = "FontSettingsDialog"
        const val REQUEST_KEY = "font_settings_request"
        const val RESULT_SETTINGS = "result_settings"
        private const val ARG_CURRENT_SETTINGS = "current_settings"

        fun newInstance(currentSettings: FontSettings) = FontSettingsDialog().apply {
            arguments = bundleOf(ARG_CURRENT_SETTINGS to currentSettings)
        }
    }
}

data class FontSettings(
    val fontSize: Int = DEFAULT_FONT_SIZE,
    val lineSpacing: Int = DEFAULT_LINE_SPACING,
    val fontFamily: FontFamily = FontFamily.ROBOTO,
    val isBold: Boolean = false
) : android.os.Parcelable {
    companion object {
        const val DEFAULT_FONT_SIZE = 16
        const val DEFAULT_LINE_SPACING = 1
    }

    // Parcelable implementation
    constructor(parcel: android.os.Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        FontFamily.valueOf(parcel.readString() ?: FontFamily.ROBOTO.name),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeInt(fontSize)
        parcel.writeInt(lineSpacing)
        parcel.writeString(fontFamily.name)
        parcel.writeByte(if (isBold) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : android.os.Parcelable.Creator<FontSettings> {
        override fun createFromParcel(parcel: android.os.Parcel): FontSettings = FontSettings(parcel)
        override fun newArray(size: Int): Array<FontSettings?> = arrayOfNulls(size)
    }
}

enum class FontFamily {
    ROBOTO,
    OPEN_SANS,
    LORA,
    MERRIWEATHER
}
