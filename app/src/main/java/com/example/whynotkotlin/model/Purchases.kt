package com.example.whynotkotlin.model

import android.util.Log
import androidx.collection.ArrayMap

/**
 * Purchases screen (Miguel's Sprint 3 feature), corrected after the Sprint 3
 * viva: its list is now computed off the main thread, the chosen order is kept
 * on the phone and already computed orders are cached.
 */
enum class PurchaseSort { NONE, LOW_TO_HIGH, HIGH_TO_LOW }

/** What the Purchases screen draws: the purchased products, already in order. */
data class PurchasesList(
    val purchases: List<Product>,
    val sort: PurchaseSort
)

/** Pure filtering and ordering, so it can run on any thread and be tested alone. */
object PurchaseSorting {
    fun apply(products: List<Product>, sort: PurchaseSort): List<Product> {
        val purchased = products.filter { it.purchased }
        return when (sort) {
            PurchaseSort.LOW_TO_HIGH -> purchased.sortedBy { it.price }
            PurchaseSort.HIGH_TO_LOW -> purchased.sortedByDescending { it.price }
            PurchaseSort.NONE -> purchased
        }
    }
}

/**
 * Caching (Sprint 3 correction): memoizes the purchases list for each order.
 *
 * An [ArrayMap] — Android's memory-efficient map for a handful of keys — maps
 * each [PurchaseSort] to its list. Switching between "Low to high" and "High to
 * low" reuses the list instead of filtering and sorting again. The entries are
 * only valid for one product list: when it changes (a purchase marked, a
 * product edited or deleted) the cache is emptied.
 */
class PurchasesSortCache(
    private val logger: (String) -> Unit = { Log.d(TAG, it) }
) {
    private val lists = ArrayMap<PurchaseSort, List<Product>>()
    private var products: List<Product>? = null

    @Synchronized
    fun resolve(productList: List<Product>, sort: PurchaseSort, compute: () -> List<Product>): List<Product> {
        if (productList !== products) {
            if (products != null) logger("INVALIDATE (products changed)")
            lists.clear()
            products = productList
        }
        lists[sort]?.let {
            logger("HIT $sort")
            return it
        }
        logger("MISS $sort")
        return compute().also { lists[sort] = it }
    }

    companion object {
        const val TAG = "WhyNotPurchases"
    }
}
