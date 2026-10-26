package com.example.whynotkotlin.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.view.theme.WhyNotBlack
import com.example.whynotkotlin.view.theme.WhyNotGray

@Composable
fun ProductCard(
    name: String,
    price: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    originalPrice: String = "",
    imageUrl: String = ""
) {
    Column(
        modifier = modifier.clickable { onClick() }
    ) {
        ProductImage(imageUrl = imageUrl, ratio = 0.78f)

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            color = WhyNotBlack,
            modifier = Modifier.padding(start = 4.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.padding(start = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (originalPrice.isNotBlank()) {
                Text(
                    text = originalPrice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WhyNotGray,
                    textDecoration = TextDecoration.LineThrough
                )
            }

            Text(
                text = price,
                style = MaterialTheme.typography.bodyMedium,
                color = WhyNotGray
            )
        }
    }
}
