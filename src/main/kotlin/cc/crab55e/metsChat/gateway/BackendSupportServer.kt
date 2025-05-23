package cc.crab55e.metsChat.gateway

import cc.crab55e.metsChat.MetsChat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.ServerSocket
import kotlin.concurrent.thread
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
    fun start() {
        thread(name = "BackendSupportServer") {
            val serverSocket = ServerSocket(port)
            thread {
                while (true) {
                    val client = serverSocket.accept()
                    thread client@{
                        val reader = client.getInputStream().bufferedReader()
                        val writer = client.getOutputStream().bufferedWriter()

                        val backendSupportServerTable = plugin.getConfigManager().get().getTable("general.backend-support.server")
                        val expectedSecret = backendSupportServerTable.getString("secret")

                        val message = reader.readLine()
                        val mapType = object : TypeToken<Map<String, Any>>() {}.type
                        val messageJson = gson.fromJson<Map<String, Any>>(message, mapType)

                        val messageData = messageJson["message"]
                        val messageDataString = gson.toJson(messageData)

                        val expectedSignature = generateHMAC(messageDataString, expectedSecret)

                        val clientSignature = messageJson["signature"]

                        if (clientSignature != expectedSignature) {
                            writer.write("{\"error\": \"invalid signature\"}\n")
                            writer.flush()
                            client.close()
                            logger.info("Invalid signature message: $message")
                            return@client
                        }


                        handler.onBackendMessageReceived(messageDataString)

                        writer.write("{\"ack\": true}\n")
                        writer.flush()

                        client.close()
                    }
                }
            }
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
