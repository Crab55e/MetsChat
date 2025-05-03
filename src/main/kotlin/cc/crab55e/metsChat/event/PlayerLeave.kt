package cc.crab55e.metsChat.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.ColorCodeToColor
import cc.crab55e.metsChat.util.PlaceholderFormatter
import cc.crab55e.metsChat.util.PlayerSkinTextureIdResolver
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

class PlayerLeave(
    private val plugin: MetsChat
) {
    private val logger = plugin.getLogger()
    @Subscribe
    fun onPlayerLeave(event: DisconnectEvent) {
        val config = plugin.getConfigManager().get()
        val messagesConfig = plugin.getMessageConfigManager().get()

        val joinLeavesLeaveTableKey = "message-share.to-discord.join-leaves.leave"
        val joinLeavesLeaveTable = config.getTable(joinLeavesLeaveTableKey)

        if (!joinLeavesLeaveTable.getBoolean("enable")) return

        val discordClient = plugin.getDiscordClient()
        discordClient!!.awaitReady()

        val defaultChannelId = config.getTable("discord.general").getString("default-channel-id")
        var playerLeaveChannelId = joinLeavesLeaveTable.getString("channel-id")
        if (playerLeaveChannelId == "") playerLeaveChannelId = defaultChannelId

        val playerLeaveChannel = discordClient.getChannelById(TextChannel::class.java, playerLeaveChannelId)
        if (playerLeaveChannel != null) {
            val joinLeavesLeaveMessagesTable = messagesConfig.getTable(joinLeavesLeaveTableKey)

            val defaultPlayerIconUrl = messagesConfig.getTable("discord.general").getString("default-player-icon-url")
            var authorIconUrlFormat = joinLeavesLeaveMessagesTable.getString("author-icon-url")
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

            val authorNameFormat = joinLeavesLeaveMessagesTable.getString("author-name")

            val server = plugin.getServer()
            val authorName = PlaceholderFormatter.format(
                authorNameFormat,
                mapOf(
                    "playerName" to event.player.username,
                    "playersCount" to server.playerCount.toString(),
                    "maxPlayers" to server.configuration.showMaxPlayers.toString()
                )
            )

            val embedColor = joinLeavesLeaveMessagesTable.getString("color")

            val embed = EmbedBuilder()
                .setColor(ColorCodeToColor(embedColor).color)
                .setAuthor(
                    authorName,
                    null,
                    authorIconUrl
                )
                .build()
            playerLeaveChannel.sendMessageEmbeds(embed).queue()
        }

    }
}