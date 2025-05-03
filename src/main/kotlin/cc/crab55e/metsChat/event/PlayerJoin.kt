package cc.crab55e.metsChat.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.ColorCodeToColor
import cc.crab55e.metsChat.util.PlaceholderFormatter
import cc.crab55e.metsChat.util.PlayerSkinTextureIdResolver
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

class PlayerJoin(
    private val plugin: MetsChat
) {
    private val logger = plugin.getLogger()
    @Subscribe
    fun onPlayerJoin(event: LoginEvent) {
        val config = plugin.getConfigManager().get()
        val messagesConfig = plugin.getMessageConfigManager().get()

        val joinLeavesJoinTableKey = "message-share.to-discord.join-leaves.join"
        val joinLeavesJoinTable = config.getTable(joinLeavesJoinTableKey)

        if (!joinLeavesJoinTable.getBoolean("enable")) return

        val discordClient = plugin.getDiscordClient()
        discordClient!!.awaitReady()
        val player = event.player

        val defaultChannelId = config.getTable("discord.general").getString("default-channel-id")
        var playerJoinChannelId = joinLeavesJoinTable.getString("channel-id")
        if (playerJoinChannelId == "") playerJoinChannelId = defaultChannelId

        val playerJoinChannel = discordClient.getChannelById(TextChannel::class.java, playerJoinChannelId)
        if (playerJoinChannel != null) {
            val joinLeavesJoinMessagesTable = messagesConfig.getTable(joinLeavesJoinTableKey)

            val defaultPlayerIconUrl = messagesConfig.getTable("discord.general").getString("default-player-icon-url")
            var authorIconUrlFormat = joinLeavesJoinMessagesTable.getString("author-icon-url")
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

            val authorNameFormat = joinLeavesJoinMessagesTable.getString("author-name")

            val server = plugin.getServer()
            val authorName = PlaceholderFormatter.format(
                authorNameFormat,
                mapOf(
                    "playerName" to event.player.username,
                    "playersCount" to server.playerCount.toString(),
                    "maxPlayers" to server.configuration.showMaxPlayers.toString()
                )
            )

            val embedColor = joinLeavesJoinMessagesTable.getString("color")

            val embed = EmbedBuilder()
                .setColor(ColorCodeToColor(embedColor).color)
                .setAuthor(
                    authorName,
                    null,
                    authorIconUrl
                )
                .build()
            playerJoinChannel.sendMessageEmbeds(embed).queue()
        }

    }
}