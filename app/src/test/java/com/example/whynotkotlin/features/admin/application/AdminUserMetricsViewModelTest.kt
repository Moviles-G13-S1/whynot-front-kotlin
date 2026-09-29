package com.example.whynotkotlin.features.admin.application

import com.example.whynotkotlin.features.admin.fakes.FakeAdminRepository
import com.example.whynotkotlin.features.products.domain.Product
import com.example.whynotkotlin.features.profile.domain.UserProfile
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
class AdminUserMetricsViewModelTest {

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
    fun `BQ2 and BQ5 calculate repeat and zero product users`() =
        runTest(dispatcher) {

            val repository = FakeAdminRepository(
                initialUsers = listOf(
                    createUser("user-1"),
                    createUser("user-2"),
                    createUser("user-3")
                ),
                initialProducts = listOf(
                    createProduct("product-1", "user-1"),
                    createProduct("product-2", "user-1"),
                    createProduct("product-3", "user-2")
                )
            )

            val viewModel =
                AdminUserMetricsViewModel(repository)

            advanceUntilIdle()

            val state = viewModel.state.value

            assertFalse(state.loading)
            assertNull(state.errorMessage)

            assertEquals(3, state.totalUsers)
            assertEquals(2, state.activeUsers)
            assertEquals(1, state.repeatSaverUsers)
            assertEquals(50.0, state.repeatSaverPercentage, 0.001)
            assertEquals(1, state.zeroProductUsers)
            assertEquals(100.0 / 3.0, state.zeroProductPercentage, 0.001)
        }

    private fun createUser(uid: String) =
        UserProfile(
            uid = uid,
            name = "Test User",
            email = "$uid@example.com",
            gender = "Other",
            age = 21,
            preferredCategoryId = "tech",
            cityId = "bogota",
            createdAt = null,
            updatedAt = null
        )

    private fun createProduct(
        id: String,
        ownerId: String
    ) = Product(
        id = id,
        ownerId = ownerId,
        wishlistId = "wishlist-$ownerId",
        categoryId = "tech",
        name = "Product",
        brand = "Brand",
        price = 10.0,
        imageUrl = "",
        productUrl = "",
        purchased = false,
        purchasedAt = null,
        createdAt = null,
        updatedAt = null
    )
}