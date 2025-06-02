/*
 * File: TextUtils.kt
 * Created: 2025-06-02
 * Author: Vishalsnw
 * Last Modified: 2025-06-02 14:42:21 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.utils

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import java.text.Normalizer
import java.util.*
import java.util.regex.Pattern

/**
 * Utility class for text processing and manipulation.
 * Features:
 * - Text formatting
 * - String manipulation
 * - Text analysis
 * - Text styling
 */
object TextUtils {

    private const val ELLIPSIS = "..."
    private val WORD_SEPARATORS = charArrayOf(' ', '\n', '\t', ',', '.', '!', '?', ';', ':')

    /**
     * Text Formatting
     */
    fun capitalize(text: String): String {
        if (text.isEmpty()) return text
        return text.substring(0, 1).uppercase(Locale.getDefault()) +
                text.substring(1).lowercase(Locale.getDefault())
    }

    fun titleCase(text: String): String {
        return text.split(" ").joinToString(" ") { word ->
            if (word.isNotEmpty()) capitalize(word) else word
        }
    }

    fun truncate(text: String, maxLength: Int, ellipsis: String = ELLIPSIS): String {
        if (text.length <= maxLength) return text
        return text.take(maxLength - ellipsis.length) + ellipsis
    }

    fun removeExtraSpaces(text: String): String {
        return text.replace("\\s+".toRegex(), " ").trim()
    }

    /**
     * String Manipulation
     */
    fun stripHtml(html: String): String {
        return html.replace(Regex("<[^>]*>"), "")
    }

    fun removeAccents(text: String): String {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(normalized).replaceAll("")
    }

    fun slugify(text: String): String {
        return removeAccents(text)
            .lowercase(Locale.getDefault())
            .replace("[^a-z0-9\\s-]".toRegex(), "")
            .replace("\\s+".toRegex(), "-")
            .replace("-+".toRegex(), "-")
            .trim('-')
    }

    fun extractDigits(text: String): String {
        return text.filter { it.isDigit() }
    }

    /**
     * Text Analysis
     */
    fun countWords(text: String): Int {
        if (text.isEmpty()) return 0
        return text.trim().split("\\s+".toRegex()).size
    }

    fun calculateReadingTime(text: String, wordsPerMinute: Int = 250): Int {
        val wordCount = countWords(text)
        return (wordCount.toFloat() / wordsPerMinute).toInt()
    }

    fun findSimilarity(first: String, second: String): Float {
        if (first == second) return 1.0f
        if (first.isEmpty() || second.isEmpty()) return 0.0f

        val longer = if (first.length > second.length) first else second
        val shorter = if (first.length > second.length) second else first

        val longerLength = longer.length
        if (longerLength == 0) return 1.0f

        return (longerLength - calculateLevenshteinDistance(longer, shorter)) /
                longerLength.toFloat()
    }

    /**
     * Text Styling
     */
    fun createSpannableString(text: String, vararg spans: TextSpan): SpannableString {
        val spannableString = SpannableString(text)
        spans.forEach { span ->
            spannableString.setSpan(
                span.what,
                span.start,
                span.end,
                span.flags
            )
        }
        return spannableString
    }

    fun highlight(text: String, query: String, color: Int): SpannableString {
        val spannable = SpannableString(text)
        if (query.isEmpty()) return spannable

        var startIndex = text.lowercase(Locale.getDefault())
            .indexOf(query.lowercase(Locale.getDefault()))
        while (startIndex >= 0) {
            val endIndex = startIndex + query.length
            spannable.setSpan(
                BackgroundColorSpan(color),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            startIndex = text.lowercase(Locale.getDefault())
                .indexOf(query.lowercase(Locale.getDefault()), endIndex)
        }
        return spannable
    }

    /**
     * Helper Functions
     */
    private fun calculateLevenshteinDistance(s1: String, s2: String): Int {
        val costs = IntArray(s2.length + 1)
        for (i in 0..s2.length) costs[i] = i
        
        var s1Prev = 0
        var s1Curr: Int
        
        for (i in 0 until s1.length) {
            costs[0] = i + 1
            var s2Prev = costs[0]
            
            for (j in 0 until s2.length) {
                s1Curr = s1Prev
                s1Prev = costs[j + 1]
                
                costs[j + 1] = minOf(
                    s1Prev + 1,
                    costs[j] + 1,
                    s1Curr + if (s1[i] != s2[j]) 1 else 0
                )
            }
        }
        
        return costs[s2.length]
    }

    /**
     * Data Classes
     */
    data class TextSpan(
        val what: Any,
        val start: Int,
        val end: Int,
        val flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    /**
     * Extension Functions
     */
    fun SpannableStringBuilder.appendStyled(
        text: CharSequence,
        vararg spans: Any
    ): SpannableStringBuilder {
        val start = length
        append(text)
        spans.forEach { span ->
            setSpan(span, start, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return this
    }

    /**
     * Common Spans
     */
    object Spans {
        fun bold() = StyleSpan(Typeface.BOLD)
        fun italic() = StyleSpan(Typeface.ITALIC)
        fun underline() = UnderlineSpan()
        fun color(color: Int) = ForegroundColorSpan(color)
        fun background(color: Int) = BackgroundColorSpan(color)
        fun size(size: Int) = AbsoluteSizeSpan(size)
        fun strike() = StrikethroughSpan()
        fun superscript() = SuperscriptSpan()
        fun subscript() = SubscriptSpan()
    }

    /**
     * Constants
     */
    object Constants {
        const val DEFAULT_TRUNCATE_LENGTH = 100
        const val DEFAULT_WORDS_PER_MINUTE = 250
        const val SIMILARITY_THRESHOLD = 0.85f
    }
}
