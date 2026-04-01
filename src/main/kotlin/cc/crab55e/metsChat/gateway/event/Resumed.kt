package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.gateway.HeartbeatTracker
import cc.crab55e.metsChat.gateway.BaseBackendEvent
import cc.crab55e.metsChat.util.ColorCodeToColor
import cc.crab55e.metsChat.util.PlaceholderFormatter
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.time.Instant

class Resumed(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    private val heartbeatTracker = plugin.getHeartbeatTracker()
    private val gson = Gson()

    fun handler(data: JsonObject) {
        val payload = gson.fromJson(data, BaseBackendEvent::class.java)
        val serverName = payload.serverId
        val timestamp = payload.timestamp ?: ""
        logger.info("Server $serverName is connection resumed.")

        plugin.getHeartbeatTimeoutEvent().stoppedNotifyLoop = true

        val server = heartbeatTracker.getServer(serverName)
        server.removeTimeoutSeconds()

        val backendSupportConfig = plugin.getBackendSupportConfigManager().get()
        val discordNotifyTable = backendSupportConfig.getTable("gateway.resumed.discord-notify") ?: return

        if (discordNotifyTable.getBoolean("enabled", false)) {
            val discordClient = plugin.getDiscordClient()
            discordClient?.awaitReady()

            val defaultChannelId = backendSupportConfig.getTable("discord.general")?.getString("default-channel-id") ?: ""
            var channelId = discordNotifyTable.getString("channel-id") ?: ""
            if (channelId.isEmpty()) channelId = defaultChannelId

            val channel = discordClient?.getTextChannelById(channelId)
            if (channel != null) {
                val messageConfig = plugin.getMessageConfigManager().get()
                val discordNotifyMessageTable = messageConfig.getTable("backend-support.resumed.discord-notify") ?: return
                
                val unixTimestamp = try {
                    Instant.parse(timestamp).toEpochMilli() / 1000
                } catch (e: Exception) { 0 }

                val titleFormat = discordNotifyMessageTable.getString("title") ?: ""
                val descriptionFormat = discordNotifyMessageTable.getString("desc") ?: ""
                val contentFormat = discordNotifyMessageTable.getString("content") ?: ""

                val placeholders = mapOf(
                    "backendServer" to serverName,
                    "lastTimeoutUnix" to unixTimestamp.toString()
                )

                val title = PlaceholderFormatter.format(titleFormat, placeholders)
                val description = PlaceholderFormatter.format(descriptionFormat, placeholders)
                val content = PlaceholderFormatter.format(contentFormat, placeholders)

                val colorHex = discordNotifyMessageTable.getString("color") ?: "#FFFFFF"
                val color = ColorCodeToColor(colorHex).color

                val embed = EmbedBuilder()
                    .setTitle(title)
                    .setDescription(description)
                    .setColor(color)
                    .build()

                val message = MessageCreateBuilder()
                    .setContent(content)
                    .addEmbeds(embed)
                    .build()

                channel.sendMessage(message).queue()
            }
        }
    }
}