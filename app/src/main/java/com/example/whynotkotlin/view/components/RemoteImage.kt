package com.example.whynotkotlin.view.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.SubcomposeAsyncImage

/**
 * Loads an image from a URL through Coil, which keeps it in the app-wide memory
 * and disk caches (configured in WhyNotApplication), so the same picture is not
 * downloaded again and still shows offline once it has been seen.
 *
 * [fallback] is drawn when the URL is blank, while loading, and when the image
 * cannot be loaded, so a missing picture never leaves an empty hole.
 */
@Composable
fun RemoteImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallback: @Composable () -> Unit = {}
) {
    if (url.isNullOrBlank()) {
        fallback()
        return
    }

    SubcomposeAsyncImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        loading = { fallback() },
        error = { fallback() }
    )
}
