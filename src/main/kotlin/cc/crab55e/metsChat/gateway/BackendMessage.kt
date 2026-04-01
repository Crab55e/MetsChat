package cc.crab55e.metsChat.gateway

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.gateway.event.*
import com.google.gson.Gson
import com.google.gson.JsonObject

class BackendMessage(
    private val plugin: MetsChat
) {
    private val logger = plugin.getLogger()
    private val gson = Gson()

    fun onBackendMessageReceived(data: String) {
        val payload = try {
            gson.fromJson(data, BaseBackendEvent::class.java)
        } catch (e: Exception) {
            logger.error("Failed to parse backend message: $data", e)
            return
        }

        val jsonMessage = try {
            gson.fromJson(data, JsonObject::class.java)
        } catch (e: Exception) { return }

        if (payload.eventName != "heartbeat") logger.info(data)

        when (payload.eventName) {
            "plugin_enabled" -> PluginEnabled(plugin).handler(jsonMessage)
            "plugin_disabled" -> PluginDisabled(plugin).handler(jsonMessage)
            "heartbeat" -> Heartbeat(plugin).handler(jsonMessage)
            "player_death_event" -> PlayerDeath(plugin).handler(jsonMessage)
            "player_advancement_done_event" -> PlayerAdvancementDone(plugin).handler(jsonMessage)
            else -> logger.error("Unknown event name: ${payload.eventName}")
        }
    }
}