package com.example.whynotkotlin.features.nearby.domain

data class NearbyStore(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val categoryIds: List<String>,
    val websiteUrl: String,
    val imageUrl: String,
    val distanceKm: Double
)