package cc.crab55e.metsChat.gateway

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.event.BackendMessage
import java.net.ServerSocket
import kotlin.concurrent.thread

class BackendSupportServer(
    private val plugin: MetsChat,
    private val port: Int,
    private val handler: BackendMessage
) {
    private val logger = plugin.getLogger()
    fun start() {
        thread(name = "TcpServer") {
            val serverSocket = ServerSocket(port)

            while (true) {
                val socket = serverSocket.accept()
                thread {
                    socket.use {
                        val reader = it.getInputStream().bufferedReader()
                        val message = reader.readLine()

                        // 💡 他の処理スレッドに流す（イベント的な仕組み）
                        handler.onBackendMessageReceived(message)
                    }
                }
            }
        }
    }

}