package com.mikhail.vortex.api

import okhttp3.*
import okio.ByteString

class WebSocketClient {

    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect(onMessage: (String) -> Unit) {
        val request = Request.Builder()
            .url("ws://18.195.239.151:8000/ws")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                println("WS Connected")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                onMessage(text)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                println("WS Error: ${t.message}")
            }
        })
    }

    fun close() {
        webSocket?.close(1000, null)
    }
}
