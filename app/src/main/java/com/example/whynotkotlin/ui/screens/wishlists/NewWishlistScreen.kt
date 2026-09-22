package com.example.whynotkotlin.ui.screens.wishlists

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.features.wishlists.application.WishlistUiState
import com.example.whynotkotlin.ui.components.ProductPictureField
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.components.WhyNotDropdownField
import com.example.whynotkotlin.ui.components.WhyNotErrorBanner
import com.example.whynotkotlin.ui.theme.WhyNotCream
import com.example.whynotkotlin.ui.theme.WhyNotDisabled
import com.example.whynotkotlin.ui.theme.WhyNotGray

@Composable
fun NewWishlistScreen(
    state: WishlistUiState,
    onSave: (categoryId: String, imageUrl: String) -> Unit,
    onCancelClick: () -> Unit,
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var categoryId by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    // Only categories without a wishlist yet, so the user cannot create a
    // duplicate. The backend does not enforce this, it is a UI convention.
    val options = state.availableCategories.map { it.id to it.name }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .statusBarsPadding()
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "New Wishlist",
                style = MaterialTheme.typography.displaySmall
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
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                ProductPictureField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            WhyNotErrorBanner(message = state.errorMessage)

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

                val canSave = categoryId.isNotBlank() && !state.saving

                Text(
                    text = if (state.saving) "Saving..." else "Save",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (canSave) WhyNotGray else WhyNotDisabled,
                    modifier = Modifier.clickable(enabled = canSave) {
                        onSave(categoryId, imageUrl)
                    }
                )
            }
        }

        WhyNotBottomBar(
            onHomeClick = onHomeClick,
            onWishlistsClick = onWishlistsClick,
            onAddClick = onAddClick,
            onPurchasesClick = onPurchasesClick,
            onProfileClick = onProfileClick
        )
    }
}
