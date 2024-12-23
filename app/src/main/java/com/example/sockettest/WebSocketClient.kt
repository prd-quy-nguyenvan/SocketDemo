package com.example.sockettest

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.headers
import io.ktor.http.URLProtocol
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull

class WebSocketClient(private val client: HttpClient, private val ipAddress: String) : RealtimeMessagingClient {

    private var session: WebSocketSession? = null

    override fun getStateStream(): Flow<String> {
        return flow {
            session = client.webSocketSession(ipAddress) {
                headers {
                    append("user_id", "123455")
                }
                url {
                    protocol = URLProtocol.WS
                    port = 7003
                }
            }
            val messageState = session?.incoming?.consumeAsFlow()?.filterIsInstance<Frame.Text>()?.mapNotNull { it.readText() }
            messageState?.let { emitAll(messageState) }
        }
    }

    override suspend fun sendAction(action: String) {
        session?.outgoing?.send(
            Frame.Text(action)
        )
    }

    override suspend fun close() {
        session?.close()
        session = null
    }

}

interface RealtimeMessagingClient {
    fun getStateStream(): Flow<String>
    suspend fun sendAction(action: String)
    suspend fun close()
}
