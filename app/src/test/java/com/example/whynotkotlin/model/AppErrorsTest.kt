package com.example.whynotkotlin.model

import com.example.whynotkotlin.model.repositories.OfflineException
import org.junit.Assert.assertEquals
import org.junit.Test

class AppErrorsTest {

    @Test fun `messages written for the user are shown as they are`() {
        assertEquals("Enter a brand", ValidationException("Enter a brand").readableMessage())
        assertEquals("You're offline.", OfflineException("You're offline.").readableMessage())
        assertEquals(
            "Could not read your location. Check that location is turned on.",
            LocationUnavailableException().readableMessage()
        )
    }

    @Test fun `technical messages are replaced by a generic one`() {
        assertEquals(
            "Something went wrong. Try again.",
            IllegalStateException("get_recommendation returned no 'product' field").readableMessage()
        )
        assertEquals("Something went wrong. Try again.", RuntimeException().readableMessage())
    }

    @Test fun `a taken email does not say that an account exists`() {
        assertEquals(
            "We could not create an account with that email. If you already have one, log in instead.",
            IllegalStateException("The email address is already in use by another account.").readableMessage()
        )
    }
}
