package com.example.whynotkotlin.features.speech.domain

import java.util.Locale

/**
 * Turns the user's voice into text through the phone's microphone.
 *
 * This is the sensor feature: the only input is what the microphone captures.
 * Nothing is recorded or stored; the audio goes to the device's recognition
 * service and only the resulting text comes back.
 *
 * The ViewModel owns the listening / error / result states. This contract only
 * answers "what did the user say?" and fails with one of the exceptions in
 * SpeechErrors.kt when it cannot.
 */
interface SpeechRecognitionRepository {

    /**
     * Whether the phone has a speech recognition service at all.
     *
     * The screen can use it to hide the microphone button on devices that
     * could never answer, instead of showing a button that always fails.
     */
    fun isAvailable(): Boolean

    /**
     * Listens once and returns the best transcription.
     *
     * Suspends until the user stops talking. Cancelling the calling coroutine
     * stops the microphone.
     *
     * @param languageTag BCP 47 tag of the language to recognise. Defaults to
     * the phone's language, since product names are dictated the way the user
     * says them.
     */
    suspend fun recognize(
        languageTag: String = Locale.getDefault().toLanguageTag()
    ): String
}
