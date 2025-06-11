package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.gateway.HeartbeatTracker.Server
import cc.crab55e.metsChat.util.ColorCodeToColor
import cc.crab55e.metsChat.util.PlaceholderFormatter
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import java.time.Duration
import java.time.Instant
import java.util.function.Consumer

class Timeout(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    private val heartbeatTracker = plugin.getHeartbeatTracker()

    var stoppedNotifyLoop = true
    fun startTimeoutNotify(
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
                val messageConfig = plugin.getMessageConfigManager().get()
                val discordNotifyMessageTable = messageConfig.getTable("backend-support.timeout.discord-notify")

                val unixTimestamp = Instant.parse(timestamp).toEpochMilli() / 1000

                val titleFormat = discordNotifyMessageTable.getString("title")
                val descriptionFormat = discordNotifyMessageTable.getString("desc")
                val contentFormat = discordNotifyMessageTable.getString("content")

                val title = PlaceholderFormatter.format(
                    titleFormat,
                    mapOf(
                        "backendServer" to server.id,
                        "lastHeartbeatUnix" to unixTimestamp.toString()
                    )
                )
                val description = PlaceholderFormatter.format(
                    descriptionFormat,
                    mapOf(
                        "backendServer" to server.id,
                        "lastHeartbeatUnix" to unixTimestamp.toString()
                    )
                )
                val content = PlaceholderFormatter.format(
                    contentFormat,
                    mapOf(
                        "backendServer" to server.id,
                        "lastHeartbeatUnix" to unixTimestamp.toString()
                    )
                )
                val color = ColorCodeToColor(discordNotifyMessageTable.getString("color")).color
                val stopButtonLabel = discordNotifyMessageTable.getString("stop-button-label")

                val loopInterval = discordNotifyTable.getLong("message-interval")
                var currentLoop = 1

                stoppedNotifyLoop = false
                plugin.getServer().scheduler.buildTask(plugin, Consumer {
                    task ->
                    run {
                        val maxMessageLoop = discordNotifyTable.getLong("max-message-loop")

                        if (currentLoop > maxMessageLoop) {
                            task.cancel()
                            return@Consumer
                        }
                        if (stoppedNotifyLoop) {
                            task.cancel()
                            logger.info("Stopped discord timeout notify message loop.")
                            return@Consumer
                        }

                        val embed = EmbedBuilder()
                            .setTitle(title)
                            .setDescription(description)
                            .setColor(color)
                            .build()

                        val message = MessageCreateBuilder()
                            .setContent(content)
                            .addEmbeds(embed)
                            .addActionRow(
                                Button.danger("metschat:timeout_notify_stop", stopButtonLabel)
                            )
                            .build()
                        channel.sendMessage(message).queue()

                        currentLoop++
                    }
                })
                    .repeat(Duration.ofSeconds(loopInterval))
                    .schedule()


            } else logger.warn("failed to get to timeout message channel.")
        }
    }
}