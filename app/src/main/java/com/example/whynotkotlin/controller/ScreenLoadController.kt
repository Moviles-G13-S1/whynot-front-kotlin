package com.example.whynotkotlin.controller

import android.os.Build
import android.os.SystemClock
import android.os.Trace
import android.util.Log
import com.example.whynotkotlin.model.repositories.ConnectivityRepository
import com.example.whynotkotlin.model.repositories.ScreenLoadRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Sprint 4 BQ (Miguel, Type 1): measures how long a screen takes to load and
 * records it in `screenLoadEvents`.
 *
 * Home's load is "opening Home until its nearby store and recommendation have
 * finished loading" (both run in parallel in Home's `loadHome`).
 *
 * - The clock is [SystemClock.elapsedRealtime]: monotonic, so changing the
 *   phone's time cannot produce a negative or huge duration.
 * - Each load is also an async section in System Trace (`WhyNot#homeLoad`), so
 *   the Android Studio Profiler shows the same interval the BQ measures.
 * - Only loads that started online are recorded: offline, Home skips the
 *   network and would distort the average. If the connection drops before the
 *   write, Firestore queues it and sends it later (eventual connectivity).
 * - Recording is fire-and-forget in [backgroundScope]: Home never waits for it.
 */
class ScreenLoadController(
    private val repository: ScreenLoadRepository?,
    private val currentUid: () -> String?,
    private val connectivity: ConnectivityRepository?,
    private val backgroundScope: CoroutineScope,
    private val clock: () -> Long = SystemClock::elapsedRealtime,
    private val tracer: LoadTracer = SystemTracer,
    private val logger: (String) -> Unit = { Log.d(TAG, it) }
) {

    /** One load in progress. */
    class Measurement internal constructor(
        val screen: String,
        internal val startedAt: Long,
        internal val startedOnline: Boolean,
        internal val cookie: Int
    )

    private var nextCookie = 0

    fun start(screen: String): Measurement {
        val cookie = synchronized(this) { nextCookie++ }
        tracer.begin(sectionName(screen), cookie)
        return Measurement(
            screen = screen,
            startedAt = clock(),
            startedOnline = connectivity?.isOnline?.value ?: true,
            cookie = cookie
        )
    }

    /** Ends [measurement]; returns the duration in milliseconds. */
    fun finish(measurement: Measurement): Long {
        val durationMs = (clock() - measurement.startedAt).coerceAtLeast(0)
        tracer.end(sectionName(measurement.screen), measurement.cookie)
        logger("${measurement.screen} loaded in ${durationMs}ms online=${measurement.startedOnline}")

        val uid = currentUid()
        if (uid != null && measurement.startedOnline && durationMs in 1..MAX_DURATION_MS) {
            backgroundScope.launch {
                try {
                    repository?.recordScreenLoad(uid, measurement.screen, durationMs)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    // Analytics only: losing one measurement never affects Home.
                }
            }
        }
        return durationMs
    }

    /** The load was abandoned (the user left the screen): close the trace, record nothing. */
    fun cancel(measurement: Measurement) {
        tracer.end(sectionName(measurement.screen), measurement.cookie)
    }

    private fun sectionName(screen: String) = "WhyNot#${screen}Load"

    /** System Trace sections; replaced by a no-op in unit tests. */
    interface LoadTracer {
        fun begin(name: String, cookie: Int)
        fun end(name: String, cookie: Int)
    }

    private object SystemTracer : LoadTracer {
        override fun begin(name: String, cookie: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Trace.beginAsyncSection(name, cookie)
        }

        override fun end(name: String, cookie: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) Trace.endAsyncSection(name, cookie)
        }
    }

    companion object {
        const val TAG = "WhyNotScreenLoad"
        const val HOME = "home"

        /** Matches the rules: a load over two minutes is a measurement error. */
        const val MAX_DURATION_MS = 120_000L
    }
}
