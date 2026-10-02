package com.example.whynotkotlin.ui.screens.purchases

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.features.products.domain.Product
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.theme.WhyNotBeige
import com.example.whynotkotlin.ui.theme.WhyNotBorder
import com.example.whynotkotlin.ui.theme.WhyNotGray
import com.example.whynotkotlin.ui.theme.WhyNotWhite

@Composable
fun PurchasesScreen(
    purchases: List<Product>,
    onHomeClick: () -> Unit,
    onWishlistsClick: () -> Unit,
    onAddClick: () -> Unit,
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 34.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp)
            ) {
                Text(
                    text = "Purchases",
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "${purchases.size} ${if (purchases.size == 1) "item" else "items"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = WhyNotGray
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(
                color = WhyNotBorder
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 22.dp,
                        vertical = 10.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    label = "Filter",
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    label = "Filter",
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    label = "Filter",
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(
                color = WhyNotBorder
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding =
                androidx.compose.foundation.layout.PaddingValues(
                    start = 22.dp,
                    end = 22.dp,
                    top = 22.dp,
                    bottom = 24.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(
                items = purchases,
                key = { product -> product.id }
            ) { purchase ->
                PurchaseCard(purchase)
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

@Composable
private fun FilterChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(32.dp),
        shape = RoundedCornerShape(18.dp),
        color = WhyNotWhite,
        border = BorderStroke(
            width = 1.dp,
            color = WhyNotBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = WhyNotGray
            )
        }
    }
}

@Composable
private fun PurchaseCard(
    purchase: Product
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(176.dp)
                .clip(
                    RoundedCornerShape(14.dp)
                )
                .background(WhyNotBeige)
        )

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        Text(
            text = purchase.name,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = String.format(
                "$%.2f",
                purchase.price
            ),
            style = MaterialTheme.typography.labelSmall,
            color = WhyNotGray
        )
    }
}