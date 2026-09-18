package com.example.whynotkotlin.ui.screens.wishlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.components.WishlistCard
import com.example.whynotkotlin.ui.theme.WhyNotGray

data class WishlistUi(
    val name: String,
    val itemCount: Int
)

private val sampleWishlists = listOf(
    WishlistUi("Beauty", 6),
    WishlistUi("Clothes", 6),
    WishlistUi("Tech", 6)
)

@Composable
fun WishlistsScreen(
    onWishlistClick: (WishlistUi) -> Unit,
    onNewWishlistClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(sampleWishlists) { wishlist ->
                    WishlistCard(
                        name = wishlist.name,
                        itemCount = wishlist.itemCount,
                        onClick = { onWishlistClick(wishlist) }
                    )
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
            onProfileClick = onProfileClick,
            onAddClick = onAddClick,
            onWishlistsClick = onWishlistsClick
        )
    }
}
