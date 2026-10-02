package com.example.whynotkotlin.features.speech.domain

/**
 * The failures the ViewModel can tell apart.
 *
 * They share a base type so a screen can catch every speech problem at once,
 * while still reacting differently to the permission case: that one needs the
 * screen to ask for RECORD_AUDIO and try again, which the repository cannot do
 * because requesting a runtime permission needs an Activity.
 */
sealed class SpeechRecognitionException(message: String) : Exception(message)

/** The user has not granted the microphone permission yet. */
class SpeechPermissionDeniedException :
    SpeechRecognitionException("Microphone permission is required to use voice input.")

/** The phone has no speech recognition service. */
class SpeechUnavailableException :
    SpeechRecognitionException("Voice input is not available on this device.")

/** The user said nothing, or nothing that could be understood. */
class NoSpeechMatchException :
    SpeechRecognitionException("Didn't catch that. Try again.")

/** The recognition service needs a connection and could not reach it. */
class SpeechNetworkException :
    SpeechRecognitionException("Voice input needs an internet connection.")

/** The recognition service is already serving another request. */
class SpeechBusyException :
    SpeechRecognitionException("Voice input is busy. Try again in a moment.")

/** Any other failure reported by the recognition service. */
class SpeechServiceException(errorCode: Int) :
    SpeechRecognitionException("Voice input failed (code $errorCode).")

