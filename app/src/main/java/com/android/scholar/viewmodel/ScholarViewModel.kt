package com.android.scholar.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.scholar.model.AudioState
import com.android.scholar.model.ConnectionState
import com.android.scholar.model.LearningResponse
import com.android.scholar.service.AudioPlayerManager
import com.android.scholar.service.WebSocketService
import com.android.scholar.utils.AudioDebugUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScholarViewModel(application: Application) : AndroidViewModel(application) {
    
    private val webSocketService = WebSocketService()
    private val audioPlayerManager = AudioPlayerManager(application)
    
    // WebSocket connection state
    val connectionState: StateFlow<ConnectionState> = webSocketService.connectionState
    
    // Learning response state
    val learningResponse: StateFlow<LearningResponse?> = webSocketService.learningResponse
    
    // Audio player state
    val audioState: StateFlow<AudioState> = audioPlayerManager.audioState
    
    // UI state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _expandedQuestionIndex = MutableStateFlow<Int?>(null)
    val expandedQuestionIndex: StateFlow<Int?> = _expandedQuestionIndex.asStateFlow()
    
    init {
        // Observe learning responses and handle them
        viewModelScope.launch {
            learningResponse.collect { response ->
                if (response != null) {
                    Log.d("ScholarViewModel", "=== NEW LEARNING RESPONSE RECEIVED ===")
                    _isLoading.value = false
                    // Log the response for debugging
                    AudioDebugUtils.logResponseReceived(response)
                    
                    // Auto-play audio if TTS URL is available
                    val ttsUrl = response.ttsUrl
                    if (!ttsUrl.isNullOrEmpty()) {
                        // Small delay to ensure UI is updated, then auto-play audio
                        kotlinx.coroutines.delay(500) // 500ms delay
                        Log.d("ScholarViewModel", "Starting auto-play of TTS audio...")
                        playTtsAudio(ttsUrl)
                    } else {
                        Log.w("ScholarViewModel", "No TTS URL available in response")
                    }
                }
            }
        }
        
        // Observe audio errors for automatic retry
        viewModelScope.launch {
            audioState.collect { state ->
                if (state.error != null && state.currentUrl != null) {
                    Log.d("ScholarViewModel", "Audio error detected: ${state.error}")
                    // Auto-retry after 2 seconds
                    kotlinx.coroutines.delay(2000)
                    retryAudioPlayback()
                }
            }
        }
    }
    
    // WebSocket operations
    fun connectToServer(host: String = "10.10.30.172", port: Int = 8000) {
        _isLoading.value = true
        webSocketService.connect(host, port)
    }
    
    fun disconnectFromServer() {
        webSocketService.disconnect()
        audioPlayerManager.stopAudio()
        _isLoading.value = false
    }
    
    fun clearConnectionError() {
        webSocketService.clearError()
    }
    
    fun clearLearningResponse() {
        webSocketService.clearLearningResponse()
        _expandedQuestionIndex.value = null
    }
    
    // Audio operations
    fun playTtsAudio(ttsUrl: String) {
        Log.d("ScholarViewModel", "=== ATTEMPTING TTS PLAYBACK ===")
        Log.d("ScholarViewModel", "Received TTS URL: $ttsUrl")
        
        if (!AudioDebugUtils.validateTtsUrl(ttsUrl)) {
            Log.e("ScholarViewModel", "Invalid TTS URL: $ttsUrl")
            return
        }
        
        val completeUrl = webSocketService.getCompleteTtsUrl(ttsUrl)
        Log.d("ScholarViewModel", "Complete audio URL: $completeUrl")
        
        AudioDebugUtils.logAudioPlaybackAttempt(ttsUrl, completeUrl)
        
        // Start playback immediately
        Log.d("ScholarViewModel", "Triggering audio playback...")
        audioPlayerManager.playTtsAudio(completeUrl)
        
        // Log success after a short delay
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            val currentAudioState = audioState.value
            Log.d("ScholarViewModel", "Audio state after 1s: isPlaying=${currentAudioState.isPlaying}, isLoading=${currentAudioState.isLoading}, error=${currentAudioState.error}")
        }
    }
    
    fun pauseAudio() {
        audioPlayerManager.pauseAudio()
    }
    
    fun resumeAudio() {
        audioPlayerManager.resumeAudio()
    }
    
    fun stopAudio() {
        audioPlayerManager.stopAudio()
    }
    
    fun clearAudioError() {
        audioPlayerManager.clearError()
    }
    
    private fun retryAudioPlayback() {
        val currentResponse = learningResponse.value
        val ttsUrl = currentResponse?.ttsUrl
        if (currentResponse != null && !ttsUrl.isNullOrEmpty()) {
            Log.d("ScholarViewModel", "Retrying audio playback for: $ttsUrl")
            clearAudioError()
            playTtsAudio(ttsUrl)
        } else {
            Log.w("ScholarViewModel", "Cannot retry: no valid TTS URL available")
        }
    }
    
    // Method to manually trigger audio playback for debugging
    fun forcePlayAudio() {
        val currentResponse = learningResponse.value
        val ttsUrl = currentResponse?.ttsUrl
        if (currentResponse != null && !ttsUrl.isNullOrEmpty()) {
            Log.d("ScholarViewModel", "Force playing audio for: $ttsUrl")
            stopAudio() // Stop any current playback
            clearAudioError()
            playTtsAudio(ttsUrl)
        } else {
            Log.w("ScholarViewModel", "Cannot force play: no valid TTS URL available")
        }
    }
    
    // UI operations
    fun toggleQuestionExpansion(questionIndex: Int) {
        _expandedQuestionIndex.value = if (_expandedQuestionIndex.value == questionIndex) {
            null
        } else {
            questionIndex
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        webSocketService.disconnect()
        audioPlayerManager.release()
    }
}