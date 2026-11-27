package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.model.PurchaseSort
import com.example.whynotkotlin.model.PurchaseSorting
import com.example.whynotkotlin.model.PurchasesList
import com.example.whynotkotlin.model.PurchasesSortCache
import com.example.whynotkotlin.model.repositories.PurchaseSortStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Purchases (Miguel's Sprint 3 feature), corrected after the Sprint 3 viva.
 *
 * Before, the screen filtered and sorted the list inside the composable, on
 * the main thread, on every recomposition. Now:
 * - Concurrency: the list is a Flow that combines the products with the chosen
 *   order and runs the filtering and sorting on [Dispatchers.Default] through
 *   `flowOn`, so the main thread only draws.
 * - Local storage: the chosen order is kept per user in DataStore and restored
 *   when Purchases opens again, even after closing the app.
 * - Caching: [PurchasesSortCache] memoizes each order for the current list.
 */
class PurchasesController(
    products: Flow<Result<List<Product>>?>,
    private val currentUid: () -> String?,
    private val sortStore: PurchaseSortStore?,
    private val backgroundScope: CoroutineScope,
    private val cache: PurchasesSortCache = PurchasesSortCache(),
    computeDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private val sort = MutableStateFlow(PurchaseSort.NONE)

    /** null while the products are still loading. */
    val purchases: StateFlow<Result<PurchasesList>?> =
        combine(products, sort) { result, order ->
            result?.map { list ->
                PurchasesList(
                    purchases = cache.resolve(list, order) { PurchaseSorting.apply(list, order) },
                    sort = order
                )
            }
        }
            .flowOn(computeDispatcher)
            .stateIn(backgroundScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    /** Restores the order this user chose last time (called when Purchases opens). */
    suspend fun restoreSort() {
        val uid = currentUid() ?: return
        val saved = try {
            sortStore?.load(uid)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null // A disk failure only means starting with the default order.
        }
        if (saved != null) sort.value = saved
    }

    fun setSort(order: PurchaseSort) {
        sort.value = order
        val uid = currentUid() ?: return
        backgroundScope.launch {
            try {
                sortStore?.save(uid, order)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // The order still applies now; it is just not remembered.
            }
        }
    }

    fun onSignedOut() {
        sort.value = PurchaseSort.NONE
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
