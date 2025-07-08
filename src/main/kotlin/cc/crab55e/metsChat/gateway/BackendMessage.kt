package cc.crab55e.metsChat.gateway

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.gateway.event.Heartbeat
import cc.crab55e.metsChat.gateway.event.PluginDisabled
import cc.crab55e.metsChat.gateway.event.PluginEnabled
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
        when (val eventName = jsonMessage.getString("event")) {
            "plugin_enabled" -> PluginEnabled(plugin).handler(jsonMessage)
            "plugin_disabled" -> PluginDisabled(plugin).handler(jsonMessage)
            "heartbeat" -> Heartbeat(plugin).handler(jsonMessage)
            else -> logger.error("Unknown event name: $eventName")
        }

        val displayMessageJson = jsonMessage.getString("json_component")
        if (displayMessageJson != "") {
            val displayMessage = GsonComponentSerializer.gson().deserialize(displayMessageJson)
            sendDisplayMessagesToServer(displayMessage)
        }
    }

    private fun sendDisplayMessagesToServer(message: Component) {
        val server = plugin.getServer()
        server.allPlayers.forEach {
            val receiver = it
            receiver.sendMessage(message)
        }
    }
}