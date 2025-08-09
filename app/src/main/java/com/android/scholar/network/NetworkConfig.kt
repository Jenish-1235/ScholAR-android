package com.android.scholar.network

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
        // Transform /tts/... to /static/tts/... if needed
        val correctedUrl = if (ttsUrl.startsWith("/tts/")) {
            ttsUrl.replace("/tts/", "/static/tts/")
        } else if (!ttsUrl.startsWith("/static/")) {
            // If it doesn't start with /static/ but looks like a TTS file, prepend /static
            if (ttsUrl.contains("tts_") && ttsUrl.endsWith(".mp3")) {
                "/static$ttsUrl"
            } else {
                ttsUrl
            }
        } else {
            ttsUrl
        }
        return "${getHttpBaseUrl(serverHost, serverPort)}$correctedUrl"
    }
}
