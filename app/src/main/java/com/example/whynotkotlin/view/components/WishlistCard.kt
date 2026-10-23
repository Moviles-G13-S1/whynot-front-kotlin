package com.example.whynotkotlin.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.view.theme.WhyNotBeige
import com.example.whynotkotlin.view.theme.whyNotMuted
import com.example.whynotkotlin.view.theme.whyNotSerif

/** Wishlist cover with its name and item count, shared with the iOS design. */
@Composable
fun WishlistCard(
    name: String,
    itemCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        horizontalAlignment = horizontalAlignment
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(131f / 170f)
                .clip(RoundedCornerShape(18.dp))
                .background(WhyNotBeige)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = name, style = whyNotSerif(17))

        Spacer(modifier = Modifier.height(5.dp))

        Text(text = "$itemCount items", style = whyNotMuted(12))
    }
}
