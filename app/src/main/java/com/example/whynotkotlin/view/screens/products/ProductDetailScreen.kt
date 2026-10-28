package com.example.whynotkotlin.view.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.model.Product
import com.example.whynotkotlin.view.components.ProductImage
import com.example.whynotkotlin.view.components.WhyNotBottomBar
import com.example.whynotkotlin.view.components.WhyNotConfirmDialog
import com.example.whynotkotlin.view.components.WhyNotTab
import com.example.whynotkotlin.view.screens.wishlists.asPriceLabel
import com.example.whynotkotlin.view.theme.WhyNotBeige
import com.example.whynotkotlin.view.theme.WhyNotDanger
import com.example.whynotkotlin.view.theme.WhyNotDivider
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.theme.whyNotMuted
import com.example.whynotkotlin.view.theme.whyNotSerif
import androidx.compose.material.icons.outlined.CloudUpload
import com.example.whynotkotlin.view.components.OfflineBanner

@Composable
fun ProductDetailScreen(
    product: Product?,
    loading: Boolean,
    errorMessage: String?,
    sourceTab: WhyNotTab,
    onMarkPurchased: (purchasePrice: String) -> Unit,
    onEditClick: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    isOnline: Boolean = true
) {
    var confirmingDelete by remember { mutableStateOf(false) }
    var askingPurchasePrice by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            if (product == null) {
                Text(
                    text = if (loading) "" else "Product not found.",
                    style = whyNotMuted(14),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 32.dp, end = 32.dp, top = 47.dp, bottom = 40.dp)
                ) {
                    OfflineBanner(
                        isOnline = isOnline,
                        message = "You're offline. Purchases you mark here sync when you're back online.",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = product.name.ifEmpty { "Product" },
                        style = whyNotSerif(30)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.brand,
                            style = whyNotMuted(15),
                            modifier = Modifier.weight(1f)
                        )

                        // Purchasing is one-way on the backend, so the action
                        // becomes a static label once it is set.
                        IconLabel(
                            icon = if (product.purchased) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            iconSize = 18,
                            label = if (product.purchased) "Purchased" else "Mark as purchased",
                            iconColor = if (product.purchased) Color.Black else WhyNotGray,
                            labelColor = if (product.purchased) Color(0xFFA9A7A7) else WhyNotGray,
                            onClick = if (product.purchased) null else {
                                { askingPurchasePrice = true }
                            }
                        )
                    }

                    // A change made offline (a purchase) waits in Firestore's
                    // queue; this says so until the backend confirms it.
                    if (product.pendingSync) {
                        Spacer(modifier = Modifier.height(10.dp))

                        IconLabel(
                            icon = Icons.Outlined.CloudUpload,
                            iconSize = 16,
                            label = "Waiting for connection to sync",
                            labelSize = 13,
                            onClick = null
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    ProductImage(
                        imageUrl = product.imageUrl,
                        ratio = null,
                        cornerRadius = 18,
                        modifier = Modifier.height(360.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.price.asPriceLabel(),
                            style = whyNotMuted(15),
                            modifier = Modifier.weight(1f)
                        )

                        IconLabel(
                            icon = Icons.Outlined.Edit,
                            iconSize = 15,
                            label = "Edit item",
                            onClick = onEditClick
                        )
                    }

                    if (product.productUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(22.dp))

                        Text(text = "Product link", style = whyNotSerif(20))

                        Spacer(modifier = Modifier.height(8.dp))

                        SelectionContainer {
                            Text(text = product.productUrl, style = whyNotMuted(13))
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = errorMessage, style = whyNotMuted(13))
                    }

                    Spacer(modifier = Modifier.height(34.dp))

                    HorizontalDivider(color = WhyNotDivider)

                    Spacer(modifier = Modifier.height(18.dp))

                    IconLabel(
                        icon = Icons.Outlined.DeleteOutline,
                        iconSize = 18,
                        label = "Delete item",
                        iconColor = WhyNotDanger,
                        labelColor = WhyNotDanger,
                        labelSize = 14,
                        onClick = { confirmingDelete = true },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }

        WhyNotBottomBar(
            selectedTab = sourceTab,
            onHomeClick = onHomeClick,
            onWishlistsClick = onWishlistsClick,
            onAddClick = onAddClick,
            onPurchasesClick = onPurchasesClick,
            onProfileClick = onProfileClick
        )
    }

    if (askingPurchasePrice && product != null) {
        PurchasePriceDialog(
            savedPrice = product.price,
            onDismiss = { askingPurchasePrice = false },
            onConfirm = { paid ->
                askingPurchasePrice = false
                onMarkPurchased(paid)
            }
        )
    }

    if (confirmingDelete) {
        WhyNotConfirmDialog(
            title = "Delete item?",
            message = "This item will be permanently removed from your wishlist.",
            confirmLabel = "Delete",
            onDismiss = { confirmingDelete = false },
            onConfirm = {
                confirmingDelete = false
                onDeleteConfirmed()
            }
        )
    }
}

/** Icon followed by a light label, used for the inline actions. */
@Composable
private fun IconLabel(
    icon: ImageVector,
    iconSize: Int,
    label: String,
    modifier: Modifier = Modifier,
    iconColor: Color = WhyNotGray,
    labelColor: Color = WhyNotGray,
    labelSize: Int = 15,
    onClick: (() -> Unit)?
) {
    Row(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(iconSize.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            style = whyNotMuted(labelSize, labelColor)
        )
    }
}
