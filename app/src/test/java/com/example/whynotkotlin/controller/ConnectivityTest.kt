package com.example.whynotkotlin.controller

import com.example.whynotkotlin.fakes.FakeConnectivityRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityTest {

    @Test fun `a consumer sees the device go offline and come back online`() = runTest(UnconfinedTestDispatcher()) {
        val connectivity = FakeConnectivityRepository()
        val seen = mutableListOf<Boolean>()

        val job = launch { connectivity.isOnline.collect { seen += it } }

        connectivity.goOffline()
        connectivity.goOnline()
        job.cancel()

        assertEquals(listOf(true, false, true), seen)
    }

    @Test fun `a refresh waits for the connection to return`() = runTest(UnconfinedTestDispatcher()) {
        val connectivity = FakeConnectivityRepository(initiallyOnline = false)
        var refreshed = false

        // The pattern screens use to refresh stale data once back online.
        val job = launch {
            connectivity.isOnline.drop(1).filter { it }.first()
            refreshed = true
        }

        assertFalse(refreshed)
        connectivity.goOnline()

        assertTrue(refreshed)
        job.cancel()
    }
}
