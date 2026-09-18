package com.example.whynotkotlin.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val WhyNotColorScheme = lightColorScheme(
    primary = WhyNotBlack,
    onPrimary = WhyNotWhite,

    primaryContainer = WhyNotBeige,
    onPrimaryContainer = WhyNotBlack,

    secondary = WhyNotGray,
    onSecondary = WhyNotWhite,

    secondaryContainer = WhyNotCream,
    onSecondaryContainer = WhyNotBlack,

    background = WhyNotWhite,
    onBackground = WhyNotBlack,

    surface = WhyNotWhite,
    onSurface = WhyNotBlack,

    surfaceVariant = WhyNotCream,
    onSurfaceVariant = WhyNotGray,

    outline = WhyNotBorder
)

@Composable
fun WhynotkotlinTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WhyNotColorScheme,
        typography = WhyNotTypography,
        content = content
    )
}