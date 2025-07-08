package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import org.json.JSONObject

class PlayerAdvancementDone(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    private val heartbeatTracker = plugin.getHeartbeatTracker()
    fun handler(data: JSONObject) {
    }
}