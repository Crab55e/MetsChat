package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.ColorCodeToColor
import cc.crab55e.metsChat.util.PlaceholderFormatter
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import org.json.JSONObject
import kotlin.math.log

class PluginEnabled(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    private val heartbeatTracker = plugin.getHeartbeatTracker()
    fun handler(data: JSONObject) {
        val serverName = data.getString("server_id")
        logger.info("Backend server connected: $serverName")

        heartbeatTracker.make(serverName)

        val backendSupportConfig = plugin.getBackendSupportConfigManager().get()
        val discordNotifyTable = backendSupportConfig.getTable("gateway.plugin-enabled.discord-notify")

        if (discordNotifyTable.getBoolean("enabled")) {
            val discordClient = plugin.getDiscordClient()
            discordClient!!.awaitReady()

            val defaultChannelId = backendSupportConfig.getTable("discord.general").getString("default-channel-id")
            var channelId = discordNotifyTable.getString("channel-id")
            if (channelId == "") channelId = defaultChannelId


            val channel = discordClient.getTextChannelById(channelId)
            if (channel != null) {
                val messageConfig = plugin.getMessageConfigManager().get()
                val discordNotifyMessagesTableKey = "backend-support.plugin-enabled.discord-notify"
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
            } else logger.warn("failed to get to plugin enabled message channel.")
        }
    }
}