package com.android.scholar.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.scholar.model.AudioState
import com.android.scholar.model.ConnectionState
import com.android.scholar.model.LearningResponse
import com.android.scholar.service.AudioPlayerManager
import com.android.scholar.service.WebSocketService
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
                    _isLoading.value = false
                }
            }
        }
    }
    
    // WebSocket operations
    fun connectToServer(host: String = "192.168.1.100", port: Int = 8000) {
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
        val completeUrl = webSocketService.getCompleteTtsUrl(ttsUrl)
        audioPlayerManager.playTtsAudio(completeUrl)
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