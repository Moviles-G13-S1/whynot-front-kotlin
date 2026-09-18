package com.example.whynotkotlin.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.ui.theme.WhyNotBlack
import com.example.whynotkotlin.ui.theme.WhyNotBorder
import com.example.whynotkotlin.ui.theme.WhyNotDisabled
import com.example.whynotkotlin.ui.theme.WhyNotWhite

@Composable
fun WhyNotButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    filled: Boolean = false
) {
    if (filled) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WhyNotBlack,
                contentColor = WhyNotWhite,
                disabledContainerColor = WhyNotBorder,
                disabledContentColor = WhyNotDisabled
            )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                width = 1.dp,
                color = if (enabled) WhyNotBorder else WhyNotBorder
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = WhyNotWhite,
                contentColor = WhyNotBlack,
                disabledContainerColor = WhyNotWhite,
                disabledContentColor = WhyNotDisabled
            )
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}