/*
 * File: ColorCustomizationDialog.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 04:39:14 UTC
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
import com.google.android.material.color.MaterialColors
import com.yourapp.readers.R
import com.yourapp.readers.databinding.DialogColorCustomizationBinding
import com.yourapp.readers.domain.models.ColorScheme
import com.yourapp.readers.utils.setOnSafeClickListener
import com.yourapp.readers.utils.viewBinding
import com.github.dhaval2404.colorpicker.MaterialColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape

class ColorCustomizationDialog : DialogFragment() {

    private val binding by viewBinding(DialogColorCustomizationBinding::bind)
    private var currentScheme = ColorScheme()
    private var isCustomMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogStyle)
        
        arguments?.let {
            currentScheme = it.getParcelable(ARG_CURRENT_SCHEME) ?: ColorScheme()
            isCustomMode = it.getBoolean(ARG_CUSTOM_MODE, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_color_customization, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupInitialState()
        setupClickListeners()
        updatePreview()
    }

    private fun setupInitialState() {
        binding.apply {
            // Theme selection
            when (currentScheme.theme) {
                ColorTheme.LIGHT -> rbLight
                ColorTheme.DARK -> rbDark
                ColorTheme.SYSTEM -> rbSystem
                ColorTheme.CUSTOM -> rbCustom
            }.isChecked = true

            // Custom colors visibility
            groupCustomColors.visibility = if (isCustomMode) View.VISIBLE else View.GONE

            // Color preview buttons
            updateColorPreviews()
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            // Theme radio group
            rgTheme.setOnCheckedChangeListener { _, checkedId ->
                currentScheme = currentScheme.copy(
                    theme = when (checkedId) {
                        R.id.rbLight -> ColorTheme.LIGHT
                        R.id.rbDark -> ColorTheme.DARK
                        R.id.rbSystem -> ColorTheme.SYSTEM
                        R.id.rbCustom -> ColorTheme.CUSTOM
                        else -> ColorTheme.SYSTEM
                    }
                )
                isCustomMode = currentScheme.theme == ColorTheme.CUSTOM
                groupCustomColors.visibility = if (isCustomMode) View.VISIBLE else View.GONE
                updatePreview()
            }

            // Custom color pickers
            btnPrimaryColor.setOnSafeClickListener { showColorPicker(ColorType.PRIMARY) }
            btnSecondaryColor.setOnSafeClickListener { showColorPicker(ColorType.SECONDARY) }
            btnBackgroundColor.setOnSafeClickListener { showColorPicker(ColorType.BACKGROUND) }
            btnTextColor.setOnSafeClickListener { showColorPicker(ColorType.TEXT) }

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

    private fun showColorPicker(colorType: ColorType) {
        val currentColor = when (colorType) {
            ColorType.PRIMARY -> currentScheme.primaryColor
            ColorType.SECONDARY -> currentScheme.secondaryColor
            ColorType.BACKGROUND -> currentScheme.backgroundColor
            ColorType.TEXT -> currentScheme.textColor
        }

        MaterialColorPickerDialog
            .Builder(requireContext())
            .setTitle(getString(when (colorType) {
                ColorType.PRIMARY -> R.string.pick_primary_color
                ColorType.SECONDARY -> R.string.pick_secondary_color
                ColorType.BACKGROUND -> R.string.pick_background_color
                ColorType.TEXT -> R.string.pick_text_color
            }))
            .setDefaultColor(currentColor)
            .setColorShape(ColorShape.CIRCLE)
            .setColorListener { color, _ ->
                when (colorType) {
                    ColorType.PRIMARY -> currentScheme = currentScheme.copy(primaryColor = color)
                    ColorType.SECONDARY -> currentScheme = currentScheme.copy(secondaryColor = color)
                    ColorType.BACKGROUND -> currentScheme = currentScheme.copy(backgroundColor = color)
                    ColorType.TEXT -> currentScheme = currentScheme.copy(textColor = color)
                }
                updateColorPreviews()
                updatePreview()
            }
            .show()
    }

    private fun updateColorPreviews() {
        binding.apply {
            btnPrimaryColor.setBackgroundColor(currentScheme.primaryColor)
            btnSecondaryColor.setBackgroundColor(currentScheme.secondaryColor)
            btnBackgroundColor.setBackgroundColor(currentScheme.backgroundColor)
            btnTextColor.setBackgroundColor(currentScheme.textColor)
        }
    }

    private fun updatePreview() {
        binding.previewCard.apply {
            setCardBackgroundColor(currentScheme.backgroundColor)
            binding.tvPreview.setTextColor(currentScheme.textColor)
        }
    }

    private fun resetToDefaults() {
        currentScheme = ColorScheme()
        isCustomMode = false
        setupInitialState()
        updatePreview()
    }

    private fun createResultBundle() = bundleOf(
        RESULT_SCHEME to currentScheme
    )

    companion object {
        const val TAG = "ColorCustomizationDialog"
        const val REQUEST_KEY = "color_customization_request"
        const val RESULT_SCHEME = "result_scheme"
        private const val ARG_CURRENT_SCHEME = "current_scheme"
        private const val ARG_CUSTOM_MODE = "custom_mode"

        fun newInstance(
            currentScheme: ColorScheme,
            customMode: Boolean = false
        ) = ColorCustomizationDialog().apply {
            arguments = bundleOf(
                ARG_CURRENT_SCHEME to currentScheme,
                ARG_CUSTOM_MODE to customMode
            )
        }
    }
}

enum class ColorType {
    PRIMARY,
    SECONDARY,
    BACKGROUND,
    TEXT
}

enum class ColorTheme {
    LIGHT,
    DARK,
    SYSTEM,
    CUSTOM
}

data class ColorScheme(
    val theme: ColorTheme = ColorTheme.SYSTEM,
    val primaryColor: Int = DEFAULT_PRIMARY_COLOR,
    val secondaryColor: Int = DEFAULT_SECONDARY_COLOR,
    val backgroundColor: Int = DEFAULT_BACKGROUND_COLOR,
    val textColor: Int = DEFAULT_TEXT_COLOR
) : android.os.Parcelable {
    companion object {
        val DEFAULT_PRIMARY_COLOR = MaterialColors.getColor(android.content.Context(android.app.Application()), com.google.android.material.R.attr.colorPrimary, 0)
        val DEFAULT_SECONDARY_COLOR = MaterialColors.getColor(android.content.Context(android.app.Application()), com.google.android.material.R.attr.colorSecondary, 0)
        val DEFAULT_BACKGROUND_COLOR = MaterialColors.getColor(android.content.Context(android.app.Application()), android.R.attr.colorBackground, 0)
        val DEFAULT_TEXT_COLOR = MaterialColors.getColor(android.content.Context(android.app.Application()), android.R.attr.textColor, 0)
    }

    // Parcelable implementation
    constructor(parcel: android.os.Parcel) : this(
        ColorTheme.valueOf(parcel.readString() ?: ColorTheme.SYSTEM.name),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeString(theme.name)
        parcel.writeInt(primaryColor)
        parcel.writeInt(secondaryColor)
        parcel.writeInt(backgroundColor)
        parcel.writeInt(textColor)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : android.os.Parcelable.Creator<ColorScheme> {
        override fun createFromParcel(parcel: android.os.Parcel): ColorScheme = ColorScheme(parcel)
        override fun newArray(size: Int): Array<ColorScheme?> = arrayOfNulls(size)
    }
}
