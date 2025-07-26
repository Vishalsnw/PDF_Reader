/*
 * File: DateUtils.kt
 * Created: 2025-06-02
 * Author: Vishalsnw
 * Last Modified: 2025-06-02 14:34:50 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.render.app.utils

import android.content.Context
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Utility class for handling date and time operations.
 * Features:
 * - Date formatting
 * - Time calculations
 * - Relative time
 * - Date parsing
 */
object DateUtils {

    private const val SECOND_MILLIS = 1000L
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS
    private const val WEEK_MILLIS = 7 * DAY_MILLIS
    private const val MONTH_MILLIS = 30L * DAY_MILLIS
    private const val YEAR_MILLIS = 365L * DAY_MILLIS

    /**
     * Date Formatting
     */
    fun formatDate(date: Date?, pattern: String = "MMM dd, yyyy"): String {
        return date?.let {
            SimpleDateFormat(pattern, Locale.getDefault()).format(it)
        } ?: ""
    }

    fun formatDateTime(date: Date?, pattern: String = "MMM dd, yyyy HH:mm"): String {
        return date?.let {
            SimpleDateFormat(pattern, Locale.getDefault()).format(it)
        } ?: ""
    }

    fun formatTime(date: Date?, is24Hour: Boolean = true): String {
        return date?.let {
            val pattern = if (is24Hour) "HH:mm" else "hh:mm a"
            SimpleDateFormat(pattern, Locale.getDefault()).format(it)
        } ?: ""
    }

    fun formatDuration(durationMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60

        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%02d:%02d", minutes, seconds)
            else -> String.format("00:%02d", seconds)
        }
    }

    /**
     * Relative Time
     */
    fun getRelativeTimeSpan(context: Context, date: Date?): String {
        if (date == null) return ""

        val now = System.currentTimeMillis()
        val time = date.time
        val diff = now - time

        return when {
            diff < MINUTE_MILLIS -> context.getString(R.string.just_now)
            diff < 2 * MINUTE_MILLIS -> context.getString(R.string.a_minute_ago)
            diff < 50 * MINUTE_MILLIS -> context.getString(R.string.minutes_ago, diff / MINUTE_MILLIS)
            diff < 90 * MINUTE_MILLIS -> context.getString(R.string.an_hour_ago)
            diff < 24 * HOUR_MILLIS -> context.getString(R.string.hours_ago, diff / HOUR_MILLIS)
            diff < 48 * HOUR_MILLIS -> context.getString(R.string.yesterday)
            diff < 7 * DAY_MILLIS -> context.getString(R.string.days_ago, diff / DAY_MILLIS)
            diff < 2 * WEEK_MILLIS -> context.getString(R.string.a_week_ago)
            diff < 4 * WEEK_MILLIS -> context.getString(R.string.weeks_ago, diff / WEEK_MILLIS)
            diff < 2 * MONTH_MILLIS -> context.getString(R.string.a_month_ago)
            diff < 12 * MONTH_MILLIS -> context.getString(R.string.months_ago, diff / MONTH_MILLIS)
            diff < 2 * YEAR_MILLIS -> context.getString(R.string.a_year_ago)
            else -> context.getString(R.string.years_ago, diff / YEAR_MILLIS)
        }
    }

    /**
     * Date Calculations
     */
    fun addDays(date: Date, days: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.time
    }

    fun addMonths(date: Date, months: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MONTH, months)
        return calendar.time
    }

    fun getDaysBetween(startDate: Date, endDate: Date): Int {
        val diff = endDate.time - startDate.time
        return (diff / DAY_MILLIS).toInt()
    }

    fun isToday(date: Date): Boolean {
        val calendar1 = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()
        calendar2.time = date
        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Date Parsing
     */
    fun parseDate(dateString: String, pattern: String): Date? {
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun parseDateTime(dateTimeString: String, pattern: String): Date? {
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).parse(dateTimeString)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Time Zone Handling
     */
    fun convertToLocalTime(utcDate: Date): Date {
        val tz = TimeZone.getDefault()
        val utcOffset = tz.getOffset(utcDate.time)
        return Date(utcDate.time + utcOffset)
    }

    fun convertToUTC(localDate: Date): Date {
        val tz = TimeZone.getDefault()
        val utcOffset = tz.getOffset(localDate.time)
        return Date(localDate.time - utcOffset)
    }

    /**
     * Reading Time Calculations
     */
    fun calculateReadingTime(wordCount: Int, wordsPerMinute: Int = 250): Int {
        return (wordCount.toFloat() / wordsPerMinute).toInt()
    }

    fun formatReadingTime(minutes: Int, context: Context): String {
        return when {
            minutes < 1 -> context.getString(R.string.less_than_minute)
            minutes == 1 -> context.getString(R.string.one_minute)
            minutes < 60 -> context.getString(R.string.x_minutes, minutes)
            minutes == 60 -> context.getString(R.string.one_hour)
            else -> {
                val hours = minutes / 60
                val remainingMinutes = minutes % 60
                when {
                    remainingMinutes == 0 -> context.getString(R.string.x_hours, hours)
                    hours == 1 -> context.getString(R.string.one_hour_x_minutes, remainingMinutes)
                    else -> context.getString(R.string.x_hours_x_minutes, hours, remainingMinutes)
                }
            }
        }
    }

    /**
     * Date Validation
     */
    fun isValidDate(year: Int, month: Int, day: Int): Boolean {
        return try {
            val calendar = Calendar.getInstance()
            calendar.setLenient(false)
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month - 1)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            calendar.time
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Common Date Patterns
     */
    object Pattern {
        const val DATE_ONLY = "yyyy-MM-dd"
        const val DATE_TIME = "yyyy-MM-dd HH:mm:ss"
        const val TIME_ONLY = "HH:mm:ss"
        const val DATE_TIME_MS = "yyyy-MM-dd HH:mm:ss.SSS"
        const val ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        const val READABLE_DATE = "MMMM dd, yyyy"
        const val READABLE_DATE_TIME = "MMMM dd, yyyy HH:mm"
        const val FILE_NAME_DATE = "yyyyMMdd_HHmmss"
    }
}
