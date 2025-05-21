package cc.crab55e.metsChat.event

import cc.crab55e.metsChat.MetsChat
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.json.JSONObject

class BackendMessage(
    private val plugin: MetsChat
) {
    private val logger = plugin.getLogger()
    fun onBackendMessageReceived(data: String) {
        logger.info(data)
        val jsonMessage = JSONObject(data)
        val eventName = jsonMessage.getString("event")
        val serverName = jsonMessage.getString("server_id")
        if (eventName == "plugin_enabled") {
            logger.info("Backend server connected: $serverName")
            return
        }
        val displayMessageJson = jsonMessage.getJSONObject("data").getString("message_json")

        val displayMessage = GsonComponentSerializer.gson().deserialize(displayMessageJson);
        sendDisplayMessagesToServer(displayMessage)

    }

    private fun sendDisplayMessagesToServer(message: Component) {
        val server = plugin.getServer()
        server.allPlayers.forEach {
            val receiver = it
            receiver.sendMessage(message)
        }
    }
}