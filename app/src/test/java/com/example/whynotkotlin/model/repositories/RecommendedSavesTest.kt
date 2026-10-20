package com.example.whynotkotlin.model.repositories

import org.junit.Assert.assertEquals
import org.junit.Test

class RecommendedSavesTest {

    @Test
    fun `counts only saved recommendation events`() {
        val events = listOf(
            mapOf<String, Any>(
                "eventType" to "recommendation_shown",
                "savedAt" to "2026-10-20",
                "savedProductId" to "product-now-deleted"
            ),
            mapOf<String, Any>("eventType" to "recommendation_shown"),
            mapOf<String, Any>(
                "eventType" to "recommendation_shown",
                "savedAt" to "2026-10-20",
                "savedProductId" to ""
            ),
            mapOf<String, Any>(
                "eventType" to "manual_save",
                "savedAt" to "2026-10-20",
                "savedProductId" to "other"
            )
        )

        assertEquals(1L, countSavedRecommendationEvents(events))
    }
}
