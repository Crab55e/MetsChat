package cc.crab55e.metsChat.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.ColorCodeToColor
import cc.crab55e.metsChat.util.PlaceholderFormatter
import cc.crab55e.metsChat.util.PlayerSkinTextureIdResolver
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import net.dv8tion.jda.api.EmbedBuilder

class PlayerLeave(
    private val plugin: MetsChat
) {
    private val logger = plugin.getLogger()
    @Subscribe
    fun onPlayerLeave(event: DisconnectEvent) {
        val config = plugin.getConfigManager().get()
        val messagesConfig = plugin.getMessageConfigManager().get()

        val connectionLeaveTableKey = "message-share.to-discord.connection.leave"
        val connectionLeaveTable = config.getTable(connectionLeaveTableKey)

        if (!connectionLeaveTable.getBoolean("enabled")) return

        val discordClient = plugin.getDiscordClient()
        discordClient!!.awaitReady()

        val defaultChannelId = config.getTable("discord.general").getString("default-channel-id")
        var connectionLeaveChannelId = connectionLeaveTable.getString("channel-id")
        if (connectionLeaveChannelId == "") connectionLeaveChannelId = defaultChannelId

        val connectionLeaveChannel = discordClient.getTextChannelById(connectionLeaveChannelId)
        if (connectionLeaveChannel != null) {
            val connectionLeaveMessagesTable = messagesConfig.getTable(connectionLeaveTableKey)

            val defaultPlayerIconUrl = messagesConfig.getTable("discord.general").getString("default-player-icon-url")
            var authorIconUrlFormat = connectionLeaveMessagesTable.getString("author-icon-url")
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

            val authorNameFormat = connectionLeaveMessagesTable.getString("author-name")

            val server = plugin.getServer()
            val authorName = PlaceholderFormatter.format(
                authorNameFormat,
                mapOf(
                    "playerName" to event.player.username,
                    "playersCount" to server.playerCount.toString(),
                    "maxPlayers" to server.configuration.showMaxPlayers.toString()
                )
            )

            val embedColor = connectionLeaveMessagesTable.getString("color")

            val embed = EmbedBuilder()
                .setColor(ColorCodeToColor(embedColor).color)
                .setAuthor(
                    authorName,
                    null,
                    authorIconUrl
                )
                .build()
            connectionLeaveChannel.sendMessageEmbeds(embed).queue()
        }

    }
}