package com.example.whynotkotlin.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.whynotkotlin.ui.theme.WhyNotBlack
import com.example.whynotkotlin.ui.theme.WhyNotGray

@Composable
fun VoiceInputButton(
    onVoiceInput: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isListening: Boolean = false
) {
    val context = LocalContext.current

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                onVoiceInput()
            }
        }

    IconButton(
        onClick = {
            val permissionGranted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

            if (permissionGranted) {
                onVoiceInput()
            } else {
                permissionLauncher.launch(
                    Manifest.permission.RECORD_AUDIO
                )
            }
        },
        enabled = enabled && !isListening,
        modifier = modifier.size(40.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Mic,
            contentDescription =
                if (isListening) "Listening" else "Voice input",
            tint =
                if (isListening) WhyNotBlack else WhyNotGray
        )
    }
}