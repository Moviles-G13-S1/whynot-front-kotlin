package com.example.whynotkotlin.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordPolicyTest {

    @Test fun `a password meeting every rule is valid`() {
        assertTrue(PasswordPolicy.isValid("Wishlist2026"))
        assertNull(PasswordPolicy.firstError("Wishlist2026"))
    }

    @Test fun `reports the first missing requirement in order`() {
        assertEquals("The password needs at least 8 characters", PasswordPolicy.firstError("Ab1"))
        assertEquals("The password needs at least one uppercase letter", PasswordPolicy.firstError("wishlist2026"))
        assertEquals("The password needs at least one lowercase letter", PasswordPolicy.firstError("WISHLIST2026"))
        assertEquals("The password needs at least one number", PasswordPolicy.firstError("Wishlists"))
        assertEquals("The password cannot contain spaces", PasswordPolicy.firstError("Wish list 2026"))
    }

    @Test fun `an empty password is not valid`() {
        assertFalse(PasswordPolicy.isValid(""))
    }

    @Test fun `a password with an emoji is not valid`() {
        assertEquals("The password cannot contain emojis", PasswordPolicy.firstError("Abcdefg1😀"))
    }

    @Test fun `a password cannot include the name or the email`() {
        val error = "The password cannot include your name or email"

        assertEquals(error, PasswordPolicy.firstError("Valentina2026", name = "Valentina Ruiz"))
        assertEquals(error, PasswordPolicy.firstError("Lopez2026x", email = "maria.lopez@mail.com"))
        assertNull(PasswordPolicy.firstError("Wishlist2026", email = "maria.lopez@mail.com", name = "Ana Ruiz"))
    }

    @Test fun `the personal rule is listed only once the name or email is known`() {
        assertEquals(PasswordPolicy.rules.size, PasswordPolicy.rulesFor().size)
        assertEquals(PasswordPolicy.rules.size + 1, PasswordPolicy.rulesFor(email = "maria@mail.com").size)
    }
}
