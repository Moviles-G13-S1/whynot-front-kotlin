package com.example.whynotkotlin.view.screens.auth

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.controller.BiometricController
import com.example.whynotkotlin.view.components.Biometrics
import com.example.whynotkotlin.view.components.WhyNotConfirmDialog
import kotlinx.coroutines.launch

private enum class Offer { TURN_ON, ALREADY_ON }

/**
 * What happens right after logging in or creating an account: as Flutter's
 * offerFaceId, the user is asked whether to unlock WhyNot with their face or
 * fingerprint next time (or, when it is already on, whether to keep it), and
 * then goes Home. Declining or failing never blocks access to the account.
 *
 * Returns the action to run when sign-in succeeds.
 */
@Composable
fun rememberAfterSignIn(goHome: () -> Unit): () -> Unit {
    val controller = LocalAppDependencies.current.biometricController
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var offer by remember { mutableStateOf<Offer?>(null) }

    when (offer) {
        Offer.TURN_ON -> WhyNotConfirmDialog(
            title = "Biometric unlock",
            message = "Use your face or fingerprint to unlock WhyNot next time you open the app?",
            confirmLabel = "Turn on",
            dismissLabel = "Not now",
            onDismiss = {
                offer = null
                goHome()
            },
            onConfirm = {
                offer = null
                scope.launch {
                    val passed = Biometrics.authenticate(
                        context,
                        BiometricController.PROMPT_TITLE,
                        BiometricController.PROMPT_SUBTITLE
                    )
                    if (passed) {
                        runCatching { controller.enable() }
                    } else {
                        Toast.makeText(context, BiometricController.ENABLE_FAILED, Toast.LENGTH_LONG).show()
                    }
                    goHome()
                }
            }
        )

        Offer.ALREADY_ON -> WhyNotConfirmDialog(
            title = "Biometric unlock",
            message = "Biometric unlock is on for this account. Keep it on?",
            confirmLabel = "Turn off",
            dismissLabel = "Keep on",
            onDismiss = {
                offer = null
                goHome()
            },
            onConfirm = {
                offer = null
                scope.launch {
                    runCatching { controller.disable() }
                    goHome()
                }
            }
        )

        null -> Unit
    }

    return remember(controller, context) {
        {
            // The password was just typed, so this run is already unlocked.
            controller.markUnlocked()
            scope.launch {
                val enabled = controller.isEnabled()
                offer = when {
                    enabled -> Offer.ALREADY_ON
                    Biometrics.isAvailable(context) -> Offer.TURN_ON
                    else -> null
                }
                if (offer == null) goHome()
            }
        }
    }
}
