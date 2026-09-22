package com.example.whynotkotlin.ui.screens.wishlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.features.products.domain.Product
import com.example.whynotkotlin.ui.components.CategoryChip
import com.example.whynotkotlin.ui.components.ProductCard
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.components.WhyNotEmptyState
import com.example.whynotkotlin.ui.components.WhyNotErrorBanner
import com.example.whynotkotlin.ui.theme.WhyNotBorder
import com.example.whynotkotlin.ui.theme.WhyNotGray

/** The orderings the detail screen offers over an already-loaded list. */
private enum class ProductSort { NEWEST, CHEAPEST, PENDING }

@Composable
fun WishlistDetailScreen(
    wishlistName: String,
    products: List<Product>,
    errorMessage: String?,
    onProductClick: (String) -> Unit,
    onAddItemClick: () -> Unit,
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var sort by remember { mutableStateOf(ProductSort.NEWEST) }

    // Sorting and filtering happen in memory: the backend ships no composite
    // indexes, so a server-side sort would need one that does not exist.
    val visible = when (sort) {
        ProductSort.NEWEST -> products
        ProductSort.CHEAPEST -> products.sortedBy { it.price }
        ProductSort.PENDING -> products.filterNot { it.purchased }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .statusBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = wishlistName,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(horizontal = 22.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${products.size} items",
                    style = MaterialTheme.typography.bodyLarge,
                    color = WhyNotGray
                )

                Text(
                    text = "+ Add item",
                    style = MaterialTheme.typography.bodyLarge,
                    color = WhyNotGray,
                    modifier = Modifier.clickable { onAddItemClick() }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = WhyNotBorder)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CategoryChip(
                    text = "Newest",
                    icon = Icons.Outlined.SwapVert,
                    selected = sort == ProductSort.NEWEST,
                    onClick = { sort = ProductSort.NEWEST }
                )

                CategoryChip(
                    text = "Cheapest",
                    icon = Icons.Outlined.Percent,
                    selected = sort == ProductSort.CHEAPEST,
                    onClick = { sort = ProductSort.CHEAPEST }
                )

                CategoryChip(
                    text = "Pending",
                    selected = sort == ProductSort.PENDING,
                    onClick = { sort = ProductSort.PENDING }
                )
            }

            HorizontalDivider(color = WhyNotBorder)

            WhyNotErrorBanner(
                message = errorMessage,
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp)
            )

            if (visible.isEmpty()) {
                WhyNotEmptyState(
                    title = "Nothing here yet",
                    hint = "Use \"Add item\" to save your first product.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        horizontal = 22.dp,
                        vertical = 24.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(visible, key = { it.id }) { product ->
                        ProductCard(
                            name = product.name,
                            price = product.price.asPriceLabel(),
                            onClick = { onProductClick(product.id) }
                        )
                    }
                }
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

/** Formats a stored price. Whole amounts drop the decimals. */
fun Double.asPriceLabel(): String =
    if (this % 1.0 == 0.0) "\$${toLong()}" else String.format("\$%.2f", this)
