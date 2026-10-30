package com.example.whynotkotlin.view.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.controller.ProductController
import com.example.whynotkotlin.model.InputRules
import com.example.whynotkotlin.model.PriceRules
import com.example.whynotkotlin.model.WishlistSummary
import com.example.whynotkotlin.view.components.FieldSupportText
import com.example.whynotkotlin.view.components.CategoryChip
import com.example.whynotkotlin.view.components.ProductPictureField
import com.example.whynotkotlin.view.components.WhyNotBottomBar
import com.example.whynotkotlin.view.components.WhyNotButton
import com.example.whynotkotlin.view.components.WhyNotErrorBanner
import com.example.whynotkotlin.view.components.WhyNotTextField
import com.example.whynotkotlin.view.theme.WhyNotCream
import com.example.whynotkotlin.view.theme.WhyNotGray
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import com.example.whynotkotlin.model.ProductFormDraft
import com.example.whynotkotlin.view.components.OfflineBanner

/**
 * The manual product form.
 *
 * `brand` and `price` are required by the Security Rules, so they are part of
 * the form even though the original mockup only showed name and picture.
 *
 * A product can only be saved into one of the user's wishlists. Without any,
 * the form points to creating one instead of offering a save that cannot work.
 *
 * Local storage: every change is reported through [onDraftChange], which keeps
 * the form on this phone, and [initialDraft] brings it back when the user
 * returns, even after closing the app or without a connection.
 *
 * Eventual connectivity: offline, the user can keep filling the form (it is
 * kept as a draft) but Save waits until the connection returns.
 *
 * Each field shows its own error underneath once the user leaves it, with the
 * rules [ProductController] applies. Tapping Save with errors shows them all
 * and reports the rejected attempt through [onRejectedSave] (BQ Type 2)
 * instead of saving.
 */
@Composable
fun NewProductScreen(
    wishlists: List<WishlistSummary>,
    loadingWishlists: Boolean = false,
    prefilledProductUrl: String,
    saving: Boolean,
    errorMessage: String?,
    onSave: (
        wishlistId: String,
        name: String,
        brand: String,
        price: String,
        imageUrl: String,
        productUrl: String
    ) -> Unit,
    onRejectedSave: (
        wishlistId: String,
        name: String,
        brand: String,
        price: String,
        imageUrl: String,
        productUrl: String
    ) -> Unit = { _, _, _, _, _, _ -> },
    onCreateWishlistClick: () -> Unit = {},
    initialDraft: ProductFormDraft? = null,
    isOnline: Boolean = true,
    onDraftChange: (ProductFormDraft) -> Unit = {},
    onDiscardDraft: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var name by rememberSaveable { mutableStateOf(initialDraft?.name.orEmpty()) }
    var brand by rememberSaveable { mutableStateOf(initialDraft?.brand.orEmpty()) }
    var price by rememberSaveable { mutableStateOf(initialDraft?.price.orEmpty()) }
    var imageUrl by rememberSaveable { mutableStateOf(initialDraft?.imageUrl.orEmpty()) }
    var productUrl by rememberSaveable {
        mutableStateOf(prefilledProductUrl.ifBlank { initialDraft?.productUrl.orEmpty() })
    }
    var selectedWishlistId by rememberSaveable { mutableStateOf(initialDraft?.wishlistId.orEmpty()) }
    var showRestoredNote by rememberSaveable { mutableStateOf(initialDraft != null) }
    var showAllErrors by rememberSaveable { mutableStateOf(false) }

    // A restored draft may point at a wishlist that no longer exists.
    val wishlistId = selectedWishlistId.takeIf { id ->
        wishlists.any { it.wishlist.id == id }
    }.orEmpty()

    val nameError = InputRules.productName(name)
    val brandError = InputRules.brand(brand)
    val priceError = PriceRules.error(price)
    val imageUrlError = InputRules.optionalLink(imageUrl, ProductController.PICTURE_LINK)
    val productUrlError = InputRules.optionalLink(productUrl, ProductController.STORE_LINK)
    val wishlistError = if (wishlistId.isBlank()) "Choose a wishlist" else null
    val formValid = listOf(
        nameError, brandError, priceError, imageUrlError, productUrlError, wishlistError
    ).all { it == null }

    // Every change goes to the controller, which debounces the disk write.
    LaunchedEffect(name, brand, price, imageUrl, productUrl, selectedWishlistId) {
        onDraftChange(
            ProductFormDraft(
                wishlistId = selectedWishlistId,
                name = name,
                brand = brand,
                price = price,
                imageUrl = imageUrl,
                productUrl = productUrl
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "New Product",
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(modifier = Modifier.height(12.dp))

            OfflineBanner(
                isOnline = isOnline,
                message = "You're offline. What you type is kept on this phone as a draft.",
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (showRestoredNote) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "We kept the product you were adding.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WhyNotGray,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "Discard",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable {
                            name = ""
                            brand = ""
                            price = ""
                            imageUrl = ""
                            productUrl = prefilledProductUrl
                            selectedWishlistId = ""
                            showRestoredNote = false
                            onDiscardDraft()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        WhyNotCream,
                        RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                WhyNotTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name",
                    error = nameError,
                    showError = showAllErrors,
                    maxLength = InputRules.PRODUCT_NAME_MAX_LENGTH
                )

                Spacer(modifier = Modifier.height(14.dp))

                WhyNotTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = "Brand",
                    error = brandError,
                    showError = showAllErrors,
                    maxLength = InputRules.BRAND_MAX_LENGTH
                )

                Spacer(modifier = Modifier.height(14.dp))

                WhyNotTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = "Price",
                    placeholder = "1500",
                    error = priceError,
                    showError = showAllErrors,
                    keyboardType = KeyboardType.Decimal,
                    maxLength = PriceRules.MAX_LENGTH
                )

                Spacer(modifier = Modifier.height(14.dp))

                ProductPictureField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    error = imageUrlError,
                    showError = showAllErrors
                )

                Spacer(modifier = Modifier.height(14.dp))

                ProductPictureField(
                    value = productUrl,
                    onValueChange = { productUrl = it },
                    label = "Store link",
                    placeholder = "https://www.somestore.com/...",
                    error = productUrlError,
                    showError = showAllErrors
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Save to:",
                style = MaterialTheme.typography.bodyLarge,
                color = WhyNotGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (loadingWishlists) {
                Text(
                    text = "Loading your wishlists...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WhyNotGray
                )
            } else if (wishlists.isEmpty()) {
                Text(
                    text = "Create a wishlist before adding a product.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WhyNotGray
                )

                Spacer(modifier = Modifier.height(10.dp))

                WhyNotButton(
                    text = "Create wishlist",
                    onClick = onCreateWishlistClick
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    wishlists.forEach { summary ->
                        CategoryChip(
                            text = summary.categoryName,
                            selected = wishlistId == summary.wishlist.id,
                            onClick = {
                                selectedWishlistId =
                                    if (selectedWishlistId == summary.wishlist.id) {
                                        ""
                                    } else {
                                        summary.wishlist.id
                                    }
                            }
                        )
                    }
                }

                FieldSupportText(
                    error = wishlistError.takeIf { showAllErrors },
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            WhyNotErrorBanner(message = errorMessage)

            Spacer(modifier = Modifier.height(16.dp))

            WhyNotButton(
                text = when {
                    saving -> "Saving..."
                    !isOnline -> "Save when back online"
                    else -> "Save Item"
                },
                onClick = {
                    if (formValid) {
                        onSave(wishlistId, name, brand, price, imageUrl, productUrl)
                    } else {
                        showAllErrors = true
                        onRejectedSave(wishlistId, name, brand, price, imageUrl, productUrl)
                    }
                },
                filled = true,
                enabled = !saving && isOnline && wishlists.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        WhyNotBottomBar(
            onHomeClick = onHomeClick,
            onWishlistsClick = onWishlistsClick,
            onAddClick = onAddClick,
            onPurchasesClick = onPurchasesClick,
            onProfileClick = onProfileClick
        )
    }
}
