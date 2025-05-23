package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import org.json.JSONObject

class PluginEnabled(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    fun handler(data: JSONObject) {
        val serverName = data.getString("server_id")
        logger.info("Backend server connected: $serverName")

        val discordClient = plugin.getDiscordClient()
        discordClient!!.awaitReady()
    }
}