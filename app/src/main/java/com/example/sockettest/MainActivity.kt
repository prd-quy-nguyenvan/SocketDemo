package com.example.sockettest

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val client = HttpClient(CIO) {
        install(Logging)
        install(WebSockets) {
            pingInterval = 5000
        }
        engine {
            requestTimeout = 60_000
            endpoint.connectTimeout = 30_000
            endpoint.keepAliveTime = 30_000
        }
    }
    private val ip = "192.168.30.141"
    private val webSocketClient = WebSocketClient(client, "http://$ip:7003")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.btnSocketOpen).setOnClickListener {
            lifecycleScope.launch {
                webSocketClient.getStateStream()
                    .flowOn(Dispatchers.IO)
                    .onStart { Log.d("TAG onStart", "Connecting...") }
                    .onEach { Log.d("TAG onEach", it) }
                    .catch { Log.d("TAG Catch", it.message!!) }.collectLatest {
                        Log.d("TAG Collect", it)
                    }
            }
        }
    }
}
