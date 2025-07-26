
package com.render.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReaderConfig(
    val fontSize: Float = 16f,
    val fontFamily: String = "default",
    val lineSpacing: Float = 1.2f,
    val theme: Theme = Theme.LIGHT,
    val readingMode: ReadingMode = ReadingMode.Scroll,
    val brightness: Float = -1f, // -1 means system brightness
    val keepScreenOn: Boolean = false,
    val showPageNumbers: Boolean = true,
    val showProgress: Boolean = true,
    val margins: Int = 16
) : Parcelable
