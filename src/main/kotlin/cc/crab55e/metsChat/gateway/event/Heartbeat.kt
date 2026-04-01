package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import org.json.JSONObject

class Heartbeat(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    private val heartbeatTracker = plugin.getHeartbeatTracker()
    fun handler(data: JSONObject) {
        val serverName = data.getString("server_id")
        val timestamp = data.getString("timestamp")

        val isNewServer = !heartbeatTracker.hasServer(serverName)
        val server = heartbeatTracker.getServer(serverName)

        if (isNewServer) {
            PluginEnabled(plugin).handler(data)
        }

        val serverIsResumed = server.timeoutSeconds != null
        if (serverIsResumed) {
            Resumed(plugin).handler(data)
        }

        server.updateHeartbeat(timestamp)
    }
}