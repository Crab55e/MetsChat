package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import org.json.JSONObject

class Heartbeat(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    private val heartbeatTracker = plugin.getHeartbeatTracker()
    fun handler(data: JSONObject) {
        val serverName = data.getString("server_id")
        val timestamp = data.getString("timestamp")

        val server = heartbeatTracker.getServer(serverName)

        val serverIsResumed = server.timeoutSeconds != null
        if (serverIsResumed) {
            Resumed(plugin).handler(data)
        }

        heartbeatTracker.getServer(serverName).updateHeartbeat(timestamp)
    }
}