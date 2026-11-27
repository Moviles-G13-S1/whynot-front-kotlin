package com.example.whynotkotlin.view.screens.purchases

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.model.PurchaseSort
import com.example.whynotkotlin.view.components.WhyNotTab
import com.example.whynotkotlin.view.navigation.WhyNotNavigator
import com.example.whynotkotlin.view.components.rememberIsOnline

@Composable
fun PurchasesRoute(navigator: WhyNotNavigator) {
    val purchasesController = LocalAppDependencies.current.purchasesController
    val purchasesResult by purchasesController.purchases.collectAsStateWithLifecycle()
    val purchasesList = purchasesResult?.getOrNull()

    // Local storage: the order this user chose last time (DataStore).
    LaunchedEffect(Unit) { purchasesController.restoreSort() }

    PurchasesScreen(
        isOnline = rememberIsOnline(),
        purchases = purchasesList?.purchases.orEmpty(),
        sort = purchasesList?.sort ?: PurchaseSort.NONE,
        onSortChange = purchasesController::setSort,
        loading = purchasesResult == null,
        errorMessage = purchasesResult?.exceptionOrNull()?.let { "Could not load purchases." },
        onProductClick = { productId ->
            navigator.toProductDetail(productId, WhyNotTab.PURCHASES)
        },
        onHomeClick = navigator::toHome,
        onWishlistsClick = navigator::toWishlists,
        onAddClick = navigator::toAdd,
        onPurchasesClick = {},
        onProfileClick = navigator::toProfile,
        onSpendingClick = navigator::toSpending
    )
}
