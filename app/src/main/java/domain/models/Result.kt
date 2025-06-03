/*
 * File: Result.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 14:21:16 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.domain.models

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(
        val exception: Throwable,
        val message: String = exception.localizedMessage ?: "An unexpected error occurred",
        val code: Int = -1
    ) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        
        fun error(
            exception: Throwable,
            message: String = exception.localizedMessage ?: "An unexpected error occurred",
            code: Int = -1
        ): Result<Nothing> = Error(exception, message, code)

        fun <T> loading(): Result<T> = Loading
    }

    /**
     * Returns true if this instance represents a successful outcome.
     */
    val isSuccess: Boolean get() = this is Success

    /**
     * Returns true if this instance represents a loading state.
     */
    val isLoading: Boolean get() = this is Loading

    /**
     * Returns true if this instance represents an error outcome.
     */
    val isError: Boolean get() = this is Error

    /**
     * Returns the encapsulated data if this instance represents [Success] or null otherwise.
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Returns the encapsulated data if this instance represents [Success] or the result of [defaultValue] otherwise.
     */
    fun getOrDefault(defaultValue: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> defaultValue
    }

    /**
     * Returns the encapsulated data if this instance represents [Success] or throws the encapsulated [Throwable] otherwise.
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is in Loading state")
    }

    /**
     * Performs the given [action] on the encapsulated data if this instance represents [Success].
     */
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Performs the given [action] on the encapsulated [Throwable] if this instance represents [Error].
     */
    inline fun onError(action: (Error) -> Unit): Result<T> {
        if (this is Error) action(this)
        return this
    }

    /**
     * Performs the given [action] if this instance represents [Loading].
     */
    inline fun onLoading(action: () -> Unit): Result<T> {
        if (this is Loading) action()
        return this
    }

    /**
     * Maps the encapsulated value of a [Success] instance to a new value using the given [transform] function.
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> Loading
    }

    /**
     * Maps the encapsulated value of a [Success] instance to a new [Result] using the given [transform] function.
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> Loading
    }

    /**
     * Returns the result of [onSuccess] for [Success], [onError] for [Error], and [onLoading] for [Loading].
     */
    inline fun <R> fold(
        onSuccess: (T) -> R,
        onError: (Error) -> R,
        onLoading: () -> R
    ): R = when (this) {
        is Success -> onSuccess(data)
        is Error -> onError(this)
        is Loading -> onLoading()
    }
}

/**
 * Extension function to wrap a suspending operation in a Result
 */
suspend fun <T> resultOf(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: Exception) {
    Result.error(e)
}

/**
 * Extension function to convert a nullable value to a Result
 */
fun <T> T?.toResult(errorMessage: String = "Value is null"): Result<T> = when (this) {
    null -> Result.error(NullPointerException(errorMessage))
    else -> Result.success(this)
}

/**
 * Extension function to combine multiple Results
 */
fun <T> List<Result<T>>.combine(): Result<List<T>> {
    val successes = mutableListOf<T>()
    for (result in this) {
        when (result) {
            is Result.Success -> successes.add(result.data)
            is Result.Error -> return result
            is Result.Loading -> return Result.Loading
        }
    }
    return Result.success(successes)
}

/**
 * Extension function to retry an operation with exponential backoff
 */
suspend fun <T> retry(
    times: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    block: suspend () -> T
): Result<T> {
    var currentDelay = initialDelay
    repeat(times) { attempt ->
        when (val result = resultOf { block() }) {
            is Result.Success -> return result
            is Result.Error -> {
                if (attempt == times - 1) return result
                kotlinx.coroutines.delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
            is Result.Loading -> return result
        }
    }
    return Result.error(
        Exception("Failed after $times attempts"),
        "Operation failed after $times attempts"
    )
}
