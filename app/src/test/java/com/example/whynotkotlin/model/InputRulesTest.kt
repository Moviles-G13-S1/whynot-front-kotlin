package com.example.whynotkotlin.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InputRulesTest {

    @Test fun `names accept letters, accents, numbers, spaces and hyphens`() {
        assertNull(InputRules.name("María José"))
        assertNull(InputRules.name("Demo User 24"))
        assertNull(InputRules.name("Ana-Lucía O'Neil".take(InputRules.NAME_MAX_LENGTH)))
    }

    @Test fun `names reject emojis, digits, symbols and long text`() {
        assertEquals("Enter your name", InputRules.name("   "))
        assertEquals("Your name cannot contain emojis", InputRules.name("Ana 😀"))
        assertEquals("Your name needs at least one letter", InputRules.name("12345"))
        assertEquals("Use only letters, numbers, spaces, hyphens or apostrophes", InputRules.name("<script>"))
        assertEquals(
            "Your name can have at most ${InputRules.NAME_MAX_LENGTH} characters",
            InputRules.name("a".repeat(InputRules.NAME_MAX_LENGTH + 1))
        )
    }

    @Test fun `emails need a valid address without emojis`() {
        assertNull(InputRules.email("  name@example.com "))
        assertEquals("Enter your email", InputRules.email(""))
        assertEquals("Enter a valid email, such as name@example.com", InputRules.email("name@example"))
        assertEquals(
            "Enter a valid email, such as name@example.com",
            InputRules.email("na😀me@example.com")
        )
    }

    @Test fun `ages must be whole numbers from 13 to 120`() {
        assertNull(InputRules.age("13"))
        assertNull(InputRules.age(" 120 "))
        assertEquals("Enter your age", InputRules.age(""))
        assertEquals("Enter your age in whole numbers", InputRules.age("2a"))
        assertEquals("Enter your age in whole numbers", InputRules.age("-5"))
        assertEquals("Age must be between 13 and 120", InputRules.age("12"))
        assertEquals("Age must be between 13 and 120", InputRules.age("121"))
        assertEquals("Age must be between 13 and 120", InputRules.age("99999999999"))
    }

    @Test fun `product text rejects emojis, angle brackets and long text`() {
        assertNull(InputRules.productName("Sony WH-1000XM5 (black)"))
        assertEquals("Enter a product name", InputRules.productName(" "))
        assertEquals("The product name cannot contain emojis", InputRules.productName("Cap ⭐"))
        assertEquals("The brand cannot contain < or >", InputRules.brand("<b>Nike</b>"))
        assertEquals(
            "The brand can have at most ${InputRules.BRAND_MAX_LENGTH} characters",
            InputRules.brand("b".repeat(InputRules.BRAND_MAX_LENGTH + 1))
        )
    }

    @Test fun `links are optional but must be full web addresses`() {
        assertNull(InputRules.optionalLink("", "Store link"))
        assertNull(InputRules.optionalLink("https://www.store.com/item?id=1", "Store link"))
        assertEquals(
            "The store link must start with http:// or https://",
            InputRules.optionalLink("www.store.com", "Store link")
        )
        assertEquals(
            "The store link is not a valid web address",
            InputRules.optionalLink("https://my store.com", "Store link")
        )
        assertEquals(
            "The picture link is not a valid web address",
            InputRules.optionalLink("https://localhost", "Picture link")
        )
    }

    @Test fun `emoji detection covers pictographs, symbols and keycaps`() {
        assertTrue(InputRules.containsEmoji("😀"))
        assertTrue(InputRules.containsEmoji("❤️"))
        assertTrue(InputRules.containsEmoji("1️⃣"))
        assertFalse(InputRules.containsEmoji("Ñandú 2026 -_.!"))
    }
}
