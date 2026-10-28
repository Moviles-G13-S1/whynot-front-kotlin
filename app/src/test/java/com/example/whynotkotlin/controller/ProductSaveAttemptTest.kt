package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.*
import com.example.whynotkotlin.model.repositories.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class ProductSaveAttemptTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    private class Auth : AuthRepository {
        val user = AuthUser("uid", "user@example.com")
        override fun observeSession() = flowOf(user)
        override fun currentUser() = user
        override suspend fun signIn(email: String, password: String) = user
        override suspend fun signUp(email: String, password: String) = user
        override suspend fun signOut() = Unit
        override suspend fun refreshClaims() = user
        override suspend fun changePassword(currentPassword: String, newPassword: String) = Unit
    }

    private class Products : ProductRepository {
        val created = mutableListOf<ProductDraft>()
        val attempts = mutableListOf<Pair<String, ProductSaveFailure?>>()
        var createFailure: Exception? = null
        var recordFailure: Exception? = null

        override fun observeProducts(ownerId: String) = flowOf(emptyList<Product>())
        override fun observeWishlistProducts(ownerId: String, wishlistId: String) = flowOf(emptyList<Product>())
        override fun observeProduct(productId: String) = flowOf<Product?>(null)
        override suspend fun getProduct(productId: String): Product? = null
        override suspend fun createProduct(ownerId: String, draft: ProductDraft): String {
            createFailure?.let { throw it }
            created += draft
            return "product-id"
        }
        override suspend fun updateProduct(productId: String, update: ProductUpdate) = Unit
        override suspend fun markPurchased(productId: String) = Unit
        override suspend fun deleteProduct(productId: String) = Unit
        override suspend fun recordSaveAttempt(ownerId: String, failure: ProductSaveFailure?) {
            recordFailure?.let { throw it }
            attempts += ownerId to failure
        }
        override suspend fun recordPurchase(ownerId: String, product: Product, purchasePrice: Double) = Unit
    }

    private suspend fun ProductController.save(price: String = "120000", wishlistId: String = "w1") =
        createProduct(wishlistId, "Jacket", "Example", price, "", "")

    @Test fun `a saved product records a success`() = runTest(dispatcher) {
        val products = Products()
        val controller = ProductController(Auth(), products, backgroundScope)

        controller.save()
        runCurrent()

        assertEquals(1, products.created.size)
        assertEquals(listOf("uid" to null), products.attempts)
    }

    @Test fun `invalid input records its reason and saves nothing`() = runTest(dispatcher) {
        val products = Products()
        val controller = ProductController(Auth(), products, backgroundScope)

        val error = runCatching { controller.save(price = "abc") }.exceptionOrNull()
        runCurrent()

        assertTrue(error is ValidationException)
        assertEquals("Enter a valid price", error?.message)
        assertTrue(products.created.isEmpty())
        assertEquals(listOf("uid" to ProductSaveFailure.INVALID_PRICE), products.attempts)
    }

    @Test fun `a missing wishlist is recorded as its own reason`() = runTest(dispatcher) {
        val products = Products()
        val controller = ProductController(Auth(), products, backgroundScope)

        runCatching { controller.save(wishlistId = "") }
        runCurrent()

        assertEquals(listOf("uid" to ProductSaveFailure.MISSING_WISHLIST), products.attempts)
    }

    @Test fun `a backend rejection records a failure and still reaches the view`() = runTest(dispatcher) {
        val products = Products().apply { createFailure = IllegalStateException("PERMISSION_DENIED") }
        val controller = ProductController(Auth(), products, backgroundScope)

        val error = runCatching { controller.save() }.exceptionOrNull()
        runCurrent()

        assertEquals("PERMISSION_DENIED", error?.message)
        assertEquals(listOf("uid" to ProductSaveFailure.BACKEND_ERROR), products.attempts)
    }

    @Test fun `failing to record the attempt does not fail the save`() = runTest(dispatcher) {
        val products = Products().apply { recordFailure = IllegalStateException("offline") }
        val controller = ProductController(Auth(), products, backgroundScope)

        controller.save()
        runCurrent()

        assertEquals(1, products.created.size)
        assertTrue(products.attempts.isEmpty())
    }
}
