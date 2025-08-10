package com.android.scholar.network

import android.util.Log

object NetworkConfig {
    const val DEFAULT_SERVER_HOST = "10.10.30.172"
    const val DEFAULT_SERVER_PORT = 8000
    const val WEBSOCKET_PATH = "/ws/android"
    const val CONNECTION_TIMEOUT = 10000L // 10 seconds
    const val READ_TIMEOUT = 30000L // 30 seconds
    const val MAX_RECONNECT_ATTEMPTS = 5
    const val RECONNECT_DELAY = 5000L // 5 seconds
    
    fun getWebSocketUrl(serverHost: String = DEFAULT_SERVER_HOST, serverPort: Int = DEFAULT_SERVER_PORT): String {
        return "ws://$serverHost:$serverPort$WEBSOCKET_PATH"
    }
    
    fun getHttpBaseUrl(serverHost: String = DEFAULT_SERVER_HOST, serverPort: Int = DEFAULT_SERVER_PORT): String {
        return "http://$serverHost:$serverPort"
    }
    
    fun getCompleteTtsUrl(ttsUrl: String, serverHost: String = DEFAULT_SERVER_HOST, serverPort: Int = DEFAULT_SERVER_PORT): String {
        Log.d("NetworkConfig", "Input TTS URL: $ttsUrl")
        
        // The TTS URL should already be in the correct format: /static/tts/tts_xxxxx.mp3
        // But let's handle different cases just to be safe
        val correctedUrl = when {
            ttsUrl.startsWith("/static/tts/") -> {
                // Already correct format
                Log.d("NetworkConfig", "URL already in correct format")
                ttsUrl
            }
            ttsUrl.startsWith("/tts/") -> {
                // Transform /tts/... to /static/tts/...
                val newUrl = ttsUrl.replace("/tts/", "/static/tts/")
                Log.d("NetworkConfig", "Transformed /tts/ to /static/tts/: $newUrl")
                newUrl
            }
            ttsUrl.startsWith("tts_") && ttsUrl.endsWith(".mp3") -> {
                // Add full path prefix
                val newUrl = "/static/tts/$ttsUrl"
                Log.d("NetworkConfig", "Added full path prefix: $newUrl")
                newUrl
            }
            else -> {
                Log.w("NetworkConfig", "Unknown TTS URL format, using as-is: $ttsUrl")
                ttsUrl
            }
        }
        
        val completeUrl = "${getHttpBaseUrl(serverHost, serverPort)}$correctedUrl"
        Log.d("NetworkConfig", "Complete URL: $completeUrl")
        return completeUrl
    }
}
