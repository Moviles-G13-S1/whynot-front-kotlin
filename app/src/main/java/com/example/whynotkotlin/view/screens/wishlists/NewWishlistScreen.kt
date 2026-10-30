package com.example.whynotkotlin.view.screens.wishlists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.whynotkotlin.controller.WishlistController
import com.example.whynotkotlin.model.InputRules
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.model.Category
import com.example.whynotkotlin.view.components.ProductPictureField
import com.example.whynotkotlin.view.components.WhyNotBottomBar
import com.example.whynotkotlin.view.components.WhyNotTab
import com.example.whynotkotlin.view.components.WhyNotDropdownField
import com.example.whynotkotlin.view.components.WhyNotErrorBanner
import com.example.whynotkotlin.view.theme.WhyNotCream
import com.example.whynotkotlin.view.theme.WhyNotDisabled
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.components.OfflineBanner

@Composable
fun NewWishlistScreen(
    availableCategories: List<Category>,
    saving: Boolean,
    errorMessage: String?,
    onSave: (categoryId: String, imageUrl: String) -> Unit,
    onCancelClick: () -> Unit,
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    isOnline: Boolean = true
) {
    var categoryId by rememberSaveable { mutableStateOf("") }
    var imageUrl by rememberSaveable { mutableStateOf("") }
    var showAllErrors by rememberSaveable { mutableStateOf(false) }

    val categoryError = if (categoryId.isBlank()) "Choose a category" else null
    val imageUrlError = InputRules.optionalLink(imageUrl, WishlistController.PICTURE_LINK)

    // Only categories without a wishlist yet, so the user cannot create a
    // duplicate. The backend does not enforce this, it is a UI convention.
    val options = availableCategories.map { it.id to it.name }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .statusBarsPadding()
                // Scrolls so Save stays reachable in landscape and with large fonts.
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "New Wishlist",
                style = MaterialTheme.typography.displaySmall
            )

            OfflineBanner(
                isOnline = isOnline,
                message = "You're offline. You can create a wishlist once you're back online.",
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        WhyNotCream,
                        RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 22.dp)
            ) {
                WhyNotDropdownField(
                    label = "Category",
                    selectedValue = categoryId,
                    options = options,
                    onValueChange = { categoryId = it },
                    placeholder = if (options.isEmpty()) {
                        "Every category already has a wishlist"
                    } else {
                        "Choose a category"
                    },
                    error = categoryError,
                    showError = showAllErrors
                )

                Spacer(modifier = Modifier.height(18.dp))

                ProductPictureField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    error = imageUrlError,
                    showError = showAllErrors
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            WhyNotErrorBanner(message = errorMessage)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyLarge,
                    color = WhyNotGray,
                    modifier = Modifier.clickable { onCancelClick() }
                )

                Text(
                    text = if (saving) "Saving..." else "Save",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (saving) WhyNotDisabled else WhyNotGray,
                    modifier = Modifier.clickable(enabled = !saving) {
                        if (categoryError == null && imageUrlError == null) {
                            onSave(categoryId, imageUrl)
                        } else {
                            showAllErrors = true
                        }
                    }
                )
            }
        }

        WhyNotBottomBar(
            selectedTab = WhyNotTab.WISHLISTS,
            onHomeClick = onHomeClick,
            onWishlistsClick = onWishlistsClick,
            onAddClick = onAddClick,
            onPurchasesClick = onPurchasesClick,
            onProfileClick = onProfileClick
        )
    }
}
