package com.example.whynotkotlin.view.screens.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
internal fun SmartDetailDialog(title: String, onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp)) {
                TextButton(onClick = onClose) { Text("Back") }
                Text(title, style = MaterialTheme.typography.headlineMedium)
                content()
            }
        }
    }
}

@Composable
internal fun ExternalWebsiteButton(url: String, label: String) {
    val context = LocalContext.current
    val uri = remember(url) { Uri.parse(url) }
    var error by remember(url) { mutableStateOf<String?>(null) }
    if (uri.scheme !in listOf("http", "https") || uri.host.isNullOrBlank()) {
        Text("Website unavailable")
        return
    }
    TextButton(onClick = {
        error = null
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: ActivityNotFoundException) {
            error = "No browser is available to open this link."
        }
    }) { Text(label) }
    error?.let { Text(it) }
}
