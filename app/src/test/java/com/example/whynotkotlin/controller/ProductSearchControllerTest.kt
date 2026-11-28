package com.example.whynotkotlin.controller

import com.example.whynotkotlin.fakes.FakeConnectivityRepository
import com.example.whynotkotlin.model.LocalSearchSnapshot
import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.model.ProductDraft
import com.example.whynotkotlin.model.ProductSaveFailure
import com.example.whynotkotlin.model.ProductSearch
import com.example.whynotkotlin.model.ProductUpdate
import com.example.whynotkotlin.model.SearchCriteria
import com.example.whynotkotlin.model.SearchFilters
import com.example.whynotkotlin.model.SearchResultCache
import com.example.whynotkotlin.model.SearchSource
import com.example.whynotkotlin.model.SearchState
import com.example.whynotkotlin.model.repositories.ProductRepository
import com.example.whynotkotlin.model.repositories.SearchFilterStore
import com.example.whynotkotlin.model.repositories.SearchProductStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Lets debounces and background work run, the way time passes on a phone. */
@OptIn(ExperimentalCoroutinesApi::class)
private fun TestScope.settle() {
    advanceTimeBy(1_000)
    runCurrent()
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProductSearchControllerTest {

    private class Products : ProductRepository {
        val products = MutableStateFlow<List<Product>>(emptyList())
        var failing = false
        var subscriptions = 0

        override fun observeProducts(ownerId: String): Flow<List<Product>> {
            subscriptions++
            return if (failing) flow { throw IllegalStateException("listener failed") } else products
        }

        override fun observeWishlistProducts(ownerId: String, wishlistId: String): Flow<List<Product>> = products
        override fun observeProduct(productId: String): Flow<Product?> = TODO()
        override suspend fun getProduct(productId: String): Product? = null
        override suspend fun createProduct(ownerId: String, draft: ProductDraft): String = TODO()
        override suspend fun updateProduct(productId: String, update: ProductUpdate) = TODO()
        override suspend fun markPurchased(productId: String) = TODO()
        override suspend fun deleteProduct(productId: String) = TODO()
        override suspend fun recordSaveAttempt(ownerId: String, failure: ProductSaveFailure?) = Unit
        override suspend fun recordPurchase(ownerId: String, product: Product, purchasePrice: Double) = Unit
    }

    private class Store : SearchProductStore {
        val saved = mutableMapOf<String, LocalSearchSnapshot>()
        override suspend fun load(ownerId: String) = saved[ownerId]
        var writes = 0
        override suspend fun replace(ownerId: String, products: List<Product>, syncedAtMillis: Long) {
            writes++
            saved[ownerId] = LocalSearchSnapshot(products, syncedAtMillis)
        }
        override suspend fun clear(ownerId: String) {
            saved.remove(ownerId)
        }
    }

    private class Filters : SearchFilterStore {
        val saved = mutableMapOf<String, SearchFilters>()
        override suspend fun load(ownerId: String) = saved[ownerId]
        override suspend fun save(ownerId: String, filters: SearchFilters) {
            saved[ownerId] = filters
        }
    }

    private fun product(
        id: String,
        name: String,
        brand: String = "Brand",
        price: Double = 100.0,
        categoryId: String = "technology"
    ) = Product(
        id = id, ownerId = "uid", wishlistId = "w1", categoryId = categoryId,
        name = name, brand = brand, price = price, imageUrl = "", productUrl = "",
        purchased = false, purchasedAt = null, createdAt = null, updatedAt = null
    )

    private val headphones = product("p1", "Sony headphones", "Sony", 250.0)
    private val backpack = product("p2", "Travel backpack", "Totto", 180.0, categoryId = "travel")
    private val speaker = product("p3", "Bluetooth speaker", "JBL", 400.0)

    private class Harness(
        val products: Products = Products(),
        val store: Store = Store(),
        val filters: Filters = Filters(),
        val connectivity: FakeConnectivityRepository = FakeConnectivityRepository(),
        var uid: String? = "uid"
    ) {
        val logs = mutableListOf<String>()
        val states = mutableListOf<SearchState>()

        fun controller(scope: TestScope) = ProductSearchController(
            productRepository = products,
            currentUid = { uid },
            store = store,
            filterStore = filters,
            connectivity = connectivity,
            backgroundScope = scope.backgroundScope,
            cache = SearchResultCache(maxEntries = 2, logger = { logs += it }),
            computeDispatcher = UnconfinedTestDispatcher(scope.testScheduler),
            clock = { 1_000L }
        )

        fun collect(scope: TestScope, controller: ProductSearchController) {
            scope.backgroundScope.launch { controller.searchState().collect { states += it } }
        }

        val last get() = states.last()
    }

    // --- The search itself -------------------------------------------------

    @Test
    fun `matches name or brand, applies category and price, sorts by name`() {
        val all = listOf(headphones, backpack, speaker)

        val byBrand = ProductSearch.filter(all, ProductSearch.criteria(SearchFilters(query = "jbl"))!!)
        assertEquals(listOf(speaker), byBrand)

        val technology = ProductSearch.filter(all, ProductSearch.criteria(SearchFilters(categoryId = "technology"))!!)
        assertEquals(listOf(speaker, headphones), technology)

        val cheap = ProductSearch.filter(all, ProductSearch.criteria(SearchFilters(maxPrice = "250"))!!)
        assertEquals(listOf(headphones, backpack), cheap)
    }

    @Test
    fun `an inverted or unreadable price range is rejected`() {
        assertNull(ProductSearch.criteria(SearchFilters(minPrice = "500", maxPrice = "100")))
        assertNull(ProductSearch.criteria(SearchFilters(minPrice = "abc")))
        assertNotNull(ProductSearch.criteria(SearchFilters(minPrice = "0", maxPrice = "100")))
    }

    // --- LFU cache -----------------------------------------------------------

    @Test
    fun `the cache evicts the least frequently used search, not the most recent`() {
        val logs = mutableListOf<String>()
        val cache = SearchResultCache(maxEntries = 2, logger = { logs += it })
        val list = listOf(headphones)
        val popular = SearchCriteria("sony", null, null, null)
        val typo = SearchCriteria("sonu", null, null, null)
        val other = SearchCriteria("jbl", null, null, null)

        cache.get("uid", list, popular)
        cache.put("uid", list, popular, list)
        cache.get("uid", list, popular) // used twice
        cache.put("uid", list, typo, emptyList()) // used once
        cache.put("uid", list, other, emptyList()) // full: evicts the typo

        assertNotNull(cache.get("uid", list, popular))
        assertNull(cache.get("uid", list, typo))
        assertTrue(logs.any { it.startsWith("EVICT \"sonu\"") })
    }

    @Test
    fun `a new product list or another user empties the cache`() {
        val cache = SearchResultCache(logger = {})
        val criteria = SearchCriteria("", null, null, null)
        val first = listOf(headphones)
        cache.get("uid", first, criteria)
        cache.put("uid", first, criteria, first)

        assertNull(cache.get("uid", listOf(headphones, speaker), criteria))
        assertNull(cache.get("other", first, criteria))
    }

    @Test
    fun `a result computed for an old list is never stored`() {
        val cache = SearchResultCache(logger = {})
        val criteria = SearchCriteria("", null, null, null)
        val old = listOf(headphones)
        cache.get("uid", old, criteria)
        cache.get("uid", listOf(speaker), criteria) // the list changed meanwhile

        cache.put("uid", old, criteria, old)

        assertEquals(0, cache.size())
    }

    // --- Controller: concurrency, storage, connectivity ----------------------

    @Test
    fun `online the server list is searched and saved on the phone`() = runTest {
        val harness = Harness()
        harness.products.products.value = listOf(headphones, speaker)
        val controller = harness.controller(this)

        harness.collect(this, controller)
        settle()

        assertEquals(SearchSource.SERVER, harness.last.source)
        assertEquals(listOf(speaker, headphones), harness.last.results)
        assertEquals(listOf(headphones, speaker), harness.store.saved["uid"]?.products)
    }

    @Test
    fun `an identical list from Firestore is not searched or saved again`() = runTest {
        val harness = Harness()
        harness.products.products.value = listOf(headphones, speaker)
        val controller = harness.controller(this)
        harness.collect(this, controller)
        settle()
        val statesBefore = harness.states.size

        // Same products in a new list instance, as Firestore sends on a metadata-only change.
        harness.products.products.value = listOf(headphones.copy(), speaker.copy())
        settle()

        assertEquals(1, harness.store.writes)
        assertEquals(statesBefore, harness.states.size)
        assertFalse(harness.logs.any { it.startsWith("INVALIDATE") })
    }

    @Test
    fun `typing waits for a pause and repeating a search is a cache hit`() = runTest {
        val harness = Harness()
        harness.products.products.value = listOf(headphones, speaker)
        val controller = harness.controller(this)
        harness.collect(this, controller)
        settle()

        controller.updateFilters(SearchFilters(query = "s"))
        controller.updateFilters(SearchFilters(query = "so"))
        controller.updateFilters(SearchFilters(query = "sony"))
        settle()

        assertEquals(listOf(headphones), harness.last.results)
        // Debounce: the intermediate "s" and "so" were never searched.
        assertFalse(harness.logs.any { it == "MISS \"so\"" || it == "MISS \"s\"" })

        controller.updateFilters(SearchFilters())
        settle()
        controller.updateFilters(SearchFilters(query = "sony"))
        settle()
        assertTrue(harness.logs.any { it.startsWith("HIT \"sony\"") })
    }

    @Test
    fun `offline with an empty Firestore cache it searches the copy in Room`() = runTest {
        val harness = Harness(connectivity = FakeConnectivityRepository(initiallyOnline = false))
        harness.store.saved["uid"] = LocalSearchSnapshot(listOf(backpack), syncedAtMillis = 500L)
        val controller = harness.controller(this)

        harness.collect(this, controller)
        settle()

        assertEquals(SearchSource.PHONE, harness.last.source)
        assertEquals(listOf(backpack), harness.last.results)
        assertEquals(500L, harness.last.syncedAtMillis)
    }

    @Test
    fun `when the listener fails it falls back to the phone and can be refreshed`() = runTest {
        val harness = Harness()
        harness.store.saved["uid"] = LocalSearchSnapshot(listOf(backpack), syncedAtMillis = 500L)
        harness.products.failing = true
        val controller = harness.controller(this)
        harness.collect(this, controller)
        settle()

        assertTrue(harness.last.loadFailed)
        assertEquals(listOf(backpack), harness.last.results)

        harness.products.failing = false
        harness.products.products.value = listOf(headphones)
        controller.refresh()
        settle()

        assertFalse(harness.last.loadFailed)
        assertEquals(SearchSource.SERVER, harness.last.source)
        assertEquals(listOf(headphones), harness.last.results)
    }

    @Test
    fun `the listener restarts by itself when the connection returns`() = runTest {
        val harness = Harness(connectivity = FakeConnectivityRepository(initiallyOnline = false))
        val controller = harness.controller(this)
        harness.collect(this, controller)
        settle()
        val before = harness.products.subscriptions

        harness.connectivity.goOnline()
        settle()

        assertEquals(before + 1, harness.products.subscriptions)
    }

    @Test
    fun `each user's filters are restored and saved on the phone`() = runTest {
        val harness = Harness()
        harness.filters.saved["uid"] = SearchFilters(query = "sony", categoryId = "technology")
        harness.products.products.value = listOf(headphones, speaker)
        val controller = harness.controller(this)
        harness.collect(this, controller)
        settle()

        assertEquals("sony", controller.filters.value.query)
        assertEquals(listOf(headphones), harness.last.results)

        controller.updateFilters(SearchFilters(query = "jbl"))
        settle()
        assertEquals("jbl", harness.filters.saved["uid"]?.query)
    }

    @Test
    fun `signed out shows the sign-in message and sign-out clears the form`() = runTest {
        val harness = Harness(uid = null)
        val controller = harness.controller(this)
        harness.collect(this, controller)
        settle()
        assertTrue(harness.last.signedOut)

        controller.updateFilters(SearchFilters(query = "x"))
        controller.onSignedOut()
        assertTrue(controller.filters.value.isEmpty())
    }
}
