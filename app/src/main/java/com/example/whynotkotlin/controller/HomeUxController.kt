package com.example.whynotkotlin.controller

import android.util.Log
import com.example.whynotkotlin.model.repositories.UxEventRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class HomeUxController(private val repository: UxEventRepository, private val scope: CoroutineScope) {
    fun track(uid: String, event: String, source: String) {
        scope.launch {
            try {
                repository.record(uid, event, source)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Log.w("WhyNotUX", "UX event was not confirmed by Firestore: $event", error)
            }
        }
    }
}
