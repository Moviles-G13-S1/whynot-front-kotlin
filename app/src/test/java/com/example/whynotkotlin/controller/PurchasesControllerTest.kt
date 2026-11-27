package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.model.PurchaseSort
import com.example.whynotkotlin.model.PurchasesSortCache
import com.example.whynotkotlin.model.repositories.PurchaseSortStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PurchasesControllerTest {

    private class Store : PurchaseSortStore {
        val saved = mutableMapOf<String, PurchaseSort>()
        override suspend fun load(ownerId: String) = saved[ownerId]
        override suspend fun save(ownerId: String, sort: PurchaseSort) {
            saved[ownerId] = sort
        }
    }

    private fun product(id: String, price: Double, purchased: Boolean = true) = Product(
        id = id, ownerId = "uid", wishlistId = "w", categoryId = "technology",
        name = id, brand = "b", price = price, imageUrl = "", productUrl = "",
        purchased = purchased, purchasedAt = null, createdAt = null, updatedAt = null
    )

    private val cheap = product("cheap", 10.0)
    private val expensive = product("expensive", 900.0)
    private val saved = product("saved", 50.0, purchased = false)

    private class Harness(scope: TestScope, val uid: String? = "uid") {
        val products = MutableStateFlow<Result<List<Product>>?>(null)
        val store = Store()
        val logs = mutableListOf<String>()
        val controller = PurchasesController(
            products = products,
            currentUid = { uid },
            sortStore = store,
            backgroundScope = scope.backgroundScope,
            cache = PurchasesSortCache(logger = { logs += it }),
            computeDispatcher = UnconfinedTestDispatcher(scope.testScheduler)
        )

        init {
            scope.backgroundScope.launch { controller.purchases.collect {} }
        }
    }

    private fun TestScope.settle() {
        advanceTimeBy(1_000)
        runCurrent()
    }

    @Test
    fun `only purchased products are listed, in the chosen order`() = runTest {
        val harness = Harness(this)
        harness.products.value = Result.success(listOf(expensive, saved, cheap))
        settle()

        assertEquals(listOf(expensive, cheap), harness.controller.purchases.value?.getOrNull()?.purchases)

        harness.controller.setSort(PurchaseSort.LOW_TO_HIGH)
        settle()
        assertEquals(listOf(cheap, expensive), harness.controller.purchases.value?.getOrNull()?.purchases)

        harness.controller.setSort(PurchaseSort.HIGH_TO_LOW)
        settle()
        assertEquals(listOf(expensive, cheap), harness.controller.purchases.value?.getOrNull()?.purchases)
    }

    @Test
    fun `going back to an order already computed is a cache hit`() = runTest {
        val harness = Harness(this)
        harness.products.value = Result.success(listOf(expensive, cheap))
        settle()

        harness.controller.setSort(PurchaseSort.LOW_TO_HIGH)
        settle()
        harness.controller.setSort(PurchaseSort.NONE)
        settle()

        assertTrue(harness.logs.contains("HIT NONE"))
    }

    @Test
    fun `a new product list empties the cache`() = runTest {
        val harness = Harness(this)
        harness.products.value = Result.success(listOf(expensive))
        settle()
        harness.products.value = Result.success(listOf(expensive, cheap))
        settle()

        assertTrue(harness.logs.contains("INVALIDATE (products changed)"))
        assertEquals(2, harness.controller.purchases.value?.getOrNull()?.purchases?.size)
    }

    @Test
    fun `the order is saved per user and restored`() = runTest {
        val harness = Harness(this)
        harness.controller.setSort(PurchaseSort.HIGH_TO_LOW)
        settle()
        assertEquals(PurchaseSort.HIGH_TO_LOW, harness.store.saved["uid"])

        val reopened = Harness(this)
        reopened.store.saved["uid"] = PurchaseSort.LOW_TO_HIGH
        reopened.products.value = Result.success(listOf(expensive, cheap))
        reopened.controller.restoreSort()
        settle()

        assertEquals(PurchaseSort.LOW_TO_HIGH, reopened.controller.purchases.value?.getOrNull()?.sort)
        assertEquals(listOf(cheap, expensive), reopened.controller.purchases.value?.getOrNull()?.purchases)
    }

    @Test
    fun `while products load the state is null and errors pass through`() = runTest {
        val harness = Harness(this)
        settle()
        assertEquals(null, harness.controller.purchases.value)

        harness.products.value = Result.failure(IllegalStateException("offline"))
        settle()
        assertTrue(harness.controller.purchases.value?.isFailure == true)
    }
}
