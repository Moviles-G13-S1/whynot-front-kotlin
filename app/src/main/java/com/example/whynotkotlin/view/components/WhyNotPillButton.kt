package com.example.whynotkotlin.view.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.view.theme.WhyNotBorder
import com.example.whynotkotlin.view.theme.WhyNotField
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.theme.whyNotMuted

/** White pill-shaped action with a soft shadow, as used across the iOS app. */
@Composable
fun WhyNotPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .width(190.dp)
            .height(38.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, WhyNotBorder),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 4.dp,
            disabledElevation = 4.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = WhyNotField,
            contentColor = WhyNotGray,
            disabledContainerColor = WhyNotField,
            disabledContentColor = WhyNotGray
        )
    ) {
        Text(text = text, style = whyNotMuted(15))
    }
}
