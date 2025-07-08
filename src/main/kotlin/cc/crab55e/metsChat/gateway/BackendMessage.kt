package cc.crab55e.metsChat.gateway

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.gateway.event.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.json.JSONObject

class BackendMessage(
    private val plugin: MetsChat
) {
    private val logger = plugin.getLogger()
    fun onBackendMessageReceived(data: String) {
        if (JSONObject(data).getString("event") != "heartbeat") logger.info(data)
        val jsonMessage = JSONObject(data)
        val serverId = jsonMessage.getString("server_id")
        when (val eventName = jsonMessage.getString("event")) {
            "plugin_enabled" -> PluginEnabled(plugin).handler(jsonMessage)
            "plugin_disabled" -> PluginDisabled(plugin).handler(jsonMessage)
            "heartbeat" -> Heartbeat(plugin).handler(jsonMessage)
            "player_death_event" -> PlayerDeath(plugin).handler(jsonMessage)
            "player_advancement_done_event" -> PlayerAdvancementDone(plugin).handler(jsonMessage)
            else -> logger.error("Unknown event name: $eventName")
        }

        val displayMessageJson = jsonMessage.getString("json_component")
        if (displayMessageJson != "") {
            val displayMessage = GsonComponentSerializer.gson()
                .deserialize(displayMessageJson)
                .append(MiniMessage.miniMessage().deserialize(" <reset><gray>*[$serverId]</gray></reset>"))
            broadCastMessage(displayMessage, listOf(serverId))
        }
    }

    private fun broadCastMessage(message: Component, ignoredServerIds: List<String>) {
        val server = plugin.getServer()
        server.allPlayers.forEach {
            val receiver = it
            val receiverServer = receiver.currentServer.get()
            if (ignoredServerIds.contains(receiverServer.serverInfo.name)) return
            receiver.sendMessage(message)
        }
    }
}