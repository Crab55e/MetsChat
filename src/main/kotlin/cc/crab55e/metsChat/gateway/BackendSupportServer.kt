package cc.crab55e.metsChat.gateway

import cc.crab55e.metsChat.MetsChat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import java.net.SocketException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

class BackendSupportServer(
    private val plugin: MetsChat,
    private val port: Int,
    private val handler: BackendMessage
) {
    private val logger = plugin.getLogger()
    private val gson = Gson()
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null

    fun start() {
        serverJob = plugin.pluginScope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(port)

                while (isActive) {
                    val client = withContext(Dispatchers.IO) {
                        try {
                            serverSocket?.accept()
                        } catch (e: SocketException) {
                            null // ソケットが閉じられたら抜ける
                        }
                    } ?: break

                    plugin.pluginScope.launch(Dispatchers.IO) {
                        try {
                            client.use { socket ->
                                val reader = socket.getInputStream().bufferedReader()
                                val writer = socket.getOutputStream().bufferedWriter()

                                val backendSupportServerTable = plugin.getBackendSupportConfigManager().get().getTable("general.server-setting")
                                val expectedSecret = backendSupportServerTable.getString("secret")

                                val message = reader.readLine() ?: return@use
                                
                                val mapType = object : TypeToken<Map<String, Any>>() {}.type
                                val messageJson = try {
                                    gson.fromJson<Map<String, Any>>(message, mapType)
                                } catch (e: JsonSyntaxException) {
                                    logger.warn("Received malformed JSON from BackendSupportClient: $message")
                                    return@use
                                }

                                val messageData = messageJson["message"] ?: return@use
                                val messageDataString = gson.toJson(messageData)

                                val expectedSignature = generateHMAC(messageDataString, expectedSecret)
                                val clientSignature = messageJson["signature"]

                                if (clientSignature != expectedSignature) {
                                    writer.write("{\"error\": \"invalid signature\"}\n")
                                    writer.flush()
                                    logger.info("Invalid signature message: $message")
                                    return@use
                                }

                                handler.onBackendMessageReceived(messageDataString)

                                writer.write("{\"ack\": true, \"proxy_start_time\": ${plugin.proxyStartTime}}\n")
                                writer.flush()
                            }
                        } catch (e: Exception) {
                            logger.error("Error processing client connection in BackendSupportServer", e)
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("BackendSupportServer encountered an error on port $port", e)
            }
        }
    }

    fun stop() {
        try {
            serverJob?.cancel()
            serverSocket?.close()
        } catch (e: Exception) {
            logger.error("Error closing BackendSupportServer", e)
        }
    }

    private fun generateHMAC(message: String, secret: String): String {
        val algorithm = "HmacSHA256"
        val keySpec = SecretKeySpec(secret.toByteArray(), algorithm)
        val mac = Mac.getInstance(algorithm)
        mac.init(keySpec)
        val hmacBytes = mac.doFinal(message.toByteArray())
        return Base64.getEncoder().encodeToString(hmacBytes)
    }
}
