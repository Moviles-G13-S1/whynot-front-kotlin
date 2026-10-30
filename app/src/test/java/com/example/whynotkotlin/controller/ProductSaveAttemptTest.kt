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
        override suspend fun deleteCurrentUser() = Unit
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

    private suspend fun ProductController.save(
        price: String = "120000",
        wishlistId: String = "w1",
        name: String = "Jacket",
        imageUrl: String = ""
    ) = createProduct(wishlistId, name, "Example", price, imageUrl, "")

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
        assertEquals("Enter a valid price, such as 1500 or 1.500,50", error?.message)
        assertTrue(products.created.isEmpty())
        assertEquals(listOf("uid" to ProductSaveFailure.INVALID_PRICE), products.attempts)
    }

    @Test fun `prices are read with thousands separators and decimals`() = runTest(dispatcher) {
        val products = Products()
        val controller = ProductController(Auth(), products, backgroundScope)

        controller.save(price = "1.000")
        controller.save(price = "1,000.50")
        controller.save(price = "1.500,50")

        assertEquals(listOf(1000.0, 1000.5, 1500.5), products.created.map { it.price })
    }

    @Test fun `NaN, Infinity and exponents are not prices`() = runTest(dispatcher) {
        val products = Products()
        val controller = ProductController(Auth(), products, backgroundScope)

        listOf("NaN", "Infinity", "1e9").forEach { price ->
            runCatching { controller.save(price = price) }
        }
        runCurrent()

        assertTrue(products.created.isEmpty())
        assertEquals(List(3) { "uid" to ProductSaveFailure.INVALID_PRICE }, products.attempts)
    }

    @Test fun `invalid text and links record their own reasons`() = runTest(dispatcher) {
        val products = Products()
        val controller = ProductController(Auth(), products, backgroundScope)

        runCatching { controller.save(name = "x".repeat(InputRules.PRODUCT_NAME_MAX_LENGTH + 1)) }
        runCatching { controller.save(imageUrl = "photo.png") }
        runCatching { controller.save(price = "-5") }
        runCurrent()

        assertTrue(products.created.isEmpty())
        assertEquals(
            listOf(
                "uid" to ProductSaveFailure.INVALID_NAME,
                "uid" to ProductSaveFailure.INVALID_LINK,
                "uid" to ProductSaveFailure.NEGATIVE_PRICE
            ),
            products.attempts
        )
    }

    @Test fun `a save the form stops itself is still recorded`() = runTest(dispatcher) {
        val products = Products()
        val controller = ProductController(Auth(), products, backgroundScope)

        controller.recordRejectedSave("w1", "", "Example", "120000", "", "")
        controller.recordRejectedSave("w1", "Jacket", "Example", "120000", "", "")
        runCurrent()

        assertTrue(products.created.isEmpty())
        assertEquals(listOf("uid" to ProductSaveFailure.MISSING_NAME), products.attempts)
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
