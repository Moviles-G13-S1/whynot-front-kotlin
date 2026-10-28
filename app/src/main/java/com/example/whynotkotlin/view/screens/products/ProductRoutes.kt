package com.example.whynotkotlin.view.screens.products

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.model.readableMessage
import com.example.whynotkotlin.view.components.WhyNotTab
import com.example.whynotkotlin.view.components.rememberSubmission
import com.example.whynotkotlin.view.navigation.WhyNotNavigator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.whynotkotlin.model.ProductFormDraft
import com.example.whynotkotlin.view.components.WhyNotLoading
import com.example.whynotkotlin.view.components.rememberIsOnline

@Composable
fun ProductDetailRoute(
    productId: String,
    sourceTab: WhyNotTab,
    navigator: WhyNotNavigator
) {
    val productController = LocalAppDependencies.current.productController
    val productsResult by productController.products.collectAsStateWithLifecycle()
    val submission = rememberSubmission()

    val product = productController.find(productsResult?.getOrNull().orEmpty(), productId)

    ProductDetailScreen(
        product = product,
        isOnline = rememberIsOnline(),
        loading = productsResult == null,
        errorMessage = submission.errorMessage
            ?: productsResult?.exceptionOrNull()?.readableMessage(),
        sourceTab = sourceTab,
        onMarkPurchased = { purchasePrice ->
            product?.let {
                submission.run {
                    productController.markPurchased(it, purchasePrice)
                }
            }
        },
        onEditClick = { navigator.toEditProduct(productId, sourceTab) },
        onDeleteConfirmed = {
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
fun EditProductRoute(
    productId: String,
    sourceTab: WhyNotTab,
    navigator: WhyNotNavigator
) {
    val dependencies = LocalAppDependencies.current
    val productController = dependencies.productController
    val productsResult by productController.products.collectAsStateWithLifecycle()
    val catalogResult by dependencies.wishlistController.catalog.collectAsStateWithLifecycle()
    val submission = rememberSubmission()

    EditProductScreen(
        isOnline = rememberIsOnline(),
        product = productController.find(productsResult?.getOrNull().orEmpty(), productId),
        loading = productsResult == null || catalogResult == null,
        wishlists = catalogResult?.getOrNull()?.summaries.orEmpty(),
        sourceTab = sourceTab,
        saving = submission.running,
        errorMessage = submission.errorMessage,
        onSave = { wishlistId, name, brand, price, imageUrl, productUrl ->
            submission.run(onSuccess = navigator::back) {
                productController.updateProduct(
                    productId = productId,
                    wishlistId = wishlistId,
                    name = name,
                    brand = brand,
                    price = price,
                    imageUrl = imageUrl,
                    productUrl = productUrl
                )
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
fun NewProductRoute(
    prefilledProductUrl: String,
    navigator: WhyNotNavigator
) {
    val dependencies = LocalAppDependencies.current
    val productController = dependencies.productController
    val catalogResult by dependencies.wishlistController.catalog.collectAsStateWithLifecycle()
    val productsResult by productController.products.collectAsStateWithLifecycle()
    val submission = rememberSubmission()

    // The draft is read from local storage once, before the form is shown, so
    // the fields start with what the user left instead of filling in later.
    var draftLoaded by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf<ProductFormDraft?>(null) }

    LaunchedEffect(Unit) {
        draft = productController.loadDraft()
        draftLoaded = true
    }

    if (!draftLoaded) {
        WhyNotLoading()
        return
    }

    NewProductScreen(
        initialDraft = draft,
        isOnline = rememberIsOnline(),
        onDraftChange = productController::saveDraft,
        onDiscardDraft = productController::discardDraft,
        wishlists = catalogResult?.getOrNull()?.summaries.orEmpty(),
        loadingWishlists = catalogResult == null,
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
        onCreateWishlistClick = navigator::toNewWishlist,
        onHomeClick = navigator::toHome,
        onWishlistsClick = navigator::toWishlists,
        onAddClick = navigator::toAdd,
        onPurchasesClick = navigator::toPurchases,
        onProfileClick = navigator::toProfile
    )
}
