package com.android.scholar.service

import android.util.Log
import com.android.scholar.viewmodel.ScholarViewModel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketService (
    private val viewModel: ScholarViewModel
) {
    private lateinit var webSocket: WebSocket

    fun connect() {

        val request = Request.Builder()
            .url("wss://your-websocket-url") // Replace with your WebSocket URL
            .build()
        val client = OkHttpClient()
        webSocket = client.newWebSocket(request, object : WebSocketListener(){
            override fun onMessage(webSocket: WebSocket, text: String) {
                viewModel.updateAnswer(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("Websocket", "Connection failed", t)
            }
        })

        fun close(){
            webSocket.close(1000, "Closed")
        }
    }
}