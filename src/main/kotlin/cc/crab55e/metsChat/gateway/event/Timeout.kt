package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.gateway.HeartbeatTracker.Server

class Timeout(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    private val heartbeatTracker = plugin.getHeartbeatTracker()
    fun handler(
        server: Server,
        diff: Long,
        timestamp: String
    ) {
        server.updateTimeoutSeconds(diff)

        val backendSupportConfig = plugin.getBackendSupportConfigManager().get()
        val gatewayTimeoutTable = backendSupportConfig.getTable("gateway.timeout")
        val discordNotifyTable = backendSupportConfig.getTable("gateway.timeout.discord-notify")

        if (gatewayTimeoutTable.getBoolean("enabled-logging")) {
            logger.error("Timeout server: ${server.id}, $diff seconds ago, last timestamp is $timestamp")
        }

        if (discordNotifyTable.getBoolean("enabled")) {
            val discordClient = plugin.getDiscordClient()
            discordClient!!.awaitReady()

            val defaultChannelId = backendSupportConfig.getTable("discord.general").getString("default-channel-id")
            var channelId = discordNotifyTable.getString("channel-id")
            if (channelId == "") channelId = defaultChannelId

            val channel = discordClient.getTextChannelById(channelId)
            if (channel != null) {
                return
            } else logger.warn("failed to get to timeout message channel.")
        }
    }
}