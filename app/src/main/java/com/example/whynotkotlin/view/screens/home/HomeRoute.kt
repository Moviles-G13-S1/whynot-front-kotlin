package com.example.whynotkotlin.view.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.whynotkotlin.model.readableMessage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.model.NearbyStore
import com.example.whynotkotlin.model.ProductRecommendation
import com.example.whynotkotlin.view.components.rememberIsOnline
import com.example.whynotkotlin.view.navigation.WhyNotNavigator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

/**
 * The home screen and its two smart sections.
 *
 * The nearest store is kept with rememberSaveable, so it stays while Home is on
 * the back stack. The recommendation comes from RecommendationController's
 * cache instead, so returning to Home reuses it without calling the backend,
 * and offline it shows the last one marked as stale.
 */
@Composable
fun HomeRoute(navigator: WhyNotNavigator) {
    val dependencies = LocalAppDependencies.current
    val nearbyStoreController = dependencies.nearbyStoreController
    val recommendationController = dependencies.recommendationController
    val scope = rememberCoroutineScope()

    val session by dependencies.authController.session.collectAsStateWithLifecycle()
    val profileResult by dependencies.profileController.profile.collectAsStateWithLifecycle()
    val catalogResult by dependencies.wishlistController.catalog.collectAsStateWithLifecycle()

    val productsResult by dependencies.productController.products.collectAsStateWithLifecycle()
    val uid = session?.getOrNull()?.uid
    val emptyProducts = uid != null && profileResult?.getOrNull()?.uid == uid &&
        productsResult?.getOrNull()?.isEmpty() == true
    val catalogReady = catalogResult?.isSuccess == true
    var emptyTracked by rememberSaveable(uid) { mutableStateOf(false) }
    LaunchedEffect(uid, emptyProducts, catalogReady) {
        if (uid != null && emptyProducts && catalogReady && !emptyTracked) {
            dependencies.homeUxController.track(uid, "home_empty_state_viewed", "home_empty_state")
            emptyTracked = true
        }
    }

    var nearbyStore by rememberSaveable(uid, stateSaver = NearbyStoreSaver) {
        mutableStateOf<NearbyStore?>(null)
    }
    var nearbyLoading by remember { mutableStateOf(false) }
    var nearbyErrorMessage by rememberSaveable(uid) { mutableStateOf<String?>(null) }

    var recommendation by remember(uid) { mutableStateOf<ProductRecommendation?>(null) }
    var recommendationStale by remember(uid) { mutableStateOf(false) }
    var recommendationLoading by remember { mutableStateOf(false) }
    var recommendationSaving by remember { mutableStateOf(false) }
    var recommendationSaved by rememberSaveable(uid) { mutableStateOf(false) }
    var recommendationErrorMessage by rememberSaveable(uid) { mutableStateOf<String?>(null) }

    val isOnline = rememberIsOnline()

    suspend fun fetchNearby() {
        if (nearbyLoading) return
        if (!dependencies.connectivity.isOnline.value) {
            nearbyErrorMessage = "Nearby stores need internet. Reconnect and retry."
            return
        }
        nearbyErrorMessage = null
        nearbyLoading = true
        try {
            nearbyStore = withContext(Dispatchers.IO) {
                nearbyStoreController.loadNearestStore()
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            nearbyErrorMessage = "Could not refresh nearby stores. ${error.readableMessage()}"
        } finally {
            nearbyLoading = false
        }
    }

    suspend fun fetchRecommendation(forceRefresh: Boolean = false) {
        if (recommendationLoading) return
        recommendationLoading = true
        recommendationErrorMessage = null
        try {
            val load = withContext(Dispatchers.IO) {
                recommendationController.loadRecommendation(forceRefresh)
            }
            recommendation = load.recommendation
            recommendationStale = load.isStale
            recommendationSaved = false
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            recommendationErrorMessage = error.readableMessage()
        } finally {
            recommendationLoading = false
        }
    }

    fun loadNearby() { scope.launch { fetchNearby() } }
    fun loadRecommendation(forceRefresh: Boolean = false) {
        scope.launch { fetchRecommendation(forceRefresh) }
    }

    suspend fun loadHome(includeNearby: Boolean, forceRefresh: Boolean) {
        supervisorScope {
            val nearby = async { if (includeNearby) fetchNearby() }
            val recommended = async { fetchRecommendation(forceRefresh) }
            nearby.await()
            recommended.await()
        }
    }

    /** Saves to [wishlistId], or to a new wishlist in its category when null. */
    fun saveRecommendation(wishlistId: String?) {
        val current = recommendation ?: return
        if (recommendationSaving || recommendationSaved) return
        if (!dependencies.connectivity.isOnline.value) {
            recommendationErrorMessage = "Saving this recommendation needs internet. Reconnect and try again."
            return
        }
        recommendationSaving = true
        recommendationErrorMessage = null

        scope.launch {
            try {
                val targetWishlistId = wishlistId
                    ?: dependencies.wishlistController.createWishlist(
                        categoryId = current.categoryId,
                        imageUrl = ""
                    )

                recommendationSaved =
                    recommendationController.saveRecommendation(current, targetWishlistId)
                if (!recommendationSaved) recommendationErrorMessage = "The product was not saved. Try again."
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                recommendationErrorMessage = error.readableMessage()
            } finally {
                recommendationSaving = false
            }
        }
    }

    HomeScreen(
        showActivation = emptyProducts && catalogReady,
        onActivate = {
            uid?.let { dependencies.homeUxController.track(it, "home_empty_state_clicked", "home_empty_state") }
            if (catalogResult?.getOrNull()?.summaries.isNullOrEmpty()) navigator.toNewWishlist()
            else {
                uid?.let { dependencies.homeUxController.track(it, "add_product_navigation", "home_empty_state") }
                navigator.toAdd()
            }
        },
        userName = profileResult?.getOrNull()?.takeIf {
            it.uid == session?.getOrNull()?.uid
        }?.name,
        nearbyStore = nearbyStore,
        nearbyLoading = nearbyLoading,
        nearbyErrorMessage = if (!isOnline) "Nearby stores cannot refresh offline. Reconnect and retry."
            else nearbyErrorMessage,
        recommendation = recommendation,
        recommendationLoading = recommendationLoading,
        recommendationSaving = recommendationSaving,
        recommendationSaved = recommendationSaved,
        recommendationErrorMessage = recommendationErrorMessage,
        wishlistSummaries = catalogResult?.getOrNull()?.summaries.orEmpty(),
        onLoadNearby = ::loadNearby,
        onLoadHome = ::loadHome,
        onLoadRecommendation = { loadRecommendation() },
        onRefreshRecommendation = { loadRecommendation(forceRefresh = true) },
        recommendationStale = recommendationStale || !isOnline,
        isOnline = isOnline,
        onSaveRecommendation = ::saveRecommendation,
        onHomeClick = {},
        onWishlistsClick = navigator::toWishlists,
        onWishlistClick = navigator::toWishlistDetail,
        onNewWishlistClick = {
            if (emptyProducts && catalogReady) uid?.let {
                dependencies.homeUxController.track(it, "home_empty_state_clicked", "home_empty_state")
            }
            navigator.toNewWishlist()
        },
        onAddClick = {
            uid?.let { dependencies.homeUxController.track(it, "add_product_navigation", "home_bottom_bar") }
            navigator.toAdd()
        },
        onPurchasesClick = navigator::toPurchases,
        onProfileClick = navigator::toProfile
    )
}

private val NearbyStoreSaver = Saver<NearbyStore?, ArrayList<Any>>(
    save = { store ->
        store?.let {
            arrayListOf(
                it.id,
                it.name,
                it.address,
                it.latitude,
                it.longitude,
                ArrayList(it.categoryIds),
                it.websiteUrl,
                it.imageUrl,
                it.distanceKm
            )
        }
    },
    restore = { saved ->
        @Suppress("UNCHECKED_CAST")
        NearbyStore(
            id = saved[0] as String,
            name = saved[1] as String,
            address = saved[2] as String,
            latitude = saved[3] as Double,
            longitude = saved[4] as Double,
            categoryIds = saved[5] as List<String>,
            websiteUrl = saved[6] as String,
            imageUrl = saved[7] as String,
            distanceKm = saved[8] as Double
        )
    }
)
