package com.example.whynotkotlin.features.authentication.data

import com.example.whynotkotlin.features.authentication.domain.AuthRepository
import com.example.whynotkotlin.features.authentication.domain.AuthUser
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val ADMIN_CLAIM = "admin"

class FirebaseAuthRepository(
    private val auth: FirebaseAuth
) : AuthRepository {

    override fun observeSession(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { instance ->
            trySend(instance.currentUser?.toAuthUser())
        }

        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun currentUser(): AuthUser? = auth.currentUser?.toAuthUser()

    override suspend fun signIn(email: String, password: String): AuthUser {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
        return requireNotNull(result.user).toAuthUser()
    }

    override suspend fun signUp(email: String, password: String): AuthUser {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        return requireNotNull(result.user).toAuthUser()
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser ?: error("No signed-in user")
        val email = user.email ?: error("Signed-in user has no email")

        // Firebase requires a recent sign-in before a password change, so
        // reauthenticate with the current password first.
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).await()
        user.updatePassword(newPassword).await()
    }

    override suspend fun refreshClaims(): AuthUser? {
        val user = auth.currentUser ?: return null
        val token = user.getIdToken(true).await()
        val isAdmin = token.claims[ADMIN_CLAIM] == true

        return AuthUser(
            uid = user.uid,
            email = user.email.orEmpty(),
            isAdmin = isAdmin
        )
    }
}

private fun FirebaseUser.toAuthUser() = AuthUser(
    uid = uid,
    email = email.orEmpty()
)
