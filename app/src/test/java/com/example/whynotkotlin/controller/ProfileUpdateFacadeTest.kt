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
class ProfileUpdateFacadeTest {
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

    @Test fun `profile facade trims the name before saving`() = runTest(dispatcher) {
        val users = Users()
        ProfileUpdateFacade(Auth(), users).save("uid", "  Martin  ", "Male", " 23 ", "tech")
        assertEquals("Martin", users.update?.name)
        assertEquals(23, users.update?.age)
    }

    @Test fun `profile facade rejects another users profile`() = runTest(dispatcher) {
        val users = Users()
        val error = runCatching {
            ProfileUpdateFacade(Auth(), users).save("other-user", "Martin", "Male", "23", "tech")
        }.exceptionOrNull()
        assertTrue(error is ValidationException)
        assertNull(users.update)
    }

    @Test fun `profile facade rejects unsupported gender`() = runTest(dispatcher) {
        val users = Users()
        val error = runCatching {
            ProfileUpdateFacade(Auth(), users).save("uid", "Martin", "unknown", "23", "tech")
        }.exceptionOrNull()
        assertTrue(error is ValidationException)
        assertNull(users.update)
    }

    @Test fun `profile facade propagates failed writes`() = runTest(dispatcher) {
        val users = Users().apply { failure = IllegalStateException("offline") }
        val error = runCatching {
            ProfileUpdateFacade(Auth(), users).save("uid", "Martin", "Male", "23", "tech")
        }.exceptionOrNull()
        assertEquals("offline", error?.message)
        assertNull(users.update)
    }

}
