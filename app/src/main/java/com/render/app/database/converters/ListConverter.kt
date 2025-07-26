/*
 * File: ListConverter.kt
 * Created: 2025-06-01
 * Author: Vishalsnw
 * Last Modified: 2025-06-01 20:02:21 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * ListConverter provides Room type conversion for List objects.
 * Features:
 * - List to JSON string conversion
 * - JSON string to List conversion
 * - Support for multiple list types
 * - Null safety handling
 */
class ListConverter {
    private val gson = Gson()

    /**
     * Converts a List of Strings to JSON string
     * @param list The List<String> to convert
     * @return JSON string or null if list is null
     */
    @TypeConverter
    fun stringListToJson(list: List<String>?): String? {
        return list?.let { gson.toJson(it) }
    }

    /**
     * Converts a JSON string to List of Strings
     * @param json The JSON string to convert
     * @return List<String> or null if JSON is null
     */
    @TypeConverter
    fun jsonToStringList(json: String?): List<String>? {
        return json?.let {
            try {
                val listType: Type = object : TypeToken<List<String>>() {}.type
                gson.fromJson(it, listType)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Converts a List of Integers to JSON string
     * @param list The List<Int> to convert
     * @return JSON string or null if list is null
     */
    @TypeConverter
    fun intListToJson(list: List<Int>?): String? {
        return list?.let { gson.toJson(it) }
    }

    /**
     * Converts a JSON string to List of Integers
     * @param json The JSON string to convert
     * @return List<Int> or null if JSON is null
     */
    @TypeConverter
    fun jsonToIntList(json: String?): List<Int>? {
        return json?.let {
            try {
                val listType: Type = object : TypeToken<List<Int>>() {}.type
                gson.fromJson(it, listType)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Converts a List of Longs to JSON string
     * @param list The List<Long> to convert
     * @return JSON string or null if list is null
     */
    @TypeConverter
    fun longListToJson(list: List<Long>?): String? {
        return list?.let { gson.toJson(it) }
    }

    /**
     * Converts a JSON string to List of Longs
     * @param json The JSON string to convert
     * @return List<Long> or null if JSON is null
     */
    @TypeConverter
    fun jsonToLongList(json: String?): List<Long>? {
        return json?.let {
            try {
                val listType: Type = object : TypeToken<List<Long>>() {}.type
                gson.fromJson(it, listType)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Generic converter for List to JSON
     * @param list The List to convert
     * @return JSON string or null if list is null
     */
    inline fun <reified T> listToJson(list: List<T>?): String? {
        return list?.let { gson.toJson(it) }
    }

    /**
     * Generic converter for JSON to List
     * @param json The JSON string to convert
     * @return List<T> or null if JSON is null
     */
    inline fun <reified T> jsonToList(json: String?): List<T>? {
        return json?.let {
            try {
                val listType: Type = object : TypeToken<List<T>>() {}.type
                gson.fromJson(it, listType)
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Converts a Set to JSON string
     * @param set The Set to convert
     * @return JSON string or null if set is null
     */
    inline fun <reified T> setToJson(set: Set<T>?): String? {
        return set?.let { gson.toJson(it) }
    }

    /**
     * Converts a JSON string to Set
     * @param json The JSON string to convert
     * @return Set<T> or null if JSON is null
     */
    inline fun <reified T> jsonToSet(json: String?): Set<T>? {
        return json?.let {
            try {
                val setType: Type = object : TypeToken<Set<T>>() {}.type
                gson.fromJson(it, setType)
            } catch (e: Exception) {
                null
            }
        }
    }

    companion object {
        /**
         * Checks if a JSON string is a valid list
         * @param json The JSON string to validate
         * @return true if valid list JSON, false otherwise
         */
        fun isValidListJson(json: String?): Boolean {
            return try {
                json?.let {
                    it.trim().startsWith("[") && it.trim().endsWith("]")
                } ?: false
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Gets empty list if null
         * @param list The list to check
         * @return Original list or empty list if null
         */
        fun <T> List<T>?.orEmpty(): List<T> = this ?: emptyList()

        /**
         * Gets empty set if null
         * @param set The set to check
         * @return Original set or empty set if null
         */
        fun <T> Set<T>?.orEmpty(): Set<T> = this ?: emptySet()
    }
}
package com.render.app.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListConverter {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            Gson().fromJson<ArrayList<String>>(value, object : TypeToken<ArrayList<String>>() {}.type) ?: arrayListOf()
        } catch (e: Exception) {
            arrayListOf()
        }
    }
}
