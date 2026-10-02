package com.example.whynotkotlin.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whynotkotlin.features.nearby.application.NearbyStoreUiState
import com.example.whynotkotlin.features.recommendations.application.RecommendationUiState
import com.example.whynotkotlin.features.wishlists.domain.WishlistSummary
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.theme.WhyNotBeige
import com.example.whynotkotlin.ui.theme.WhyNotBorder
import com.example.whynotkotlin.ui.theme.WhyNotGray

@Composable
fun HomeScreen(
    userName: String?,
    nearbyState: NearbyStoreUiState,
    recommendationState: RecommendationUiState,
    wishlistSummaries: List<WishlistSummary>,
    onLoadNearby: () -> Unit,
    onLoadRecommendation: () -> Unit,
    onSaveRecommendation: (String) -> Unit,
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit,
    onWishlistClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onPurchasesClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 22.dp,
                end = 22.dp,
                top = 34.dp,
                bottom = 26.dp
            )
        ) {
            item {
                Text(
                    text = "W H Y N O T",
                    style = MaterialTheme.typography.bodySmall.copy(
                        letterSpacing = 2.sp
                    ),
                    color = WhyNotGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userName?.trim()?.takeIf { it.isNotEmpty() }
                        ?.let { "Good Morning, $it" }
                        ?: "Good Morning",
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = "What are we saving today?",
                    style = MaterialTheme.typography.bodySmall,
                    color = WhyNotGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                SearchBar()

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Your Wishlists",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "View all",
                        style = MaterialTheme.typography.labelSmall,
                        color = WhyNotGray,
                        modifier = Modifier.clickable {
                            onWishlistsClick()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (wishlistSummaries.isEmpty()) {
                    Text(
                        text = "You do not have any wishlists yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WhyNotGray
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        wishlistSummaries
                            .take(3)
                            .forEach { summary ->
                                WishlistPreview(
                                    label = summary.categoryName,
                                    onClick = {
                                        onWishlistClick(
                                            summary.wishlist.id
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                HomeSmartSections(
                    nearbyState = nearbyState,
                    recommendationState = recommendationState,
                    wishlistSummaries = wishlistSummaries,
                    onLoadNearby = onLoadNearby,
                    onLoadRecommendation = onLoadRecommendation,
                    onSaveRecommendation = onSaveRecommendation,
                    onOpenWishlists = onWishlistsClick
                )
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
private fun SearchBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                WhyNotBorder.copy(alpha = 0.42f)
            )
            .padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = "Search",
            tint = WhyNotGray,
            modifier = Modifier.size(18.dp)
        )

        Text(
            text = "Search",
            style = MaterialTheme.typography.bodySmall,
            color = WhyNotGray,
            modifier = Modifier
                .weight(1f)
                .padding(start = 7.dp)
        )

        Icon(
            imageVector = Icons.Outlined.Mic,
            contentDescription = "Voice search",
            tint = WhyNotGray,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun WishlistPreview(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PlaceholderImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun PlaceholderImage(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(WhyNotBeige)
    )
}
