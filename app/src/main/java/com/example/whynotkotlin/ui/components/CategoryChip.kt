package com.example.whynotkotlin.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.ui.theme.WhyNotBlack
import com.example.whynotkotlin.ui.theme.WhyNotBorder
import com.example.whynotkotlin.ui.theme.WhyNotGray
import com.example.whynotkotlin.ui.theme.WhyNotWhite

@Composable
fun CategoryChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    icon: String = ""
) {
    val shape = RoundedCornerShape(50)

    Row(
        modifier = modifier
            .background(
                if (selected) WhyNotBlack else WhyNotWhite,
                shape
            )
            .border(
                BorderStroke(1.dp, if (selected) WhyNotBlack else WhyNotBorder),
                shape
            )
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon.isNotBlank()) {
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyLarge,
                color = if (selected) WhyNotWhite else WhyNotGray
            )
        }

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) WhyNotWhite else WhyNotGray
        )
    }
}
