package cc.crab55e.metsChat.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.ColorCodeToColor
import cc.crab55e.metsChat.util.PlaceholderFormatter
import cc.crab55e.metsChat.util.PlayerSkinTextureIdResolver
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerConnectedEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

class PlayerServerChange(
    private val plugin: MetsChat
) {
    private val logger = plugin.getLogger()
    @Subscribe
    fun onServerChange(event: ServerConnectedEvent) {
        if (!event.previousServer.isPresent) return
        // PlayerJoinに任せるぜ！
        val previousServer = event.previousServer.get()

        val config = plugin.getConfigManager().get()
        val messagesConfig = plugin.getMessageConfigManager().get()

        val connectionServerChangeTableKey = "message-share.to-discord.connection.server-change"
        val connectionServerChangeTable = config.getTable(connectionServerChangeTableKey)

        if (!connectionServerChangeTable.getBoolean("enabled")) return

        val discordClient = plugin.getDiscordClient()
        discordClient!!.awaitReady()

        val defaultChannelId = config.getTable("discord.general").getString("default-channel-id")
        var connectionServerChangeChannelId = connectionServerChangeTable.getString("channel-id")
        if (connectionServerChangeChannelId == "") connectionServerChangeChannelId = defaultChannelId

        val connectionServerChangeChannel = discordClient.getTextChannelById(connectionServerChangeChannelId)

        if (connectionServerChangeChannel != null) {
            val connectionServerChangeMessagesTable = messagesConfig.getTable(connectionServerChangeTableKey)

            val defaultPlayerIconUrl = messagesConfig.getTable("discord.general").getString("default-player-icon-url")
            var authorIconUrlFormat = connectionServerChangeMessagesTable.getString("author-icon-url")
            if (authorIconUrlFormat == "") authorIconUrlFormat = defaultPlayerIconUrl

            val authorIconUrl = PlaceholderFormatter.format(
                authorIconUrlFormat,
                mapOf(
                    "mcid" to event.player.username,
                    "uuid" to event.player.gameProfile.id.toString(),
                    "uuidNoDashes" to event.player.gameProfile.undashedId,
                    "textureId" to PlayerSkinTextureIdResolver(event.player).textureId
                )
            )

            val authorNameFormat = connectionServerChangeMessagesTable.getString("author-name")

            val server = plugin.getServer()
            val authorName = PlaceholderFormatter.format(
                authorNameFormat,
                mapOf(
                    "playerName" to event.player.username,
                    "previousServer" to previousServer.serverInfo.name,
                    "nextServer" to event.server.serverInfo.name
                )
            )
            // playerName, previousServer, nextServer

            val embedColor = connectionServerChangeMessagesTable.getString("color")


            val embed = EmbedBuilder()
                .setColor(ColorCodeToColor(embedColor).color)
                .setAuthor(
                    authorName,
                    null,
                    authorIconUrl
                )
                .build()
            connectionServerChangeChannel.sendMessageEmbeds(embed).queue()
        }
    }
}