package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class Timeout(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    private val heartbeatTracker = plugin.getHeartbeatTracker()
    fun handler(
        serverId: String,
        diff: Long,
        timestamp: String
    ) {
        val isoTimestamp = DateTimeFormatter.ISO_INSTANT
            .withZone(ZoneOffset.UTC)
            .format(Instant.ofEpochMilli(System.currentTimeMillis()))
        heartbeatTracker.update(
            serverId,
            null,
            isoTimestamp
        )
        return
    }
}