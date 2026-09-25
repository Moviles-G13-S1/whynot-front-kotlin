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
 * BQ2:
 * How many users save another product after their first one?
 */
@Composable
fun AdminRepeatSaversScreen(
    state: AdminUserMetricsUiState,
    onSavedProductsClick: () -> Unit,
    onRecommendedSavesClick: () -> Unit,
    onPurchasesByCategoryClick: () -> Unit,
    onZeroProductsClick: () -> Unit,
    onDemographicProfileClick: () -> Unit
) {
    AdminPage(
        title = "Repeat savers",
        subtitle = "How many users save another product after their first one?",
        selectedSection = AdminSection.REPEAT_SAVERS,
        onSavedProductsClick = onSavedProductsClick,
        onRepeatSaversClick = {},
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard(
                        label = "REPEAT SAVERS",
                        value = state.repeatSaverUsers.toString(),
                        subtitle = "users with 2+ products",
                        modifier = Modifier.weight(1f)
                    )

                    AdminMetricCard(
                        label = "REPEAT RATE",
                        value = String.format(
                            "%.1f%%",
                            state.repeatSaverPercentage
                        ),
                        subtitle = "of active savers",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                AdminMetricCard(
                    label = "ACTIVE SAVERS",
                    value = state.activeUsers.toString(),
                    subtitle = "users with at least 1 product",
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
                        "A repeat saver is a registered user who currently " +
                                "has at least two saved product documents.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WhyNotGray
                )
            }
        }
    }
}