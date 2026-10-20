package com.example.whynotkotlin.view.screens.products

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.model.readableMessage
import com.example.whynotkotlin.view.components.rememberSubmission
import com.example.whynotkotlin.view.navigation.WhyNotNavigator

@Composable
fun ProductDetailRoute(
    productId: String,
    navigator: WhyNotNavigator
) {
    val productController = LocalAppDependencies.current.productController
    val productsResult by productController.products.collectAsStateWithLifecycle()
    val submission = rememberSubmission()

    val product = productController.find(productsResult?.getOrNull().orEmpty(), productId)

    ProductDetailScreen(
        product = product,
        errorMessage = submission.errorMessage
            ?: productsResult?.exceptionOrNull()?.readableMessage(),
        onMarkPurchased = {
            product?.let {
                submission.run {
                    productController.markPurchased(it)
                }
            }
        },
        onDeleteClick = {
            product?.let {
                submission.run(onSuccess = navigator::back) {
                    productController.deleteProduct(it.id)
                }
            }
        },
        onHomeClick = navigator::toHome,
        onWishlistsClick = navigator::toWishlists,
        onAddClick = navigator::toAdd,
        onPurchasesClick = navigator::toPurchases,
        onProfileClick = navigator::toProfile
    )
}

@Composable
fun AddProductRoute(navigator: WhyNotNavigator) {
    AddProductScreen(
        onContinueWithLink = navigator::toNewProduct,
        onAddManuallyClick = { navigator.toNewProduct() },
        onHomeClick = navigator::toHome,
        onWishlistsClick = navigator::toWishlists,
        onAddClick = {},
        onPurchasesClick = navigator::toPurchases,
        onProfileClick = navigator::toProfile
    )
}

@Composable
fun NewProductRoute(
    prefilledProductUrl: String,
    navigator: WhyNotNavigator
) {
    val dependencies = LocalAppDependencies.current
    val productController = dependencies.productController
    val catalogResult by dependencies.wishlistController.catalog.collectAsStateWithLifecycle()
    val productsResult by productController.products.collectAsStateWithLifecycle()
    val submission = rememberSubmission()

    NewProductScreen(
        wishlists = catalogResult?.getOrNull()?.summaries.orEmpty(),
        prefilledProductUrl = prefilledProductUrl,
        saving = submission.running,
        errorMessage = submission.errorMessage
            ?: productsResult?.exceptionOrNull()?.readableMessage(),
        onSave = { wishlistId, name, brand, price, imageUrl, productUrl ->
            submission.run(onSuccess = navigator::back) {
                productController.createProduct(
                    wishlistId = wishlistId,
                    name = name,
                    brand = brand,
                    price = price,
                    imageUrl = imageUrl,
                    productUrl = productUrl
                )
            }
        },
        onHomeClick = navigator::toHome,
        onWishlistsClick = navigator::toWishlists,
        onAddClick = navigator::toAdd,
        onPurchasesClick = navigator::toPurchases,
        onProfileClick = navigator::toProfile
    )
}
