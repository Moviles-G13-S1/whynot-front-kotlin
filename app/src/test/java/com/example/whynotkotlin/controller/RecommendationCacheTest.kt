package com.example.whynotkotlin.controller

import com.example.whynotkotlin.fakes.FakeConnectivityRepository
import com.example.whynotkotlin.fakes.FakeRecommendationRepository
import com.example.whynotkotlin.model.ProductRecommendation
import com.example.whynotkotlin.model.RecommendationCache
import com.example.whynotkotlin.model.RecommendationSaveResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RecommendationCacheTest {

    private var now = 1_000_000L
    private val logs = mutableListOf<String>()

    private fun cache(maxEntries: Int = 5, ttlMillis: Long = 1_000) =
        RecommendationCache(maxEntries, ttlMillis, clock = { now }, logger = { logs += it })

    private class CountingRepository(
        recommendation: ProductRecommendation? = recommendation("event-1")
    ) {
        val fake = FakeRecommendationRepository(recommendation = recommendation)
        var calls = 0
        val repository = object : com.example.whynotkotlin.model.repositories.RecommendationRepository {
            override suspend fun getRecommendation(): ProductRecommendation? {
                calls++
                return fake.getRecommendation()
            }

            override suspend fun saveRecommendedProduct(recommendationEventId: String, wishlistId: String) =
                fake.saveRecommendedProduct(recommendationEventId, wishlistId)
        }
    }

    private fun controller(
        counting: CountingRepository,
        cache: RecommendationCache,
        connectivity: FakeConnectivityRepository = FakeConnectivityRepository(),
        uid: () -> String? = { "user-a" }
    ) = RecommendationController(counting.repository, cache, connectivity, uid)

    // --- The cache on its own ------------------------------------------------

    @Test fun `a fresh entry is a hit and an absent one is a miss`() {
        val cache = cache()

        assertNull(cache.getFresh("user-a"))
        cache.put("user-a", recommendation("event-1"))

        assertEquals("event-1", cache.getFresh("user-a")?.recommendationEventId)
        assertEquals(1, cache.hits)
        assertEquals(1, cache.misses)
        assertTrue(logs.any { it.startsWith("HIT uid=user-a") })
    }

    @Test fun `an expired entry is not fresh but is still available as stale`() {
        val cache = cache(ttlMillis = 1_000)
        cache.put("user-a", recommendation("event-1"))

        now += 1_001

        assertNull(cache.getFresh("user-a"))
        assertEquals(1, cache.expired)
        assertEquals("event-1", cache.getStale("user-a")?.recommendation?.recommendationEventId)
    }

    @Test fun `the least recently used user is evicted beyond capacity`() {
        val cache = cache(maxEntries = 2)
        cache.put("user-a", recommendation("a"))
        cache.put("user-b", recommendation("b"))
        cache.getFresh("user-a") // user-a is now the most recently used

        cache.put("user-c", recommendation("c"))

        assertEquals(1, cache.evictions)
        assertNull(cache.getStale("user-b"))
        assertEquals("a", cache.getStale("user-a")?.recommendation?.recommendationEventId)
        assertTrue(logs.any { it.startsWith("EVICTED uid=user-b") })
    }

    @Test fun `clearing does not count as eviction`() {
        val cache = cache()
        cache.put("user-a", recommendation("a"))

        cache.clear(reason = "sign_out")

        assertNull(cache.getStale("user-a"))
        assertEquals(0, cache.evictions)
    }

    // --- Through the controller ---------------------------------------------

    @Test fun `a fresh hit does not call the backend`() = runTest {
        val counting = CountingRepository()
        val controller = controller(counting, cache())

        controller.loadRecommendation()
        val second = controller.loadRecommendation()

        assertEquals(1, counting.calls)
        assertTrue(second.fromCache)
        assertFalse(second.isStale)
    }

    @Test fun `a miss fetches and stores the recommendation`() = runTest {
        val counting = CountingRepository()
        val cache = cache()

        val load = controller(counting, cache).loadRecommendation()

        assertEquals(1, counting.calls)
        assertFalse(load.fromCache)
        assertEquals("event-1", cache.getStale("user-a")?.recommendation?.recommendationEventId)
    }

    @Test fun `an expired entry is fetched again`() = runTest {
        val counting = CountingRepository()
        val controller = controller(counting, cache(ttlMillis = 1_000))

        controller.loadRecommendation()
        now += 1_001
        controller.loadRecommendation()

        assertEquals(2, counting.calls)
    }

    @Test fun `force refresh skips a fresh entry`() = runTest {
        val counting = CountingRepository()
        val controller = controller(counting, cache())

        controller.loadRecommendation()
        controller.loadRecommendation(forceRefresh = true)

        assertEquals(2, counting.calls)
    }

    @Test fun `each user has their own cached recommendation`() = runTest {
        val counting = CountingRepository()
        val cache = cache()
        var uid = "user-a"
        val controller = controller(counting, cache, uid = { uid })

        controller.loadRecommendation()
        counting.fake.recommendation = recommendation("event-b")
        uid = "user-b"
        val forB = controller.loadRecommendation()

        assertEquals(2, counting.calls)
        assertEquals("event-b", forB.recommendation?.recommendationEventId)
        assertEquals("event-1", cache.getStale("user-a")?.recommendation?.recommendationEventId)
    }

    @Test fun `saving the recommendation invalidates it`() = runTest {
        val counting = CountingRepository()
        counting.fake.saveResult = RecommendationSaveResult(saved = true, alreadySaved = false, productId = "p")
        val cache = cache()
        val controller = controller(counting, cache)

        val load = controller.loadRecommendation()
        controller.saveRecommendation(load.recommendation!!, wishlistId = "w1")

        assertNull(cache.getStale("user-a"))
        assertTrue(logs.any { it.contains("reason=saved") })
    }

    @Test fun `signing out clears every cached recommendation`() = runTest {
        val counting = CountingRepository()
        val cache = cache()
        val controller = controller(counting, cache)

        controller.loadRecommendation()
        controller.onSignedOut()

        assertNull(cache.getStale("user-a"))
    }

    @Test fun `offline returns the cached recommendation marked stale`() = runTest {
        val counting = CountingRepository()
        val connectivity = FakeConnectivityRepository()
        val controller = controller(counting, cache(ttlMillis = 1_000), connectivity)

        controller.loadRecommendation()
        now += 1_001
        connectivity.goOffline()
        val offline = controller.loadRecommendation()

        assertEquals(1, counting.calls)
        assertTrue(offline.isStale)
        assertEquals("event-1", offline.recommendation?.recommendationEventId)
    }

    @Test fun `offline with nothing cached reports it`() = runTest {
        val controller = controller(
            CountingRepository(),
            cache(),
            FakeConnectivityRepository(initiallyOnline = false)
        )

        try {
            controller.loadRecommendation()
            fail("Expected the offline error")
        } catch (expected: RecommendationOfflineException) {
            // The view shows this message with a retry action.
        }
    }

    @Test fun `a backend failure falls back to the stale recommendation`() = runTest {
        val counting = CountingRepository()
        val controller = controller(counting, cache())

        controller.loadRecommendation()
        counting.fake.loadError = IllegalStateException("Backend down")
        val failed = controller.loadRecommendation(forceRefresh = true)

        assertTrue(failed.isStale)
        assertEquals("event-1", failed.recommendation?.recommendationEventId)
    }
}

private fun recommendation(eventId: String) = ProductRecommendation(
    recommendationEventId = eventId,
    name = "Headphones",
    brand = "Sony",
    price = 100_000.0,
    imageUrl = "",
    productUrl = "",
    categoryId = "tech",
    reason = "Similar users saved it"
)
