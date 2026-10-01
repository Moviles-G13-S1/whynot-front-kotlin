package com.example.whynotkotlin.features.admin.domain

/** Share of one gender among the buyers of a category. */
data class GenderShare(
    val gender: String,
    val userCount: Int,
    val percentage: Double
)

/** Share of one age group among the buyers of a category. */
data class AgeGroupShare(
    val label: String,
    val userCount: Int
)

/** Buyers of a category living in one city. */
data class CityShare(
    val cityId: String,
    val userCount: Int
)

/**
 * The demographic profile of the buyers of a single category.
 *
 * A user is counted once per category, no matter how many products they bought
 * in it.
 */
data class CategoryDemographics(
    val categoryId: String,
    val buyers: Int,
    val medianAge: Int?,
    val ageGroups: List<AgeGroupShare>,
    val genders: List<GenderShare>,
    val topCities: List<CityShare>
)

/**
 * BQ6:
 * What is the demographic profile of the average buyer per category?
 *
 * Built by joining the products that are marked as purchased with the profile
 * of their owner. Reading every profile needs the `admin` custom claim.
 *
 * Buyers whose profile predates the city catalog have no `cityId`; they are
 * grouped under [UNKNOWN_CITY] rather than dropped, so the city shares still
 * add up to the number of buyers.
 */
data class DemographicProfileStats(
    val categories: List<CategoryDemographics> = emptyList(),
    val totalBuyers: Int = 0,
    val profilesWithoutCity: Int = 0
) {
    companion object {
        const val UNKNOWN_CITY = "unknown"

        /** The same age groups the backend uses to score similarity. */
        val AGE_GROUPS = listOf(
            "13–17" to 13..17,
            "18–24" to 18..24,
            "25–34" to 25..34,
            "35–44" to 35..44,
            "45+" to 45..200
        )

        const val TOP_CITIES = 4
    }
}
