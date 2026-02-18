package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.gateway.HeartbeatTracker
import cc.crab55e.metsChat.util.ColorCodeToColor
import cc.crab55e.metsChat.util.PlaceholderFormatter
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import org.json.JSONObject
import java.time.Instant

class Resumed(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    private val heartbeatTracker = plugin.getHeartbeatTracker()
    fun handler(data: JSONObject) {
        val serverName = data.getString("server_id")
        val timestamp = data.getString("timestamp")
        logger.info("Server $serverName is connection resumed.")

        plugin.getHeartbeatTimeoutEvent().stoppedNotifyLoop = true

        val server = heartbeatTracker.getServer(serverName)
        server.removeTimeoutSeconds()

        val backendSupportConfig = plugin.getBackendSupportConfigManager().get()
        val discordNotifyTable = backendSupportConfig.getTable("gateway.resumed.discord-notify")

        if (discordNotifyTable.getBoolean("enabled")) {
            val discordClient = plugin.getDiscordClient()
            discordClient!!.awaitReady()

            val defaultChannelId = backendSupportConfig.getTable("discord.general").getString("default-channel-id")
            var channelId = discordNotifyTable.getString("channel-id")
            if (channelId == "") channelId = defaultChannelId

            val channel = discordClient.getTextChannelById(channelId)
            if (channel != null) {
                val messageConfig = plugin.getMessageConfigManager().get()
                val discordNotifyMessageTable = messageConfig.getTable("backend-support.resumed.discord-notify")
                val unixTimestamp = Instant.parse(timestamp).toEpochMilli() / 1000

                val titleFormat = discordNotifyMessageTable.getString("title")
                val descriptionFormat = discordNotifyMessageTable.getString("desc")
                val contentFormat = discordNotifyMessageTable.getString("content")

                val title = PlaceholderFormatter.format(
                    titleFormat,
                    mapOf(
                        "backendServer" to serverName,
                        "lastTimeoutUnix" to unixTimestamp.toString()
                    )
                )
                val description = PlaceholderFormatter.format(
                    descriptionFormat,
                    mapOf(
                        "backendServer" to serverName,
                        "lastTimeoutUnix" to unixTimestamp.toString()
                    )
                )
                val content = PlaceholderFormatter.format(
                    contentFormat,
                    mapOf(
                        "backendServer" to serverName,
                        "lastTimeoutUnix" to unixTimestamp.toString()
                    )
                )

                val color = ColorCodeToColor(discordNotifyMessageTable.getString("color")).color

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