package com.example.whynotkotlin.view.screens.wishlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.model.WishlistSummary
import com.example.whynotkotlin.view.components.WhyNotBottomBar
import com.example.whynotkotlin.view.components.WhyNotTab
import com.example.whynotkotlin.view.components.WishlistCard
import com.example.whynotkotlin.view.theme.whyNotMuted
import com.example.whynotkotlin.view.theme.whyNotSerif

@Composable
fun WishlistsScreen(
    summaries: List<WishlistSummary>,
    loading: Boolean,
    errorMessage: String?,
    onWishlistClick: (String) -> Unit,
    onNewWishlistClick: () -> Unit,
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
                .padding(start = 25.dp, end = 25.dp, top = 47.dp, bottom = 23.dp)
        ) {
            Text(
                text = "My Wishlists",
                style = whyNotSerif(30)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Every thing you want, all in one place.",
                style = whyNotMuted(15)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {
                    loading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )

                    errorMessage != null -> Text(
                        text = "Could not load wishlists.",
                        style = whyNotMuted(14),
                        modifier = Modifier.align(Alignment.Center)
                    )

                    summaries.isEmpty() -> Text(
                        text = "You do not have any wishlists yet.",
                        style = whyNotMuted(14),
                        modifier = Modifier.align(Alignment.Center)
                    )

                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(summaries, key = { it.wishlist.id }) { summary ->
                            WishlistCard(
                                name = summary.categoryName,
                                itemCount = summary.productCount,
                                imageUrl = summary.wishlist.imageUrl,
                                onClick = { onWishlistClick(summary.wishlist.id) }
                            )
                        }
                    }
                }
            }

            Text(
                text = "New Wishlist",
                style = whyNotMuted(15),
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onNewWishlistClick() }
            )
        }

        WhyNotBottomBar(
            selectedTab = WhyNotTab.WISHLISTS,
            onHomeClick = onHomeClick,
            onWishlistsClick = onWishlistsClick,
            onAddClick = onAddClick,
            onPurchasesClick = onPurchasesClick,
            onProfileClick = onProfileClick
        )
    }
}
