package com.example.whynotkotlin.features.recommendations.application

import com.example.whynotkotlin.features.recommendations.domain.ProductRecommendation
import com.example.whynotkotlin.features.recommendations.domain.RecommendationSaveResult
import com.example.whynotkotlin.features.recommendations.fakes.FakeRecommendationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecommendationViewModelTest {

    private lateinit var dispatcher: TestDispatcher

    @Before
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadRecommendation exposes recommendation in UI state`() =
        runTest(dispatcher) {

            val expectedRecommendation =
                ProductRecommendation(
                    recommendationEventId =
                        "event-123",
                    name = "Headphones",
                    brand = "Test Brand",
                    price = 120.0,
                    imageUrl = "",
                    productUrl =
                        "https://example.com/product",
                    categoryId = "tech",
                    reason =
                        "Recommended from similar users"
                )

            val repository =
                FakeRecommendationRepository(
                    recommendation =
                        expectedRecommendation
                )

            val viewModel =
                RecommendationViewModel(
                    repository
                )

            viewModel.loadRecommendation()

            advanceUntilIdle()

            val state = viewModel.state.value

            assertFalse(state.loading)
            assertFalse(state.saving)
            assertFalse(state.saved)
            assertNull(state.errorMessage)

            assertEquals(
                expectedRecommendation,
                state.recommendation
            )
        }

    @Test
    fun `saveRecommendation sends recommendation event and wishlist to repository`() =
        runTest(dispatcher) {

            val recommendation =
                ProductRecommendation(
                    recommendationEventId =
                        "event-456",
                    name = "Keyboard",
                    brand = "Test Brand",
                    price = 200.0,
                    imageUrl = "",
                    productUrl =
                        "https://example.com/keyboard",
                    categoryId = "tech",
                    reason =
                        "A similar user saved this product"
                )

            val repository =
                FakeRecommendationRepository(
                    recommendation =
                        recommendation,
                    saveResult =
                        RecommendationSaveResult(
                            saved = true,
                            alreadySaved = false,
                            productId =
                                "product-456"
                        )
                )

            val viewModel =
                RecommendationViewModel(
                    repository
                )

            viewModel.loadRecommendation()
            advanceUntilIdle()

            viewModel.saveRecommendation(
                wishlistId = "wishlist-123"
            )

            advanceUntilIdle()

            val state = viewModel.state.value

            assertFalse(state.saving)
            assertTrue(state.saved)
            assertNull(state.errorMessage)

            assertEquals(
                "event-456",
                repository
                    .lastRecommendationEventId
            )

            assertEquals(
                "wishlist-123",
                repository.lastWishlistId
            )
        }

    @Test
    fun `already saved recommendation is considered saved`() =
        runTest(dispatcher) {

            val recommendation =
                ProductRecommendation(
                    recommendationEventId =
                        "event-idempotent",
                    name = "Mouse",
                    brand = "Test Brand",
                    price = 80.0,
                    imageUrl = "",
                    productUrl =
                        "https://example.com/mouse",
                    categoryId = "tech",
                    reason =
                        "Recommended"
                )

            val repository =
                FakeRecommendationRepository(
                    recommendation =
                        recommendation,
                    saveResult =
                        RecommendationSaveResult(
                            saved = true,
                            alreadySaved = true,
                            productId =
                                "existing-product"
                        )
                )

            val viewModel =
                RecommendationViewModel(
                    repository
                )

            viewModel.loadRecommendation()
            advanceUntilIdle()

            viewModel.saveRecommendation(
                "wishlist-1"
            )

            advanceUntilIdle()

            assertTrue(
                viewModel.state.value.saved
            )
        }

    @Test
    fun `recommendation loading error is exposed in UI state`() =
        runTest(dispatcher) {

            val repository =
                FakeRecommendationRepository(
                    loadError =
                        IllegalStateException(
                            "Recommendation unavailable"
                        )
                )

            val viewModel =
                RecommendationViewModel(
                    repository
                )

            viewModel.loadRecommendation()

            advanceUntilIdle()

            val state = viewModel.state.value

            assertFalse(state.loading)
            assertNull(state.recommendation)

            assertEquals(
                "Recommendation unavailable",
                state.errorMessage
            )
        }
}