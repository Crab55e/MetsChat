package cc.crab55e.metsChat.gateway.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.ColorCodeToColor
import cc.crab55e.metsChat.util.PlaceholderFormatter
import cc.crab55e.metsChat.util.PlayerSkinTextureIdResolver
import com.google.gson.JsonParser
import net.dv8tion.jda.api.EmbedBuilder
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.json.JSONObject

class PlayerDeath(private val plugin: MetsChat) {
    private val logger = plugin.getLogger()
    private val heartbeatTracker = plugin.getHeartbeatTracker()
    fun handler(data: JSONObject) {
        val serverId = data.getString("server_id")
        val jsonDisplayMessage = data.getString("json_component")
        val componentMessage = GsonComponentSerializer.gson().deserialize(jsonDisplayMessage)
        val backendSupportConfig = plugin.getBackendSupportConfigManager().get()
        val messagesConfig = plugin.getMessageConfigManager().get()

        val enabledInServersMessage = backendSupportConfig
            .getTable("player-events.on-death.in-servers")
            .getBoolean("enabled")
        if (enabledInServersMessage) {
            val server = plugin.getServer()
            server.allPlayers.forEach inServersLoop@{
                val receiver = it
                val receiverServer = receiver.currentServer.get()
                if (serverId == receiverServer.serverInfo.name) return@inServersLoop

                val inServersMessageFormat = messagesConfig
                    .getTable("backend-support.player-events.on-death.in-servers")
                    .getString("format")

                val inServersMessage = PlaceholderFormatter.format(
                    inServersMessageFormat,
                    mapOf(
                        "eventServer" to serverId,
                        "message" to MiniMessage.miniMessage().serialize(componentMessage)
                    )
                )

                receiver.sendMessage(
                    componentMessage.append(
                        MiniMessage.miniMessage().deserialize(
                            inServersMessage
                        )
                    )
                )
            }
        }
        val onDeathTable = backendSupportConfig.getTable("player-events.on-death.discord")
        val enabledDiscordMessage = onDeathTable.getBoolean("enabled")
        if (enabledDiscordMessage) {
            val discordClient = plugin.getDiscordClient()
            discordClient!!.awaitReady()

            val defaultChannelId = backendSupportConfig.getTable("discord.general").getString("default-channel-id")
            var playerDeathChannelId = onDeathTable.getString("channel-id")
            if (playerDeathChannelId == "") playerDeathChannelId = defaultChannelId

            val channel = discordClient.getTextChannelById(playerDeathChannelId)
            if (channel == null) {
                logger.warn("failed to get player death channel.")
                return
            }

            val onDeathMessagesTable = messagesConfig.getTable("backend-support.player-events.on-death.discord")

            val defaultPlayerIconUrl = messagesConfig.getTable("discord.general").getString("default-player-icon-url")
            var authorIconUrlFormat = onDeathMessagesTable.getString("author-icon-url")
            if (authorIconUrlFormat == "") authorIconUrlFormat = defaultPlayerIconUrl
            logger.info(data.toString())
            val playerData = data.getJSONObject("data").getJSONObject("player")
            val authorIconUrl = PlaceholderFormatter.format(
                authorIconUrlFormat,
                mapOf(
                    "mcid" to playerData.getString("name"),
                    "uuid" to playerData.getString("uuid"),
                    "uuidNoDashes" to playerData.getString("name").replace("-", ""),
                )
            )

            val authorNameFormat = onDeathMessagesTable.getString("author-name")

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

            val embedColor = onDeathMessagesTable.getString("color")

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