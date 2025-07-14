package cc.crab55e.metsChat.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.ColorCodeToColor
import cc.crab55e.metsChat.util.PlaceholderFormatter
import cc.crab55e.metsChat.util.PlayerSkinTextureIdResolver
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerConnectedEvent
import net.dv8tion.jda.api.EmbedBuilder

class PlayerJoin(
    private val plugin: MetsChat
) {
    private val logger = plugin.getLogger()
    @Subscribe
    fun onPlayerJoin(event: ServerConnectedEvent) {

        if (event.previousServer.isPresent) return
        // PlayerServerChangeに任せるぜ！

        // TODO: サーバー全体で共有するのを忘れている

        val config = plugin.getConfigManager().get()
        val messagesConfig = plugin.getMessageConfigManager().get()

        val connectionJoinTableKey = "message-share.to-discord.connection.join"
        val connectionJoinTable = config.getTable(connectionJoinTableKey)

        if (!connectionJoinTable.getBoolean("enabled")) return

        val discordClient = plugin.getDiscordClient()
        discordClient!!.awaitReady()

        val defaultChannelId = config.getTable("discord.general").getString("default-channel-id")
        var connectionJoinChannelId = connectionJoinTable.getString("channel-id")
        if (connectionJoinChannelId == "") connectionJoinChannelId = defaultChannelId

        val connectionJoinChannel = discordClient.getTextChannelById(connectionJoinChannelId)
        if (connectionJoinChannel != null) {
            val connectionJoinMessagesTable = messagesConfig.getTable(connectionJoinTableKey)

            val defaultPlayerIconUrl = messagesConfig.getTable("discord.general").getString("default-player-icon-url")
            var authorIconUrlFormat = connectionJoinMessagesTable.getString("author-icon-url")
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

            val authorNameFormat = connectionJoinMessagesTable.getString("author-name")

            val server = plugin.getServer()
            val authorName = PlaceholderFormatter.format(
                authorNameFormat,
                mapOf(
                    "playerName" to event.player.username,
                    "serverName" to event.server.serverInfo.name,
                    "playersCount" to server.playerCount.toString(),
                    "maxPlayers" to server.configuration.showMaxPlayers.toString()
                )
            )

            val embedColor = connectionJoinMessagesTable.getString("color")

            val embed = EmbedBuilder()
                .setColor(ColorCodeToColor(embedColor).color)
                .setAuthor(
                    authorName,
                    null,
                    authorIconUrl
                )
                .build()
            connectionJoinChannel.sendMessageEmbeds(embed).queue()
        } else logger.warn("failed to get player join channel.")
    }
}