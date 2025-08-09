package com.android.scholar.service

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.scholar.model.ConnectionState
import com.android.scholar.model.LearningResponse
import com.android.scholar.model.BackendResponse
import com.android.scholar.model.toLearningResponse
import com.android.scholar.network.NetworkConfig
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WebSocketService {
    
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(NetworkConfig.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
        .readTimeout(NetworkConfig.READ_TIMEOUT, TimeUnit.MILLISECONDS)
        .build()
    
    private val gson = Gson()
    private val handler = Handler(Looper.getMainLooper())
    
    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _learningResponse = MutableStateFlow<LearningResponse?>(null)
    val learningResponse: StateFlow<LearningResponse?> = _learningResponse.asStateFlow()
    
    private var serverHost = NetworkConfig.DEFAULT_SERVER_HOST
    private var serverPort = NetworkConfig.DEFAULT_SERVER_PORT
    
    fun connect(host: String = NetworkConfig.DEFAULT_SERVER_HOST, port: Int = NetworkConfig.DEFAULT_SERVER_PORT) {
        serverHost = host
        serverPort = port
        
        if (_connectionState.value.isConnected || _connectionState.value.isConnecting) {
            Log.d("WebSocket", "Already connected or connecting")
            return
        }
        
        _connectionState.value = _connectionState.value.copy(
            isConnecting = true,
            error = null
        )
        
        val url = NetworkConfig.getWebSocketUrl(host, port)
        val request = Request.Builder()
            .url(url)
            .build()
        
        Log.d("WebSocket", "Attempting to connect to: $url")
        webSocket = client.newWebSocket(request, webSocketListener)
    }
    
    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d("WebSocket", "Connected to ScholAR Backend")
            _connectionState.value = _connectionState.value.copy(
                isConnected = true,
                isConnecting = false,
                error = null,
                reconnectAttempts = 0
            )
        }
        
        override fun onMessage(webSocket: WebSocket, text: String) {
            Log.d("WebSocket", "Received message: $text")
            handleMessage(text)
        }
        
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e("WebSocket", "Connection failed", t)
            _connectionState.value = _connectionState.value.copy(
                isConnected = false,
                isConnecting = false,
                error = "Connection failed: ${t.message}"
            )
            handleConnectionFailure()
        }
        
        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("WebSocket", "Connection closing: $code - $reason")
            _connectionState.value = _connectionState.value.copy(
                isConnected = false,
                isConnecting = false
            )
        }
        
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d("WebSocket", "Connection closed: $code - $reason")
            _connectionState.value = _connectionState.value.copy(
                isConnected = false,
                isConnecting = false
            )
        }
    }
    
    private fun handleMessage(text: String) {
        try {
            // First try to parse as BackendResponse
            val backendResponse = gson.fromJson(text, com.android.scholar.model.BackendResponse::class.java)
            if (validateBackendResponse(backendResponse)) {
                val learningResponse = backendResponse.toLearningResponse()
                _learningResponse.value = learningResponse
                Log.d("WebSocket", "Valid backend response received and converted")
            } else {
                // Fallback: try to parse as direct LearningResponse
                val directResponse = gson.fromJson(text, LearningResponse::class.java)
                if (validateLearningResponse(directResponse)) {
                    _learningResponse.value = directResponse
                    Log.d("WebSocket", "Valid direct learning response received")
                } else {
                    Log.w("WebSocket", "Invalid response structure")
                    _connectionState.value = _connectionState.value.copy(
                        error = "Received invalid response format"
                    )
                }
            }
        } catch (e: JsonSyntaxException) {
            Log.e("WebSocket", "JSON parsing error", e)
            _connectionState.value = _connectionState.value.copy(
                error = "Failed to parse response: ${e.message}"
            )
        }
    }
    
    private fun validateBackendResponse(response: com.android.scholar.model.BackendResponse?): Boolean {
        return response != null &&
                response.type == "response" &&
                response.text.explanation.isNotEmpty() &&
                response.ttsUrl.isNotEmpty()
    }
    
    private fun validateLearningResponse(response: LearningResponse?): Boolean {
        return response != null &&
                response.type == "learning_response" &&
                response.explanation.isNotEmpty() &&
                response.ttsUrl.isNotEmpty()
    }
    
    private fun handleConnectionFailure() {
        val currentState = _connectionState.value
        if (currentState.reconnectAttempts < NetworkConfig.MAX_RECONNECT_ATTEMPTS) {
            handler.postDelayed({
                Log.d("WebSocket", "Attempting reconnection ${currentState.reconnectAttempts + 1}")
                _connectionState.value = currentState.copy(
                    reconnectAttempts = currentState.reconnectAttempts + 1
                )
                connect(serverHost, serverPort)
            }, NetworkConfig.RECONNECT_DELAY)
        } else {
            Log.e("WebSocket", "Max reconnection attempts reached")
            _connectionState.value = currentState.copy(
                error = "Failed to connect after ${NetworkConfig.MAX_RECONNECT_ATTEMPTS} attempts"
            )
        }
    }
    
    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = ConnectionState()
        _learningResponse.value = null
    }
    
    fun clearError() {
        _connectionState.value = _connectionState.value.copy(error = null)
    }
    
    fun clearLearningResponse() {
        _learningResponse.value = null
    }
    
    fun getCompleteTtsUrl(ttsUrl: String): String {
        return NetworkConfig.getCompleteTtsUrl(ttsUrl, serverHost, serverPort)
    }
}