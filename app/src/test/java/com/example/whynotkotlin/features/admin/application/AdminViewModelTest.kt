package com.example.whynotkotlin.features.admin.application

import com.example.whynotkotlin.features.admin.fakes.FakeAdminRepository
import com.example.whynotkotlin.features.products.domain.Product
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModelTest {

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
    fun `BQ1 calculates all distribution buckets and BQ3 exposes backend metric`() =
        runTest(dispatcher) {

            /*
             * Counts chosen intentionally to verify every BQ1
             * boundary:
             *
             * 1, 5      -> 1-5
             * 6, 10     -> 6-10
             * 11, 20    -> 11-20
             * 21, 50    -> 21-50
             * 51        -> 51+
             */
            val counts = listOf(
                1,
                5,
                6,
                10,
                11,
                20,
                21,
                50,
                51
            )

            val products =
                counts.flatMapIndexed {
                        userIndex,
                        count ->

                    List(count) { productIndex ->

                        createProduct(
                            id =
                                "product-$userIndex-$productIndex",

                            ownerId =
                                "user-$userIndex"
                        )
                    }
                }

            val repository =
                FakeAdminRepository(
                    initialProducts = products,
                    initialRecommendedProductSaves =
                        12L
                )

            val viewModel =
                AdminViewModel(repository)

            advanceUntilIdle()

            val state = viewModel.state.value
            val stats =
                state.savedProductsStats

            assertFalse(state.loading)
            assertNull(state.errorMessage)

            assertEquals(
                175,
                stats.totalProducts
            )

            assertEquals(
                9,
                stats.activeUsers
            )

            assertEquals(
                175.0 / 9.0,
                stats.averagePerActiveUser,
                0.0001
            )

            assertEquals(
                1,
                stats.singleProductUsers
            )

            assertEquals(
                8,
                stats.multipleProductUsers
            )

            assertEquals(
                5,
                stats.distribution.size
            )

            assertEquals(
                "1–5",
                stats.distribution[0].label
            )

            assertEquals(
                2,
                stats.distribution[0].userCount
            )

            assertEquals(
                "6–10",
                stats.distribution[1].label
            )

            assertEquals(
                2,
                stats.distribution[1].userCount
            )

            assertEquals(
                "11–20",
                stats.distribution[2].label
            )

            assertEquals(
                2,
                stats.distribution[2].userCount
            )

            assertEquals(
                "21–50",
                stats.distribution[3].label
            )

            assertEquals(
                2,
                stats.distribution[3].userCount
            )

            assertEquals(
                "51+",
                stats.distribution[4].label
            )

            assertEquals(
                1,
                stats.distribution[4].userCount
            )

            // BQ3
            assertEquals(
                12L,
                state.recommendedProductsSaved
            )
        }

    @Test
    fun `BQ1 handles zero products without division by zero`() =
        runTest(dispatcher) {

            val repository =
                FakeAdminRepository()

            val viewModel =
                AdminViewModel(repository)

            advanceUntilIdle()

            val state = viewModel.state.value
            val stats =
                state.savedProductsStats

            assertFalse(state.loading)

            assertEquals(
                0,
                stats.totalProducts
            )

            assertEquals(
                0,
                stats.activeUsers
            )

            assertEquals(
                0.0,
                stats.averagePerActiveUser,
                0.0
            )

            assertEquals(
                0,
                stats.singleProductUsers
            )

            assertEquals(
                0,
                stats.multipleProductUsers
            )

            assertEquals(
                listOf(0, 0, 0, 0, 0),
                stats.distribution.map {
                    it.userCount
                }
            )
        }

    @Test
    fun `AdminViewModel reacts to live product and BQ3 metric changes`() =
        runTest(dispatcher) {

            val repository =
                FakeAdminRepository()

            val viewModel =
                AdminViewModel(repository)

            advanceUntilIdle()

            repository.products.value =
                listOf(
                    createProduct(
                        id = "product-1",
                        ownerId = "user-1"
                    ),
                    createProduct(
                        id = "product-2",
                        ownerId = "user-1"
                    )
                )

            repository
                .recommendedProductSaves
                .value = 7L

            advanceUntilIdle()

            val state = viewModel.state.value

            assertEquals(
                2,
                state.savedProductsStats
                    .totalProducts
            )

            assertEquals(
                1,
                state.savedProductsStats
                    .activeUsers
            )

            assertEquals(
                2.0,
                state.savedProductsStats
                    .averagePerActiveUser,
                0.0
            )

            assertEquals(
                7L,
                state.recommendedProductsSaved
            )
        }

    private fun createProduct(
        id: String,
        ownerId: String
    ): Product {

        return Product(
            id = id,
            ownerId = ownerId,
            wishlistId = "wishlist-$ownerId",
            categoryId = "tech",
            name = "Test product",
            brand = "Test brand",
            price = 100.0,
            imageUrl = "",
            productUrl = "",
            purchased = false,
            purchasedAt = null,
            createdAt = null,
            updatedAt = null
        )
    }
}