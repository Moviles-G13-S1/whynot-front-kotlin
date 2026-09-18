package com.example.whynotkotlin.ui.screens.wishlists

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whynotkotlin.ui.components.ProductPictureField
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.theme.WhyNotBlack
import com.example.whynotkotlin.ui.theme.WhyNotBorder
import com.example.whynotkotlin.ui.theme.WhyNotCream
import com.example.whynotkotlin.ui.theme.WhyNotGray
import com.example.whynotkotlin.ui.theme.WhyNotWhite

private val wishlistCategories = listOf(
    "Beauty",
    "Clothes",
    "Tech",
    "Home",
    "Sports"
)

@Composable
fun NewWishlistScreen(
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {}
) {
    var category by remember { mutableStateOf("") }
    var picture by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

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
                CategoryDropdownField(
                    value = category,
                    onValueChange = { category = it }
                )

                Spacer(modifier = Modifier.height(18.dp))

                ProductPictureField(
                    fileName = picture,
                    onPickPictureClick = { picture = "wishlist_cover.png" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    text = "Save",
                    style = MaterialTheme.typography.bodyLarge,
                    color = WhyNotGray,
                    modifier = Modifier.clickable { onSaveClick() }
                )
            }
        }

        WhyNotBottomBar(
            onProfileClick = onProfileClick,
            onWishlistsClick = onWishlistsClick
        )
    }
}

@Composable
private fun CategoryDropdownField(
    value: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.titleMedium,
            color = WhyNotBlack
        )

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(WhyNotWhite, RoundedCornerShape(10.dp))
                    .border(
                        BorderStroke(1.dp, WhyNotBorder),
                        RoundedCornerShape(10.dp)
                    )
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = WhyNotBlack
                )

                Text(
                    text = "⌄",
                    fontSize = 18.sp,
                    color = WhyNotGray
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                wishlistCategories.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
