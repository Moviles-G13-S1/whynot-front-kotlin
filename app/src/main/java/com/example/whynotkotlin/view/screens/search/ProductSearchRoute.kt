package com.example.whynotkotlin.view.screens.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.model.CanonicalCategories
import com.example.whynotkotlin.model.SearchState
import com.example.whynotkotlin.view.components.WhyNotTab
import com.example.whynotkotlin.view.components.rememberIsOnline
import com.example.whynotkotlin.view.navigation.WhyNotNavigator

/**
 * Connects [ProductSearchScreen] to [com.example.whynotkotlin.controller.ProductSearchController].
 *
 * The search runs only while this screen collects it; leaving the screen
 * cancels the pipeline and closes the Firestore listener.
 */
@Composable
fun ProductSearchRoute(navigator: WhyNotNavigator) {
    val controller = LocalAppDependencies.current.productSearchController
    val stateFlow = remember(controller) { controller.searchState() }
    val state by stateFlow.collectAsStateWithLifecycle(initialValue = SearchState())
    val filters by controller.filters.collectAsStateWithLifecycle()

    ProductSearchScreen(
        state = state,
        filters = filters,
        // The seeded categories, available offline too.
        categories = CanonicalCategories.all,
        isOnline = rememberIsOnline(),
        onFiltersChange = controller::updateFilters,
        onClearFilters = controller::clearFilters,
        onRefresh = controller::refresh,
        onProductClick = { navigator.toProductDetail(it, WhyNotTab.WISHLISTS) },
        navigation = SearchNavigation(
            onBackClick = navigator::back,
            onHomeClick = navigator::toHome,
            onWishlistsClick = navigator::toWishlists,
            onAddClick = navigator::toAdd,
            onPurchasesClick = navigator::toPurchases,
            onProfileClick = navigator::toProfile
        )
    )
}
