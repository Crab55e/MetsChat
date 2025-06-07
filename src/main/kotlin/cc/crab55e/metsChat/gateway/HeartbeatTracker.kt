package cc.crab55e.metsChat.gateway

import cc.crab55e.metsChat.MetsChat

class HeartbeatTracker(private val plugin: MetsChat) {
    private val heartbeats = mutableMapOf<String, MutableMap<String, String?>>()

    fun make(id: String) {
        if (!heartbeats[id].isNullOrEmpty()) return
        heartbeats[id] = mutableMapOf()
    }

    fun update(id: String, timestamp: String?, lastDisconnect: String?) {
        if (!timestamp.isNullOrEmpty()) {
            heartbeats[id]?.set("timestamp", timestamp)
        }
        if (!lastDisconnect.isNullOrEmpty()) {
            heartbeats[id]?.set("last_disconnect", timestamp)
        }
    }
    fun remove(id: String) {
        heartbeats.remove(id)
    }

    fun getLastHeartbeat(id: String): String? = heartbeats[id]?.get("timestamp")
    fun getLastDisconnect(id: String): String? = heartbeats[id]?.get("last_disconnect")
    fun getServers(): List<String> = heartbeats.keys.toList()
}
