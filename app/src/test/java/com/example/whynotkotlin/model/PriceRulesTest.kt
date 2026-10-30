package com.example.whynotkotlin.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceRulesTest {

    @Test fun `whole amounts and thousands separators`() {
        assertEquals(1500.0, PriceRules.parse("1500")!!, 0.0)
        assertEquals(1000.0, PriceRules.parse("1.000")!!, 0.0)
        assertEquals(12500.0, PriceRules.parse("12,500")!!, 0.0)
        assertEquals(1_000_000.0, PriceRules.parse("1.000.000")!!, 0.0)
        assertEquals(250_000.0, PriceRules.parse(" $250.000 ")!!, 0.0)
    }

    @Test fun `decimals with either mark`() {
        assertEquals(12.5, PriceRules.parse("12,5")!!, 0.0)
        assertEquals(19.99, PriceRules.parse("19.99")!!, 0.0)
        assertEquals(1000.5, PriceRules.parse("1,000.50")!!, 0.0)
        assertEquals(1500.5, PriceRules.parse("1.500,50")!!, 0.0)
        assertEquals(0.5, PriceRules.parse("0,50")!!, 0.0)
    }

    @Test fun `text that is not a price is rejected`() {
        listOf("NaN", "Infinity", "1e9", "abc", "12,", "1,5.3", "1.00.0").forEach { text ->
            assertEquals(text, "Enter a valid price, such as 1500 or 1.500,50", PriceRules.error(text))
            assertNull(text, PriceRules.parse(text))
        }
    }

    @Test fun `missing, zero, negative, too precise and too large prices`() {
        assertEquals("Enter a price", PriceRules.error("  "))
        assertEquals("The price must be greater than 0", PriceRules.error("0"))
        assertEquals("The price must be greater than 0", PriceRules.error("0,00"))
        assertEquals("The price cannot be negative", PriceRules.error("-5"))
        assertEquals("Use at most 2 decimals", PriceRules.error("1.2345"))
        assertEquals("The price can be at most 100.000.000", PriceRules.error("100000001"))
        assertNull(PriceRules.error("100.000.000"))
    }

    @Test fun `only a negative amount counts as negative`() {
        assertTrue(PriceRules.isNegative("-5"))
        assertFalse(PriceRules.isNegative("abc"))
        assertFalse(PriceRules.isNegative("5"))
    }
}
