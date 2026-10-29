package com.example.whynotkotlin.view.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whynotkotlin.model.NearbyStore
import com.example.whynotkotlin.model.ProductRecommendation
import com.example.whynotkotlin.model.WishlistSummary
import com.example.whynotkotlin.view.components.OfflineBanner
import com.example.whynotkotlin.view.components.WhyNotBottomBar
import com.example.whynotkotlin.view.components.WhyNotPillButton
import com.example.whynotkotlin.view.components.WhyNotTab
import com.example.whynotkotlin.view.components.WishlistCard
import com.example.whynotkotlin.view.theme.Poppins
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.theme.whyNotMuted
import com.example.whynotkotlin.view.theme.whyNotSerif

@Composable
fun HomeScreen(
    showActivation: Boolean,
    onActivate: () -> Unit,
    userName: String?,
    nearbyStore: NearbyStore?,
    nearbyLoading: Boolean,
    nearbyErrorMessage: String?,
    recommendation: ProductRecommendation?,
    recommendationLoading: Boolean,
    recommendationSaving: Boolean,
    recommendationSaved: Boolean,
    recommendationErrorMessage: String?,
    wishlistSummaries: List<WishlistSummary>,
    onLoadNearby: () -> Unit,
    onLoadRecommendation: () -> Unit,
    onSaveRecommendation: (String?) -> Unit,
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit,
    onWishlistClick: (String) -> Unit,
    onNewWishlistClick: () -> Unit,
    onAddClick: () -> Unit,
    onPurchasesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRefreshRecommendation: () -> Unit = onLoadRecommendation,
    recommendationStale: Boolean = false,
    isOnline: Boolean = true,
    onLoadHome: suspend (Boolean, Boolean) -> Unit = { _, _ -> onLoadRecommendation() }
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 18.dp,
                top = 42.dp,
                bottom = 40.dp
            )
        ) {
            item {
                OfflineBanner(
                    isOnline = isOnline,
                    message = "Home is offline. Available wishlists remain accessible; nearby stores and new recommendations need internet.",
                    onRetry = onRefreshRecommendation,
                    modifier = Modifier.padding(end = 18.dp, bottom = 16.dp)
                )

                Text(
                    text = "WHYNOT",
                    style = TextStyle(
                        fontFamily = Poppins,
                        fontSize = 16.sp,
                        letterSpacing = 4.sp,
                        color = WhyNotGray
                    )
                )

                Spacer(modifier = Modifier.height(46.dp))

                val firstName = userName?.trim()?.split(" ")?.firstOrNull().orEmpty()

                Text(
                    text = if (firstName.isEmpty()) "Good Morning" else "Good Morning, $firstName",
                    style = whyNotSerif(27)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "What are we saving today?",
                    style = whyNotMuted(13)
                )

                if (showActivation && wishlistSummaries.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    WhyNotPillButton(text = "Add your first product", onClick = onActivate)
                }
                Spacer(modifier = Modifier.height(38.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Wishlists",
                        style = whyNotSerif(23),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "View all",
                        style = whyNotMuted(13),
                        modifier = Modifier
                            .clickable { onWishlistsClick() }
                            .padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(29.dp))

                WishlistRail(
                    summaries = wishlistSummaries,
                    isOnline = isOnline,
                    onWishlistClick = onWishlistClick,
                    onNewWishlistClick = onNewWishlistClick
                )

                Spacer(modifier = Modifier.height(44.dp))

                HomeSmartSections(
                    nearbyStore = nearbyStore,
                    isOnline = isOnline,
                    onLoadHome = onLoadHome,
                    nearbyLoading = nearbyLoading,
                    nearbyErrorMessage = nearbyErrorMessage,
                    recommendation = recommendation,
                    recommendationLoading = recommendationLoading,
                    recommendationSaving = recommendationSaving,
                    recommendationSaved = recommendationSaved,
                    recommendationErrorMessage = recommendationErrorMessage,
                    wishlistSummaries = wishlistSummaries,
                    onLoadNearby = onLoadNearby,
                    onLoadRecommendation = onLoadRecommendation,
                    onRefreshRecommendation = onRefreshRecommendation,
                    recommendationStale = recommendationStale,
                    onSaveRecommendation = onSaveRecommendation
                )
            }
        }

        WhyNotBottomBar(
            selectedTab = WhyNotTab.HOME,
            onHomeClick = onHomeClick,
            onWishlistsClick = onWishlistsClick,
            onAddClick = onAddClick,
            onPurchasesClick = onPurchasesClick,
            onProfileClick = onProfileClick
        )
    }
}

/** Horizontally scrolling preview of the user's wishlists. */
@Composable
private fun WishlistRail(
    summaries: List<WishlistSummary>,
    isOnline: Boolean,
    onWishlistClick: (String) -> Unit,
    onNewWishlistClick: () -> Unit
) {
    if (summaries.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isOnline) "Create your first wishlist." else "No wishlists available offline yet.",
                    style = whyNotMuted(13)
                )

                Spacer(modifier = Modifier.height(16.dp))

                WhyNotPillButton(
                    text = "Create a wishlist",
                    onClick = onNewWishlistClick
                )
            }
        }
        return
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp),
        contentPadding = PaddingValues(end = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(17.dp)
    ) {
        items(summaries, key = { it.wishlist.id }) { summary ->
            WishlistCard(
                name = summary.categoryName,
                itemCount = summary.productCount,
                imageUrl = summary.wishlist.imageUrl,
                onClick = { onWishlistClick(summary.wishlist.id) },
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(131.dp)
            )
        }
    }
}
