package cc.crab55e.metsChat.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.ColorCodeToColor
import cc.crab55e.metsChat.util.Message
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent
import com.velocitypowered.api.event.player.ServerConnectedEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

class PlayerJoin(
    private val plugin: MetsChat
) {
    private val logger = plugin.getLogger()
    private val config = plugin.getConfigManager().getConfig()
    @Subscribe
    fun onPlayerJoin(event: PlayerChooseInitialServerEvent) {
        val discordClient = plugin.getDiscordClient()
        discordClient?.awaitReady()
        if (discordClient == null) throw Exception("何故か、Null discordClient(字余り)")
        val player = event.player

        val channelIdsTable = config.getTable("discord.channel-ids")
        val playerJoinLeaveChannelId = channelIdsTable.getString("player-join-leave")

        val playerJoinLeaveChannel = discordClient.getChannelById(TextChannel::class.java, playerJoinLeaveChannelId)
        if (playerJoinLeaveChannel != null) {
            val embed = EmbedBuilder()
                .setColor(ColorCodeToColor("#44ff44").color)
                .setAuthor("${player.username} connecting to ${event.initialServer.get().serverInfo.name}",null, "https://かに.com/s/Crab55e.png")
                .build()
            playerJoinLeaveChannel.sendMessageEmbeds(embed).queue()
        }

    }
}