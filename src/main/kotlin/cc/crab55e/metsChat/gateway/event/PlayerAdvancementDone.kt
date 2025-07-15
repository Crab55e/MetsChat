package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.ColorCodeToColor
import cc.crab55e.metsChat.util.PlaceholderFormatter
import com.google.gson.JsonParser
import net.dv8tion.jda.api.EmbedBuilder
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.json.JSONObject

class PlayerAdvancementDone(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    private val heartbeatTracker = plugin.getHeartbeatTracker()
    fun handler(data: JSONObject) {
        val serverId = data.getString("server_id")
        val jsonDisplayMessage = data.getString("json_component")
        val componentMessage = GsonComponentSerializer.gson().deserialize(jsonDisplayMessage)
        val backendSupportConfig = plugin.getBackendSupportConfigManager().get()
        val messagesConfig = plugin.getMessageConfigManager().get()

        val enabledInServersMessage = backendSupportConfig
            .getTable("player-events.on-advancement-done.in-servers")
            .getBoolean("enabled")
        if (enabledInServersMessage) {
            val server = plugin.getServer()
            server.allPlayers.forEach inServersLoop@{
                val receiver = it
                val receiverServer = receiver.currentServer.get()
                if (serverId == receiverServer.serverInfo.name) return@inServersLoop

                val inServersMessageFormat = messagesConfig
                    .getTable("backend-support.player-events.on-advancement-done.in-servers")
                    .getString("format")

                val inServersMessage = PlaceholderFormatter.format(
                    inServersMessageFormat,
                    mapOf(
                        "eventServer" to serverId,
                        "message" to MiniMessage.miniMessage().serialize(componentMessage)
                    )
                )

                receiver.sendMessage(
                        MiniMessage.miniMessage().deserialize(
                            inServersMessage
                    )
                )
            }
        }
        val onAdvancementDoneTable = backendSupportConfig.getTable("player-events.on-advancement-done.discord")
        val enabledDiscordMessage = onAdvancementDoneTable.getBoolean("enabled")
        if (enabledDiscordMessage) {
            val discordClient = plugin.getDiscordClient()
            discordClient!!.awaitReady()

            val defaultChannelId = backendSupportConfig.getTable("discord.general").getString("default-channel-id")
            var playerAdvancementDoneChannelId = onAdvancementDoneTable.getString("channel-id")
            if (playerAdvancementDoneChannelId == "") playerAdvancementDoneChannelId = defaultChannelId

            val channel = discordClient.getTextChannelById(playerAdvancementDoneChannelId)
            if (channel == null) {
                logger.warn("failed to get player advancement done channel.")
                return
            }

            val onAdvancementDoneMessagesTable = messagesConfig.getTable("backend-support.player-events.on-advancement-done.discord")

            val defaultPlayerIconUrl = messagesConfig.getTable("discord.general").getString("default-player-icon-url")
            var authorIconUrlFormat = onAdvancementDoneMessagesTable.getString("author-icon-url")
            if (authorIconUrlFormat == "") authorIconUrlFormat = defaultPlayerIconUrl

            val playerData = data.getJSONObject("data").getJSONObject("player")
            val authorIconUrl = PlaceholderFormatter.format(
                authorIconUrlFormat,
                mapOf(
                    "mcid" to playerData.getString("name"),
                    "uuid" to playerData.getString("uuid"),
                    "uuidNoDashes" to playerData.getString("name").replace("-", ""),
                )
            )

            val authorNameFormat = onAdvancementDoneMessagesTable.getString("author-name")

            val componentParser = plugin.getJsonComponentParser()
            val stringMessage = GsonComponentSerializer.gson().serialize(componentMessage)
            val jsonElementMessage = JsonParser.parseString(stringMessage)
            val parsedMessage = componentParser.parseComponent(jsonElementMessage)

            val authorName = PlaceholderFormatter.format(
                authorNameFormat,
                mapOf(
                    "eventServer" to serverId,
                    "message" to parsedMessage
                )
            )

            val embedColor = onAdvancementDoneMessagesTable.getString("color")

            val embed = EmbedBuilder()
                .setAuthor(
                    authorName,
                    null,
                    authorIconUrl
                )
                .setColor(ColorCodeToColor(embedColor).color)
                .build()
            channel.sendMessageEmbeds(embed).queue()
        }
    }
}