package com.example.whynotkotlin.view.screens.admin

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.model.SavedProductsOverview
import com.example.whynotkotlin.view.theme.WhyNotGray

/**
 * BQ3:
 * How many recommended products have been saved?
 *
 * Counts successful saves in productEvents. A displayed recommendation
 * contributes only after its event has savedAt and savedProductId.
 */
@Composable
fun AdminRecommendedSavesScreen(
    overview: SavedProductsOverview,
    loading: Boolean,
    errorMessage: String?,
    onSavedProductsClick: () -> Unit,
    onRepeatSaversClick: () -> Unit = {},
    onPurchasesByCategoryClick: () -> Unit = {},
    onZeroProductsClick: () -> Unit = {},
    onDemographicProfileClick: () -> Unit = {}
) {
    AdminPage(
        title = "Recommended products",
        subtitle =
            "How many recommended products have been saved?",
        selectedSection = AdminSection.RECOMMENDED_SAVES,
        onSavedProductsClick = onSavedProductsClick,
        onRepeatSaversClick = onRepeatSaversClick,
        onRecommendedSavesClick = {},
        onPurchasesByCategoryClick = onPurchasesByCategoryClick,
        onZeroProductsClick = onZeroProductsClick,
        onDemographicProfileClick = onDemographicProfileClick
    ) {

        when {
            loading -> {
                CircularProgressIndicator()
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    color = WhyNotGray
                )
            }

            else -> {
                AdminMetricCard(
                    label = "RECOMMENDED PRODUCTS SAVED",
                    value =
                        overview.recommendedProductsSaved.toString(),
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
