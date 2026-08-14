package com.coffevendor.data.remote

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.TimeUnit

object WebSocketClient {

    private const val WS_URL = "wss://api.coffevendor.com/ws/orders"

    private val _orderEvents = MutableSharedFlow<WebSocketEvent>(extraBufferCapacity = 64)
    val orderEvents: SharedFlow<WebSocketEvent> = _orderEvents.asSharedFlow()

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null

    fun connect() {
        val request = Request.Builder()
            .url(WS_URL)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                _orderEvents.tryEmit(WebSocketEvent.Connected)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                _orderEvents.tryEmit(WebSocketEvent.OrderUpdate(text))
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                _orderEvents.tryEmit(WebSocketEvent.Disconnected)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                _orderEvents.tryEmit(WebSocketEvent.Error(t.message ?: "Unknown error"))
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnecting")
        webSocket = null
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }
}

sealed class WebSocketEvent {
    data object Connected : WebSocketEvent()
    data object Disconnected : WebSocketEvent()
    data class OrderUpdate(val payload: String) : WebSocketEvent()
    data class Error(val message: String) : WebSocketEvent()
}
