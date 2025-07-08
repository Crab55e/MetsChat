package cc.crab55e.metsChat.gateway

import cc.crab55e.metsChat.MetsChat

class HeartbeatTracker(private val plugin: MetsChat) {
    private val servers = mutableMapOf<String, Server>()

    fun registerServer(id: String): Server {
        if (servers[id] != null) return servers[id]!!
        val server = Server(id)
        servers[id] = server
        return server
    }

    fun getServer(id: String): Server = servers[id] ?: registerServer(id)

    fun removeServer(server: Server): Map<String, Server> {
        servers.remove(server.id)
        return servers
    }

    fun getServers(): List<Server> = servers.values.toList()

    class Server(val id: String) {
        var lastHeartbeat: String? = null
        var lastCheck: String? = null
        var timeoutSeconds: Long? = null

        fun updateHeartbeat(timestamp: String) {
            lastHeartbeat = timestamp
        }
        fun updateLastCheck(timestamp: String) {
            lastCheck = timestamp
        }
        fun updateTimeoutSeconds(seconds: Long) {
            timeoutSeconds = seconds
        }
        fun removeTimeoutSeconds() {
            timeoutSeconds = null
        }
    }
}
