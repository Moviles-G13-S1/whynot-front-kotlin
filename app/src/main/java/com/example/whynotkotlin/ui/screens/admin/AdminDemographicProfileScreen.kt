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
import com.example.whynotkotlin.features.admin.application.AdminInsightsUiState
import com.example.whynotkotlin.features.admin.domain.CategoryDemographics
import com.example.whynotkotlin.ui.theme.WhyNotBlack
import com.example.whynotkotlin.ui.theme.WhyNotBorder
import com.example.whynotkotlin.ui.theme.WhyNotCream
import com.example.whynotkotlin.ui.theme.WhyNotGray

/**
 * BQ6:
 * What is the demographic profile of the average buyer per category?
 */
@Composable
fun AdminDemographicProfileScreen(
    state: AdminInsightsUiState,
    onSavedProductsClick: () -> Unit,
    onRecommendedSavesClick: () -> Unit,
    onPurchasesByCategoryClick: () -> Unit
) {
    AdminPage(
        title = "Demographic profile",
        subtitle = "Who buys in each category?",
        selectedSection = AdminSection.DEMOGRAPHIC_PROFILE,
        onSavedProductsClick = onSavedProductsClick,
        onRecommendedSavesClick = onRecommendedSavesClick,
        onPurchasesByCategoryClick = onPurchasesByCategoryClick,
        onDemographicProfileClick = {}
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
                val stats = state.demographicProfileStats

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard(
                        label = "BUYERS",
                        value = stats.totalBuyers.toString(),
                        subtitle = "users with a purchase",
                        modifier = Modifier.weight(1f)
                    )

                    AdminMetricCard(
                        label = "CATEGORIES",
                        value = stats.categories.size.toString(),
                        subtitle = "with at least one buyer",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (stats.categories.isEmpty()) {
                    Text(
                        text =
                            "No purchases yet, so there is no buyer profile " +
                                "to describe.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WhyNotGray
                    )
                }

                stats.categories.forEach { category ->
                    CategoryCard(category)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (stats.profilesWithoutCity > 0) {
                    Text(
                        text =
                            "${stats.profilesWithoutCity} buyers have no city " +
                                "because their profile is older than the city " +
                                "catalogue.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WhyNotGray
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(category: CategoryDemographics) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WhyNotCream)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = categoryLabel(category.categoryId),
                style = MaterialTheme.typography.titleLarge,
                color = WhyNotBlack
            )

            Text(
                text = "${category.buyers} buyers",
                style = MaterialTheme.typography.bodySmall,
                color = WhyNotGray
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = category.medianAge
                ?.let { "Median age $it" }
                ?: "No ages recorded",
            style = MaterialTheme.typography.bodySmall,
            color = WhyNotGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        SectionLabel("Age groups")

        category.ageGroups.forEach { group ->
            ShareRow(
                label = group.label,
                value = group.userCount.toString(),
                fraction = fractionOf(group.userCount, category.buyers)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SectionLabel("Gender")

        category.genders.forEach { share ->
            ShareRow(
                label = share.gender,
                value = String.format("%.0f%%", share.percentage),
                fraction = fractionOf(share.userCount, category.buyers)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        SectionLabel("Cities")

        category.topCities.forEach { city ->
            ShareRow(
                label = cityLabel(city.cityId),
                value = city.userCount.toString(),
                fraction = fractionOf(city.userCount, category.buyers)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = WhyNotGray
    )

    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun ShareRow(
    label: String,
    value: String,
    fraction: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = WhyNotBlack
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = WhyNotGray
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(WhyNotBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(6.dp)
                    .background(WhyNotGray)
            )
        }
    }
}

private fun fractionOf(count: Int, total: Int): Float =
    if (total == 0) 0f else count.toFloat() / total.toFloat()
