package cc.crab55e.metsChat.discord

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.PlaceholderFormatter
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import java.util.concurrent.TimeUnit

class SlashCommandInteraction(private val plugin: MetsChat) : ListenerAdapter() {
    private val logger = plugin.getLogger()
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val config = plugin.getConfigManager().get()
        val messagesConfig = plugin.getMessageConfigManager().get()

        if (event.name == "playerlist") {
            val playerlistTable = config.getTable("discord.commands.playerlist")

            val enabledPlayerlist = playerlistTable.getBoolean("enabled")
            if (enabledPlayerlist) {
                event.reply("この機能は無効化されています。").setEphemeral(true)
                return
            }

            val playerlistMessagesTable = messagesConfig.getTable("discord.commands.playerlist")

            val playerCount = plugin.getServer().playerCount

            val playerNamesSeparator = playerlistMessagesTable.getString("player-name-separator")

            val players = plugin.getServer().allPlayers
            var playerList = ""
            players.forEach {
                playerList += it.username + playerNamesSeparator
            }
            playerList = playerList.removeSuffix(playerNamesSeparator)

            val messageFormat = playerlistMessagesTable.getString("format")
            val message = PlaceholderFormatter.format(
                messageFormat,
                mapOf(
                    "currentPlayers" to playerCount.toString(),
                    "playerNames" to playerList
                )
            )
            val ephemeral = playerlistTable.getBoolean("send-ephemeral")
            val deleteAfter = playerlistTable.getLong("message-delete-after")

            event
                .reply(message)
                .setEphemeral(ephemeral)
                .queue {hook ->
                    if (deleteAfter >= 0) hook.deleteOriginal().queueAfter(deleteAfter, TimeUnit.MILLISECONDS)
                }
            return
        }
    }
}