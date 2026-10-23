package com.example.whynotkotlin.view.screens.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.whynotkotlin.model.NearbyStore
import com.example.whynotkotlin.model.ProductRecommendation
import com.example.whynotkotlin.model.WishlistSummary
import com.example.whynotkotlin.view.components.WhyNotPillButton
import com.example.whynotkotlin.view.theme.WhyNotBeige
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.theme.WhyNotWhite
import com.example.whynotkotlin.view.theme.whyNotMuted
import com.example.whynotkotlin.view.theme.whyNotSerif
import java.util.Locale

/**
 * The nearby store and the personalized recommendation, laid out as on iOS.
 *
 * [onSaveRecommendation] receives the chosen wishlist, or null to create a
 * wishlist in the recommendation's category first.
 */
@Composable
fun HomeSmartSections(
    nearbyStore: NearbyStore?,
    nearbyLoading: Boolean,
    nearbyErrorMessage: String?,
    recommendation: ProductRecommendation?,
    recommendationLoading: Boolean,
    recommendationSaving: Boolean,
    recommendationSaved: Boolean,
    recommendationErrorMessage: String?,
    wishlistSummaries: List<WishlistSummary>,
    onLoadNearby: () -> Unit,
    onLoadRecommendation: () -> Unit,
    onSaveRecommendation: (String?) -> Unit
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
            nearbyStore == null &&
            !nearbyLoading &&
            nearbyErrorMessage == null
        ) {
            onLoadNearby()
        }
    }

    LaunchedEffect(Unit) {
        if (
            recommendation == null &&
            !recommendationLoading &&
            recommendationErrorMessage == null
        ) {
            onLoadRecommendation()
        }
    }

    Text(text = "Near you", style = whyNotMuted(13))

    Spacer(modifier = Modifier.height(20.dp))

    Text(text = "Nearby recommendations", style = whyNotSerif(23))

    Spacer(modifier = Modifier.height(12.dp))

    Text(text = "A nearby store based on your preferences.", style = whyNotMuted(13))

    Spacer(modifier = Modifier.height(16.dp))

    Box(modifier = Modifier.padding(end = 30.dp)) {
        NearbySection(
            store = nearbyStore,
            loading = nearbyLoading,
            errorMessage = nearbyErrorMessage,
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
    }

    Spacer(modifier = Modifier.height(54.dp))

    Text(text = "Top pick for you", style = whyNotSerif(23))

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "A personalized recommendation based on users like you.",
        style = whyNotMuted(13)
    )

    Spacer(modifier = Modifier.height(17.dp))

    Box(modifier = Modifier.padding(end = 35.dp)) {
        RecommendationSection(
            recommendation = recommendation,
            loading = recommendationLoading,
            saving = recommendationSaving,
            saved = recommendationSaved,
            errorMessage = recommendationErrorMessage,
            wishlistSummaries = wishlistSummaries,
            onRetry = onLoadRecommendation,
            onSave = onSaveRecommendation
        )
    }
}

@Composable
private fun NearbySection(
    store: NearbyStore?,
    loading: Boolean,
    errorMessage: String?,
    permissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onRetry: () -> Unit
) {
    val isLocationError = errorMessage?.lowercase()?.let {
        "location" in it || "permission" in it
    } == true

    when {
        !permissionGranted || isLocationError -> MessageCard(
            icon = Icons.Outlined.LocationOn,
            title = "We need your location",
            message = "Allow location access so WhyNot can find something nearby for you.",
            actionLabel = "Try again",
            onAction = if (permissionGranted) onRetry else onRequestPermission
        )

        loading -> LoadingCard()

        errorMessage != null -> MessageCard(
            icon = Icons.Outlined.LocationOn,
            title = "We could not load nearby stores",
            message = "Something went wrong while finding a nearby store. Please try again.",
            actionLabel = "Try again",
            onAction = onRetry
        )

        store != null -> FeatureCard {
            Row(verticalAlignment = Alignment.Top) {
                ImageFallback(icon = Icons.Outlined.Storefront, iconSize = 29)

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = store.name,
                        style = whyNotSerif(20),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(7.dp))

                    Text(
                        text = store.address,
                        style = whyNotMuted(11),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = WhyNotGray,
                            modifier = Modifier.size(15.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = formatDistance(store.distanceKm),
                            style = whyNotMuted(12)
                        )
                    }
                }
            }
        }

        else -> MessageCard(
            icon = Icons.Outlined.Storefront,
            title = "Nothing nearby just yet.",
            message = "We could not find a nearby store that fits your preferences right now."
        )
    }
}

@Composable
private fun RecommendationSection(
    recommendation: ProductRecommendation?,
    loading: Boolean,
    saving: Boolean,
    saved: Boolean,
    errorMessage: String?,
    wishlistSummaries: List<WishlistSummary>,
    onRetry: () -> Unit,
    onSave: (String?) -> Unit
) {
    var choosingWishlist by remember { mutableStateOf(false) }

    when {
        loading -> LoadingCard()

        recommendation != null -> FeatureCard {
            Row(verticalAlignment = Alignment.Top) {
                ImageFallback(icon = Icons.Outlined.Image, iconSize = 28)

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = recommendation.name,
                        style = whyNotSerif(20),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (recommendation.brand.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = recommendation.brand,
                            style = whyNotMuted(12),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = formatRecommendationPrice(recommendation.price),
                        style = whyNotSerif(18)
                    )

                    if (recommendation.reason.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = recommendation.reason,
                            style = whyNotMuted(11),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                WhyNotPillButton(
                    text = when {
                        saved -> "Saved"
                        saving -> "Saving..."
                        else -> "Save this product"
                    },
                    onClick = { choosingWishlist = true },
                    enabled = !saving && !saved
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Could not save the recommendation. Please try again.",
                    style = whyNotMuted(11),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        errorMessage != null -> MessageCard(
            icon = Icons.Outlined.AutoAwesome,
            message = "We could not load your recommendation.",
            actionLabel = "Try again",
            onAction = onRetry
        )

        else -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(121.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(WhyNotBeige)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                tint = WhyNotGray.copy(alpha = 0.65f),
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "No recommendation available yet.",
                style = whyNotMuted(12),
                textAlign = TextAlign.Center
            )
        }
    }

    if (choosingWishlist && recommendation != null) {
        SaveToWishlistDialog(
            matching = wishlistSummaries.filter {
                it.wishlist.categoryId == recommendation.categoryId
            },
            onDismiss = { choosingWishlist = false },
            onChoose = { wishlistId ->
                choosingWishlist = false
                onSave(wishlistId)
            }
        )
    }
}

/** Lets the user pick a matching wishlist, or create one, as on iOS. */
@Composable
private fun SaveToWishlistDialog(
    matching: List<WishlistSummary>,
    onDismiss: () -> Unit,
    onChoose: (String?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save to wishlist") },
        text = {
            Column {
                if (matching.isEmpty()) {
                    Text("You need a wishlist in this category first.")
                }

                matching.forEach { summary ->
                    Text(
                        text = summary.categoryName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onChoose(summary.wishlist.id) }
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        confirmButton = {
            if (matching.isEmpty()) {
                TextButton(onClick = { onChoose(null) }) {
                    Text("Create wishlist and save")
                }
            }
        }
    )
}

@Composable
private fun FeatureCard(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(WhyNotBeige)
            .padding(14.dp)
    ) {
        content()
    }
}

@Composable
private fun LoadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(153.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(WhyNotBeige),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier.size(22.dp)
        )
    }
}

/** Card used for the empty and error states of both sections. */
@Composable
private fun MessageCard(
    icon: ImageVector,
    message: String,
    title: String? = null,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(WhyNotBeige)
            .padding(horizontal = 24.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WhyNotGray.copy(alpha = 0.65f),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (title != null) {
            Text(text = title, style = whyNotSerif(18), textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(7.dp))
        }

        Text(
            text = message,
            style = whyNotMuted(if (title != null) 11 else 12),
            textAlign = TextAlign.Center
        )

        if (actionLabel != null) {
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = actionLabel,
                style = whyNotMuted(15),
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

/** White tile standing in for a picture the backend does not provide yet. */
@Composable
private fun ImageFallback(icon: ImageVector, iconSize: Int) {
    Box(
        modifier = Modifier
            .size(width = 105.dp, height = 125.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(WhyNotWhite),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WhyNotGray.copy(alpha = 0.65f),
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

private fun formatDistance(distanceKm: Double): String =
    if (distanceKm < 1) {
        "${Math.round(distanceKm * 1000)} m away"
    } else {
        String.format(Locale.US, "%.1f km away", distanceKm)
    }

/** Whole pesos with dots between thousands, such as $126.500. */
private fun formatRecommendationPrice(price: Double): String {
    val digits = Math.round(price).toString()
    val grouped = digits.reversed().chunked(3).joinToString(".").reversed()
    return "$$grouped"
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
