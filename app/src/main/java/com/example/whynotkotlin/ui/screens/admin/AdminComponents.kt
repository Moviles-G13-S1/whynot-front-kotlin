package com.example.whynotkotlin.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whynotkotlin.ui.theme.WhyNotBlack
import com.example.whynotkotlin.ui.theme.WhyNotCream
import com.example.whynotkotlin.ui.theme.WhyNotGray
import com.example.whynotkotlin.ui.theme.WhyNotWhite

enum class AdminSection {
    SAVED_PRODUCTS,
    REPEAT_SAVERS,
    RECOMMENDED_SAVES,
    PURCHASES_BY_CATEGORY,
    ZERO_PRODUCTS,
    DEMOGRAPHIC_PROFILE
}

/**
 * Shared visual structure for Admin screens.
 *
 * Juan Felipe leaves this component reusable so the remaining
 * Business Questions can use the same Admin visual structure.
 *
 * Access control is intentionally NOT handled here.
 * The Admin route guard belongs to Martin.
 */
@Composable
fun AdminPage(
    title: String,
    subtitle: String,
    selectedSection: AdminSection,
    onSavedProductsClick: () -> Unit,
    onRecommendedSavesClick: () -> Unit,
    onRepeatSaversClick: () -> Unit = {},
    onPurchasesByCategoryClick: () -> Unit = {},
    onZeroProductsClick: () -> Unit = {},
    onDemographicProfileClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhyNotWhite)
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 28.dp,
                bottom = 36.dp
            )
    ) {
        Text(
            text = "W H Y N O T   A D M I N",
            style = MaterialTheme.typography.bodySmall.copy(
                letterSpacing = 1.5.sp
            ),
            color = WhyNotGray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = WhyNotBlack
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = WhyNotGray
        )

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminNavigationItem(
                text = "Saved products",
                selected = selectedSection == AdminSection.SAVED_PRODUCTS,
                onClick = onSavedProductsClick,
                modifier = Modifier.weight(1f)
            )

            AdminNavigationItem(
                text = "Repeat saves",
                selected = selectedSection == AdminSection.REPEAT_SAVERS,
                onClick = onRepeatSaversClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminNavigationItem(
                text = "Recommended",
                selected = selectedSection == AdminSection.RECOMMENDED_SAVES,
                onClick = onRecommendedSavesClick,
                modifier = Modifier.weight(1f)
            )

            AdminNavigationItem(
                text = "Purchases",
                selected = selectedSection == AdminSection.PURCHASES_BY_CATEGORY,
                onClick = onPurchasesByCategoryClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminNavigationItem(
                text = "Zero products",
                selected = selectedSection == AdminSection.ZERO_PRODUCTS,
                onClick = onZeroProductsClick,
                modifier = Modifier.weight(1f)
            )

            AdminNavigationItem(
                text = "Demographics",
                selected = selectedSection == AdminSection.DEMOGRAPHIC_PROFILE,
                onClick = onDemographicProfileClick,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        content()
    }
}

@Composable
private fun AdminNavigationItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background =
        if (selected) WhyNotBlack else WhyNotCream

    val textColor =
        if (selected) WhyNotWhite else WhyNotBlack

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 12.dp,
                vertical = 12.dp
            )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

@Composable
fun AdminMetricCard(
    label: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(WhyNotCream)
            .padding(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = WhyNotGray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = WhyNotBlack
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = WhyNotGray
        )
    }
}