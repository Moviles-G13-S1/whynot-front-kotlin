package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.ProductSearch
import com.example.whynotkotlin.model.SearchFilters
import com.example.whynotkotlin.model.SearchResultCache
import com.example.whynotkotlin.model.SearchSnapshot
import com.example.whynotkotlin.model.SearchSource
import com.example.whynotkotlin.model.SearchState
import com.example.whynotkotlin.model.repositories.ConnectivityRepository
import com.example.whynotkotlin.model.repositories.ProductRepository
import com.example.whynotkotlin.model.repositories.SearchFilterStore
import com.example.whynotkotlin.model.repositories.SearchProductStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Sprint 4 feature (Miguel): Global Product Search across every wishlist.
 *
 * Concurrency, with Kotlin Flow operators (a reactive pipeline):
 * - `debounce`: waits until the user pauses typing before searching.
 * - `mapLatest`: a new keystroke cancels the search still running for the old
 *   one, so stale results never reach the screen.
 * - `withContext(computeDispatcher)`: filtering and sorting run on
 *   [Dispatchers.Default], the pool for CPU work, never on the main thread.
 * - `channelFlow`: the copy on the phone (Room) and the live Firestore listener
 *   run concurrently; the phone copy is shown first, the server one replaces it.
 * - Saving to Room and SharedPreferences is fire-and-forget in the background.
 *
 * Local storage: products in Room ([store]) and the form in SharedPreferences
 * ([filterStore]), both per user.
 *
 * Caching: [cache], an LFU cache of results keyed by the search criteria.
 *
 * Eventual connectivity: offline, search runs over the copy on the phone and
 * the screen says the results may be outdated. When the connection returns the
 * Firestore listener is restarted automatically and its list replaces the copy
 * in Room; if it fails, the screen offers to refresh.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class ProductSearchController(
    private val productRepository: ProductRepository,
    private val currentUid: () -> String?,
    private val store: SearchProductStore?,
    private val filterStore: SearchFilterStore?,
    private val connectivity: ConnectivityRepository?,
    private val backgroundScope: CoroutineScope,
    private val cache: SearchResultCache = SearchResultCache(),
    private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val clock: () -> Long = System::currentTimeMillis,
    private val typingDebounceMillis: Long = TYPING_DEBOUNCE_MILLIS
) {

    private val mutableFilters = MutableStateFlow(SearchFilters())

    /** The form as the user typed it; the screen draws it directly. */
    val filters: StateFlow<SearchFilters> = mutableFilters.asStateFlow()

    /** Incremented by [refresh] to restart the product listener. */
    private val refreshRequests = MutableStateFlow(0)

    fun updateFilters(filters: SearchFilters) {
        mutableFilters.value = filters
    }

    fun clearFilters() {
        mutableFilters.value = SearchFilters()
    }

    /** Restarts loading products, for the "Refresh products" button. */
    fun refresh() {
        refreshRequests.value++
    }

    /** Called on sign-out: nothing of the previous user stays in memory. */
    fun onSignedOut() {
        cache.invalidate("sign_out")
        mutableFilters.value = SearchFilters()
    }

    /**
     * The search screen's state. Cold: it runs while the screen collects it and
     * stops when the screen leaves, closing the Firestore listener.
     */
    fun searchState(): Flow<SearchState> = channelFlow {
        val uid = currentUid()
        if (uid == null) {
            send(SearchState(loading = false, signedOut = true))
            return@channelFlow
        }

        // Local storage: restore what this user searched for last time.
        loadSavedFilters(uid)

        // Save the form in the background whenever it changes (debounced).
        launch {
            mutableFilters
                .drop(1)
                .debounce(SAVE_FILTERS_DEBOUNCE_MILLIS)
                .distinctUntilChanged()
                .collect { filters -> saveFilters(uid, filters) }
        }

        val typedFilters = mutableFilters
            .debounce { if (it.query.isBlank()) 0L else typingDebounceMillis }
            .distinctUntilChanged()

        combine(typedFilters, productSnapshots(uid)) { filters, snapshot -> filters to snapshot }
            .mapLatest { (filters, snapshot) -> search(uid, filters, snapshot) }
            .collect { send(it) }
    }

    private suspend fun search(uid: String, filters: SearchFilters, snapshot: SearchSnapshot): SearchState {
        val base = SearchState(
            loading = false,
            totalProducts = snapshot.products.size,
            source = snapshot.source,
            syncedAtMillis = snapshot.syncedAtMillis,
            loadFailed = snapshot.loadFailed
        )

        val criteria = ProductSearch.criteria(filters)
            ?: return base.copy(invalidPriceRange = true)

        val results = cache.get(uid, snapshot.products, criteria)
            ?: withContext(computeDispatcher) { ProductSearch.filter(snapshot.products, criteria) }
                .also { cache.put(uid, snapshot.products, criteria, it) }

        return base.copy(results = results)
    }

    /**
     * The products to search over: first the copy on the phone, then the live
     * listener. Restarted on [refresh] and every time the connection returns.
     */
    private fun productSnapshots(uid: String): Flow<SearchSnapshot> {
        val online = connectivity?.isOnline ?: MutableStateFlow(true)
        val reconnections = online.drop(1).filter { it }.map { Unit }
        val restarts = merge(flowOf(Unit), refreshRequests.drop(1).map { Unit }, reconnections)

        return restarts.flatMapLatest { productSnapshotsOnce(uid) }
    }

    private fun productSnapshotsOnce(uid: String): Flow<SearchSnapshot> = channelFlow {
        // 1. Whatever the phone kept, at once: search works offline and before
        //    Firestore answers, even right after the app was closed.
        val local = runCatchingStorage { store?.load(uid) }
        if (local != null) {
            send(SearchSnapshot(local.products, SearchSource.PHONE, local.syncedAtMillis))
        }

        // 2. The live listener. Online, each list is the server's and replaces
        //    the copy in Room. Offline it comes from Firestore's own cache.
        try {
            productRepository.observeProducts(uid).collect { products ->
                val isOnline = connectivity?.isOnline?.value ?: true

                // Offline with an empty Firestore cache: keep Room's copy
                // instead of showing "no products".
                if (!isOnline && products.isEmpty() && !local?.products.isNullOrEmpty()) return@collect

                if (isOnline) {
                    val now = clock()
                    send(SearchSnapshot(products, SearchSource.SERVER, now))
                    backgroundScope.launch { runCatchingStorage { store?.replace(uid, products, now) } }
                } else {
                    send(SearchSnapshot(products, SearchSource.PHONE, local?.syncedAtMillis))
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            send(
                SearchSnapshot(
                    products = local?.products.orEmpty(),
                    source = SearchSource.PHONE,
                    syncedAtMillis = local?.syncedAtMillis,
                    loadFailed = true
                )
            )
        }
        awaitClose()
    }

    private suspend fun loadSavedFilters(uid: String) {
        val saved = runCatchingStorage { filterStore?.load(uid) } ?: return
        if (mutableFilters.value.isEmpty()) mutableFilters.value = saved
    }

    private fun saveFilters(uid: String, filters: SearchFilters) {
        backgroundScope.launch { runCatchingStorage { filterStore?.save(uid, filters) } }
    }

    /** Local storage is a convenience: a disk failure must never break the search. */
    private suspend fun <T> runCatchingStorage(block: suspend () -> T?): T? =
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }

    companion object {
        const val TYPING_DEBOUNCE_MILLIS = 250L
        const val SAVE_FILTERS_DEBOUNCE_MILLIS = 400L
    }
}
