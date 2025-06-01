/*
 * File: DateConverter.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 20:00:56 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.database.converters

import androidx.room.TypeConverter
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * DateConverter provides Room type conversion for Date objects.
 * Features:
 * - Date to timestamp conversion
 * - Timestamp to Date conversion
 * - ISO8601 string conversion
 * - UTC timezone handling
 */
class DateConverter {

    /**
     * Converts a Date object to Unix timestamp (milliseconds since epoch)
     * @param date The Date to convert
     * @return Long timestamp or null if date is null
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    /**
     * Converts a Unix timestamp to Date object
     * @param timestamp The timestamp in milliseconds since epoch
     * @return Date object or null if timestamp is null
     */
    @TypeConverter
    fun timestampToDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    /**
     * Converts a Date to ISO8601 string in UTC
     * @param date The Date to convert
     * @return ISO8601 formatted string or null if date is null
     */
    @TypeConverter
    fun dateToISOString(date: Date?): String? {
        return date?.let {
            ISO8601_FORMAT.format(it)
        }
    }

    /**
     * Converts an ISO8601 string to Date object
     * @param isoString The ISO8601 formatted string
     * @return Date object or null if string is null or invalid
     */
    @TypeConverter
    fun isoStringToDate(isoString: String?): Date? {
        return try {
            isoString?.let {
                ISO8601_FORMAT.parse(it)
            }
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private val ISO8601_FORMAT = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", 
            Locale.US
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        /**
         * Formats a date to display format
         * @param date The Date to format
         * @param pattern The date pattern to use
         * @return Formatted date string
         */
        fun formatDate(
            date: Date?,
            pattern: String = "yyyy-MM-dd HH:mm:ss"
        ): String {
            return date?.let {
                SimpleDateFormat(pattern, Locale.getDefault()).format(it)
            } ?: ""
        }

        /**
         * Parses a date string using the specified pattern
         * @param dateString The date string to parse
         * @param pattern The date pattern to use
         * @return Date object or null if parsing fails
         */
        fun parseDate(
            dateString: String?,
            pattern: String = "yyyy-MM-dd HH:mm:ss"
        ): Date? {
            return try {
                dateString?.let {
                    SimpleDateFormat(pattern, Locale.getDefault()).parse(it)
                }
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Gets current UTC date
         * @return Current date in UTC
         */
        fun getCurrentUtcDate(): Date {
            return Date(System.currentTimeMillis())
        }

        /**
         * Formats a date to UTC string
         * @param date The Date to format
         * @return UTC formatted string
         */
        fun toUtcString(date: Date?): String {
            return date?.let {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                sdf.format(it)
            } ?: ""
        }

        /**
         * Common date patterns
         */
        object Patterns {
            const val ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
            const val DATE_ONLY = "yyyy-MM-dd"
            const val TIME_ONLY = "HH:mm:ss"
            const val DATE_TIME = "yyyy-MM-dd HH:mm:ss"
            const val READABLE_DATE = "MMM dd, yyyy"
            const val READABLE_DATE_TIME = "MMM dd, yyyy HH:mm"
        }
    }
}
