package com.example.whynotkotlin.controller

import com.example.whynotkotlin.fakes.FakeConnectivityRepository
import com.example.whynotkotlin.model.AuthUser
import com.example.whynotkotlin.model.ValidationException
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.OfflineException
import com.example.whynotkotlin.model.repositories.PictureFolder
import com.example.whynotkotlin.model.repositories.PictureUploader
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PictureControllerTest {

    private class Auth(private val user: AuthUser?) : AuthRepository {
        override fun observeSession() = flowOf(user)
        override fun currentUser() = user
        override suspend fun signIn(email: String, password: String) = user!!
        override suspend fun signUp(email: String, password: String) = user!!
        override suspend fun signOut() = Unit
        override suspend fun deleteCurrentUser() = Unit
        override suspend fun refreshClaims() = user
        override suspend fun changePassword(currentPassword: String, newPassword: String) = Unit
    }

    private class Uploader : PictureUploader {
        val calls = mutableListOf<Triple<String, PictureFolder, String>>()
        override suspend fun upload(ownerId: String, folder: PictureFolder, source: String): String {
            calls += Triple(ownerId, folder, source)
            return "https://firebasestorage.googleapis.com/v0/b/x/o/${folder.path}%2F$ownerId%2Fp.jpg?alt=media&token=t"
        }
    }

    private val user = AuthUser("uid", "user@example.com")

    @Test fun `a picked photo is uploaded to the owner's folder and its link returned`() = runTest {
        val uploader = Uploader()
        val link = PictureController(Auth(user), uploader, FakeConnectivityRepository()).upload(PictureFolder.WISHLIST, "content://media/1")

        assertEquals(listOf(Triple("uid", PictureFolder.WISHLIST, "content://media/1")), uploader.calls)
        assertTrue(link.contains("wishlistPhotos%2Fuid"))
    }

    @Test fun `offline nothing is uploaded and the user is told why`() = runTest {
        val uploader = Uploader()
        val result = runCatching {
            PictureController(Auth(user), uploader, FakeConnectivityRepository(initiallyOnline = false))
                .upload(PictureFolder.PRODUCT, "content://media/1")
        }
        assertTrue(result.exceptionOrNull() is OfflineException)
        assertTrue(uploader.calls.isEmpty())
    }

    @Test fun `signed out nothing is uploaded`() = runTest {
        val uploader = Uploader()
        val result = runCatching { PictureController(Auth(null), uploader).upload(PictureFolder.PRODUCT, "content://media/1") }
        assertTrue(result.exceptionOrNull() is ValidationException)
        assertTrue(uploader.calls.isEmpty())
    }
}
