package com.example.whynotkotlin.view.screens.purchases

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.view.navigation.WhyNotNavigator

@Composable
fun PurchasesRoute(navigator: WhyNotNavigator) {
    val productController = LocalAppDependencies.current.productController
    val productsResult by productController.products.collectAsStateWithLifecycle()

    PurchasesScreen(
        purchases = productController.purchased(productsResult?.getOrNull().orEmpty()),
        onHomeClick = navigator::toHome,
        onWishlistsClick = navigator::toWishlists,
        onAddClick = navigator::toAdd,
        onPurchasesClick = {},
        onProfileClick = navigator::toProfile
    )
}
