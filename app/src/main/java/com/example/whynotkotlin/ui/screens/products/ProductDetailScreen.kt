package com.example.whynotkotlin.ui.screens.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.ui.components.CategoryChip
import com.example.whynotkotlin.ui.components.ProductImage
import com.example.whynotkotlin.ui.components.WhyNotBottomBar
import com.example.whynotkotlin.ui.theme.WhyNotGray

@Composable
fun ProductDetailScreen(
    productName: String,
    price: String,
    originalPrice: String = "",
    storeName: String = "Product Store",
    onEditItemClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onWishlistsClick: () -> Unit = {}
) {
    var purchased by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = productName,
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.weight(1f)
                )

                CategoryChip(
                    text = "Purchased",
                    selected = purchased,
                    onClick = { purchased = !purchased }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = storeName,
                style = MaterialTheme.typography.bodyLarge,
                color = WhyNotGray
            )

            Spacer(modifier = Modifier.height(14.dp))

            ProductImage(ratio = 1f, cornerRadius = 22)

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (originalPrice.isNotBlank()) {
                        Text(
                            text = originalPrice,
                            style = MaterialTheme.typography.bodyLarge,
                            color = WhyNotGray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                    Text(
                        text = price,
                        style = MaterialTheme.typography.bodyLarge,
                        color = WhyNotGray
                    )
                }

                Text(
                    text = "✎ Edit item",
                    style = MaterialTheme.typography.bodyLarge,
                    color = WhyNotGray,
                    modifier = Modifier.clickable { onEditItemClick() }
                )
            }
        }

        WhyNotBottomBar(
            onProfileClick = onProfileClick,
            onAddClick = onAddClick,
            onWishlistsClick = onWishlistsClick
        )
    }
}
