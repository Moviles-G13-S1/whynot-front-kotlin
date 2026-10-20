package com.example.whynotkotlin.view.screens.home

import androidx.compose.runtime.Composable
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
import com.example.whynotkotlin.view.navigation.WhyNotNavigator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * The home screen and its two smart sections.
 *
 * The nearest store and the recommendation are kept with rememberSaveable, so
 * they stay while Home is on the back stack and the user moves between tabs,
 * instead of being requested again every time Home is shown.
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

    var nearbyStore by rememberSaveable(stateSaver = NearbyStoreSaver) {
        mutableStateOf<NearbyStore?>(null)
    }
    var nearbyLoading by remember { mutableStateOf(false) }
    var nearbyErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    var recommendation by rememberSaveable(stateSaver = RecommendationSaver) {
        mutableStateOf<ProductRecommendation?>(null)
    }
    var recommendationLoading by remember { mutableStateOf(false) }
    var recommendationSaving by remember { mutableStateOf(false) }
    var recommendationSaved by rememberSaveable { mutableStateOf(false) }
    var recommendationErrorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    fun loadNearby() {
        if (nearbyLoading) return
        nearbyStore = null
        nearbyErrorMessage = null
        nearbyLoading = true

        scope.launch {
            try {
                nearbyStore = nearbyStoreController.loadNearestStore()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                nearbyStore = null
                nearbyErrorMessage = error.message ?: "Could not load nearby stores."
            } finally {
                nearbyLoading = false
            }
        }
    }

    fun loadRecommendation() {
        if (recommendationLoading) return
        recommendationLoading = true
        recommendationErrorMessage = null

        scope.launch {
            try {
                recommendation = recommendationController.loadRecommendation()
                recommendationSaved = false
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                recommendationErrorMessage = error.message ?: "Unable to load recommendation."
            } finally {
                recommendationLoading = false
            }
        }
    }

    fun saveRecommendation(wishlistId: String) {
        val current = recommendation ?: return
        if (recommendationSaving) return
        recommendationSaving = true
        recommendationErrorMessage = null

        scope.launch {
            try {
                recommendationSaved =
                    recommendationController.saveRecommendation(current, wishlistId)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                recommendationErrorMessage = error.message ?: "Unable to save recommendation."
            } finally {
                recommendationSaving = false
            }
        }
    }

    HomeScreen(
        userName = profileResult?.getOrNull()?.takeIf {
            it.uid == session?.getOrNull()?.uid
        }?.name,
        nearbyStore = nearbyStore,
        nearbyLoading = nearbyLoading,
        nearbyErrorMessage = nearbyErrorMessage,
        recommendation = recommendation,
        recommendationLoading = recommendationLoading,
        recommendationSaving = recommendationSaving,
        recommendationSaved = recommendationSaved,
        recommendationErrorMessage = recommendationErrorMessage,
        wishlistSummaries = catalogResult?.getOrNull()?.summaries.orEmpty(),
        onLoadNearby = ::loadNearby,
        onLoadRecommendation = ::loadRecommendation,
        onSaveRecommendation = ::saveRecommendation,
        onHomeClick = {},
        onWishlistsClick = navigator::toWishlists,
        onWishlistClick = navigator::toWishlistDetail,
        onAddClick = navigator::toAdd,
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

private val RecommendationSaver = Saver<ProductRecommendation?, ArrayList<Any>>(
    save = { recommendation ->
        recommendation?.let {
            arrayListOf(
                it.recommendationEventId,
                it.name,
                it.brand,
                it.price,
                it.imageUrl,
                it.productUrl,
                it.categoryId,
                it.reason
            )
        }
    },
    restore = { saved ->
        ProductRecommendation(
            recommendationEventId = saved[0] as String,
            name = saved[1] as String,
            brand = saved[2] as String,
            price = saved[3] as Double,
            imageUrl = saved[4] as String,
            productUrl = saved[5] as String,
            categoryId = saved[6] as String,
            reason = saved[7] as String
        )
    }
)
