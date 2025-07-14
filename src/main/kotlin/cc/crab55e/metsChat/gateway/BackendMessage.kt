package cc.crab55e.metsChat.gateway

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.gateway.event.*
import com.google.gson.JsonParser
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
    }
}