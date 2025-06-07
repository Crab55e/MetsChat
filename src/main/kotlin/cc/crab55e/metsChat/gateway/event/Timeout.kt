package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import org.json.JSONObject

class Timeout(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    private val heartbeatTracker = plugin.getHeartbeatTracker()
    fun handler(
        serverId: String,
        diff: Long,
        timestamp: String
    ) {
        val now = ""
        heartbeatTracker.update(
            serverId,
            null,
            now
        )
        return
    }
}