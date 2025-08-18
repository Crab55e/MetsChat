package cc.crab55e.metsChat.discord.command

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.PlaceholderFormatter
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.util.concurrent.TimeUnit

class Playerlist(private val plugin: MetsChat) {
    val config = plugin.getConfigManager().get()
    val messagesConfig = plugin.getMessageConfigManager().get()

    fun handler(event: SlashCommandInteractionEvent) {
        val playerlistTable = config.getTable("discord.commands.playerlist")

        val enabledPlayerlist = playerlistTable.getBoolean("enabled")
        if (!enabledPlayerlist) {
            val message = messagesConfig.getTable("discord.commands").getString("command-disabled-message")
            event.reply(message).setEphemeral(true).queue()
            return
        }

        val playerlistMessagesTable = messagesConfig.getTable("discord.commands.playerlist")

        val playerCount = plugin.getServer().playerCount

        val playerNamesSeparator = playerlistMessagesTable.getString("player-name-separator")

        val players = plugin.getServer().allPlayers

        if (players.isEmpty()) {
            val message = playerlistMessagesTable.getString("no-players")
            event.reply(message).setEphemeral(true).queue()
            return
        }

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