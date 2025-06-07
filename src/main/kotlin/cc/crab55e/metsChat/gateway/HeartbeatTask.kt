package cc.crab55e.metsChat.gateway

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.gateway.event.Timeout
import java.time.Duration
import java.time.Instant

class HeartbeatTask (private val plugin: MetsChat): Runnable {
    override fun run() {
        val backendSupportConfig = plugin.getBackendSupportConfigManager().get()

        val gatewayTimeoutTable = backendSupportConfig.getTable("gateway.timeout")

        val gatewayTimeout = gatewayTimeoutTable.getLong("timeout")

        val now = Instant.now()
        val heartbeatTracker = plugin.getHeartbeatTracker()
        val connectedServers = heartbeatTracker.getServers()
        for (server in connectedServers) {
            val lastHeartbeatISO = heartbeatTracker.getLastHeartbeat(server) ?: continue
            val lastHeartbeatTime = Instant.parse(lastHeartbeatISO)
            val timeDifference = Duration.between(lastHeartbeatTime, now)

            if (timeDifference.toSeconds() >= gatewayTimeout) {
                Timeout(plugin).handler(
                    server,
                    timeDifference.toSeconds(),
                    lastHeartbeatISO
                )
            }
        }
    }
}