package com.example.whynotkotlin.model

data class RecommendationSaveResult(
    val saved: Boolean,
    val alreadySaved: Boolean,
    val productId: String? = null
)