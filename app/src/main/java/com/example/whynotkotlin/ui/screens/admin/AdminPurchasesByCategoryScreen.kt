package com.example.whynotkotlin.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.features.admin.application.AdminInsightsUiState
import com.example.whynotkotlin.features.admin.domain.PurchasedProductsStats
import com.example.whynotkotlin.ui.theme.WhyNotBlack
import com.example.whynotkotlin.ui.theme.WhyNotBorder
import com.example.whynotkotlin.ui.theme.WhyNotCream
import com.example.whynotkotlin.ui.theme.WhyNotGray
import com.example.whynotkotlin.ui.theme.WhyNotWhite

/**
 * BQ4:
 * Which category has the highest number of products marked as purchased
 * per month?
 */
@Composable
fun AdminPurchasesByCategoryScreen(
    state: AdminInsightsUiState,
    onSavedProductsClick: () -> Unit,
    onRepeatSaversClick: () -> Unit,
    onRecommendedSavesClick: () -> Unit,
    onZeroProductsClick: () -> Unit,
    onDemographicProfileClick: () -> Unit,
    onWindowChange: (Int) -> Unit
) {
    AdminPage(
        title = "Purchases by category",
        subtitle =
            "Which category has the most purchases each month?",
        selectedSection = AdminSection.PURCHASES_BY_CATEGORY,
        onSavedProductsClick = onSavedProductsClick,
        onRepeatSaversClick = onRepeatSaversClick,
        onRecommendedSavesClick = onRecommendedSavesClick,
        onPurchasesByCategoryClick = {},
        onZeroProductsClick = onZeroProductsClick,
        onDemographicProfileClick = onDemographicProfileClick
    ) {

        when {
            state.loading -> {
                CircularProgressIndicator()
            }

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage,
                    color = WhyNotGray
                )
            }

            else -> {
                val stats = state.purchasedProductsStats

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PurchasedProductsStats.WINDOW_OPTIONS.forEach { months ->
                        WindowChip(
                            text = "$months months",
                            selected = stats.monthsWindow == months,
                            onClick = { onWindowChange(months) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard(
                        label = "PURCHASES",
                        value = stats.totalPurchases.toString(),
                        subtitle = "in the last ${stats.monthsWindow} months",
                        modifier = Modifier.weight(1f)
                    )

                    AdminMetricCard(
                        label = "LEADING CATEGORY",
                        value = categoryLabel(stats.overallTopCategoryId),
                        subtitle = "across the window",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "By month",
                    style = MaterialTheme.typography.titleLarge,
                    color = WhyNotBlack
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "The winning category of each month, newest first",
                    style = MaterialTheme.typography.bodySmall,
                    color = WhyNotGray
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (stats.months.isEmpty()) {
                    Text(
                        text =
                            "No purchases recorded in this window yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WhyNotGray
                    )
                }

                stats.months.forEach { month ->

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = month.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = WhyNotBlack
                            )

                            Text(
                                text = "${month.totalPurchases} purchases",
                                style = MaterialTheme.typography.bodySmall,
                                color = WhyNotGray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        month.byCategory.forEach { entry ->

                            val fraction =
                                if (month.totalPurchases == 0) {
                                    0f
                                } else {
                                    entry.purchases.toFloat() /
                                        month.totalPurchases.toFloat()
                                }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = categoryLabel(entry.categoryId),
                                    style =
                                        MaterialTheme.typography.bodySmall,
                                    color =
                                        if (entry.categoryId ==
                                            month.topCategoryId
                                        ) {
                                            WhyNotBlack
                                        } else {
                                            WhyNotGray
                                        }
                                )

                                Text(
                                    text = entry.purchases.toString(),
                                    style =
                                        MaterialTheme.typography.bodySmall,
                                    color = WhyNotGray
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(WhyNotBorder)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(
                                            fraction.coerceIn(0f, 1f)
                                        )
                                        .height(6.dp)
                                        .background(
                                            if (entry.categoryId ==
                                                month.topCategoryId
                                            ) {
                                                WhyNotBlack
                                            } else {
                                                WhyNotGray
                                            }
                                        )
                                )
                            }
                        }
                    }
                }

                if (stats.undatedPurchases > 0) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text =
                            "${stats.undatedPurchases} purchased products " +
                                "have no purchase date and are not counted. " +
                                "They were bought before the backend started " +
                                "storing purchasedAt.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WhyNotGray
                    )
                }
            }
        }
    }
}

@Composable
private fun WindowChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) WhyNotBlack else WhyNotCream)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) WhyNotWhite else WhyNotBlack
        )
    }
}
