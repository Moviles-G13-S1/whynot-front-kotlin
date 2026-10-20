package com.example.whynotkotlin.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.view.theme.WhyNotBeige
import com.example.whynotkotlin.view.theme.WhyNotDisabled

@Composable
fun ProductImage(
    modifier: Modifier = Modifier,
    ratio: Float = 0.78f,
    cornerRadius: Int = 16,
    label: String = ""
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .background(
                WhyNotBeige,
                RoundedCornerShape(cornerRadius.dp)
            ),
        contentAlignment = Alignment.Center
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
