package com.example.whynotkotlin.model.repositories

interface UxEventRepository {
    suspend fun record(ownerId: String, eventName: String, source: String)
}
