package com.example.whynotkotlin.ui.screens.products

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.ui.components.CategoryChip
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.components.WhyNotButton
import com.example.whynotkotlin.ui.theme.WhyNotBlack
import com.example.whynotkotlin.ui.theme.WhyNotBorder
import com.example.whynotkotlin.ui.theme.WhyNotGray
import com.example.whynotkotlin.ui.theme.WhyNotWhite

private val productWishlists = listOf(
    "Beauty",
    "Clothes",
    "Tech"
)

@Composable
fun AddProductScreen(
    onAddManuallyClick: () -> Unit,
    onSaveItemClick: () -> Unit,
    onHomeClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onPurchasesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var link by remember { mutableStateOf("") }
    var selectedWishlist by remember { mutableStateOf("") }

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
                text = "Why Not?",
                style = MaterialTheme.typography.displaySmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Paste a link and we'll do the rest.",
                style = MaterialTheme.typography.bodyLarge,
                color = WhyNotGray
            )

            Spacer(modifier = Modifier.height(18.dp))

            LinkField(
                value = link,
                onValueChange = { link = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Or, add manually",
                style = MaterialTheme.typography.bodyLarge,
                color = WhyNotGray
            )

            Spacer(modifier = Modifier.height(10.dp))

            WhyNotButton(
                text = "Add manually",
                onClick = onAddManuallyClick
            )

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
                productWishlists.forEach { wishlist ->
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
                enabled = link.isNotBlank() && selectedWishlist.isNotBlank()
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

@Composable
private fun LinkField(
    value: String,
    onValueChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(50)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(WhyNotWhite, shape)
            .border(BorderStroke(1.dp, WhyNotBorder), shape)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Link,
            contentDescription = null,
            tint = WhyNotGray,
            modifier = Modifier.size(20.dp)
        )

        Box(
            modifier = Modifier.weight(1f)
        ) {
            if (value.isBlank()) {
                Text(
                    text = "http://www.somestore.com",
                    style = MaterialTheme.typography.bodyLarge,
                    color = WhyNotGray
                )
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = WhyNotBlack
                ),
                cursorBrush = SolidColor(WhyNotBlack),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
