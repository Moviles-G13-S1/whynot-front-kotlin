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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.whynotkotlin.ui.components.CategoryChip
import com.example.whynotkotlin.ui.components.ProductCard
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.theme.WhyNotBorder
import com.example.whynotkotlin.ui.theme.WhyNotGray

data class ProductUi(
    val name: String,
    val price: String,
    val originalPrice: String = ""
)

private val sampleProducts = listOf(
    ProductUi("Item name", "\$100"),
    ProductUi("Item name", "\$100"),
    ProductUi("Item name", "\$50", "\$100")
)

private val wishlistFilters = listOf(
    "⇅" to "Filter",
    "%" to "Filter",
    "☷" to "Filter"
)

@Composable
fun WishlistDetailScreen(
    wishlistName: String,
    onProductClick: (ProductUi) -> Unit,
    onAddItemClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = wishlistName,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${sampleProducts.size} items",
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
                wishlistFilters.forEachIndexed { index, filter ->
                    CategoryChip(
                        text = filter.second,
                        icon = filter.first,
                        selected = selectedFilter == index,
                        onClick = {
                            selectedFilter = if (selectedFilter == index) -1 else index
                        }
                    )
                }
            }

            HorizontalDivider(color = WhyNotBorder)

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    horizontal = 24.dp,
                    vertical = 24.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(sampleProducts) { product ->
                    ProductCard(
                        name = product.name,
                        price = product.price,
                        originalPrice = product.originalPrice,
                        onClick = { onProductClick(product) }
                    )
                }
            }
        }

        WhyNotBottomBar(
            onProfileClick = onProfileClick,
            onAddClick = onAddClick,
            onWishlistsClick = onWishlistsClick
        )
    }
}
