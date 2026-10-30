package com.example.whynotkotlin.controller

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
class ProfileAndPasswordTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    private class Auth : AuthRepository {
        var changed: Pair<String, String>? = null
        var failure: Exception? = null
        val user = AuthUser("uid", "user@example.com")
        override fun observeSession() = flowOf(user)
        override fun currentUser() = user
        override suspend fun signIn(email: String, password: String) = user
        override suspend fun signUp(email: String, password: String) = user
        override suspend fun signOut() = Unit
        override suspend fun deleteCurrentUser() = Unit
        override suspend fun refreshClaims() = user
        override suspend fun changePassword(currentPassword: String, newPassword: String) {
            failure?.let { throw it }
            changed = currentPassword to newPassword
        }
    }

    private class Users : UserRepository {
        val profile = UserProfile("uid", "Test User", "user@example.com", "Male", 22, "tech", "", null, null)
        var update: UserProfileUpdate? = null
        var uid: String? = null
        var failure: Exception? = null
        override fun observeProfile(uid: String) = flow {
            failure?.let { throw it }
            emit(profile)
        }
        override suspend fun getProfile(uid: String) = profile
        override suspend fun createProfile(uid: String, email: String, draft: UserProfileDraft) = Unit
        override suspend fun updateProfile(uid: String, update: UserProfileUpdate) {
            failure?.let { throw it }
            this.uid = uid
            this.update = update
        }
    }

    private class Lists : WishlistRepository {
        override suspend fun getCategories() = emptyList<Category>()
        override fun observeCategories() = flowOf(emptyList<Category>())
        override fun observeWishlists(ownerId: String) = flowOf(emptyList<Wishlist>())
        override suspend fun getWishlist(wishlistId: String): Wishlist? = null
        override suspend fun createWishlist(ownerId: String, draft: WishlistDraft) = "unused"
    }

    private class NoOnboarding : OnboardingRepository {
        override suspend fun recordStep(userId: String, step: OnboardingStep) = Unit
    }

    @Test fun `profile saves real uid and selected category`() = runTest(dispatcher) {
        val users = Users()
        val controller = ProfileController(Auth(), users, Lists(), backgroundScope)
        val profile = watch(controller.profile)
        runCurrent()
        assertEquals(users.profile, profile.value?.getOrNull())
        controller.updateProfile(profile.value?.getOrNull()?.uid, "Test User", "Male", "23", "beauty")
        assertEquals("uid", users.uid)
        assertEquals("beauty", users.update?.preferredCategoryId)
        assertEquals(23, users.update?.age)
    }

    @Test fun `an unrecognised failure reaches the form with a generic message`() = runTest(dispatcher) {
        val users = Users()
        val controller = ProfileController(Auth(), users, Lists(), backgroundScope)
        users.failure = IllegalStateException("No connection")
        val error = runCatching {
            controller.updateProfile("uid", "Test User", "Male", "23", "tech")
        }.exceptionOrNull()
        assertNotNull(error)
        assertNull(users.update)
        assertEquals("Something went wrong. Try again.", error!!.readableMessage())
    }

    @Test fun `invalid age is rejected before reaching the backend`() = runTest(dispatcher) {
        val users = Users()
        val controller = ProfileController(Auth(), users, Lists(), backgroundScope)
        val error = runCatching {
            controller.updateProfile("uid", "Test User", "Male", "7", "tech")
        }.exceptionOrNull()
        assertTrue(error is ValidationException)
        assertNull(users.update)
    }

    @Test fun `profile read failure finishes loading with error`() = runTest(dispatcher) {
        val users = Users().apply { failure = IllegalStateException("Offline") }
        val controller = ProfileController(Auth(), users, Lists(), backgroundScope)
        val profile = watch(controller.profile)
        runCurrent()
        assertNotNull(profile.value)
        assertNull(profile.value?.getOrNull())
        assertNotNull(profile.value?.exceptionOrNull())
    }

    @Test fun `password change passes credentials to the backend`() = runTest(dispatcher) {
        val auth = Auth()
        val controller = AuthController(auth, Users(), NoOnboarding(), backgroundScope)
        controller.changePassword("old-password", "NewPassword1", "NewPassword1")
        assertEquals("old-password" to "NewPassword1", auth.changed)
    }

    @Test fun `mismatched new passwords are rejected before the backend`() = runTest(dispatcher) {
        val auth = Auth()
        val controller = AuthController(auth, Users(), NoOnboarding(), backgroundScope)
        val error = runCatching {
            controller.changePassword("old-password", "NewPassword1", "OtherPassword1")
        }.exceptionOrNull()
        assertTrue(error is ValidationException)
        assertNull(auth.changed)
    }

    @Test fun `weak new password is rejected with the missing requirement`() = runTest(dispatcher) {
        val auth = Auth()
        val controller = AuthController(auth, Users(), NoOnboarding(), backgroundScope)
        val error = runCatching {
            controller.changePassword("old-password", "newpassword1", "newpassword1")
        }.exceptionOrNull()
        assertEquals("The new password needs at least one uppercase letter", error?.message)
        assertNull(auth.changed)
    }

    @Test fun `new password equal to the current one is rejected`() = runTest(dispatcher) {
        val auth = Auth()
        val controller = AuthController(auth, Users(), NoOnboarding(), backgroundScope)
        val error = runCatching {
            controller.changePassword("SamePassword1", "SamePassword1", "SamePassword1")
        }.exceptionOrNull()
        assertEquals("The new password must be different from the current one", error?.message)
        assertNull(auth.changed)
    }

    @Test fun `wrong password response keeps form open`() = runTest(dispatcher) {
        val auth = Auth().apply { failure = IllegalArgumentException("credential is incorrect") }
        val controller = AuthController(auth, Users(), NoOnboarding(), backgroundScope)
        val error = runCatching {
            controller.changePassword("wrong", "NewPassword1", "NewPassword1")
        }.exceptionOrNull()
        assertEquals("Wrong email or password", error?.readableMessage())
    }
}
