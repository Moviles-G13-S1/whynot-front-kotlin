package com.example.whynotkotlin.ui.screens.products

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.features.products.domain.Product
import com.example.whynotkotlin.ui.components.CategoryChip
import com.example.whynotkotlin.ui.components.ProductImage
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.components.WhyNotEmptyState
import com.example.whynotkotlin.ui.components.WhyNotErrorBanner
import com.example.whynotkotlin.ui.screens.wishlists.asPriceLabel
import com.example.whynotkotlin.ui.theme.WhyNotGray

@Composable
fun ProductDetailScreen(
    product: Product?,
    errorMessage: String?,
    onTogglePurchased: () -> Unit,
    onDeleteClick: () -> Unit,
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .statusBarsPadding()
                .padding(horizontal = 22.dp)
        ) {
            if (product == null) {
                WhyNotEmptyState(
                    title = "Product not available",
                    hint = "It may have been deleted from this wishlist.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.displaySmall,
                        modifier = Modifier.weight(1f)
                    )

                    CategoryChip(
                        text = "Purchased",
                        selected = product.purchased,
                        onClick = onTogglePurchased
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = product.brand,
                    style = MaterialTheme.typography.bodyLarge,
                    color = WhyNotGray
                )

                Spacer(modifier = Modifier.height(14.dp))

                ProductImage(ratio = 1f, cornerRadius = 22)

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.price.asPriceLabel(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = WhyNotGray
                    )

                    Row(
                        modifier = Modifier.clickable { onDeleteClick() },
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = null,
                            tint = WhyNotGray,
                            modifier = Modifier.size(18.dp)
                        )

                        Text(
                            text = "Delete item",
                            style = MaterialTheme.typography.bodyLarge,
                            color = WhyNotGray
                        )
                    }
                }

                if (product.productUrl.isNotBlank()) {
                    Text(
                        text = product.productUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = WhyNotGray,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                WhyNotErrorBanner(message = errorMessage)
            }
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
