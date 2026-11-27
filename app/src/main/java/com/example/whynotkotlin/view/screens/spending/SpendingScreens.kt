package com.example.whynotkotlin.view.screens.spending

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.model.CategorySpending
import com.example.whynotkotlin.model.CategorySpendingDetailLoad
import com.example.whynotkotlin.model.SpendingPurchase
import com.example.whynotkotlin.model.SpendingSummaryLoad
import com.example.whynotkotlin.model.YearMonth
import com.example.whynotkotlin.view.components.OfflineBanner
import com.example.whynotkotlin.view.components.WhyNotBottomBar
import com.example.whynotkotlin.view.components.WhyNotTab
import com.example.whynotkotlin.view.screens.wishlists.asPriceLabel
import com.example.whynotkotlin.view.theme.WhyNotBeige
import com.example.whynotkotlin.view.theme.WhyNotBlack
import com.example.whynotkotlin.view.theme.WhyNotCream
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.theme.whyNotMuted
import com.example.whynotkotlin.view.theme.whyNotSerif
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * Micro-optimization (Sprint 4): everything a row shows (price labels,
 * percentages, bar sizes, dates) is computed once per loaded month and kept
 * with remember, instead of on every recomposition. Before, each CategoryRow
 * recomputed the month's maximum (an O(n) pass per row, O(n^2) per screen) and
 * formatted its percentage with String.format, and each PurchaseRow created a
 * new SimpleDateFormat. Rows are keyed by id and receive immutable, already
 * formatted values and one remembered click lambda, so Compose can skip rows
 * that did not change.
 */

/** What one category row shows, already formatted. */
@Immutable
private data class CategoryRowUi(
    val categoryId: String,
    val name: String,
    val totalLabel: String,
    val barFraction: Float,
    val detail: String
)

/** What one purchase row shows, already formatted. */
@Immutable
private data class PurchaseRowUi(
    val productId: String,
    val name: String,
    val subtitle: String,
    val priceLabel: String,
    val pending: Boolean
)

private fun categoryRows(categories: List<CategorySpending>, monthTotal: Double): List<CategoryRowUi> {
    val maxTotal = categories.maxOfOrNull { it.total } ?: 0.0
    return categories.map { category ->
        val share = if (monthTotal > 0) Math.round(category.total * 100 / monthTotal) else 0L
        val pending = if (category.pendingCount > 0) " · ${category.pendingCount} pending sync" else ""
        CategoryRowUi(
            categoryId = category.categoryId,
            name = category.categoryName,
            totalLabel = category.total.asPriceLabel(),
            barFraction = if (maxTotal > 0) (category.total / maxTotal).toFloat() else 0f,
            detail = "${category.purchaseCount} purchases · " + share + "%" + pending
        )
    }
}

private fun purchaseRows(purchases: List<SpendingPurchase>): List<PurchaseRowUi> {
    // One formatter for the whole list instead of one per row and recomposition.
    val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.US)
    return purchases.map { purchase ->
        PurchaseRowUi(
            productId = purchase.productId,
            name = purchase.name,
            subtitle = purchase.brand + " · " + dateFormat.format(Date(purchase.purchasedAtMillis)),
            priceLabel = purchase.price.asPriceLabel(),
            pending = purchase.pending
        )
    }
}

/** Navigation shared by both spending screens (bottom bar, Purchases tab). */
data class SpendingNavigation(
    val onHomeClick: () -> Unit,
    val onWishlistsClick: () -> Unit,
    val onAddClick: () -> Unit,
    val onPurchasesClick: () -> Unit,
    val onProfileClick: () -> Unit
)

/**
 * Monthly Spending Summary: total spent in a month and per category.
 *
 * Offline it shows what is saved on the phone with its own message and the
 * time of the last update; with nothing saved, a specific message and Retry.
 */
@Composable
fun SpendingSummaryScreen(
    load: SpendingSummaryLoad?,
    loading: Boolean,
    errorMessage: String?,
    isOnline: Boolean,
    onMonthSelected: (Int) -> Unit,
    onCategoryClick: (Int, String) -> Unit,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    navigation: SpendingNavigation
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 25.dp, vertical = 24.dp)
            ) {
                TextButton(onClick = onBackClick) { Text("‹ Purchases", style = whyNotMuted(14)) }

                OfflineBanner(
                    isOnline = isOnline,
                    message = "You're offline. Showing the spending saved on this phone.",
                    onRetry = onRetry,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(text = "Monthly spending", style = whyNotSerif(30))
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "What you bought, by month and category.", style = whyNotMuted(14))
                Spacer(modifier = Modifier.height(20.dp))

                when {
                    loading && load == null -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    load == null -> SpendingMessage(
                        text = errorMessage ?: "Could not load your spending.",
                        onRetry = onRetry
                    )

                    load.month == null -> Text(
                        text = "You haven't marked any purchase yet. Your spending will appear here.",
                        style = whyNotMuted(14),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    else -> {
                        if (load.isStale) {
                            StaleNotice(load.updatedAtMillis, onRetry)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        MonthSelector(
                            selected = load.month.yearMonth,
                            available = load.availableMonths,
                            onMonthSelected = onMonthSelected
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        val month = load.month
                        val totalLabel = remember(month) { month.total.asPriceLabel() }
                        val rows = remember(month) { categoryRows(month.categories, month.total) }
                        val onRowClick: (String) -> Unit = remember(month.yearMonth, onCategoryClick) {
                            { categoryId -> onCategoryClick(month.yearMonth, categoryId) }
                        }

                        Text(text = totalLabel, style = whyNotSerif(40))
                        Text(
                            text = "${load.month.purchaseCount} purchases in ${YearMonth.label(load.month.yearMonth)}",
                            style = whyNotMuted(14)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            rows.forEach { row ->
                                key(row.categoryId) {
                                    CategoryRow(row = row, onClick = onRowClick)
                                }
                            }
                        }
                    }
                }
            }
        }

        WhyNotBottomBar(
            selectedTab = WhyNotTab.PURCHASES,
            onHomeClick = navigation.onHomeClick,
            onWishlistsClick = navigation.onWishlistsClick,
            onAddClick = navigation.onAddClick,
            onPurchasesClick = navigation.onPurchasesClick,
            onProfileClick = navigation.onProfileClick
        )
    }
}

/** Category Spending Detail: the purchases of one category in one month. */
@Composable
fun CategorySpendingDetailScreen(
    load: CategorySpendingDetailLoad?,
    loading: Boolean,
    errorMessage: String?,
    isOnline: Boolean,
    onRetry: () -> Unit,
    onBackClick: () -> Unit,
    navigation: SpendingNavigation
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 25.dp, vertical = 24.dp)
            ) {
                TextButton(onClick = onBackClick) { Text("‹ Monthly spending", style = whyNotMuted(14)) }

                OfflineBanner(
                    isOnline = isOnline,
                    message = "You're offline. These purchases come from this phone and may be outdated.",
                    onRetry = onRetry,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                when {
                    loading && load == null -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    load?.category == null -> SpendingMessage(
                        text = errorMessage ?: "There are no purchases in this category for this month.",
                        onRetry = onRetry
                    )

                    else -> {
                        val category = load.category
                        Text(text = category.categoryName, style = whyNotSerif(30))
                        Text(text = YearMonth.label(load.yearMonth), style = whyNotMuted(14))

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(text = category.total.asPriceLabel(), style = whyNotSerif(40))
                        val summary = remember(category, load.monthTotal) {
                            val share = if (load.monthTotal > 0) Math.round(category.total * 100 / load.monthTotal) else 0L
                            "${category.purchaseCount} purchases · " + share + "% of the month"
                        }
                        Text(
                            text = summary,
                            style = whyNotMuted(14)
                        )
                        if (category.pendingCount > 0) {
                            Text(
                                text = "${category.pendingCount} waiting for connection to sync",
                                style = whyNotMuted(13)
                            )
                        }

                        if (load.isStale) {
                            Spacer(modifier = Modifier.height(12.dp))
                            StaleNotice(load.updatedAtMillis, onRetry)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            val purchaseRows = remember(load.purchases) { purchaseRows(load.purchases) }
                            purchaseRows.forEach { row ->
                                key(row.productId) { PurchaseRow(row) }
                            }
                        }
                    }
                }
            }
        }

        WhyNotBottomBar(
            selectedTab = WhyNotTab.PURCHASES,
            onHomeClick = navigation.onHomeClick,
            onWishlistsClick = navigation.onWishlistsClick,
            onAddClick = navigation.onAddClick,
            onPurchasesClick = navigation.onPurchasesClick,
            onProfileClick = navigation.onProfileClick
        )
    }
}

@Composable
private fun MonthSelector(selected: Int, available: List<Int>, onMonthSelected: (Int) -> Unit) {
    // available is newest first: "older" moves forward in the list. The
    // neighbours and the label are only recomputed when the month changes.
    val neighbours by remember(selected, available) {
        derivedStateOf {
            val index = available.indexOf(selected)
            available.getOrNull(index + 1) to available.getOrNull(index - 1)
        }
    }
    val (older, newer) = neighbours
    val label = remember(selected) { YearMonth.label(selected) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WhyNotCream)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { older?.let(onMonthSelected) }, enabled = older != null) { Text("‹") }
        Text(
            text = label,
            style = whyNotSerif(18),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = { newer?.let(onMonthSelected) }, enabled = newer != null) { Text("›") }
    }
}

@Composable
private fun CategoryRow(row: CategoryRowUi, onClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WhyNotBeige)
            .clickable { onClick(row.categoryId) }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = row.name,
                style = whyNotSerif(18),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(text = row.totalLabel, style = whyNotSerif(18))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(WhyNotCream)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(row.barFraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(WhyNotBlack)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = row.detail, style = whyNotMuted(12))
    }
}

@Composable
private fun PurchaseRow(row: PurchaseRowUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WhyNotCream)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.name,
                style = whyNotSerif(16),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = row.subtitle, style = whyNotMuted(12))
            if (row.pending) {
                Text(text = "Pending sync", style = whyNotMuted(12, WhyNotGray))
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = row.priceLabel, style = whyNotSerif(16))
    }
}

@Composable
private fun StaleNotice(updatedAtMillis: Long?, onRetry: () -> Unit) {
    val updated = remember(updatedAtMillis) {
        updatedAtMillis?.let { SimpleDateFormat("d MMM, h:mm a", Locale.US).format(Date(it)) }
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = if (updated != null) "Last updated $updated. It may be outdated." else "It may be outdated.",
            style = whyNotMuted(12),
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun SpendingMessage(text: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = text, style = whyNotMuted(14), textAlign = TextAlign.Center)
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}
