package com.example.whynotkotlin.ui.screens.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.whynotkotlin.ui.components.CategoryChip
import com.example.whynotkotlin.ui.components.ProductPictureField
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.components.WhyNotButton
import com.example.whynotkotlin.ui.components.WhyNotTextField
import com.example.whynotkotlin.ui.theme.WhyNotCream
import com.example.whynotkotlin.ui.theme.WhyNotGray

private val productWishlistOptions = listOf(
    "Beauty",
    "Clothes",
    "Tech"
)

@Composable
fun NewProductScreen(
    onSaveItemClick: () -> Unit,
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var picture by remember { mutableStateOf("") }
    var selectedWishlist by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = "New Product",
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
                WhyNotTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Name"
                )

                Spacer(modifier = Modifier.height(18.dp))

                ProductPictureField(
                    fileName = picture,
                    onPickPictureClick = { picture = "product.png" }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Save to:",
                style = MaterialTheme.typography.bodyLarge,
                color = WhyNotGray
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                productWishlistOptions.forEach { wishlist ->
                    CategoryChip(
                        text = wishlist,
                        selected = selectedWishlist == wishlist,
                        onClick = {
                            selectedWishlist = if (selectedWishlist == wishlist) "" else wishlist
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            WhyNotButton(
                text = "Save Item",
                onClick = onSaveItemClick,
                filled = true,
                enabled = name.isNotBlank() && selectedWishlist.isNotBlank()
            )
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
