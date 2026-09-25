package com.example.whynotkotlin.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.features.admin.application.AdminUserMetricsUiState
import com.example.whynotkotlin.ui.theme.WhyNotGray

/**
 * BQ5:
 * How many users have 0 products saved?
 */
@Composable
fun AdminZeroProductsScreen(
    state: AdminUserMetricsUiState,
    onSavedProductsClick: () -> Unit,
    onRepeatSaversClick: () -> Unit,
    onRecommendedSavesClick: () -> Unit,
    onPurchasesByCategoryClick: () -> Unit,
    onDemographicProfileClick: () -> Unit
) {
    AdminPage(
        title = "Users with no products",
        subtitle = "How many users have 0 products saved?",
        selectedSection = AdminSection.ZERO_PRODUCTS,
        onSavedProductsClick = onSavedProductsClick,
        onRepeatSaversClick = onRepeatSaversClick,
        onRecommendedSavesClick = onRecommendedSavesClick,
        onPurchasesByCategoryClick = onPurchasesByCategoryClick,
        onZeroProductsClick = {},
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard(
                        label = "ZERO PRODUCTS",
                        value = state.zeroProductUsers.toString(),
                        subtitle = "registered users",
                        modifier = Modifier.weight(1f)
                    )

                    AdminMetricCard(
                        label = "SHARE",
                        value = String.format(
                            "%.1f%%",
                            state.zeroProductPercentage
                        ),
                        subtitle = "of all users",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                AdminMetricCard(
                    label = "TOTAL USERS",
                    value = state.totalUsers.toString(),
                    subtitle = "registered profiles",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "How this is calculated",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text =
                        "The app compares every registered user against the " +
                                "owners present in the products collection.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WhyNotGray
                )
            }
        }
    }
}