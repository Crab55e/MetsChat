package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import org.json.JSONObject

class PluginDisabled(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    fun handler(data: JSONObject) {
        val serverName = data.getString("server_id")
        logger.info("Backend server disconnected: $serverName")
    }
}