package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.AuthUser
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.InMemoryBiometricPreferenceStore
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BiometricControllerTest {

    private class Auth(var user: AuthUser?) : AuthRepository {
        override fun observeSession() = flowOf(user)
        override fun currentUser() = user
        override suspend fun signIn(email: String, password: String) = user!!
        override suspend fun signUp(email: String, password: String) = user!!
        override suspend fun signOut() { user = null }
        override suspend fun deleteCurrentUser() = Unit
        override suspend fun refreshClaims() = user
        override suspend fun changePassword(currentPassword: String, newPassword: String) = Unit
    }

    private val alice = AuthUser("alice", "alice@example.com")
    private val bob = AuthUser("bob", "bob@example.com")

    @Test fun `no lock unless the signed-in user turned it on`() = runTest {
        val controller = BiometricController(Auth(alice), InMemoryBiometricPreferenceStore())
        assertFalse(controller.needsUnlock())
        assertFalse(BiometricController(Auth(null), InMemoryBiometricPreferenceStore()).needsUnlock())
    }

    @Test fun `once turned on, the next app start asks for biometrics`() = runTest {
        val preferences = InMemoryBiometricPreferenceStore()
        BiometricController(Auth(alice), preferences).enable()

        // A new app process: nothing unlocked yet.
        val nextStart = BiometricController(Auth(alice), preferences)
        assertTrue(nextStart.isEnabled())
        assertTrue(nextStart.needsUnlock())

        nextStart.markUnlocked()
        assertFalse(nextStart.needsUnlock())
    }

    @Test fun `the choice belongs to each account on the phone`() = runTest {
        val preferences = InMemoryBiometricPreferenceStore()
        val auth = Auth(alice)
        BiometricController(auth, preferences).enable()

        auth.user = bob
        assertFalse(BiometricController(auth, preferences).needsUnlock())
    }

    @Test fun `turning it off or signing out removes the lock state`() = runTest {
        val preferences = InMemoryBiometricPreferenceStore()
        val controller = BiometricController(Auth(alice), preferences)
        controller.enable()
        controller.onSignedOut()
        assertTrue(controller.needsUnlock())

        controller.disable()
        assertFalse(controller.needsUnlock())
        assertFalse(controller.isEnabled())
    }
}
