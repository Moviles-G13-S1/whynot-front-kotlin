package com.example.whynotkotlin.features.profile.domain

/** A `cities/{cityId}` document. */
data class City(
    val id: String,
    val name: String
)

/**
 * The seeded `cities` documents, mirrored from the backend seed script.
 *
 * This list exists for the same reason as `CanonicalCategories`: the Security
 * Rules allow reading `cities` only when `request.auth != null`, but a visitor
 * filling the registration form is not signed in yet and the profile it creates
 * now requires a `cityId` that references an existing city.
 *
 * If the backend ever opens `cities` to visitors, this file should be deleted
 * and the form should read the live collection instead. If the backend reseeds
 * different ids, profile creation will fail loudly because the rules check that
 * the reference exists.
 *
 * `other` is kept last so it reads as a fallback rather than a city.
 */
object CanonicalCities {

    val all = listOf(
        City("armenia", "Armenia"),
        City("barranquilla", "Barranquilla"),
        City("bogota", "Bogotá"),
        City("bucaramanga", "Bucaramanga"),
        City("cali", "Cali"),
        City("cartagena", "Cartagena"),
        City("cucuta", "Cúcuta"),
        City("florencia", "Florencia"),
        City("ibague", "Ibagué"),
        City("manizales", "Manizales"),
        City("medellin", "Medellín"),
        City("monteria", "Montería"),
        City("neiva", "Neiva"),
        City("pasto", "Pasto"),
        City("pereira", "Pereira"),
        City("popayan", "Popayán"),
        City("quibdo", "Quibdó"),
        City("riohacha", "Riohacha"),
        City("san-andres", "San Andrés"),
        City("santa-marta", "Santa Marta"),
        City("sincelejo", "Sincelejo"),
        City("tunja", "Tunja"),
        City("valledupar", "Valledupar"),
        City("villavicencio", "Villavicencio"),
        City("other", "Other")
    )
}
