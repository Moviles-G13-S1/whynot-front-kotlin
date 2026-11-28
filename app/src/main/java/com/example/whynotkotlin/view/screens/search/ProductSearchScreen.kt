package com.example.whynotkotlin.view.screens.search

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.model.Category
import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.model.SearchFilters
import com.example.whynotkotlin.model.SearchSource
import com.example.whynotkotlin.model.SearchState
import com.example.whynotkotlin.view.components.OfflineBanner
import com.example.whynotkotlin.view.components.WhyNotBottomBar
import com.example.whynotkotlin.view.components.WhyNotDropdownField
import com.example.whynotkotlin.view.components.WhyNotTab
import com.example.whynotkotlin.view.components.WhyNotTextField
import com.example.whynotkotlin.view.screens.wishlists.asPriceLabel
import com.example.whynotkotlin.view.theme.WhyNotBeige
import com.example.whynotkotlin.view.theme.WhyNotCream
import com.example.whynotkotlin.view.theme.whyNotMuted
import com.example.whynotkotlin.view.theme.whyNotSerif

/** Navigation of the search screen (bottom bar and back). */
data class SearchNavigation(
    val onBackClick: () -> Unit,
    val onHomeClick: () -> Unit,
    val onWishlistsClick: () -> Unit,
    val onAddClick: () -> Unit,
    val onPurchasesClick: () -> Unit,
    val onProfileClick: () -> Unit
)

/**
 * Search products (Sprint 4, Miguel): find saved items across every wishlist
 * by name or brand, filtered by category and price range.
 *
 * Only draws [state] and reports what the user does; the controller searches.
 * A [LazyColumn] lays out only the rows on screen, so a long result list does
 * not compose every product at once.
 */
@Composable
fun ProductSearchScreen(
    state: SearchState,
    filters: SearchFilters,
    categories: List<Category>,
    isOnline: Boolean,
    onFiltersChange: (SearchFilters) -> Unit,
    onClearFilters: () -> Unit,
    onRefresh: () -> Unit,
    onProductClick: (String) -> Unit,
    navigation: SearchNavigation
) {
    // Built once per category list, not on every recomposition.
    val categoryNames = remember(categories) { categories.associate { it.id to it.name } }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 25.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "header") {
                Column {
                    TextButton(onClick = navigation.onBackClick) { Text("‹ Wishlists", style = whyNotMuted(14)) }

                    OfflineBanner(
                        isOnline = isOnline,
                        message = "You're offline. You can search the products saved on this phone; results may be outdated.",
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(text = "Search products", style = whyNotSerif(30))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Find saved items across all your wishlists.", style = whyNotMuted(14))
                    Spacer(modifier = Modifier.height(18.dp))

                    WhyNotTextField(
                        value = filters.query,
                        onValueChange = { onFiltersChange(filters.copy(query = it)) },
                        label = "Search",
                        placeholder = "Search by name or brand",
                        maxLength = 100
                    )
                }
            }

            item(key = "filters") {
                Column {
                    Text(text = "Filters", style = whyNotSerif(22))
                    Spacer(modifier = Modifier.height(10.dp))

                    WhyNotDropdownField(
                        label = "Category",
                        selectedValue = filters.categoryId,
                        options = listOf("" to "All categories") + categories.map { it.id to it.name },
                        onValueChange = { onFiltersChange(filters.copy(categoryId = it)) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        WhyNotTextField(
                            value = filters.minPrice,
                            onValueChange = { onFiltersChange(filters.copy(minPrice = it)) },
                            label = "Min price",
                            keyboardType = KeyboardType.Decimal,
                            maxLength = 16,
                            modifier = Modifier.weight(1f)
                        )
                        WhyNotTextField(
                            value = filters.maxPrice,
                            onValueChange = { onFiltersChange(filters.copy(maxPrice = it)) },
                            label = "Max price",
                            keyboardType = KeyboardType.Decimal,
                            maxLength = 16,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (!filters.isEmpty()) {
                        TextButton(onClick = onClearFilters) { Text("Clear filters", style = whyNotMuted(14)) }
                    }
                }
            }

            item(key = "notices") {
                SearchNotices(state = state, isOnline = isOnline, onRefresh = onRefresh)
            }

            item(key = "results-title") {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = "Results", style = whyNotSerif(22))
                    if (!state.loading && !state.invalidPriceRange) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${state.results.size} of ${state.totalProducts}", style = whyNotMuted(13))
                    }
                }
            }

            val message = emptyMessage(state, isOnline)
            when {
                state.loading -> item(key = "loading") {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
                }

                message != null -> item(key = "message") {
                    Text(text = message, style = whyNotMuted(14))
                }

                else -> items(state.results, key = { it.id }) { product ->
                    SearchResultRow(
                        product = product,
                        categoryName = categoryNames[product.categoryId] ?: product.categoryId,
                        onClick = { onProductClick(product.id) }
                    )
                }
            }
        }

        WhyNotBottomBar(
            selectedTab = WhyNotTab.WISHLISTS,
            onHomeClick = navigation.onHomeClick,
            onWishlistsClick = navigation.onWishlistsClick,
            onAddClick = navigation.onAddClick,
            onPurchasesClick = navigation.onPurchasesClick,
            onProfileClick = navigation.onProfileClick
        )
    }
}

/** The text shown instead of results, or null when there are results to list. */
private fun emptyMessage(state: SearchState, isOnline: Boolean): String? = when {
    state.signedOut -> "Please log in."
    state.invalidPriceRange -> "Enter a valid price range."
    state.totalProducts == 0 && state.loadFailed && !isOnline ->
        "No products available locally. Reconnect to load them."
    state.totalProducts == 0 && state.loadFailed -> "Could not load products. Refresh to try again."
    state.totalProducts == 0 && !isOnline -> "No products available locally. Reconnect to download them."
    state.totalProducts == 0 -> "You have no saved products yet."
    state.results.isEmpty() -> "No matching products."
    else -> null
}

@Composable
private fun SearchNotices(state: SearchState, isOnline: Boolean, onRefresh: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (!state.loading && state.source == SearchSource.PHONE && state.totalProducts > 0) {
            Text(
                text = "You can search downloaded products. Results may be outdated.",
                style = whyNotMuted(13)
            )
        }
        if (state.hasPendingChanges) {
            Text(text = "Showing local data; waiting for server confirmation.", style = whyNotMuted(13))
        }
        if (state.loadFailed && isOnline) {
            TextButton(onClick = onRefresh) { Text("Refresh products", style = whyNotMuted(14)) }
        }
    }
}

@Composable
private fun SearchResultRow(product: Product, categoryName: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WhyNotCream)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                style = whyNotSerif(18),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${product.brand} · $categoryName",
                style = whyNotMuted(13),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = product.price.asPriceLabel(), style = whyNotMuted(14))
        }
        if (product.purchased) {
            Text(
                text = "Purchased",
                style = whyNotMuted(12),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(WhyNotBeige)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
