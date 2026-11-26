package com.example.whynotkotlin.controller

import com.example.whynotkotlin.fakes.FakeConnectivityRepository
import com.example.whynotkotlin.fakes.FakeRecommendationRepository
import com.example.whynotkotlin.model.CachedRecommendation
import com.example.whynotkotlin.model.ProductRecommendation
import com.example.whynotkotlin.model.RecommendationCache
import com.example.whynotkotlin.model.repositories.RecommendationFileStore
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Sprint 3 local storage: the last recommendation survives an app restart. */
class RecommendationFileStoreTest {

    private class FakeFileStore : RecommendationFileStore {
        val files = mutableMapOf<String, CachedRecommendation>()
        var failing = false

        override suspend fun save(uid: String, recommendation: ProductRecommendation) {
            if (failing) error("disk full")
            files[uid] = CachedRecommendation(recommendation, storedAtMillis = 42L)
        }

        override suspend fun load(uid: String): CachedRecommendation? {
            if (failing) error("disk unreadable")
            return files[uid]
        }

        override suspend fun delete(uid: String) {
            files.remove(uid)
        }

        override suspend fun clearAll() {
            files.clear()
        }
    }

    private fun TestScope.controller(
        store: FakeFileStore,
        connectivity: FakeConnectivityRepository = FakeConnectivityRepository(),
        repository: FakeRecommendationRepository = FakeRecommendationRepository(recommendation("event-1")),
        // A new cache each time simulates the app being closed and opened again.
        cache: RecommendationCache = RecommendationCache(logger = {})
    ) = RecommendationController(
        repository, cache, connectivity, { "user-a" },
        fileStore = store, backgroundScope = backgroundScope
    )

    @Test fun `a recommendation from the backend is saved on disk`() = runTest {
        val store = FakeFileStore()

        controller(store).loadRecommendation()

        assertEquals("event-1", store.files["user-a"]?.recommendation?.recommendationEventId)
    }

    @Test fun `after a restart offline the saved one is shown as stale`() = runTest {
        val store = FakeFileStore()
        store.files["user-a"] = CachedRecommendation(recommendation("event-old"), 1L)

        val load = controller(store, FakeConnectivityRepository(initiallyOnline = false))
            .loadRecommendation()

        assertEquals("event-old", load.recommendation?.recommendationEventId)
        assertTrue(load.isStale)
    }

    @Test fun `a backend failure falls back to the file too`() = runTest {
        val store = FakeFileStore()
        store.files["user-a"] = CachedRecommendation(recommendation("event-old"), 1L)
        val repository = FakeRecommendationRepository(recommendation("event-1"))
            .apply { loadError = IllegalStateException("backend down") }

        val load = controller(store, repository = repository).loadRecommendation()

        assertEquals("event-old", load.recommendation?.recommendationEventId)
        assertTrue(load.isStale)
    }

    @Test fun `saving the recommendation deletes its file`() = runTest {
        val store = FakeFileStore()
        val controller = controller(store)
        val load = controller.loadRecommendation()

        controller.saveRecommendation(load.recommendation!!, wishlistId = "w1")

        assertNull(store.files["user-a"])
    }

    @Test fun `a profile change and a sign-out clear the files`() = runTest {
        val store = FakeFileStore()
        val controller = controller(store)
        controller.loadRecommendation()

        controller.onProfileUpdated("user-a")
        runCurrent()
        assertNull(store.files["user-a"])

        store.files["user-b"] = CachedRecommendation(recommendation("event-b"), 1L)
        controller.onSignedOut()
        runCurrent()
        assertTrue(store.files.isEmpty())
    }

    @Test fun `disk errors never break the recommendation`() = runTest {
        val store = FakeFileStore().apply { failing = true }

        val load = controller(store).loadRecommendation()

        assertEquals("event-1", load.recommendation?.recommendationEventId)
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
