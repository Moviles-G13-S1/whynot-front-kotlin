package com.example.whynotkotlin.features.profile.application

import com.example.whynotkotlin.features.authentication.application.AuthViewModel
import com.example.whynotkotlin.features.authentication.domain.*
import com.example.whynotkotlin.features.profile.domain.*
import com.example.whynotkotlin.features.wishlists.domain.*
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
        val profile = UserProfile("uid", "Juan", "user@example.com", "Male", 22, "tech", "", null, null)
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

    @Test fun `profile saves real uid and selected category before navigating`() = runTest(dispatcher) {
        val users = Users()
        val vm = ProfileViewModel(Auth(), users, Lists())
        advanceUntilIdle()
        assertEquals(users.profile, vm.state.value.profile)
        var success = false
        vm.updateProfile("Juan Felipe", "Male", "23", "beauty") { success = true }
        assertFalse(success)
        advanceUntilIdle()
        assertEquals("uid", users.uid)
        assertEquals("beauty", users.update?.preferredCategoryId)
        assertEquals(23, users.update?.age)
        assertTrue(success)
        assertFalse(vm.state.value.saving)
    }

    @Test fun `failed profile save stays on form with error`() = runTest(dispatcher) {
        val users = Users()
        val vm = ProfileViewModel(Auth(), users, Lists())
        advanceUntilIdle()
        users.failure = IllegalStateException("No connection")
        var success = false
        vm.updateProfile("Juan", "Male", "23", "tech") { success = true }
        advanceUntilIdle()
        assertFalse(success)
        assertFalse(vm.state.value.saving)
        assertNotNull(vm.state.value.errorMessage)
    }

    @Test fun `profile read failure finishes loading with error`() = runTest(dispatcher) {
        val users = Users().apply { failure = IllegalStateException("Offline") }
        val vm = ProfileViewModel(Auth(), users, Lists())
        advanceUntilIdle()
        assertFalse(vm.state.value.loading)
        assertNull(vm.state.value.profile)
        assertNotNull(vm.state.value.errorMessage)
    }

    @Test fun `password change passes credentials and only succeeds after backend`() = runTest(dispatcher) {
        val auth = Auth()
        val vm = AuthViewModel(auth, Users())
        var success = false
        vm.changePassword("old-password", "new-password", "new-password") { success = true }
        assertFalse(success)
        advanceUntilIdle()
        assertEquals("old-password" to "new-password", auth.changed)
        assertTrue(success)
    }

    @Test fun `wrong password response keeps form open`() = runTest(dispatcher) {
        val auth = Auth().apply { failure = IllegalArgumentException("credential is incorrect") }
        val vm = AuthViewModel(auth, Users())
        var success = false
        vm.changePassword("wrong", "new-password", "new-password") { success = true }
        advanceUntilIdle()
        assertFalse(success)
        assertFalse(vm.state.value.submitting)
        assertNotNull(vm.state.value.errorMessage)
    }
}
