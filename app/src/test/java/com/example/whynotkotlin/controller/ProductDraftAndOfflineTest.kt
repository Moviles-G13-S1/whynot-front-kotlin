package com.example.whynotkotlin.controller

import com.example.whynotkotlin.fakes.FakeConnectivityRepository
import com.example.whynotkotlin.fakes.FakeProductDraftRepository
import com.example.whynotkotlin.model.*
import com.example.whynotkotlin.model.repositories.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Sprint 3, Miguel: local storage (product drafts), eventual connectivity for
 * products, and the purchase price recorded for the money-saved BQ.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductDraftAndOfflineTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    private class Auth(var uid: String = "uid") : AuthRepository {
        private val user get() = AuthUser(uid, "$uid@example.com")
        override fun observeSession() = flowOf<AuthUser?>(user)
        override fun currentUser() = user
        override suspend fun signIn(email: String, password: String) = user
        override suspend fun signUp(email: String, password: String) = user
        override suspend fun signOut() = Unit
        override suspend fun refreshClaims() = user
        override suspend fun changePassword(currentPassword: String, newPassword: String) = Unit
    }

    private class Products : ProductRepository {
        val created = mutableListOf<ProductDraft>()
        val attempts = mutableListOf<ProductSaveFailure?>()
        val purchased = mutableListOf<String>()
        val recordedPurchases = mutableListOf<Triple<String, String, Double>>()
        val updated = mutableListOf<String>()
        val deleted = mutableListOf<String>()

        /** When set, markPurchased waits on it, like a write with no connection. */
        var purchaseAck: CompletableDeferred<Unit>? = null

        override fun observeProducts(ownerId: String) = flowOf(emptyList<Product>())
        override fun observeWishlistProducts(ownerId: String, wishlistId: String) = flowOf(emptyList<Product>())
        override fun observeProduct(productId: String) = flowOf<Product?>(null)
        override suspend fun getProduct(productId: String): Product? = null
        override suspend fun createProduct(ownerId: String, draft: ProductDraft): String {
            created += draft
            return "product-id"
        }
        override suspend fun updateProduct(productId: String, update: ProductUpdate) {
            updated += productId
        }
        override suspend fun markPurchased(productId: String) {
            purchased += productId
            purchaseAck?.await()
        }
        override suspend fun deleteProduct(productId: String) {
            deleted += productId
        }
        override suspend fun recordSaveAttempt(ownerId: String, failure: ProductSaveFailure?) {
            attempts += failure
        }
        override suspend fun recordPurchase(ownerId: String, product: Product, purchasePrice: Double) {
            recordedPurchases += Triple(ownerId, product.id, purchasePrice)
        }
    }

    private fun product(price: Double = 120000.0, purchased: Boolean = false) = Product(
        id = "p1", ownerId = "uid", wishlistId = "w1", categoryId = "tech",
        name = "Jacket", brand = "Example", price = price, imageUrl = "", productUrl = "",
        purchased = purchased, purchasedAt = null, createdAt = null, updatedAt = null
    )

    private val draft = ProductFormDraft(wishlistId = "w1", name = "Jacket", brand = "Example", price = "12,5")

    private fun TestScope.controller(
        products: Products = Products(),
        drafts: FakeProductDraftRepository = FakeProductDraftRepository(),
        connectivity: FakeConnectivityRepository = FakeConnectivityRepository(),
        auth: Auth = Auth()
    ) = ProductController(auth, products, backgroundScope, drafts, connectivity)

    /**
     * Lets the controller's background work run: debounced draft writes and
     * the purchase event. advanceUntilIdle() would skip them, since they run
     * in backgroundScope like they run in the app-wide scope.
     */
    private fun TestScope.settle() {
        advanceTimeBy(ProductController.DRAFT_SAVE_DELAY_MILLIS + 1)
        runCurrent()
    }

    private suspend fun ProductController.save() =
        createProduct("w1", "Jacket", "Example", "120000", "", "")

    // --- Local storage: drafts ------------------------------------------------

    @Test fun `typing is written to disk once after a pause`() = runTest(dispatcher) {
        val drafts = FakeProductDraftRepository()
        val controller = controller(drafts = drafts)

        controller.saveDraft(draft.copy(name = "J"))
        controller.saveDraft(draft.copy(name = "Ja"))
        controller.saveDraft(draft)
        advanceTimeBy(ProductController.DRAFT_SAVE_DELAY_MILLIS + 1)

        assertEquals(1, drafts.writes)
        assertEquals(draft, drafts.drafts["uid"])
    }

    @Test fun `an emptied form removes the draft`() = runTest(dispatcher) {
        val drafts = FakeProductDraftRepository().apply { drafts["uid"] = draft }
        val controller = controller(drafts = drafts)

        controller.saveDraft(ProductFormDraft())
        settle()

        assertNull(drafts.drafts["uid"])
    }

    @Test fun `each user only gets back their own draft`() = runTest(dispatcher) {
        val drafts = FakeProductDraftRepository().apply { drafts["uid"] = draft }
        val auth = Auth()
        val controller = controller(drafts = drafts, auth = auth)

        assertEquals(draft, controller.loadDraft())

        auth.uid = "someone-else"
        assertNull(controller.loadDraft())
    }

    @Test fun `saving the product clears the draft, even a write still pending`() = runTest(dispatcher) {
        val drafts = FakeProductDraftRepository().apply { drafts["uid"] = draft }
        val controller = controller(drafts = drafts)

        controller.saveDraft(draft.copy(name = "Jacket 2"))
        controller.save()
        settle()

        assertNull(drafts.drafts["uid"])
    }

    @Test fun `discarding removes the draft`() = runTest(dispatcher) {
        val drafts = FakeProductDraftRepository().apply { drafts["uid"] = draft }
        val controller = controller(drafts = drafts)

        controller.discardDraft()
        settle()

        assertNull(drafts.drafts["uid"])
    }

    // --- Eventual connectivity --------------------------------------------------

    @Test fun `saving offline sends nothing, keeps the draft and records why`() = runTest(dispatcher) {
        val products = Products()
        val drafts = FakeProductDraftRepository().apply { drafts["uid"] = draft }
        val controller = controller(
            products = products,
            drafts = drafts,
            connectivity = FakeConnectivityRepository(initiallyOnline = false)
        )

        val error = runCatching { controller.save() }.exceptionOrNull()
        settle()

        assertTrue(error is OfflineException)
        assertEquals(ProductController.SAVE_OFFLINE, error?.message)
        assertTrue(products.created.isEmpty())
        assertEquals(listOf(ProductSaveFailure.OFFLINE), products.attempts)
        assertEquals(draft, drafts.drafts["uid"])
    }

    @Test fun `once back online the same save goes through`() = runTest(dispatcher) {
        val products = Products()
        val connectivity = FakeConnectivityRepository(initiallyOnline = false)
        val controller = controller(products = products, connectivity = connectivity)

        runCatching { controller.save() }
        connectivity.goOnline()
        controller.save()

        assertEquals(1, products.created.size)
    }

    @Test fun `editing and deleting offline are refused`() = runTest(dispatcher) {
        val products = Products()
        val controller = controller(
            products = products,
            connectivity = FakeConnectivityRepository(initiallyOnline = false)
        )

        val edit = runCatching {
            controller.updateProduct("p1", "w1", "Jacket", "Example", "10", "", "")
        }.exceptionOrNull()
        val delete = runCatching { controller.deleteProduct("p1") }.exceptionOrNull()

        assertEquals(ProductController.EDIT_OFFLINE, edit?.message)
        assertEquals(ProductController.DELETE_OFFLINE, delete?.message)
        assertTrue(products.updated.isEmpty())
        assertTrue(products.deleted.isEmpty())
    }

    // --- Purchase price (money-saved BQ) ----------------------------------------

    @Test fun `marking a purchase online records the price paid`() = runTest(dispatcher) {
        val products = Products()
        val controller = controller(products = products)

        controller.markPurchased(product(), "95000")
        settle()

        assertEquals(listOf("p1"), products.purchased)
        assertEquals(listOf(Triple("uid", "p1", 95000.0)), products.recordedPurchases)
    }

    @Test fun `marking a purchase offline returns at once and leaves it queued`() = runTest(dispatcher) {
        val products = Products().apply { purchaseAck = CompletableDeferred() }
        val controller = controller(
            products = products,
            connectivity = FakeConnectivityRepository(initiallyOnline = false)
        )

        // Returns although the backend has not confirmed the write.
        controller.markPurchased(product(), "100000")
        runCurrent()

        assertEquals(listOf("p1"), products.purchased)
        assertEquals(listOf(Triple("uid", "p1", 100000.0)), products.recordedPurchases)

        products.purchaseAck?.complete(Unit)
    }

    @Test fun `a missing or negative price paid is rejected before writing`() = runTest(dispatcher) {
        val products = Products()
        val controller = controller(products = products)

        val missing = runCatching { controller.markPurchased(product(), "abc") }.exceptionOrNull()
        val negative = runCatching { controller.markPurchased(product(), "-5") }.exceptionOrNull()
        settle()

        assertTrue(missing is ValidationException)
        assertTrue(negative is ValidationException)
        assertTrue(products.purchased.isEmpty())
        assertTrue(products.recordedPurchases.isEmpty())
    }

    @Test fun `an already purchased product is left alone`() = runTest(dispatcher) {
        val products = Products()
        val controller = controller(products = products)

        controller.markPurchased(product(purchased = true), "1")
        settle()

        assertTrue(products.purchased.isEmpty())
        assertTrue(products.recordedPurchases.isEmpty())
    }
}
