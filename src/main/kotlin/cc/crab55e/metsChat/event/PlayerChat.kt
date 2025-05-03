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
        val senderServerName = event.player.currentServer.get().serverInfo.name

        val inServerTableKey = "message-share.in-servers.player-chat"
        val enabledInServersMessageShare = config.getTable(inServerTableKey).getBoolean("enabled")
        if (enabledInServersMessageShare) {

            val inServerMessageShareFormat = messagesConfig.getTable(inServerTableKey).getString("format")
            server.allPlayers.forEach playerLoop@{
                val receiver = it
                val receiverServerName = receiver.currentServer.get().serverInfo.name
                if (receiverServerName == senderServerName) return@playerLoop

                val deliveringMessage = mm.deserialize(
                    PlaceholderFormatter.format(
                        inServerMessageShareFormat,
                        mapOf(
                            "senderServer" to senderServerName,
                            "senderName" to event.player.username,
                            "message" to event.message
                        )
                    )
                )
                receiver.sendMessage(deliveringMessage)
            }
        }

        val toDiscordTableKey = "message-share.to-discord.player-chat"
        val toDiscordTable = config.getTable(toDiscordTableKey)
        if (config.getTable(toDiscordTableKey).getBoolean("enabled")) {
            val content = PlaceholderFormatter.format(
                messagesConfig.getTable(toDiscordTableKey).getString("content"),
                mapOf(
                    "senderName" to event.player.username,
                    "message" to event.message,
                    "serverName" to senderServerName
                )
            )

            val toDiscordWebhookTableKey = "message-share.to-discord.player-chat.webhook"
            if (config.getTable(toDiscordWebhookTableKey).getBoolean("enabled")) {
                val toDiscordWebhookMessagesTable = messagesConfig.getTable(toDiscordWebhookTableKey)

                val senderTextureId = PlayerSkinTextureIdResolver(event.player).textureId

                val defaultPlayerIconUrl = messagesConfig.getTable("discord.general").getString("default-player-icon-url")
                var authorIconUrlFormat = toDiscordWebhookMessagesTable.getString("author-icon-url")
                if (authorIconUrlFormat == "") authorIconUrlFormat = defaultPlayerIconUrl

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
                    toDiscordWebhookMessagesTable.getString("username"),
                    mapOf(
                        "senderName" to event.player.username,
                        "message" to event.message,
                        "serverName" to senderServerName
                    )
                )

                val webhookUrl: String
                val discordWebhookUrlTable = config.getTable("discord.webhook-url")
                val discordWebhookUrlType = discordWebhookUrlTable.getString("type")
                val discordWebhookUrlValue = discordWebhookUrlTable.getString("value")
                if (discordWebhookUrlType == "system-environ") {
                    webhookUrl = System.getenv(discordWebhookUrlValue)
                } else if (discordWebhookUrlType == "raw-string") {
                    webhookUrl = discordWebhookUrlValue
                } else {
                    logger.error("$discordWebhookUrlType is invalid type.")
                    return
                }

                val webhook = WebhookWrapper(webhookUrl, plugin)
                val wAllowedMentions = AllowedMentions()
                val webhookMessage = Message(
                    username = username,
                    avatarURL = authorIconUrl,
                    content = content,
                    allowedMentions = wAllowedMentions
                )
                webhook.send(webhookMessage)

            } else {
                val discordGeneralTable = config.getTable("discord.general")
                val defaultChannelId = discordGeneralTable.getString("default-channel-id")

                var channelId = toDiscordTable.getString("channel-id")
                if (channelId == "") channelId = defaultChannelId
                logger.info("cid $channelId, dcid $defaultChannelId")
                // TODO: webhookじゃなくてbotから直送りしたいときの処理を書く、今はめんどい
            }
        }
    }
}
