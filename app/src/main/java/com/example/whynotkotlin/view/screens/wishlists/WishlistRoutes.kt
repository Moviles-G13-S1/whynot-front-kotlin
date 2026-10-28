package com.example.whynotkotlin.view.screens.wishlists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.model.readableMessage
import com.example.whynotkotlin.view.components.rememberSubmission
import com.example.whynotkotlin.view.navigation.WhyNotNavigator
import com.example.whynotkotlin.view.components.rememberIsOnline

@Composable
fun WishlistsRoute(navigator: WhyNotNavigator) {
    val catalogResult by LocalAppDependencies.current
        .wishlistController
        .catalog
        .collectAsStateWithLifecycle()

    WishlistsScreen(
        isOnline = rememberIsOnline(),
        summaries = catalogResult?.getOrNull()?.summaries.orEmpty(),
        loading = catalogResult == null,
        errorMessage = catalogResult?.exceptionOrNull()?.readableMessage(),
        onWishlistClick = navigator::toWishlistDetail,
        onNewWishlistClick = navigator::toNewWishlist,
        onHomeClick = navigator::toHome,
        onWishlistsClick = {},
        onAddClick = navigator::toAdd,
        onPurchasesClick = navigator::toPurchases,
        onProfileClick = navigator::toProfile
    )
}

@Composable
fun NewWishlistRoute(navigator: WhyNotNavigator) {
    val wishlistController = LocalAppDependencies.current.wishlistController
    val catalogResult by wishlistController.catalog.collectAsStateWithLifecycle()
    val submission = rememberSubmission()

    NewWishlistScreen(
        isOnline = rememberIsOnline(),
        availableCategories = catalogResult?.getOrNull()?.availableCategories.orEmpty(),
        saving = submission.running,
        errorMessage = submission.errorMessage
            ?: catalogResult?.exceptionOrNull()?.readableMessage(),
        onSave = { categoryId, imageUrl ->
            submission.run(onSuccess = navigator::back) {
                wishlistController.createWishlist(categoryId, imageUrl)
            }
        },
        onCancelClick = navigator::back,
        onHomeClick = navigator::toHome,
        onWishlistsClick = navigator::toWishlists,
        onAddClick = navigator::toAdd,
        onPurchasesClick = navigator::toPurchases,
        onProfileClick = navigator::toProfile
    )
}

@Composable
fun WishlistDetailRoute(
    wishlistId: String,
    navigator: WhyNotNavigator
) {
    val dependencies = LocalAppDependencies.current
    val productController = dependencies.productController
    val catalogResult by dependencies.wishlistController.catalog.collectAsStateWithLifecycle()
    val productsResult by productController.products.collectAsStateWithLifecycle()

    WishlistDetailScreen(
        isOnline = rememberIsOnline(),
        wishlistName =
            catalogResult?.getOrNull()?.find(wishlistId)?.categoryName
                ?: "Wishlist",
        products =
            productController.forWishlist(
                productsResult?.getOrNull().orEmpty(),
                wishlistId
            ),
        errorMessage = productsResult?.exceptionOrNull()?.readableMessage(),
        onProductClick = { productId -> navigator.toProductDetail(productId) },
        onAddItemClick = { navigator.toNewProduct() },
        onHomeClick = navigator::toHome,
        onWishlistsClick = navigator::toWishlists,
        onAddClick = navigator::toAdd,
        onPurchasesClick = navigator::toPurchases,
        onProfileClick = navigator::toProfile
    )
}
