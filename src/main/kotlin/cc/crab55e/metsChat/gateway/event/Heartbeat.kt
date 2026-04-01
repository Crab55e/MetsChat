package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.gateway.BaseBackendEvent
import com.google.gson.Gson
import com.google.gson.JsonObject

class Heartbeat(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    private val heartbeatTracker = plugin.getHeartbeatTracker()
    private val gson = Gson()

    fun handler(data: JsonObject) {
        val payload = gson.fromJson(data, BaseBackendEvent::class.java)
        val serverName = payload.serverId
        val timestamp = payload.timestamp ?: ""

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