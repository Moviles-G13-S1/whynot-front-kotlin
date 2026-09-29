package com.example.whynotkotlin.ui.screens.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.whynotkotlin.features.nearby.application.NearbyStoreUiState
import com.example.whynotkotlin.features.recommendations.application.RecommendationUiState
import com.example.whynotkotlin.features.wishlists.domain.WishlistSummary
import com.example.whynotkotlin.ui.components.WhyNotButton
import com.example.whynotkotlin.ui.components.WhyNotErrorBanner
import com.example.whynotkotlin.ui.theme.WhyNotBeige
import com.example.whynotkotlin.ui.theme.WhyNotGray

@Composable
fun HomeSmartSections(
    nearbyState: NearbyStoreUiState,
    recommendationState: RecommendationUiState,
    wishlistSummaries: List<WishlistSummary>,
    onLoadNearby: () -> Unit,
    onLoadRecommendation: () -> Unit,
    onSaveRecommendation: (String) -> Unit,
    onOpenWishlists: () -> Unit
) {
    val context = LocalContext.current

    var locationPermissionGranted by remember {
        mutableStateOf(
            hasLocationPermission(context)
        )
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            locationPermissionGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        }

    LaunchedEffect(locationPermissionGranted) {
        if (
            locationPermissionGranted &&
            nearbyState.store == null &&
            !nearbyState.loading &&
            nearbyState.errorMessage == null
        ) {
            onLoadNearby()
        }
    }

    LaunchedEffect(Unit) {
        if (
            recommendationState.recommendation == null &&
            !recommendationState.loading &&
            recommendationState.errorMessage == null
        ) {
            onLoadRecommendation()
        }
    }

    NearbySection(
        state = nearbyState,
        permissionGranted = locationPermissionGranted,
        onRequestPermission = {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        },
        onRetry = onLoadNearby
    )

    Spacer(modifier = Modifier.height(26.dp))

    RecommendationSection(
        state = recommendationState,
        wishlistSummaries = wishlistSummaries,
        onRetry = onLoadRecommendation,
        onSave = onSaveRecommendation,
        onOpenWishlists = onOpenWishlists
    )
}

@Composable
private fun NearbySection(
    state: NearbyStoreUiState,
    permissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onRetry: () -> Unit
) {
    Text(
        text = "Near you",
        style = MaterialTheme.typography.bodySmall,
        color = WhyNotGray
    )

    Spacer(modifier = Modifier.height(6.dp))

    when {
        !permissionGranted -> {
            Text(
                text = "Find stores close to you",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Location permission is needed to find the nearest store.",
                style = MaterialTheme.typography.bodySmall,
                color = WhyNotGray
            )

            Spacer(modifier = Modifier.height(10.dp))

            WhyNotButton(
                text = "Enable location",
                onClick = onRequestPermission
            )
        }

        state.loading -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "Finding the nearest store...",
                    style = MaterialTheme.typography.bodySmall,
                    color = WhyNotGray
                )
            }
        }

        state.errorMessage != null -> {
            WhyNotErrorBanner(
                message = state.errorMessage
            )

            Spacer(modifier = Modifier.height(10.dp))

            WhyNotButton(
                text = "Try again",
                onClick = onRetry
            )
        }

        state.store == null -> {
            Text(
                text = "No nearby store found",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Try again in a moment.",
                style = MaterialTheme.typography.bodySmall,
                color = WhyNotGray
            )

            Spacer(modifier = Modifier.height(10.dp))

            WhyNotButton(
                text = "Try again",
                onClick = onRetry
            )
        }

        else -> {
            val store = state.store ?: return

            Text(
                text = "You are close to ${store.name}",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${String.format("%.2f", store.distanceKm)} km away · ${store.address}",
                style = MaterialTheme.typography.bodySmall,
                color = WhyNotGray
            )

            Spacer(modifier = Modifier.height(10.dp))

            FeatureImagePlaceholder(
                label = store.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp)
            )
        }
    }
}

@Composable
private fun RecommendationSection(
    state: RecommendationUiState,
    wishlistSummaries: List<WishlistSummary>,
    onRetry: () -> Unit,
    onSave: (String) -> Unit,
    onOpenWishlists: () -> Unit
) {
    Text(
        text = "Top pick for you",
        style = MaterialTheme.typography.titleLarge
    )

    Spacer(modifier = Modifier.height(12.dp))

    if (state.loading) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = "Looking for a recommendation...",
                style = MaterialTheme.typography.bodySmall,
                color = WhyNotGray
            )
        }
        return
    }

    val recommendation = state.recommendation

    if (recommendation == null) {
        if (state.errorMessage != null) {
            WhyNotErrorBanner(
                message = state.errorMessage
            )

            Spacer(modifier = Modifier.height(10.dp))
        } else {
            Text(
                text = "No recommendation available right now.",
                style = MaterialTheme.typography.bodyMedium,
                color = WhyNotGray
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        WhyNotButton(
            text = "Try again",
            onClick = onRetry
        )
        return
    }

    val matchingWishlist = wishlistSummaries.firstOrNull {
        it.wishlist.categoryId == recommendation.categoryId
    }

    FeatureImagePlaceholder(
        label = recommendation.name,
        modifier = Modifier
            .fillMaxWidth()
            .height(122.dp)
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = recommendation.name,
        style = MaterialTheme.typography.titleLarge
    )

    if (recommendation.brand.isNotBlank()) {
        Text(
            text = recommendation.brand,
            style = MaterialTheme.typography.bodySmall,
            color = WhyNotGray
        )
    }

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = String.format("$%.2f", recommendation.price),
        style = MaterialTheme.typography.bodyMedium
    )

    if (recommendation.reason.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = recommendation.reason,
            style = MaterialTheme.typography.bodySmall,
            color = WhyNotGray
        )
    }

    if (state.errorMessage != null) {
        Spacer(modifier = Modifier.height(10.dp))

        WhyNotErrorBanner(
            message = state.errorMessage
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    when {
        state.saved -> {
            WhyNotButton(
                text = "Saved",
                onClick = {},
                enabled = false,
                filled = true
            )
        }

        matchingWishlist == null -> {
            Text(
                text =
                    "Create a wishlist for this category before saving " +
                            "the recommendation.",
                style = MaterialTheme.typography.bodySmall,
                color = WhyNotGray,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))

            WhyNotButton(
                text = "Open wishlists",
                onClick = onOpenWishlists
            )
        }

        else -> {
            WhyNotButton(
                text =
                    if (state.saving) {
                        "Saving..."
                    } else {
                        "Save to ${matchingWishlist.categoryName}"
                    },
                onClick = {
                    onSave(
                        matchingWishlist.wishlist.id
                    )
                },
                enabled = !state.saving,
                filled = true
            )
        }
    }
}

@Composable
private fun FeatureImagePlaceholder(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(WhyNotBeige),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = WhyNotGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(12.dp)
        )
    }
}

private fun hasLocationPermission(
    context: Context
): Boolean {
    val fineGranted =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    val coarseGranted =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    return fineGranted || coarseGranted
}