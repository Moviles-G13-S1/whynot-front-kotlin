package com.example.whynotkotlin.view.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.view.theme.WhyNotCream
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.theme.whyNotMuted

/** Whether the device is online, updated live from the shared ConnectivityRepository. */
@Composable
fun rememberIsOnline(): Boolean {
    val isOnline by LocalAppDependencies.current
        .connectivity
        .isOnline
        .collectAsStateWithLifecycle()

    return isOnline
}

/**
 * The same offline notice on every screen: shown only while the device is
 * offline, with an optional [onRetry] for screens that can refresh once the
 * connection returns.
 */
@Composable
fun OfflineBanner(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    message: String = "You are offline. Showing saved information.",
    onRetry: (() -> Unit)? = null
) {
    AnimatedVisibility(visible = !isOnline, modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(WhyNotCream)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.WifiOff,
                contentDescription = null,
                tint = WhyNotGray,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = message,
                style = whyNotMuted(12),
                modifier = Modifier.weight(1f)
            )

            if (onRetry != null) {
                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Retry",
                    style = whyNotMuted(13),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onRetry() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
        }
    }
}
