package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.gateway.HeartbeatTracker
import org.json.JSONObject

class Resumed(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    private val heartbeatTracker = plugin.getHeartbeatTracker()
    fun handler(data: JSONObject) {
        val serverName = data.getString("server_id")
        val timestamp = data.getString("timestamp")
        logger.info("Server $serverName is connection resumed.")

        val server = heartbeatTracker.getServer(serverName)
        server.removeTimeoutSeconds()
    }
}