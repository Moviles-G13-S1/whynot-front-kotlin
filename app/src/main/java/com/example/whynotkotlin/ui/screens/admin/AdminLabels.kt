package com.example.whynotkotlin.ui.screens.admin

import com.example.whynotkotlin.features.profile.domain.CanonicalCities
import com.example.whynotkotlin.features.wishlists.domain.CanonicalCategories

/**
 * Turns the ids stored on documents into something readable.
 *
 * The admin screens work with raw `categoryId` and `cityId` values because that
 * is what the product and user documents carry. The seeded catalogues already
 * hold the display names, so they are reused here instead of querying again.
 * An id that is not in a catalogue is shown as-is rather than hidden.
 */
internal fun categoryLabel(categoryId: String?): String {
    if (categoryId.isNullOrBlank()) return "—"

    return CanonicalCategories.all
        .firstOrNull { it.id == categoryId }
        ?.name
        ?: categoryId
}

internal fun cityLabel(cityId: String?): String = when {
    cityId.isNullOrBlank() -> "Unknown"
    cityId == "unknown" -> "No city set"

    else -> CanonicalCities.all
        .firstOrNull { it.id == cityId }
        ?.name
        ?: cityId
}
