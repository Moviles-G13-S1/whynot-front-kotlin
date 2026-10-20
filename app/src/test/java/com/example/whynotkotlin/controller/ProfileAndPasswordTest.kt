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

    @Test fun `failed profile save reaches the form with a readable error`() = runTest(dispatcher) {
        val users = Users()
        val controller = ProfileController(Auth(), users, Lists(), backgroundScope)
        users.failure = IllegalStateException("No connection")
        val error = runCatching {
            controller.updateProfile("uid", "Test User", "Male", "23", "tech")
        }.exceptionOrNull()
        assertNotNull(error)
        assertNull(users.update)
        assertEquals("No connection", error!!.readableMessage())
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
        val controller = AuthController(auth, Users(), backgroundScope)
        controller.changePassword("old-password", "new-password", "new-password")
        assertEquals("old-password" to "new-password", auth.changed)
    }

    @Test fun `mismatched new passwords are rejected before the backend`() = runTest(dispatcher) {
        val auth = Auth()
        val controller = AuthController(auth, Users(), backgroundScope)
        val error = runCatching {
            controller.changePassword("old-password", "new-password", "other-password")
        }.exceptionOrNull()
        assertTrue(error is ValidationException)
        assertNull(auth.changed)
    }

    @Test fun `wrong password response keeps form open`() = runTest(dispatcher) {
        val auth = Auth().apply { failure = IllegalArgumentException("credential is incorrect") }
        val controller = AuthController(auth, Users(), backgroundScope)
        val error = runCatching {
            controller.changePassword("wrong", "new-password", "new-password")
        }.exceptionOrNull()
        assertEquals("Wrong email or password", error?.readableMessage())
    }
}
