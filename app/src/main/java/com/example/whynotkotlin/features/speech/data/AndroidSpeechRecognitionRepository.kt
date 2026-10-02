package com.example.whynotkotlin.features.speech.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import com.example.whynotkotlin.features.speech.domain.NoSpeechMatchException
import com.example.whynotkotlin.features.speech.domain.SpeechBusyException
import com.example.whynotkotlin.features.speech.domain.SpeechNetworkException
import com.example.whynotkotlin.features.speech.domain.SpeechPermissionDeniedException
import com.example.whynotkotlin.features.speech.domain.SpeechRecognitionException
import com.example.whynotkotlin.features.speech.domain.SpeechRecognitionRepository
import com.example.whynotkotlin.features.speech.domain.SpeechServiceException
import com.example.whynotkotlin.features.speech.domain.SpeechUnavailableException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Reads the microphone through the platform [SpeechRecognizer].
 *
 * No extra dependency is needed: the recogniser is part of the Android
 * framework and delegates to whatever recognition service the phone ships
 * with.
 *
 * [SpeechRecognizer] must be created and driven from the main thread, and it
 * holds the microphone until it is destroyed, so every path out of
 * [recognize] — result, error or cancellation — releases it exactly once.
 */
class AndroidSpeechRecognitionRepository(
    context: Context
) : SpeechRecognitionRepository {

    // The application context: this repository lives as long as the process.
    private val appContext = context.applicationContext

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun isAvailable(): Boolean =
        SpeechRecognizer.isRecognitionAvailable(appContext)

    override suspend fun recognize(languageTag: String): String =
        withContext(Dispatchers.Main) {
            if (!hasMicrophonePermission()) {
                throw SpeechPermissionDeniedException()
            }

            if (!isAvailable()) {
                throw SpeechUnavailableException()
            }

            suspendCancellableCoroutine { continuation ->
                val recognizer =
                    SpeechRecognizer.createSpeechRecognizer(appContext)

                // Only touched on the main thread: the listener callbacks run
                // there and the cancellation handler posts to it.
                var released = false

                fun release() {
                    if (released) return
                    released = true
                    recognizer.destroy()
                }

                recognizer.setRecognitionListener(object : RecognitionListener {

                    override fun onResults(results: Bundle?) {
                        val text = results
                            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            ?.firstOrNull()
                            ?.trim()

                        release()

                        if (!continuation.isActive) return

                        if (text.isNullOrEmpty()) {
                            continuation.resumeWithException(NoSpeechMatchException())
                        } else {
                            continuation.resume(text)
                        }
                    }

                    override fun onError(error: Int) {
                        release()

                        // Cancelling makes the service report ERROR_CLIENT;
                        // by then the caller has already moved on.
                        if (continuation.isActive) {
                            continuation.resumeWithException(error.toSpeechException())
                        }
                    }

                    override fun onReadyForSpeech(params: Bundle?) = Unit
                    override fun onBeginningOfSpeech() = Unit
                    override fun onRmsChanged(rmsdB: Float) = Unit
                    override fun onBufferReceived(buffer: ByteArray?) = Unit
                    override fun onEndOfSpeech() = Unit
                    override fun onPartialResults(partialResults: Bundle?) = Unit
                    override fun onEvent(eventType: Int, params: Bundle?) = Unit
                })

                // Leaving the screen cancels the ViewModel's coroutine, which
                // must also switch the microphone off.
                continuation.invokeOnCancellation {
                    mainHandler.post {
                        if (!released) {
                            recognizer.cancel()
                            release()
                        }
                    }
                }

                try {
                    recognizer.startListening(recognitionIntent(languageTag))
                } catch (error: Exception) {
                    release()
                    if (continuation.isActive) continuation.resumeWithException(error)
                }
            }
        }

    private fun recognitionIntent(languageTag: String): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            // A form field takes one value, so alternatives are not useful.
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.packageName)
        }

    private fun hasMicrophonePermission(): Boolean =
        ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
}

private fun Int.toSpeechException(): SpeechRecognitionException = when (this) {
    SpeechRecognizer.ERROR_NO_MATCH,
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> NoSpeechMatchException()

    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> SpeechPermissionDeniedException()

    SpeechRecognizer.ERROR_NETWORK,
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
    SpeechRecognizer.ERROR_SERVER -> SpeechNetworkException()

    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> SpeechBusyException()

    else -> SpeechServiceException(this)
}

