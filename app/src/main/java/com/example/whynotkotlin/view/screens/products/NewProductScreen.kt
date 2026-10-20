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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.model.WishlistSummary
import com.example.whynotkotlin.view.components.CategoryChip
import com.example.whynotkotlin.view.components.ProductPictureField
import com.example.whynotkotlin.view.components.WhyNotBottomBar
import com.example.whynotkotlin.view.components.WhyNotButton
import com.example.whynotkotlin.view.components.WhyNotErrorBanner
import com.example.whynotkotlin.view.components.WhyNotTextField
import com.example.whynotkotlin.view.theme.WhyNotCream
import com.example.whynotkotlin.view.theme.WhyNotGray

/**
 * The manual product form.
 *
 * `brand` and `price` are required by the Security Rules, so they are part of
 * the form even though the original mockup only showed name and picture.
 *
 * A product can only be saved into one of the user's wishlists. Without any,
 * the form points to creating one instead of offering a save that cannot work.
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
    onCreateWishlistClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var productUrl by remember { mutableStateOf(prefilledProductUrl) }
    var selectedWishlistId by remember { mutableStateOf("") }

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

            Spacer(modifier = Modifier.height(20.dp))

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
                    label = "Name"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    WhyNotTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = "Brand",
                        modifier = Modifier.weight(0.6f)
                    )

                    Spacer(modifier = Modifier.size(12.dp))

                    WhyNotTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = "Price",
                        placeholder = "0",
                        modifier = Modifier.weight(0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                ProductPictureField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                ProductPictureField(
                    value = productUrl,
                    onValueChange = { productUrl = it },
                    label = "Store link",
                    placeholder = "https://www.somestore.com/..."
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
                            selected = selectedWishlistId == summary.wishlist.id,
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            WhyNotErrorBanner(message = errorMessage)

            Spacer(modifier = Modifier.height(16.dp))

            WhyNotButton(
                text = if (saving) "Saving..." else "Save Item",
                onClick = {
                    onSave(selectedWishlistId, name, brand, price, imageUrl, productUrl)
                },
                filled = true,
                enabled = !saving &&
                    name.isNotBlank() &&
                    brand.isNotBlank() &&
                    price.isNotBlank() &&
                    selectedWishlistId.isNotBlank()
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
