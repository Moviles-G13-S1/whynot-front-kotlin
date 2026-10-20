package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.SpeechUnavailableException
import com.example.whynotkotlin.model.repositories.SpeechRecognitionRepository
import java.util.Locale

class SpeechController(
    private val speechRecognitionRepository: SpeechRecognitionRepository
) {

    /**
     * Listens once and returns what the user said.
     *
     * Fails with one of the SpeechRecognitionException types. Cancelling the
     * caller stops the microphone.
     */
    suspend fun listen(languageTag: String = Locale.getDefault().toLanguageTag()): String {
        if (!speechRecognitionRepository.isAvailable()) throw SpeechUnavailableException()
        return speechRecognitionRepository.recognize(languageTag)
    }
}
