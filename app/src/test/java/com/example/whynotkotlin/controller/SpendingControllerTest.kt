package com.example.whynotkotlin.controller

import com.example.whynotkotlin.fakes.FakeConnectivityRepository
import com.example.whynotkotlin.model.*
import com.example.whynotkotlin.model.repositories.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/** Sprint 4: Monthly Spending Summary (cache, local storage, concurrency, offline). */
@OptIn(ExperimentalCoroutinesApi::class)
class SpendingControllerTest {

    private class Products(var products: List<Product> = emptyList()) : ProductRepository {
        var reads = 0
        var failure: Exception? = null
        override fun observeProducts(ownerId: String) = flowOf(products).also {
            reads++
            failure?.let { throw it }
        }
        override fun observeWishlistProducts(ownerId: String, wishlistId: String) = flowOf(emptyList<Product>())
        override fun observeProduct(productId: String) = flowOf<Product?>(null)
        override suspend fun getProduct(productId: String): Product? = null
        override suspend fun createProduct(ownerId: String, draft: ProductDraft) = "id"
        override suspend fun updateProduct(productId: String, update: ProductUpdate) = Unit
        override suspend fun markPurchased(productId: String) = Unit
        override suspend fun deleteProduct(productId: String) = Unit
        override suspend fun recordSaveAttempt(ownerId: String, failure: ProductSaveFailure?) = Unit
        override suspend fun recordPurchase(ownerId: String, product: Product, purchasePrice: Double) = Unit
    }

    private class Categories : WishlistRepository {
        override suspend fun getCategories() = listOf(Category("tech", "Technology"), Category("home", "Home"))
        override fun observeCategories() = flowOf(emptyList<Category>())
        override fun observeWishlists(ownerId: String) = flowOf(emptyList<Wishlist>())
        override suspend fun getWishlist(wishlistId: String): Wishlist? = null
        override suspend fun createWishlist(ownerId: String, draft: WishlistDraft) = "w"
    }

    /** SQLite stand-in: same grouping the SQL queries do. */
    private class MemoryStore : SpendingStore {
        val rows = mutableMapOf<String, List<SpendingPurchase>>()
        val updated = mutableMapOf<String, Long>()
        override suspend fun replace(uid: String, purchases: List<SpendingPurchase>, updatedAtMillis: Long) {
            rows[uid] = purchases
            updated[uid] = updatedAtMillis
        }
        override suspend fun months(uid: String) = rows[uid].orEmpty().groupBy { it.yearMonth }
            .map { (month, list) -> MonthSpending(month, list.sumOf { it.price }, list.size, group(list)) }
            .sortedByDescending { it.yearMonth }
        override suspend fun categories(uid: String, yearMonth: Int) =
            group(rows[uid].orEmpty().filter { it.yearMonth == yearMonth })
        override suspend fun purchases(uid: String, yearMonth: Int, categoryId: String) =
            rows[uid].orEmpty().filter { it.yearMonth == yearMonth && it.categoryId == categoryId }
        override suspend fun updatedAt(uid: String) = updated[uid]
        override suspend fun clearAll() {
            rows.clear()
            updated.clear()
        }
        private fun group(list: List<SpendingPurchase>) = list.groupBy { it.categoryId }.map { (id, items) ->
            CategorySpending(id, items.first().categoryName, items.sumOf { it.price }, items.size, items.count { it.pending })
        }.sortedByDescending { it.total }
    }

    private fun TestScope.controller(
        products: Products,
        store: MemoryStore = MemoryStore(),
        connectivity: FakeConnectivityRepository = FakeConnectivityRepository(),
        cache: SpendingCache = SpendingCache(logger = {})
    ): SpendingController {
        val dispatcher = StandardTestDispatcher(testScheduler)
        return SpendingController(
            productRepository = products,
            wishlistRepository = Categories(),
            store = store,
            connectivity = connectivity,
            currentUid = { "user-a" },
            cache = cache,
            backgroundScope = backgroundScope,
            ioDispatcher = dispatcher,
            computeDispatcher = dispatcher,
            clock = { NOW }
        )
    }

    @Test fun `totals are grouped by month and by category`() = runTest {
        val products = Products(listOf(
            purchased("p1", "tech", 100.0, NOV),
            purchased("p2", "tech", 50.0, NOV),
            purchased("p3", "home", 30.0, NOV),
            purchased("p4", "home", 20.0, OCT),
            saved("p5", "tech", 999.0)
        ))

        val load = controller(products).loadSummary()

        assertEquals(listOf(202611, 202610), load.availableMonths)
        assertEquals(180.0, load.month!!.total, 0.0)
        assertEquals(3, load.month!!.purchaseCount)
        assertEquals(listOf("tech", "home"), load.month!!.categories.map { it.categoryId })
        assertEquals("Technology", load.month!!.categories.first().categoryName)
        assertEquals(150.0, load.month!!.categories.first().total, 0.0)
        assertFalse(load.isStale)
    }

    @Test fun `another month is read from the cache without reading products again`() = runTest {
        val products = Products(listOf(purchased("p1", "tech", 100.0, NOV), purchased("p2", "home", 20.0, OCT)))
        val controller = controller(products)
        controller.loadSummary()

        val october = controller.loadSummary(202610)

        assertEquals(1, products.reads)
        assertTrue(october.fromCache)
        assertEquals(20.0, october.month!!.total, 0.0)
    }

    @Test fun `a changed purchase invalidates the cache`() = runTest {
        val products = Products(listOf(purchased("p1", "tech", 100.0, NOV)))
        val controller = controller(products)
        controller.loadSummary()

        products.products = products.products + purchased("p2", "tech", 40.0, NOV)
        controller.onPurchasesChanged()
        val load = controller.loadSummary()

        assertEquals(2, products.reads)
        assertEquals(140.0, load.month!!.total, 0.0)
    }

    @Test fun `the summary is stored on the phone`() = runTest {
        val store = MemoryStore()
        controller(Products(listOf(purchased("p1", "tech", 100.0, NOV))), store).loadSummary()

        assertEquals(1, store.rows["user-a"]?.size)
        assertEquals(NOW, store.updated["user-a"])
    }

    @Test fun `offline after a restart the stored summary is shown as stale`() = runTest {
        val store = MemoryStore()
        controller(Products(listOf(purchased("p1", "tech", 100.0, NOV))), store).loadSummary()

        // New controller and cache: the app was closed and opened offline.
        val load = controller(Products(), store, FakeConnectivityRepository(initiallyOnline = false)).loadSummary()

        assertTrue(load.isStale)
        assertEquals(100.0, load.month!!.total, 0.0)
        assertEquals(NOW, load.updatedAtMillis)
    }

    @Test fun `offline with nothing stored gives its own message`() = runTest {
        val error = runCatching {
            controller(Products(), connectivity = FakeConnectivityRepository(initiallyOnline = false)).loadSummary()
        }.exceptionOrNull()

        assertTrue(error is SpendingOfflineException)
    }

    @Test fun `a backend failure falls back to what is stored`() = runTest {
        val store = MemoryStore()
        controller(Products(listOf(purchased("p1", "tech", 100.0, NOV))), store).loadSummary()
        val failing = Products().apply { failure = IllegalStateException("backend down") }

        val load = controller(failing, store).loadSummary()

        assertTrue(load.isStale)
        assertEquals(100.0, load.month!!.total, 0.0)
    }

    @Test fun `a purchase marked offline counts in the current month as pending`() = runTest {
        val pending = purchased("p1", "tech", 70.0, null).copy(pendingSync = true)

        val load = controller(Products(listOf(pending))).loadSummary()

        assertEquals(202611, load.month!!.yearMonth)
        assertEquals(1, load.month!!.categories.single().pendingCount)
    }

    @Test fun `the category detail lists its purchases`() = runTest {
        val products = Products(listOf(
            purchased("p1", "tech", 100.0, NOV),
            purchased("p2", "home", 50.0, NOV)
        ))
        val controller = controller(products)
        controller.loadSummary()

        val detail = controller.loadCategory(202611, "tech")

        assertEquals(listOf("p1"), detail.purchases.map { it.productId })
        assertEquals(150.0, detail.monthTotal, 0.0)
        assertEquals(100.0, detail.category!!.total, 0.0)
    }

    @Test fun `sign-out clears the cache and the stored data`() = runTest {
        val store = MemoryStore()
        val cache = SpendingCache(logger = {})
        val controller = controller(Products(listOf(purchased("p1", "tech", 100.0, NOV))), store, cache = cache)
        controller.loadSummary()

        controller.onSignedOut()
        runCurrent()

        assertTrue(store.rows.isEmpty())
        assertFalse(cache.isFilledFor("user-a"))
    }
}

private val BOGOTA: TimeZone = TimeZone.getTimeZone("America/Bogota")

private fun date(year: Int, month: Int, day: Int): Date =
    Calendar.getInstance(BOGOTA).apply { set(year, month - 1, day, 12, 0, 0) }.time

private val NOV = date(2026, 11, 20)
private val OCT = date(2026, 10, 5)
private val NOW = date(2026, 11, 25).time

private fun purchased(id: String, categoryId: String, price: Double, purchasedAt: Date?) = Product(
    id = id, ownerId = "user-a", wishlistId = "w-$categoryId", categoryId = categoryId,
    name = "Item $id", brand = "Brand", price = price, imageUrl = "", productUrl = "",
    purchased = true, purchasedAt = purchasedAt, createdAt = null, updatedAt = null
)

private fun saved(id: String, categoryId: String, price: Double) =
    purchased(id, categoryId, price, null).copy(purchased = false)
