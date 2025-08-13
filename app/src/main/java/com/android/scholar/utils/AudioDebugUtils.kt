package com.android.scholar.utils

import android.util.Log
import com.android.scholar.model.LearningResponse

object AudioDebugUtils {
    
    fun logAudioPlaybackAttempt(ttsUrl: String, completeUrl: String) {
        Log.d("AudioDebug", "Attempting to play TTS audio")
        Log.d("AudioDebug", "Original TTS URL: $ttsUrl")
        Log.d("AudioDebug", "Complete URL: $completeUrl")
    }
    
    fun logAudioPlaybackSuccess(url: String) {
        Log.d("AudioDebug", "Audio playback started successfully: $url")
    }
    
    fun logAudioPlaybackError(url: String, error: String) {
        Log.e("AudioDebug", "Audio playback failed for $url: $error")
    }
    
    fun logResponseReceived(response: LearningResponse) {
        Log.d("AudioDebug", "Learning response received with TTS URL: ${response.ttsUrl}")
        Log.d("AudioDebug", "Explanation length: ${response.explanation?.length ?: 0} characters")
        Log.d("AudioDebug", "Practice questions: ${response.practiceQuestions?.size ?: 0}")
        Log.d("AudioDebug", "Additional URLs: ${response.additionalUrls?.size ?: 0}")
    }
    
    fun validateTtsUrl(ttsUrl: String): Boolean {
        val isValid = ttsUrl.isNotEmpty() && 
                     (ttsUrl.contains("tts_") || ttsUrl.contains(".mp3"))
        
        if (!isValid) {
            Log.w("AudioDebug", "Invalid TTS URL format: $ttsUrl")
        }
        
        return isValid
    }
}
