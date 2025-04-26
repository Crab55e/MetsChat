package cc.crab55e.metsChat.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.Base64

class ChatEventListener(
    private val plugin: MetsChat
) {
    private val server = plugin.getServer()
    private val logger = plugin.getLogger()
    private val mm = MiniMessage.miniMessage()

    // TODO: configにwebhook: boolなりを追加して、webhookに対応しつつmc->discordなチャット連携を実装したい
    @Subscribe
    fun onPlayerChat(event: PlayerChatEvent) {

        val config = plugin.getConfigManager().getConfig()

        val crossServerMessageShareTable = config.getTable("minecraft.cross-server-message-share")
        val toDiscordTable = config.getTable("discord.message-share.to-discord")
        val sender = event.player
        val message = event.message
        val senderServerName = sender.currentServer.get().serverInfo.name

        val enabledCrossServerMessageShare = crossServerMessageShareTable.getBoolean("enabled")
        if (enabledCrossServerMessageShare) {
            val crossServerMessageFormat = crossServerMessageShareTable.getString("format")
            server.allPlayers.forEach playerLoop@{
                val receiver = it
                val receiverServerName = receiver.currentServer.get().serverInfo.name
                if (receiverServerName == senderServerName) return@playerLoop

                val deliveringMessage = mm.deserialize(
                    PlaceholderFormatter.format(
                        crossServerMessageFormat,
                        mapOf(
                            "senderServer" to senderServerName,
                            "senderName" to sender.username,
                            "message" to message
                        )
                    )
                )
                receiver.sendMessage(deliveringMessage)
            }
        }
        if (toDiscordTable.getBoolean("enabled")) {
            val webhookTable = config.getTable("discord.message-share.webhook")
            val webhookFormatsTable = config.getTable("discord.message-share.to-discord.formats")
            if (webhookTable.getBoolean("enabled")) {
                val webhookUrl = webhookTable.getString("webhook-url")
                var authorIconUrl = webhookTable.getString("author-icon-url")

                val senderProfilePropertiesJson = Base64.getDecoder().decode(sender.gameProfileProperties[0].value).toString(Charsets.UTF_8)
                val mapType = object : TypeToken<Map<String, Any>>() {}.type
                val senderProfileMap: Map<String, Any> = Gson().fromJson(senderProfilePropertiesJson, mapType)
                val senderProfileMapTextures = senderProfileMap["textures"] as? Map<*, *>
                val senderProfileMapSkin = senderProfileMapTextures?.get("SKIN") as? Map<*, *>
                val senderSkinUrl = senderProfileMapSkin?.get("url") as? String
                // original: http://textures.minecraft.net/texture/bb20ec924eb2f949a6198f23ec4e38bcaf570d5f3b8f8003e7dcd28863007654
                var senderTextureId = senderSkinUrl?.split("/")?.last()
                if (senderTextureId == null) senderTextureId = "UNDEFINED"

                authorIconUrl = PlaceholderFormatter.format(
                    authorIconUrl,
                    mapOf(
                        "mcid" to sender.username,
                        "uuid" to sender.gameProfile.id.toString(),
                        "uuidNoDashes" to sender.gameProfile.undashedId,
                        "textureId" to senderTextureId
                    )
                )

                val username = PlaceholderFormatter.format(
                    webhookFormatsTable.getString("username"),
                    mapOf(
                        "senderName" to sender.username,
                        "message" to message,
                        "serverName" to senderServerName
                    )
                )
                val content = PlaceholderFormatter.format(
                    webhookFormatsTable.getString("content"),
                    mapOf(
                        "senderName" to sender.username,
                        "message" to message,
                        "serverName" to senderServerName
                    )
                )
                // # placeholders: senderName, message, serverName,

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
        }
    }
}
