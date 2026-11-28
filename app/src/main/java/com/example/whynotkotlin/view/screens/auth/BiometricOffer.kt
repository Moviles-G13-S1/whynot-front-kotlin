package com.example.whynotkotlin.view.screens.auth

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.controller.BiometricController
import com.example.whynotkotlin.view.components.Biometrics
import com.example.whynotkotlin.view.components.WhyNotConfirmDialog
import kotlinx.coroutines.launch

private enum class Offer { TURN_ON, ALREADY_ON }

/**
 * What happens right after logging in or creating an account: the password was
 * just typed, so this run counts as unlocked, the biometric offer is queued and
 * the user goes Home. The offer itself is shown by [BiometricOfferHost].
 */
@Composable
fun rememberAfterSignIn(goHome: () -> Unit): () -> Unit {
    val controller = LocalAppDependencies.current.biometricController
    return remember(controller, goHome) {
        {
            controller.markUnlocked()
            controller.requestOffer()
            goHome()
        }
    }
}

/**
 * As Flutter's offerFaceId: asks whether to unlock WhyNot with the face or
 * fingerprint next time (or, when already on, whether to keep it). It sits
 * above the navigation graph so it survives the move from Login to Home.
 * Declining or failing never blocks access to the account.
 */
@Composable
fun BiometricOfferHost() {
    val controller = LocalAppDependencies.current.biometricController
    val pending by controller.offerPending.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var offer by remember { mutableStateOf<Offer?>(null) }

    LaunchedEffect(pending) {
        if (!pending) return@LaunchedEffect
        offer = when {
            controller.isEnabled() -> Offer.ALREADY_ON
            Biometrics.isAvailable(context) -> Offer.TURN_ON
            else -> null
        }
        if (offer == null) controller.offerAnswered()
    }

    fun close() {
        offer = null
        controller.offerAnswered()
    }

    when (offer) {
        Offer.TURN_ON -> WhyNotConfirmDialog(
            title = "Biometric unlock",
            message = "Use your face or fingerprint to unlock WhyNot next time you open the app?",
            confirmLabel = "Turn on",
            dismissLabel = "Not now",
            onDismiss = ::close,
            onConfirm = {
                close()
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
                }
            }
        )

        Offer.ALREADY_ON -> WhyNotConfirmDialog(
            title = "Biometric unlock",
            message = "Biometric unlock is on for this account. Keep it on?",
            confirmLabel = "Turn off",
            dismissLabel = "Keep on",
            onDismiss = ::close,
            onConfirm = {
                close()
                scope.launch { runCatching { controller.disable() } }
            }
        )

        null -> Unit
    }
}
