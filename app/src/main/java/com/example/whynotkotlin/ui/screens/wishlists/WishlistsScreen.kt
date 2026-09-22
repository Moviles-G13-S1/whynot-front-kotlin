package com.example.whynotkotlin.ui.screens.wishlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.features.wishlists.application.WishlistUiState
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.components.WhyNotEmptyState
import com.example.whynotkotlin.ui.components.WhyNotErrorBanner
import com.example.whynotkotlin.ui.components.WhyNotLoading
import com.example.whynotkotlin.ui.components.WishlistCard
import com.example.whynotkotlin.ui.theme.WhyNotGray

@Composable
fun WishlistsScreen(
    state: WishlistUiState,
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
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "My Wishlists",
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Every Thing you want, all in one place.",
                style = MaterialTheme.typography.bodyLarge,
                color = WhyNotGray
            )

            Spacer(modifier = Modifier.height(16.dp))

            WhyNotErrorBanner(message = state.errorMessage)

            Spacer(modifier = Modifier.height(8.dp))

            when {
                state.loading -> WhyNotLoading(modifier = Modifier.weight(1f))

                state.summaries.isEmpty() -> WhyNotEmptyState(
                    title = "No wishlists yet",
                    hint = "Create one to start saving the things you want.",
                    modifier = Modifier.weight(1f)
                )

                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(state.summaries) { summary ->
                        WishlistCard(
                            name = summary.categoryName,
                            itemCount = summary.productCount,
                            onClick = { onWishlistClick(summary.wishlist.id) }
                        )
                    }
                }
            }

            Text(
                text = "+ New Wishlist",
                style = MaterialTheme.typography.bodyLarge,
                color = WhyNotGray,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 12.dp, bottom = 10.dp)
                    .clickable { onNewWishlistClick() }
            )
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
