package com.example.whynotkotlin.view.screens.purchases

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.view.components.WhyNotBottomBar
import com.example.whynotkotlin.view.components.WhyNotTab
import com.example.whynotkotlin.view.screens.wishlists.asPriceLabel
import com.example.whynotkotlin.view.theme.Poppins
import com.example.whynotkotlin.view.theme.WhyNotBeige
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.theme.WhyNotSearch
import com.example.whynotkotlin.view.theme.whyNotMuted
import com.example.whynotkotlin.view.theme.whyNotSerif

private enum class PurchaseSort { NONE, LOW_TO_HIGH, HIGH_TO_LOW }

@Composable
fun PurchasesScreen(
    purchases: List<Product>,
    loading: Boolean,
    errorMessage: String?,
    onProductClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    onWishlistsClick: () -> Unit,
    onAddClick: () -> Unit,
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit
) {
    var sort by rememberSaveable { mutableStateOf(PurchaseSort.NONE) }

    val visiblePurchases = when (sort) {
        PurchaseSort.LOW_TO_HIGH -> purchases.sortedBy { it.price }
        PurchaseSort.HIGH_TO_LOW -> purchases.sortedByDescending { it.price }
        PurchaseSort.NONE -> purchases
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            when {
                loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                errorMessage != null -> Text(
                    text = errorMessage,
                    style = whyNotMuted(14),
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 47.dp, bottom = 40.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 25.dp)) {
                        Text(text = "Purchases", style = whyNotSerif(30))

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "${purchases.size} items", style = whyNotMuted(15))
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .background(WhyNotSearch)
                    ) {
                        SortOption(
                            icon = Icons.Filled.ArrowUpward,
                            label = "Low to high",
                            selected = sort == PurchaseSort.LOW_TO_HIGH,
                            onClick = {
                                sort = if (sort == PurchaseSort.LOW_TO_HIGH) PurchaseSort.NONE
                                else PurchaseSort.LOW_TO_HIGH
                            },
                            modifier = Modifier.weight(1f)
                        )

                        SortOption(
                            icon = Icons.Filled.ArrowDownward,
                            label = "High to low",
                            selected = sort == PurchaseSort.HIGH_TO_LOW,
                            onClick = {
                                sort = if (sort == PurchaseSort.HIGH_TO_LOW) PurchaseSort.NONE
                                else PurchaseSort.HIGH_TO_LOW
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (purchases.isEmpty()) {
                        Text(
                            text = "You have no purchased items yet.",
                            style = whyNotMuted(14),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 25.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(horizontal = 25.dp),
                            verticalArrangement = Arrangement.spacedBy(30.dp)
                        ) {
                            visiblePurchases.chunked(2).forEach { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                    row.forEach { purchase ->
                                        PurchaseCard(
                                            purchase = purchase,
                                            onClick = { onProductClick(purchase.id) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    if (row.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        WhyNotBottomBar(
            selectedTab = WhyNotTab.PURCHASES,
            onHomeClick = onHomeClick,
            onWishlistsClick = onWishlistsClick,
            onAddClick = onAddClick,
            onPurchasesClick = onPurchasesClick,
            onProfileClick = onProfileClick
        )
    }
}

@Composable
private fun SortOption(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = if (selected) Color.Black else WhyNotGray

    Row(
        modifier = modifier
            .height(55.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = label,
            style = TextStyle(
                fontFamily = Poppins,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Light,
                color = color
            )
        )
    }
}

@Composable
private fun PurchaseCard(
    purchase: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.68f)
                .clip(RoundedCornerShape(18.dp))
                .background(WhyNotBeige)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = purchase.name.ifEmpty { "Product" },
            style = whyNotSerif(20),
            modifier = Modifier.padding(start = 4.dp)
        )

        if (purchase.brand.isNotEmpty()) {
            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = purchase.brand,
                style = whyNotMuted(11),
                modifier = Modifier.padding(start = 5.dp)
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = purchase.price.asPriceLabel(),
            style = whyNotMuted(12),
            modifier = Modifier.padding(start = 5.dp)
        )
    }
}
