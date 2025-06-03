/*
 * File: FormatTests.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 05:26:02 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yourapp.readers.domain.models.*
import com.yourapp.readers.utils.FormatUtils
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.*

@RunWith(AndroidJUnit4::class)
class FormatTests {

    private lateinit var formatUtils: FormatUtils
    private val testLocale = Locale.US
    private val testTimeZone = TimeZone.getTimeZone("UTC")

    @Before
    fun setup() {
        formatUtils = FormatUtils(testLocale, testTimeZone)
    }

    @Test
    fun `test format file size`() {
        // Test bytes
        assertEquals("100 B", formatUtils.formatFileSize(100))
        
        // Test kilobytes
        assertEquals("1.0 KB", formatUtils.formatFileSize(1024))
        assertEquals("1.5 KB", formatUtils.formatFileSize(1536))
        
        // Test megabytes
        assertEquals("1.0 MB", formatUtils.formatFileSize(1024 * 1024))
        assertEquals("1.5 MB", formatUtils.formatFileSize(1536 * 1024))
        
        // Test gigabytes
        assertEquals("1.0 GB", formatUtils.formatFileSize(1024 * 1024 * 1024))
        assertEquals("1.5 GB", formatUtils.formatFileSize(1536 * 1024 * 1024L))
    }

    @Test
    fun `test format duration`() {
        // Test seconds
        assertEquals("0:30", formatUtils.formatDuration(30_000)) // 30 seconds
        assertEquals("0:59", formatUtils.formatDuration(59_000)) // 59 seconds
        
        // Test minutes
        assertEquals("1:00", formatUtils.formatDuration(60_000)) // 1 minute
        assertEquals("1:30", formatUtils.formatDuration(90_000)) // 1 minute 30 seconds
        
        // Test hours
        assertEquals("1:00:00", formatUtils.formatDuration(3_600_000)) // 1 hour
        assertEquals("1:30:00", formatUtils.formatDuration(5_400_000)) // 1 hour 30 minutes
        assertEquals("1:00:30", formatUtils.formatDuration(3_630_000)) // 1 hour 30 seconds
    }

    @Test
    fun `test format date`() {
        val calendar = Calendar.getInstance(testTimeZone, testLocale)
        calendar.set(2025, Calendar.JUNE, 3, 5, 26, 2)
        val timestamp = calendar.timeInMillis

        // Test short date
        assertEquals("Jun 3, 2025", formatUtils.formatShortDate(timestamp))
        
        // Test long date
        assertEquals("June 3, 2025", formatUtils.formatLongDate(timestamp))
        
        // Test date time
        assertEquals("Jun 3, 2025, 05:26", formatUtils.formatDateTime(timestamp))
        
        // Test relative date
        val now = System.currentTimeMillis()
        assertTrue(formatUtils.formatRelativeDate(now).contains("now"))
        assertTrue(formatUtils.formatRelativeDate(now - 60_000).contains("minute"))
        assertTrue(formatUtils.formatRelativeDate(now - 3_600_000).contains("hour"))
    }

    @Test
    fun `test format percentage`() {
        assertEquals("0%", formatUtils.formatPercentage(0f))
        assertEquals("50%", formatUtils.formatPercentage(0.5f))
        assertEquals("100%", formatUtils.formatPercentage(1f))
        assertEquals("33%", formatUtils.formatPercentage(0.333f))
        assertEquals("67%", formatUtils.formatPercentage(0.666f))
    }

    @Test
    fun `test format chapter number`() {
        assertEquals("Chapter 1", formatUtils.formatChapterNumber(1))
        assertEquals("Chapter 10", formatUtils.formatChapterNumber(10))
        assertEquals("Chapter 100", formatUtils.formatChapterNumber(100))
        assertEquals("Chapter 1000", formatUtils.formatChapterNumber(1000))
    }

    @Test
    fun `test format page number`() {
        assertEquals("Page 1 of 10", formatUtils.formatPageNumber(1, 10))
        assertEquals("Page 5 of 100", formatUtils.formatPageNumber(5, 100))
        assertEquals("Page 50 of 500", formatUtils.formatPageNumber(50, 500))
    }

    @Test
    fun `test format reading progress`() {
        // Test percentage progress
        assertEquals("0% Complete", formatUtils.formatReadingProgress(0f))
        assertEquals("50% Complete", formatUtils.formatReadingProgress(0.5f))
        assertEquals("100% Complete", formatUtils.formatReadingProgress(1f))

        // Test with pages
        assertEquals("Page 1 of 10 (10%)", formatUtils.formatReadingProgress(1, 10))
        assertEquals("Page 5 of 10 (50%)", formatUtils.formatReadingProgress(5, 10))
        assertEquals("Page 10 of 10 (100%)", formatUtils.formatReadingProgress(10, 10))
    }

    @Test
    fun `test format file name`() {
        assertEquals("test", formatUtils.formatFileName("test.pdf"))
        assertEquals("my document", formatUtils.formatFileName("my document.epub"))
        assertEquals("chapter 1", formatUtils.formatFileName("chapter 1.cbz"))
        assertEquals("test file", formatUtils.formatFileName("test_file.txt"))
    }

    @Test
    fun `test invalid inputs`() {
        // Test negative values
        assertEquals("0 B", formatUtils.formatFileSize(-1))
        assertEquals("0%", formatUtils.formatPercentage(-0.5f))
        assertEquals("Page 1 of 1", formatUtils.formatPageNumber(-1, -1))

        // Test edge cases
        assertEquals("0 B", formatUtils.formatFileSize(0))
        assertEquals("100%", formatUtils.formatPercentage(1.5f))
        assertEquals("unknown", formatUtils.formatFileName(""))
    }

    @Test
    fun `test format with different locales`() {
        val frenchFormatUtils = FormatUtils(Locale.FRANCE, testTimeZone)
        val germanFormatUtils = FormatUtils(Locale.GERMANY, testTimeZone)

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            .parse("2025-06-03 05:26:02")?.time ?: 0

        // Test date formats in different locales
        assertNotEquals(
            frenchFormatUtils.formatShortDate(timestamp),
            germanFormatUtils.formatShortDate(timestamp)
        )

        // Test number formats in different locales
        assertNotEquals(
            frenchFormatUtils.formatFileSize(1024),
            germanFormatUtils.formatFileSize(1024)
        )
    }
}
