package com.example.whynotkotlin.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * How long a shared stream keeps its backend listener after the last screen
 * stops watching it, so moving between screens does not reconnect.
 */
private const val STOP_TIMEOUT_MILLIS = 5_000L

/** Wraps every value in a success and turns a failure into a final element. */
internal fun <T> Flow<T>.asResults(): Flow<Result<T>> =
    map { Result.success(it) }
        .catch { error -> emit(Result.failure(error)) }

/**
 * Shares a stream between every screen that shows it.
 *
 * The value is null until the first element arrives, which is how a view
 * tells "still loading" apart from an empty result.
 */
internal fun <T> Flow<Result<T>?>.shareIn(scope: CoroutineScope): StateFlow<Result<T>?> =
    stateIn(scope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)
