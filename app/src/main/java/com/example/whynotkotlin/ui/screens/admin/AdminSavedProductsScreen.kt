package com.example.whynotkotlin.ui.screens.admin

import androidx.compose.foundation.background
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
import com.example.whynotkotlin.features.admin.application.AdminUiState
import com.example.whynotkotlin.ui.theme.WhyNotBlack
import com.example.whynotkotlin.ui.theme.WhyNotBorder
import com.example.whynotkotlin.ui.theme.WhyNotGray

/**
 * BQ1:
 * How many products has a user saved?
 */
@Composable
fun AdminSavedProductsScreen(
    state: AdminUiState,
    onRecommendedSavesClick: () -> Unit,
    onRepeatSaversClick: () -> Unit = {},
    onPurchasesByCategoryClick: () -> Unit = {},
    onZeroProductsClick: () -> Unit = {},
    onDemographicProfileClick: () -> Unit = {}
) {
    AdminPage(
        title = "Saved products",
        subtitle = "How many products has a user saved?",
        selectedSection = AdminSection.SAVED_PRODUCTS,
        onSavedProductsClick = {},
        onRepeatSaversClick = onRepeatSaversClick,
        onRecommendedSavesClick = onRecommendedSavesClick,
        onPurchasesByCategoryClick = onPurchasesByCategoryClick,
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
                val stats = state.savedProductsStats

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard(
                        label = "AVERAGE",
                        value = String.format(
                            "%.1f",
                            stats.averagePerActiveUser
                        ),
                        subtitle = "per active user",
                        modifier = Modifier.weight(1f)
                    )

                    AdminMetricCard(
                        label = "TOTAL PRODUCTS",
                        value = stats.totalProducts.toString(),
                        subtitle =
                            "${stats.activeUsers} active users",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard(
                        label = "1 PRODUCT",
                        value =
                            stats.singleProductUsers.toString(),
                        subtitle = "users",
                        modifier = Modifier.weight(1f)
                    )

                    AdminMetricCard(
                        label = "2+ PRODUCTS",
                        value =
                            stats.multipleProductUsers.toString(),
                        subtitle = "users",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Distribution",
                    style = MaterialTheme.typography.titleLarge,
                    color = WhyNotBlack
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Saved products per active user",
                    style = MaterialTheme.typography.bodySmall,
                    color = WhyNotGray
                )

                Spacer(modifier = Modifier.height(18.dp))

                stats.distribution.forEach { bucket ->

                    val fraction =
                        if (stats.activeUsers == 0) {
                            0f
                        } else {
                            bucket.userCount.toFloat() /
                                    stats.activeUsers.toFloat()
                        }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = bucket.label,
                                style =
                                    MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text =
                                    "${bucket.userCount} users",
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color = WhyNotGray
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(
                                    RoundedCornerShape(8.dp)
                                )
                                .background(WhyNotBorder)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(
                                        fraction.coerceIn(
                                            0f,
                                            1f
                                        )
                                    )
                                    .height(8.dp)
                                    .background(WhyNotGray)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text =
                        "Users with 0 products are excluded from BQ1.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WhyNotGray
                )
            }
        }
    }
}