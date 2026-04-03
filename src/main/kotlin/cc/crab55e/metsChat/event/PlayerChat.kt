package cc.crab55e.metsChat.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.*
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import net.kyori.adventure.text.minimessage.MiniMessage

class ChatEventListener(
    private val plugin: MetsChat
) {
    private val server = plugin.getServer()
    private val logger = plugin.getLogger()
    private val mm = MiniMessage.miniMessage()

    @Subscribe
    fun onPlayerChat(event: PlayerChatEvent) {
        val config = plugin.getConfigManager().get()
        val messagesConfig = plugin.getMessageConfigManager().get()
        
        val senderServerInfo = event.player.currentServer.orElse(null)?.serverInfo
        val senderServerName = senderServerInfo?.name ?: "Unknown"

        val inServerTableKey = "message-share.in-servers.player-chat"
        val inServerTable = config.getTable(inServerTableKey)
        val enabledInServersMessageShare = inServerTable?.getBoolean("enabled", false) ?: false
        
        if (enabledInServersMessageShare) {
            val messagesInServerTable = messagesConfig.getTable(inServerTableKey)
            val inServerMessageShareFormat = messagesInServerTable?.getString("format") ?: ""
            
            server.allPlayers.forEach { receiver ->
                val receiverServerName = receiver.currentServer.orElse(null)?.serverInfo?.name ?: "Unknown"
                if (receiverServerName != senderServerName) {
                    val formattedString = PlaceholderFormatter.format(
                        inServerMessageShareFormat,
                        mapOf(
                            "senderServer" to senderServerName,
                            "senderName" to event.player.username,
                            "message" to event.message
                        )
                    )
                    receiver.sendMessage(mm.deserialize(formattedString))
                }
            }
        }

        val toDiscordTableKey = "message-share.to-discord.player-chat"
        val toDiscordTable = config.getTable(toDiscordTableKey) ?: return
        
        if (toDiscordTable.getBoolean("enabled", false)) {
            val toDiscordMessagesTable = messagesConfig.getTable(toDiscordTableKey) ?: return
            val content = PlaceholderFormatter.format(
                toDiscordMessagesTable.getString("content") ?: "",
                mapOf(
                    "senderName" to event.player.username,
                    "message" to event.message,
                    "serverName" to senderServerName
                )
            )

            val toDiscordWebhookTableKey = "message-share.to-discord.player-chat.webhook"
            val webhookTable = config.getTable(toDiscordWebhookTableKey)
            
            if (webhookTable != null && webhookTable.getBoolean("enabled", false)) {
                val toDiscordWebhookMessagesTable = messagesConfig.getTable(toDiscordWebhookTableKey) ?: return
                val senderTextureId = PlayerSkinTextureIdResolver(event.player).textureId

                val defaultPlayerIconUrl = messagesConfig.getTable("discord.general")?.getString("default-player-icon-url") ?: ""
                var authorIconUrlFormat = toDiscordWebhookMessagesTable.getString("author-icon-url") ?: ""
                if (authorIconUrlFormat.isEmpty()) authorIconUrlFormat = defaultPlayerIconUrl

                val authorIconUrl = PlaceholderFormatter.format(
                    authorIconUrlFormat,
                    mapOf(
                        "mcid" to event.player.username,
                        "uuid" to event.player.gameProfile.id.toString(),
                        "uuidNoDashes" to event.player.gameProfile.undashedId,
                        "textureId" to senderTextureId
                    )
                )

                val username = PlaceholderFormatter.format(
                    toDiscordWebhookMessagesTable.getString("username") ?: "",
                    mapOf(
                        "senderName" to event.player.username,
                        "message" to event.message,
                        "serverName" to senderServerName
                    )
                )

                val discordWebhookUrlTable = config.getTable("discord.webhook-url") ?: return
                val discordWebhookUrlType = discordWebhookUrlTable.getString("type") ?: ""
                val discordWebhookUrlValue = discordWebhookUrlTable.getString("value") ?: ""
                
                val webhookUrl = when (discordWebhookUrlType) {
                    "system-environ" -> System.getenv(discordWebhookUrlValue) ?: ""
                    "raw-string" -> discordWebhookUrlValue
                    else -> {
                        logger.error("$discordWebhookUrlType is an invalid webhook url type.")
                        return
                    }
                }

                if (webhookUrl.isNotEmpty()) {
                    val webhook = WebhookWrapper(webhookUrl, plugin)
                    val wAllowedMentions = AllowedMentions()
                    val webhookMessage = Message(
                        username = username,
                        avatarURL = authorIconUrl,
                        content = content,
                        allowedMentions = wAllowedMentions
                    )
                    webhook.send(webhookMessage)
                }

            } else {
                val discordGeneralTable = config.getTable("discord.general")
                val defaultChannelId = discordGeneralTable?.getString("default-channel-id") ?: ""

                var channelId = toDiscordTable.getString("channel-id") ?: ""
                if (channelId.isEmpty()) channelId = defaultChannelId
                logger.debug("Routing message to bot channel: cid $channelId, dcid $defaultChannelId")
                
                val channel = plugin.getDiscordClient()?.getChannelById(net.dv8tion.jda.api.entities.channel.concrete.TextChannel::class.java, channelId)
                if (channel != null) {
                    channel.sendMessage(content).queue()
                } else {
                    logger.warn("Failed to find Discord channel with ID $channelId")
                }
            }
        }
    }
}
