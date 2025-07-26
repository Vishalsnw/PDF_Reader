/*
 * File: TimeUtils.kt
 * Created: 2025-06-02
 * Author: Vishalsnw
 * Last Modified: 2025-06-02 14:46:44 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.render.app.utils

import android.content.Context
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Utility class for time-related operations.
 * Features:
 * - Time formatting
 * - Duration handling
 * - Time conversions
 * - Reading time calculations
 */
object TimeUtils {

    private const val SECONDS_IN_MINUTE = 60
    private const val MINUTES_IN_HOUR = 60
    private const val HOURS_IN_DAY = 24
    private const val DAYS_IN_WEEK = 7

    /**
     * Time Formatting
     */
    fun formatTime(timeInMillis: Long, pattern: String = "HH:mm:ss"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timeInMillis))
    }

    fun formatDateTime(timeInMillis: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timeInMillis))
    }

    fun formatRelativeTime(context: Context, timeInMillis: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            timeInMillis,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }

    fun formatDuration(durationMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % MINUTES_IN_HOUR
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % SECONDS_IN_MINUTE

        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%02d:%02d", minutes, seconds)
            else -> String.format("00:%02d", seconds)
        }
    }

    /**
     * Duration Calculations
     */
    fun calculateDuration(startTime: Long, endTime: Long): Long {
        return endTime - startTime
    }

    fun calculateReadingTime(wordCount: Int, wordsPerMinute: Int = 250): Long {
        val minutes = (wordCount.toFloat() / wordsPerMinute).toInt()
        return TimeUnit.MINUTES.toMillis(minutes.toLong())
    }

    fun calculateRemainingTime(totalPages: Int, currentPage: Int, averageTimePerPage: Long): Long {
        val remainingPages = totalPages - currentPage
        return remainingPages * averageTimePerPage
    }

    /**
     * Time Conversions
     */
    fun millisecondsToMinutes(milliseconds: Long): Int {
        return TimeUnit.MILLISECONDS.toMinutes(milliseconds).toInt()
    }

    fun minutesToMilliseconds(minutes: Int): Long {
        return TimeUnit.MINUTES.toMillis(minutes.toLong())
    }

    fun hoursToMilliseconds(hours: Int): Long {
        return TimeUnit.HOURS.toMillis(hours.toLong())
    }

    fun daysToMilliseconds(days: Int): Long {
        return TimeUnit.DAYS.toMillis(days.toLong())
    }

    /**
     * Time Validation
     */
    fun isTimeValid(hour: Int, minute: Int, second: Int = 0): Boolean {
        return hour in 0..23 && minute in 0..59 && second in 0..59
    }

    fun isDateValid(year: Int, month: Int, day: Int): Boolean {
        return try {
            val calendar = Calendar.getInstance()
            calendar.isLenient = false
            calendar.set(year, month - 1, day)
            calendar.time
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Time Comparisons
     */
    fun isSameDay(time1: Long, time2: Long): Boolean {
        val calendar1 = Calendar.getInstance()
        val calendar2 = Calendar.getInstance()
        calendar1.timeInMillis = time1
        calendar2.timeInMillis = time2

        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }

    fun isToday(timeInMillis: Long): Boolean {
        return isSameDay(timeInMillis, System.currentTimeMillis())
    }

    fun isFuture(timeInMillis: Long): Boolean {
        return timeInMillis > System.currentTimeMillis()
    }

    /**
     * Reading Session Management
     */
    fun formatReadingSessionTime(timeInMillis: Long, context: Context): String {
        return when {
            timeInMillis < minutesToMilliseconds(1) -> {
                context.getString(R.string.less_than_minute)
            }
            timeInMillis < hoursToMilliseconds(1) -> {
                val minutes = millisecondsToMinutes(timeInMillis)
                context.resources.getQuantityString(
                    R.plurals.minutes_count,
                    minutes,
                    minutes
                )
            }
            else -> {
                val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
                val minutes = millisecondsToMinutes(timeInMillis) % MINUTES_IN_HOUR
                context.getString(R.string.hours_and_minutes, hours, minutes)
            }
        }
    }

    fun calculateAverageReadingSpeed(wordCount: Int, timeSpentMillis: Long): Int {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeSpentMillis).toInt()
        return if (minutes > 0) wordCount / minutes else 0
    }

    /**
     * Time Zones
     */
    fun convertToLocalTime(utcTimeMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = utcTimeMillis
        return calendar.timeInMillis + calendar.timeZone.getOffset(calendar.timeInMillis)
    }

    fun convertToUTC(localTimeMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = localTimeMillis
        return calendar.timeInMillis - calendar.timeZone.getOffset(calendar.timeInMillis)
    }

    /**
     * Constants
     */
    object Patterns {
        const val TIME_24H = "HH:mm:ss"
        const val TIME_12H = "hh:mm:ss a"
        const val DATE_ONLY = "yyyy-MM-dd"
        const val DATE_TIME = "yyyy-MM-dd HH:mm:ss"
        const val ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        const val READABLE_DATE = "MMMM dd, yyyy"
        const val READABLE_TIME = "hh:mm a"
    }

    object Constants {
        const val DEFAULT_WORDS_PER_MINUTE = 250
        const val MILLIS_PER_SECOND = 1000L
        const val SECONDS_PER_MINUTE = 60L
        const val MINUTES_PER_HOUR = 60L
        const val HOURS_PER_DAY = 24L
    }

    object TimeZones {
        const val UTC = "UTC"
        const val GMT = "GMT"
        const val EST = "America/New_York"
        const val PST = "America/Los_Angeles"
    }
}
