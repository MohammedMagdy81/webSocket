package com.example.websocket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.websocket.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var webSocketListener: WebSocketListener
    private val okHttpClient = OkHttpClient()
    private var webSocket: WebSocket? = null

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        webSocketListener = WebSocketListener(viewModel)

        viewModel.socketStatus.observe(this) {
            binding.statusTV.text = if (it) "Connected" else "Disconnected"
        }

        var text = ""
        viewModel.messages.observe(this) {
            text += "${if (it.first) "You: " else "Other: "} ${it.second}\n"

            binding.messageTV.text = text
        }

        binding.connectBtn.setOnClickListener {
            webSocket = okHttpClient.newWebSocket(createRequest(), webSocketListener)
        }

        binding.disConnectBtn.setOnClickListener {
            webSocket?.close(1000, "Canceled manually.")

        }

        binding.sendMessageButton.setOnClickListener {
            webSocket?.send(binding.messageET.text.toString())
            viewModel.addMessage(Pair(true, binding.messageET.text.toString()))
        }

    }

    private fun createRequest(): Request {
        val websocketURL =
            "wss://s8998.nyc1.piesocket.com/v3/1?api_key=OEz67wkoBFI4EAJ3IA5UsiOj8WiAllgOULFZannv&notify_self=1\n"

        return Request.Builder()
            .url(websocketURL)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        okHttpClient.dispatcher.executorService.shutdown()
    }
}
