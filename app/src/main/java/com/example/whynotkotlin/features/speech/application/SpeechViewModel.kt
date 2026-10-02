package com.example.whynotkotlin.features.speech.application

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.whynotkotlin.features.speech.domain.SpeechRecognitionRepository
import com.example.whynotkotlin.features.speech.domain.SpeechUnavailableException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

sealed interface SpeechUiState {
    data object Idle : SpeechUiState
    data object Listening : SpeechUiState
    data class Result(val text: String) : SpeechUiState
    data class Error(val message: String) : SpeechUiState
}

class SpeechViewModel(
    private val speechRecognitionRepository: SpeechRecognitionRepository
) : ViewModel() {
    private val _state = MutableStateFlow<SpeechUiState>(SpeechUiState.Idle)
    val state = _state.asStateFlow()
    private var recognitionJob: Job? = null

    fun startListening(languageTag: String = Locale.getDefault().toLanguageTag()) {
        if (_state.value == SpeechUiState.Listening) return
        _state.value = SpeechUiState.Listening
        recognitionJob = viewModelScope.launch {
            try {
                if (!speechRecognitionRepository.isAvailable()) throw SpeechUnavailableException()
                _state.value = SpeechUiState.Result(speechRecognitionRepository.recognize(languageTag))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = SpeechUiState.Error(error.message ?: "Voice input failed. Try again.")
            }
        }
    }

    fun cancelListening() {
        recognitionJob?.cancel()
        recognitionJob = null
        _state.value = SpeechUiState.Idle
    }

    fun reset() = cancelListening()
}
