package com.example.whynotkotlin.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.view.theme.WhyNotBeige
import com.example.whynotkotlin.view.theme.WhyNotDisabled

/**
 * A product or wishlist picture: the real image when [imageUrl] loads, and the
 * beige box with [label] when there is no URL or it cannot be loaded.
 *
 * [ratio] fixes the shape; pass null to let [modifier] decide the size.
 */
@Composable
fun ProductImage(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    ratio: Float? = 0.78f,
    cornerRadius: Int = 16,
    label: String = ""
) {
    val shape = RoundedCornerShape(cornerRadius.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (ratio != null) Modifier.aspectRatio(ratio) else Modifier)
            .clip(shape)
            .background(WhyNotBeige, shape),
        contentAlignment = Alignment.Center
    ) {
        RemoteImage(
            url = imageUrl,
            contentDescription = label.ifBlank { null },
            modifier = Modifier.fillMaxSize()
        ) {
            if (label.isNotBlank()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = WhyNotDisabled
                )
            }
        }
    }
}
