package com.example.whynotkotlin.ui.screens.admin

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.features.admin.application.AdminUiState
import com.example.whynotkotlin.ui.theme.WhyNotGray

/**
 * BQ3:
 * How many recommended products have been saved?
 *
 * The displayed value comes from:
 *
 * adminMetrics/recommendedProductSaves.total
 *
 * The backend increments the metric when a recommendation is
 * successfully saved. This screen does not calculate the value locally.
 */
@Composable
fun AdminRecommendedSavesScreen(
    state: AdminUiState,
    onSavedProductsClick: () -> Unit,
    // Added by Miguel so the BQ4 and BQ6 tabs are reachable from here.
    onPurchasesByCategoryClick: () -> Unit = {},
    onDemographicProfileClick: () -> Unit = {}
) {
    AdminPage(
        title = "Recommended products",
        subtitle =
            "How many recommended products have been saved?",
        selectedSection = AdminSection.RECOMMENDED_SAVES,
        onPurchasesByCategoryClick = onPurchasesByCategoryClick,
        onDemographicProfileClick = onDemographicProfileClick,
        onSavedProductsClick = onSavedProductsClick,
        onRecommendedSavesClick = {}
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
                AdminMetricCard(
                    label = "RECOMMENDED PRODUCTS SAVED",
                    value =
                        state.recommendedProductsSaved.toString(),
                    subtitle = "total successful saves",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "About this metric",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text =
                        "This value only counts products saved through " +
                                "the recommendation feature. Manual product " +
                                "saves are not included.",
                    style = MaterialTheme.typography.bodySmall,
                    color = WhyNotGray
                )
            }
        }
    }
}