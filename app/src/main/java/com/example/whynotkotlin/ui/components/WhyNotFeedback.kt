package com.example.whynotkotlin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.whynotkotlin.ui.theme.WhyNotBeige
import com.example.whynotkotlin.ui.theme.WhyNotBlack
import com.example.whynotkotlin.ui.theme.WhyNotGray

/** A short message shown when an operation the user triggered failed. */
@Composable
fun WhyNotErrorBanner(
    message: String?,
    modifier: Modifier = Modifier
) {
    if (message.isNullOrBlank()) return

    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = WhyNotBlack,
        modifier = modifier
            .fillMaxWidth()
            .background(WhyNotBeige, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    )
}

/** Centred spinner for a screen that has no data to show yet. */
@Composable
fun WhyNotLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = WhyNotBlack,
            modifier = Modifier.size(28.dp)
        )
    }
}

/** Placeholder for a list that loaded correctly but has nothing in it. */
@Composable
fun WhyNotEmptyState(
    title: String,
    hint: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = WhyNotBlack,
            textAlign = TextAlign.Center
        )

        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = WhyNotGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
