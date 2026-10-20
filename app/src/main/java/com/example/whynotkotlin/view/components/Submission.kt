package com.example.whynotkotlin.view.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.whynotkotlin.model.readableMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The state of one screen action that talks to a controller: whether it is
 * running and the message of its last failure.
 *
 * A second [run] while one is in flight is ignored, so a double tap cannot
 * send the same write twice.
 */
@Stable
class Submission internal constructor(
    private val scope: CoroutineScope
) {
    var running by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun run(onSuccess: () -> Unit = {}, block: suspend () -> Unit) {
        if (running) return
        running = true
        errorMessage = null

        scope.launch {
            try {
                block()
            } catch (cancelled: CancellationException) {
                running = false
                throw cancelled
            } catch (error: Exception) {
                running = false
                errorMessage = error.readableMessage()
                return@launch
            }

            running = false
            onSuccess()
        }
    }
}

/** A [Submission] tied to the calling screen; leaving the screen cancels it. */
@Composable
fun rememberSubmission(): Submission {
    val scope = rememberCoroutineScope()
    return remember(scope) { Submission(scope) }
}
