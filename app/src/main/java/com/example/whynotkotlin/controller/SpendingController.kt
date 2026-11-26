package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.CategorySpending
import com.example.whynotkotlin.model.CategorySpendingDetailLoad
import com.example.whynotkotlin.model.MonthSpending
import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.model.SpendingCache
import com.example.whynotkotlin.model.SpendingOfflineException
import com.example.whynotkotlin.model.SpendingPurchase
import com.example.whynotkotlin.model.SpendingSummaryLoad
import com.example.whynotkotlin.model.UserFacingException
import com.example.whynotkotlin.model.YearMonth
import com.example.whynotkotlin.model.repositories.ConnectivityRepository
import com.example.whynotkotlin.model.repositories.ProductRepository
import com.example.whynotkotlin.model.repositories.SpendingStore
import com.example.whynotkotlin.model.repositories.WishlistRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Sprint 4 feature (Juan Felipe): Monthly Spending Summary and Category
 * Spending Detail.
 *
 * Strategies:
 * - Caching: [cache], a SparseArray keyed by month, answers repeated visits
 *   without reading Firestore or the database.
 * - Local storage: [store], a SQLite database, keeps the purchases on the phone
 *   for offline use and after a restart.
 * - Concurrency: [refresh] reads products and categories at the same time on
 *   [ioDispatcher], computes every month and every category in parallel on
 *   [computeDispatcher], writes to SQLite on [ioDispatcher] and returns to the
 *   caller's (main) thread.
 * - Eventual connectivity: offline, or if the backend fails, the stored
 *   summary is shown marked as stale; with nothing stored, a specific message.
 */
class SpendingController(
    private val productRepository: ProductRepository,
    private val wishlistRepository: WishlistRepository,
    private val store: SpendingStore,
    private val connectivity: ConnectivityRepository,
    private val currentUid: () -> String?,
    private val cache: SpendingCache = SpendingCache(),
    private val backgroundScope: CoroutineScope? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val clock: () -> Long = System::currentTimeMillis
) {

    /**
     * The summary of [yearMonth], or of the newest month with purchases when
     * null. [forceRefresh] skips the cache (pull to refresh, Retry, reconnect).
     */
    suspend fun loadSummary(yearMonth: Int? = null, forceRefresh: Boolean = false): SpendingSummaryLoad {
        val uid = requireUid()

        if (!forceRefresh && cache.isFilledFor(uid)) {
            return summaryFromCache(yearMonth, isStale = false, fromCache = true)
        }

        if (!connectivity.isOnline.value) {
            return storedSummary(uid, yearMonth) ?: throw SpendingOfflineException()
        }

        try {
            refresh(uid)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            return storedSummary(uid, yearMonth) ?: throw error
        }
        return summaryFromCache(yearMonth, isStale = false, fromCache = false)
    }

    /** The purchases of one category in one month, for the detail screen. */
    suspend fun loadCategory(yearMonth: Int, categoryId: String): CategorySpendingDetailLoad {
        val uid = requireUid()

        if (cache.isFilledFor(uid)) {
            val month = cache.month(yearMonth)
            return CategorySpendingDetailLoad(
                yearMonth = yearMonth,
                category = month?.categories?.firstOrNull { it.categoryId == categoryId },
                monthTotal = month?.total ?: 0.0,
                purchases = cache.purchasesOf(yearMonth).filter { it.categoryId == categoryId },
                isStale = false,
                updatedAtMillis = cache.updatedAt()
            )
        }

        // Not in memory (the app was restarted): the database has it, online or not.
        val categories = runStoreWork { store.categories(uid, yearMonth) }.orEmpty()
        val purchases = runStoreWork { store.purchases(uid, yearMonth, categoryId) }.orEmpty()
        if (categories.isEmpty() && !connectivity.isOnline.value) throw SpendingOfflineException()

        return CategorySpendingDetailLoad(
            yearMonth = yearMonth,
            category = categories.firstOrNull { it.categoryId == categoryId },
            monthTotal = categories.sumOf { it.total },
            purchases = purchases,
            isStale = !connectivity.isOnline.value,
            updatedAtMillis = runStoreWork { store.updatedAt(uid) }
        )
    }

    /** A purchase was marked, edited or deleted: the totals are no longer valid. */
    fun onPurchasesChanged() {
        cache.invalidate(reason = "purchases_changed")
    }

    /** Called on sign-out so the next user never sees this user's spending. */
    fun onSignedOut() {
        cache.invalidate(reason = "sign_out")
        backgroundScope?.launch { runStoreWork { store.clearAll() } }
    }

    /**
     * Reads, computes, stores and caches the whole summary.
     *
     * Products and category names are read at the same time (two coroutines on
     * the IO dispatcher). The grouping runs on the compute dispatcher with one
     * coroutine per month, and inside each month one per category. The result
     * is written to SQLite on the IO dispatcher.
     */
    private suspend fun refresh(uid: String) = coroutineScope {
        val products = async(ioDispatcher) { productRepository.observeProducts(uid).first() }
        val categoryNames = async(ioDispatcher) {
            runCatching { wishlistRepository.getCategories() }
                .getOrDefault(emptyList())
                .associate { it.id to it.name }
        }

        val purchases = toPurchases(products.await(), categoryNames.await())
        val months = withContext(computeDispatcher) { summarize(purchases) }
        val updatedAt = clock()

        withContext(ioDispatcher) {
            runStoreWork { store.replace(uid, purchases, updatedAt) }
        }
        cache.put(uid, months, purchases, updatedAt)
    }

    private fun toPurchases(products: List<Product>, names: Map<String, String>): List<SpendingPurchase> =
        products
            .filter { it.purchased }
            .map { product ->
                // A purchase marked offline has no server time yet: it is
                // placed in the current month and flagged as pending.
                val purchasedAt = product.purchasedAt ?: Date(clock())
                SpendingPurchase(
                    productId = product.id,
                    yearMonth = YearMonth.of(purchasedAt),
                    categoryId = product.categoryId,
                    categoryName = names[product.categoryId] ?: product.categoryId,
                    name = product.name,
                    brand = product.brand,
                    price = product.price,
                    purchasedAtMillis = purchasedAt.time,
                    pending = product.pendingSync || product.purchasedAt == null
                )
            }

    /** One coroutine per month, and inside it one per category, all in parallel. */
    private suspend fun summarize(purchases: List<SpendingPurchase>): List<MonthSpending> = coroutineScope {
        purchases.groupBy { it.yearMonth }.map { (yearMonth, inMonth) ->
            async {
                val categories = coroutineScope {
                    inMonth.groupBy { it.categoryId }.map { (categoryId, inCategory) ->
                        async {
                            CategorySpending(
                                categoryId = categoryId,
                                categoryName = inCategory.first().categoryName,
                                total = inCategory.sumOf { it.price },
                                purchaseCount = inCategory.size,
                                pendingCount = inCategory.count { it.pending }
                            )
                        }
                    }.awaitAll()
                }.sortedByDescending { it.total }

                MonthSpending(
                    yearMonth = yearMonth,
                    total = categories.sumOf { it.total },
                    purchaseCount = inMonth.size,
                    categories = categories
                )
            }
        }.awaitAll().sortedByDescending { it.yearMonth }
    }

    private fun summaryFromCache(yearMonth: Int?, isStale: Boolean, fromCache: Boolean): SpendingSummaryLoad {
        val available = cache.availableMonths()
        val selected = yearMonth?.takeIf { it in available } ?: available.firstOrNull()
        return SpendingSummaryLoad(
            month = selected?.let { cache.month(it) },
            availableMonths = available,
            isStale = isStale,
            updatedAtMillis = cache.updatedAt(),
            fromCache = fromCache
        )
    }

    /** The summary saved in SQLite, marked as stale, or null if never saved. */
    private suspend fun storedSummary(uid: String, yearMonth: Int?): SpendingSummaryLoad? {
        val updatedAt = runStoreWork { store.updatedAt(uid) } ?: return null
        val months = runStoreWork { store.months(uid) }.orEmpty()
        val available = months.map { it.yearMonth }
        val selected = yearMonth?.takeIf { it in available } ?: available.firstOrNull()
        return SpendingSummaryLoad(
            month = months.firstOrNull { it.yearMonth == selected },
            availableMonths = available,
            isStale = true,
            updatedAtMillis = updatedAt
        )
    }

    private fun requireUid(): String =
        currentUid() ?: throw UserFacingException("Sign in to see your spending.")

    /** A database problem must never break the screen; it just means no stored data. */
    private suspend fun <T> runStoreWork(block: suspend () -> T?): T? =
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }
}
