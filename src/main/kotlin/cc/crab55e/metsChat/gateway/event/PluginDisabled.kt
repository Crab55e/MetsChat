package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.ColorCodeToColor
import cc.crab55e.metsChat.util.PlaceholderFormatter
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class PluginDisabled(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    private val heartbeatTracker = plugin.getHeartbeatTracker()
    fun handler(data: JSONObject) {
        val serverName = data.getString("server_id")
        val timestamp = data.getString("timestamp")

        val backendSupportConfig = plugin.getBackendSupportConfigManager().get()
        val discordNotifyTable = backendSupportConfig.getTable("gateway.plugin-disabled.discord-notify")

        if (discordNotifyTable.getBoolean("enabled")) {
            val discordClient = plugin.getDiscordClient()
            discordClient!!.awaitReady()

            val defaultChannelId = backendSupportConfig.getTable("discord.general").getString("default-channel-id")
            var channelId = discordNotifyTable.getString("channel-id")
            if (channelId == "") channelId = defaultChannelId


            val channel = discordClient.getTextChannelById(channelId)
            if (channel != null) {
                val messageConfig = plugin.getMessageConfigManager().get()
                val discordNotifyMessagesTableKey = "backend-support.plugin-disabled.discord-notify"
                val discordNotifyMessagesTable = messageConfig.getTable(discordNotifyMessagesTableKey)

                val titleFormat = discordNotifyMessagesTable.getString("title")
                val descriptionFormat = discordNotifyMessagesTable.getString("desc")
                val contentFormat = discordNotifyMessagesTable.getString("content")

                val title = PlaceholderFormatter.format(
                    titleFormat,
                    mapOf(
                        "backendServer" to serverName
                    )
                )
                val description = PlaceholderFormatter.format(
                    descriptionFormat,
                    mapOf(
                        "backendServer" to serverName
                    )
                )
                val content = PlaceholderFormatter.format(
                    contentFormat,
                    mapOf(
                        "backendServer" to serverName
                    )
                )
                val color = ColorCodeToColor(discordNotifyMessagesTable.getString("color")).color

                val embed = EmbedBuilder()
                    .setTitle(title)
                    .setDescription(description)
                    .setColor(color)
                    .build()
                val message = MessageCreateBuilder()
                    .addEmbeds(embed)
                    .setContent(content)
                    .build()
                channel.sendMessage(message).queue()
            } else logger.warn("failed to get to plugin disabled message channel.")
        }
    }
}