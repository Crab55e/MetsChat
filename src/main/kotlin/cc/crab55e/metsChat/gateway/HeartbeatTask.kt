package cc.crab55e.metsChat.gateway

import cc.crab55e.metsChat.MetsChat
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class HeartbeatTask (private val plugin: MetsChat): Runnable {
    override fun run() {
        val backendSupportConfig = plugin.getBackendSupportConfigManager().get()
        val gatewayTimeoutTable = backendSupportConfig.getTable("gateway.timeout")
        val gatewayTimeout = gatewayTimeoutTable.getLong("timeout")

        val now = Instant.now()

        val heartbeatTracker = plugin.getHeartbeatTracker()
        val connectedServers = heartbeatTracker.getServers()

        for (server in connectedServers) {
            val isoTimestamp = DateTimeFormatter.ISO_INSTANT
                .withZone(ZoneOffset.UTC)
                .format(Instant.ofEpochMilli(System.currentTimeMillis()))
            server.updateLastCheck(isoTimestamp)

            val lastHeartbeatISO = server.lastHeartbeat ?: continue
            val lastHeartbeatTime = Instant.parse(lastHeartbeatISO)
            val timeDifference = Duration.between(lastHeartbeatTime, now)

            if (timeDifference.toSeconds() >= gatewayTimeout && server.timeoutSeconds == null) {
                plugin.getHeartbeatTimeoutEvent().startTimeoutNotify(
                    server,
                    timeDifference.toSeconds(),
                    lastHeartbeatISO
                )
            }
        }
    }
}