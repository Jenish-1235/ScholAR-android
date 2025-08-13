package com.android.scholar.model

import com.google.gson.annotations.SerializedName

data class BackendResponse(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("text")
    val text: LearningResponseText,
    
    @SerializedName("tts_url")
    val ttsUrl: String
)

data class LearningResponseText(
    @SerializedName("explanation")
    val explanation: String,
    
    @SerializedName("practice_questions")
    val practiceQuestions: List<String>,
    
    @SerializedName("additional_urls")
    val additionalUrls: List<String>
)

data class LearningResponse(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("explanation")
    val explanation: String?,
    
    @SerializedName("practice_questions")
    val practiceQuestions: List<String>?,
    
    @SerializedName("additional_urls")
    val additionalUrls: List<String>?,
    
    @SerializedName("tts_url")
    val ttsUrl: String?
)

// Extension function to convert BackendResponse to LearningResponse
fun BackendResponse.toLearningResponse(): LearningResponse {
    return LearningResponse(
        type = "learning_response",
        explanation = text.explanation,
        practiceQuestions = text.practiceQuestions,
        additionalUrls = text.additionalUrls,
        ttsUrl = ttsUrl
    )
}

data class ConnectionState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val error: String? = null,
    val reconnectAttempts: Int = 0
)

data class AudioState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUrl: String? = null
)
