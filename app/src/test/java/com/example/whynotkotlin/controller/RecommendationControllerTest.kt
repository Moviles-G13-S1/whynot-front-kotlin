package com.example.whynotkotlin.controller

import com.example.whynotkotlin.fakes.FakeConnectivityRepository
import com.example.whynotkotlin.fakes.FakeRecommendationRepository
import com.example.whynotkotlin.model.RecommendationCache
import com.example.whynotkotlin.model.ProductRecommendation
import com.example.whynotkotlin.model.RecommendationSaveResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RecommendationControllerTest {

    private fun controllerFor(repository: FakeRecommendationRepository) =
        RecommendationController(
            repository = repository,
            cache = RecommendationCache(logger = {}),
            connectivity = FakeConnectivityRepository(),
            currentUid = { null }
        )

    @Test
    fun `loadRecommendation returns the repository recommendation`() =
        runTest {

            val expectedRecommendation =
                recommendation(eventId = "event-123")

            val controller =
                controllerFor(
                    FakeRecommendationRepository(
                        recommendation = expectedRecommendation
                    )
                )

            assertEquals(
                expectedRecommendation,
                controller.loadRecommendation().recommendation
            )
        }

    @Test
    fun `saveRecommendation sends recommendation event and wishlist to repository`() =
        runTest {

            val recommendation =
                recommendation(eventId = "event-456")

            val repository =
                FakeRecommendationRepository(
                    recommendation = recommendation,
                    saveResult =
                        RecommendationSaveResult(
                            saved = true,
                            alreadySaved = false,
                            productId = "product-456"
                        )
                )

            val controller =
                controllerFor(repository)

            assertTrue(
                controller.saveRecommendation(
                    recommendation,
                    wishlistId = "wishlist-123"
                )
            )

            assertEquals(
                "event-456",
                repository.lastRecommendationEventId
            )

            assertEquals(
                "wishlist-123",
                repository.lastWishlistId
            )
        }

    @Test
    fun `already saved recommendation is considered saved`() =
        runTest {

            val recommendation =
                recommendation(eventId = "event-idempotent")

            val controller =
                controllerFor(
                    FakeRecommendationRepository(
                        recommendation = recommendation,
                        saveResult =
                            RecommendationSaveResult(
                                saved = false,
                                alreadySaved = true,
                                productId = "existing-product"
                            )
                    )
                )

            assertTrue(
                controller.saveRecommendation(recommendation, "wishlist-1")
            )
        }

    @Test
    fun `a save the backend did not perform is not considered saved`() =
        runTest {

            val recommendation =
                recommendation(eventId = "event-rejected")

            val controller =
                controllerFor(
                    FakeRecommendationRepository(
                        recommendation = recommendation,
                        saveResult =
                            RecommendationSaveResult(
                                saved = false,
                                alreadySaved = false,
                                productId = null
                            )
                    )
                )

            assertFalse(
                controller.saveRecommendation(recommendation, "wishlist-1")
            )
        }

    @Test
    fun `recommendation loading error reaches the caller`() =
        runTest {

            val controller =
                controllerFor(
                    FakeRecommendationRepository(
                        loadError =
                            IllegalStateException(
                                "Recommendation unavailable"
                            )
                    )
                )

            try {
                controller.loadRecommendation()
                fail("Expected the loading error")
            } catch (error: IllegalStateException) {
                assertEquals(
                    "Recommendation unavailable",
                    error.message
                )
            }
        }

    private fun recommendation(eventId: String) =
        ProductRecommendation(
            recommendationEventId = eventId,
            name = "Headphones",
            brand = "Test Brand",
            price = 120.0,
            imageUrl = "",
            productUrl = "https://example.com/product",
            categoryId = "tech",
            reason = "Recommended from similar users"
        )
}
