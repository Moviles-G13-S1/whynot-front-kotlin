package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.BiometricPreferenceStore

/**
 * Biometric unlock (face or fingerprint) for an existing session, as Face ID
 * in the iOS/Flutter app:
 * - after signing in or creating an account the user is offered to turn it on;
 * - when the app opens with a saved session and it is on, the app stays on the
 *   unlock screen until the biometric check passes, or the user chooses to log
 *   in with the password (which signs out).
 *
 * The biometric prompt itself is shown by the view; this class keeps the
 * per-account choice and whether this app run was already unlocked.
 */
class BiometricController(
    private val authRepository: AuthRepository,
    private val preferences: BiometricPreferenceStore
) {

    /**
     * True right after a password sign-in, until the offer dialog is answered.
     * Kept here (not in the login screen) because the login screen leaves
     * the back stack as soon as the session starts.
     */
    private val _offerPending = kotlinx.coroutines.flow.MutableStateFlow(false)
    val offerPending: kotlinx.coroutines.flow.StateFlow<Boolean> = _offerPending

    fun requestOffer() {
        _offerPending.value = true
    }

    fun offerAnswered() {
        _offerPending.value = false
    }

    /** Unlocked for this app process: reopening a screen does not ask again. */
    @Volatile
    private var unlockedUid: String? = null

    /** Whether the app must show the unlock screen before Home. */
    suspend fun needsUnlock(): Boolean {
        val uid = authRepository.currentUser()?.uid ?: return false
        if (unlockedUid == uid) return false
        return runCatching { preferences.isEnabled(uid) }.getOrDefault(false)
    }

    /** Whether the signed-in user already turned biometric unlock on. */
    suspend fun isEnabled(): Boolean {
        val uid = authRepository.currentUser()?.uid ?: return false
        return runCatching { preferences.isEnabled(uid) }.getOrDefault(false)
    }

    /** The biometric check passed (or the user just typed their password). */
    fun markUnlocked() {
        unlockedUid = authRepository.currentUser()?.uid
    }

    /** Call only after the biometric prompt succeeded. */
    suspend fun enable() {
        val uid = authRepository.currentUser()?.uid ?: return
        preferences.setEnabled(uid, true)
        unlockedUid = uid
    }

    suspend fun disable() {
        val uid = authRepository.currentUser()?.uid ?: return
        preferences.setEnabled(uid, false)
    }

    fun onSignedOut() {
        unlockedUid = null
    }

    companion object {
        const val PROMPT_TITLE = "Unlock WhyNot"
        const val PROMPT_SUBTITLE = "Use your face or fingerprint to open your account"
        const val UNLOCK_FAILED = "Biometric unlock didn't work. Try again or log in with your password."
        const val ENABLE_FAILED = "Biometric unlock was not turned on. You can try again next time you log in."
    }
}
