package com.example.whynotkotlin.features.speech.application

import com.example.whynotkotlin.features.speech.domain.SpeechRecognitionRepository
import com.example.whynotkotlin.features.speech.domain.SpeechPermissionDeniedException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

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

    @Test fun `one request exposes listening then transcription`() = runTest(dispatcher) {
        val repository = FakeSpeech()
        val vm = SpeechViewModel(repository)
        assertEquals(SpeechUiState.Idle, vm.state.value)
        vm.startListening("es-CO")
        vm.startListening("en-US")
        runCurrent()
        assertEquals(SpeechUiState.Listening, vm.state.value)
        assertEquals(1, repository.calls)
        assertEquals("es-CO", repository.language)
        repository.result.complete("Zapatos")
        advanceUntilIdle()
        assertEquals(SpeechUiState.Result("Zapatos"), vm.state.value)
    }

    @Test fun `cancellation releases request and resets state`() = runTest(dispatcher) {
        val repository = FakeSpeech()
        val vm = SpeechViewModel(repository)
        vm.startListening()
        runCurrent()
        vm.cancelListening()
        advanceUntilIdle()
        assertTrue(repository.cancelled)
        assertEquals(SpeechUiState.Idle, vm.state.value)
    }

    @Test fun `missing service does not start recognition`() = runTest(dispatcher) {
        val repository = FakeSpeech().apply { available = false }
        val vm = SpeechViewModel(repository)
        vm.startListening()
        advanceUntilIdle()
        assertTrue(vm.state.value is SpeechUiState.Error)
        assertEquals(0, repository.calls)
    }

    @Test fun `permission failure is actionable and can be reset`() = runTest(dispatcher) {
        val repository = FakeSpeech()
        val vm = SpeechViewModel(repository)
        vm.startListening()
        repository.result.completeExceptionally(SpeechPermissionDeniedException())
        advanceUntilIdle()
        assertEquals(SpeechUiState.Error(SpeechPermissionDeniedException().message!!), vm.state.value)
        vm.reset()
        assertEquals(SpeechUiState.Idle, vm.state.value)
    }
}
