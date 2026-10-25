package com.example.whynotkotlin.fakes

import com.example.whynotkotlin.model.repositories.ConnectivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory connectivity: tests flip [online] to simulate airplane mode. */
class FakeConnectivityRepository(initiallyOnline: Boolean = true) : ConnectivityRepository {

    val online = MutableStateFlow(initiallyOnline)

    override val isOnline: StateFlow<Boolean> = online

    fun goOffline() {
        online.value = false
    }

    fun goOnline() {
        online.value = true
    }
}
