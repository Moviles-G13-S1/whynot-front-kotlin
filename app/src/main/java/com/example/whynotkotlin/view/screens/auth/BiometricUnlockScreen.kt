package com.example.whynotkotlin.view.screens.auth

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.controller.BiometricController
import com.example.whynotkotlin.view.components.Biometrics
import com.example.whynotkotlin.view.components.WhyNotButton
import com.example.whynotkotlin.view.navigation.WhyNotNavigator
import com.example.whynotkotlin.view.theme.WhyNotGray
import com.example.whynotkotlin.view.theme.whyNotMuted
import kotlinx.coroutines.launch

/**
 * Startup gate shown when a saved session has biometric unlock on. Nothing
 * from the account is shown until the check passes; "Log in with password"
 * signs out and opens the login screen, as in the Flutter app.
 */
@Composable
fun BiometricUnlockRoute(navigator: WhyNotNavigator) {
    val dependencies = LocalAppDependencies.current
    val biometricController = dependencies.biometricController
    val authController = dependencies.authController
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    fun unlock() {
        if (busy) return
        busy = true
        message = null
        scope.launch {
            val passed = Biometrics.authenticate(
                context,
                BiometricController.PROMPT_TITLE,
                BiometricController.PROMPT_SUBTITLE
            )
            busy = false
            if (passed) {
                biometricController.markUnlocked()
                navigator.toHomeAfterUnlock()
            } else {
                message = BiometricController.UNLOCK_FAILED
            }
        }
    }

    fun usePassword() {
        if (busy) return
        busy = true
        scope.launch {
            // Signing out sends the app to Login (see WhyNotNavigation).
            runCatching { authController.signOut() }
            busy = false
        }
    }

    // The prompt opens by itself the first time, like Face ID on iOS.
    LaunchedEffect(Unit) { unlock() }

    // Back leaves the app instead of skipping the lock.
    BackHandler { (context as? Activity)?.finish() }

    BiometricUnlockScreen(
        busy = busy,
        message = message,
        onUnlock = ::unlock,
        onUsePassword = ::usePassword
    )
}

@Composable
fun BiometricUnlockScreen(
    busy: Boolean,
    message: String?,
    onUnlock: () -> Unit,
    onUsePassword: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "W H Y N O T",
            style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 2.sp),
            color = WhyNotGray
        )

        Spacer(modifier = Modifier.height(46.dp))

        Text(
            text = "Unlock WhyNot",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message ?: "Use your face or fingerprint to access your account.",
            style = whyNotMuted(14),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (busy) {
            CircularProgressIndicator()
        } else {
            WhyNotButton(text = "Unlock", onClick = onUnlock, filled = true)
            TextButton(onClick = onUsePassword) {
                Text(text = "Log in with password", style = whyNotMuted(14))
            }
        }
    }
}
