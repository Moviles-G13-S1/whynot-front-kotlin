package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.SpeechPermissionDeniedException
import com.example.whynotkotlin.model.SpeechUnavailableException
import com.example.whynotkotlin.model.repositories.SpeechRecognitionRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechControllerTest {

    private class FakeSpeech : SpeechRecognitionRepository {
        var available = true
        var calls = 0
        var cancelled = false
        var language = ""
        val result = CompletableDeferred<String>()
        override fun isAvailable() = available
        override suspend fun recognize(languageTag: String): String {
            calls++
            language = languageTag
            try { return result.await() }
            finally { cancelled = !result.isCompleted }
        }
    }

    @Test fun `listen returns the transcription in the requested language`() = runTest {
        val repository = FakeSpeech()
        val controller = SpeechController(repository)
        val heard = async { controller.listen("es-CO") }
        runCurrent()
        assertEquals(1, repository.calls)
        assertEquals("es-CO", repository.language)
        repository.result.complete("Zapatos")
        assertEquals("Zapatos", heard.await())
    }

    @Test fun `cancellation releases the microphone request`() = runTest {
        val repository = FakeSpeech()
        val controller = SpeechController(repository)
        val heard = async { controller.listen() }
        runCurrent()
        heard.cancel()
        runCurrent()
        assertTrue(repository.cancelled)
    }

    @Test fun `missing service does not start recognition`() = runTest {
        val repository = FakeSpeech().apply { available = false }
        val controller = SpeechController(repository)
        try {
            controller.listen()
            fail("Expected SpeechUnavailableException")
        } catch (expected: SpeechUnavailableException) {
            assertEquals(0, repository.calls)
        }
    }

    @Test fun `permission failure reaches the caller with its message`() = runTest {
        val repository = FakeSpeech()
        val controller = SpeechController(repository)
        repository.result.completeExceptionally(SpeechPermissionDeniedException())
        try {
            controller.listen()
            fail("Expected SpeechPermissionDeniedException")
        } catch (expected: SpeechPermissionDeniedException) {
            assertEquals(SpeechPermissionDeniedException().message, expected.message)
        }
    }
}
