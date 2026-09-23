package com.example.whynotkotlin.features.authentication.data

import com.example.whynotkotlin.features.authentication.domain.AuthRepository
import com.example.whynotkotlin.features.authentication.domain.AuthUser
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val ADMIN_CLAIM = "admin"

class FirebaseAuthRepository(
    private val auth: FirebaseAuth
) : AuthRepository {

    /**
     * Observes the authenticated Firebase session.
     *
     * Besides the UID and email, the Firebase ID token is read so the
     * application can know whether the user has the custom claim:
     *
     * admin = true
     *
     * UI authorization uses this value only to decide whether the Admin
     * section should be visible. Firestore Rules remain the real security
     * boundary.
     */
    override fun observeSession(): Flow<AuthUser?> = callbackFlow {

        val listener = FirebaseAuth.AuthStateListener { instance ->

            val firebaseUser = instance.currentUser

            if (firebaseUser == null) {
                trySend(null)
                return@AuthStateListener
            }

            launch {

                val authUser = runCatching {
                    firebaseUser.toAuthUserWithClaims(
                        forceRefresh = false
                    )
                }.getOrElse {

                    /*
                     * Fail closed:
                     *
                     * If claims cannot be read, the user is still treated as
                     * authenticated but NOT as an administrator.
                     */
                    firebaseUser.toBasicAuthUser()
                }

                trySend(authUser)
            }
        }

        auth.addAuthStateListener(listener)

        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    /**
     * This synchronous method cannot fetch Firebase token claims.
     *
     * Admin authorization inside the UI therefore comes from
     * observeSession(), which does read the token.
     */
    override fun currentUser(): AuthUser? =
        auth.currentUser?.toBasicAuthUser()

    override suspend fun signIn(
        email: String,
        password: String
    ): AuthUser {

        val result = auth
            .signInWithEmailAndPassword(
                email.trim(),
                password
            )
            .await()

        val user = requireNotNull(result.user)

        /*
         * Force refresh on sign-in so a recently granted/revoked
         * admin claim is immediately visible.
         */
        return user.toAuthUserWithClaims(
            forceRefresh = true
        )
    }

    override suspend fun signUp(
        email: String,
        password: String
    ): AuthUser {

        val result = auth
            .createUserWithEmailAndPassword(
                email.trim(),
                password
            )
            .await()

        val user = requireNotNull(result.user)

        return user.toAuthUserWithClaims(
            forceRefresh = false
        )
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ) {
        val user =
            auth.currentUser
                ?: error("No signed-in user")

        val email =
            user.email
                ?: error(
                    "Signed-in user has no email"
                )

        /*
         * Firebase requires a recent login before changing
         * sensitive account information.
         */
        val credential =
            EmailAuthProvider.getCredential(
                email,
                currentPassword
            )

        user.reauthenticate(
            credential
        ).await()

        user.updatePassword(
            newPassword
        ).await()
    }

    /**
     * Explicitly refreshes the Firebase ID token.
     *
     * Useful immediately after granting or revoking an admin claim.
     */
    override suspend fun refreshClaims(): AuthUser? {

        val user =
            auth.currentUser
                ?: return null

        return user.toAuthUserWithClaims(
            forceRefresh = true
        )
    }
}

/**
 * Builds an AuthUser without privileges.
 *
 * This is intentionally fail-closed: without a verified token claim,
 * isAdmin must remain false.
 */
private fun FirebaseUser.toBasicAuthUser() =
    AuthUser(
        uid = uid,
        email = email.orEmpty(),
        isAdmin = false
    )

/**
 * Reads the Firebase ID token and extracts the admin custom claim.
 */
private suspend fun FirebaseUser.toAuthUserWithClaims(
    forceRefresh: Boolean
): AuthUser {

    val token =
        getIdToken(
            forceRefresh
        ).await()

    val isAdmin =
        token.claims[ADMIN_CLAIM] == true

    return AuthUser(
        uid = uid,
        email = email.orEmpty(),
        isAdmin = isAdmin
    )
}