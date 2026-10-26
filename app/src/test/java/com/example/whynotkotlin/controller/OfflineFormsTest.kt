package com.example.whynotkotlin.controller

import com.example.whynotkotlin.fakes.FakeConnectivityRepository
import com.example.whynotkotlin.model.*
import com.example.whynotkotlin.model.repositories.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class OfflineFormsTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    private class Auth : AuthRepository {
        val user = AuthUser("uid", "user@example.com")
        var calls = 0
        var failure: Exception? = null
        override fun observeSession() = flowOf(user)
        override fun currentUser() = user
        override suspend fun signIn(email: String, password: String): AuthUser {
            calls++
            failure?.let { throw it }
            return user
        }
        override suspend fun signUp(email: String, password: String): AuthUser {
            calls++
            return user
        }
        override suspend fun signOut() = Unit
        override suspend fun refreshClaims() = user
        override suspend fun changePassword(currentPassword: String, newPassword: String) {
            calls++
        }
    }

    private class Users : UserRepository {
        var updates = 0
        override fun observeProfile(uid: String) = flowOf<UserProfile?>(null)
        override suspend fun getProfile(uid: String): UserProfile? = null
        override suspend fun createProfile(uid: String, email: String, draft: UserProfileDraft) = Unit
        override suspend fun updateProfile(uid: String, update: UserProfileUpdate) {
            updates++
        }
    }

    private class Lists : WishlistRepository {
        override suspend fun getCategories() = emptyList<Category>()
        override fun observeCategories() = flowOf(emptyList<Category>())
        override fun observeWishlists(ownerId: String) = flowOf(emptyList<Wishlist>())
        override suspend fun getWishlist(wishlistId: String): Wishlist? = null
        override suspend fun createWishlist(ownerId: String, draft: WishlistDraft) = "unused"
    }

    private object NoOnboarding : OnboardingRepository {
        override suspend fun recordStep(userId: String, step: OnboardingStep) = Unit
    }

    private fun TestScope.auth(repository: Auth, connectivity: FakeConnectivityRepository) =
        AuthController(repository, Users(), NoOnboarding, backgroundScope, connectivity)

    @Test fun `signing in offline does not reach Firebase and explains why`() = runTest(dispatcher) {
        val repository = Auth()
        val controller = auth(repository, FakeConnectivityRepository(initiallyOnline = false))

        val error = runCatching { controller.signIn("user@example.com", "Password1") }.exceptionOrNull()

        assertTrue(error is OfflineException)
        assertTrue(error!!.message!!.startsWith("No connection."))
        assertEquals(0, repository.calls)
    }

    @Test fun `signing up offline does not create the account`() = runTest(dispatcher) {
        val repository = Auth()
        val controller = auth(repository, FakeConnectivityRepository(initiallyOnline = false))

        val error = runCatching {
            controller.signUp("New User", "new@example.com", "Password1", "Female", "24", "tech", "bogota")
        }.exceptionOrNull()

        assertTrue(error is OfflineException)
        assertEquals(0, repository.calls)
    }

    @Test fun `input errors are still reported before the offline check`() = runTest(dispatcher) {
        val controller = auth(Auth(), FakeConnectivityRepository(initiallyOnline = false))

        val error = runCatching { controller.signIn("", "") }.exceptionOrNull()

        assertTrue(error is ValidationException)
    }

    @Test fun `a network failure while signing in gives a connection message`() = runTest(dispatcher) {
        val repository = Auth().apply { failure = IllegalStateException(
            // Firebase's own wording; FirebaseNetworkException cannot be built in JVM tests.
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred."
        ) }
        val controller = auth(repository, FakeConnectivityRepository())

        val error = runCatching { controller.signIn("user@example.com", "Password1") }.exceptionOrNull()

        assertEquals("No connection. Check your network and try again", error?.readableMessage())
    }

    @Test fun `signing in works again once back online`() = runTest(dispatcher) {
        val repository = Auth()
        val connectivity = FakeConnectivityRepository(initiallyOnline = false)
        val controller = auth(repository, connectivity)

        runCatching { controller.signIn("user@example.com", "Password1") }
        connectivity.goOnline()
        controller.signIn("user@example.com", "Password1")

        assertEquals(1, repository.calls)
    }

    @Test fun `changing the password offline is blocked`() = runTest(dispatcher) {
        val repository = Auth()
        val controller = auth(repository, FakeConnectivityRepository(initiallyOnline = false))

        val error = runCatching {
            controller.changePassword("OldPassword1", "NewPassword1", "NewPassword1")
        }.exceptionOrNull()

        assertTrue(error is OfflineException)
        assertEquals(0, repository.calls)
    }

    @Test fun `saving the profile offline keeps the changes unsent`() = runTest(dispatcher) {
        val users = Users()
        val controller = ProfileController(
            Auth(), users, Lists(), backgroundScope, FakeConnectivityRepository(initiallyOnline = false)
        )

        val error = runCatching {
            controller.updateProfile("uid", "Test User", "Male", "23", "tech")
        }.exceptionOrNull()

        assertTrue(error is OfflineException)
        assertTrue(error!!.message!!.contains("changes are kept"))
        assertEquals(0, users.updates)
    }
}
