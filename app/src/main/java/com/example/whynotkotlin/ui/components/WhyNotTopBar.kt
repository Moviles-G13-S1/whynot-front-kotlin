package com.example.whynotkotlin.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.ui.theme.WhyNotBlack

@Composable
fun WhyNotTopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackButton) {
            TextButton(onClick = onBackClick) {
                Text(
                    text = "‹ Back",
                    style = MaterialTheme.typography.labelMedium,
                    color = WhyNotBlack
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = WhyNotBlack,
            modifier = Modifier.padding(start = if (showBackButton) 8.dp else 0.dp)
        )
    }
}