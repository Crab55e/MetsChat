package cc.crab55e.metsChat.command

import cc.crab55e.metsChat.BuildConstants
import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.PlaceholderFormatter
import cc.crab55e.metsChat.util.PrefixedMessageBuilder
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource

class JDACommand(
    private val plugin: MetsChat
) {
    private val discordClient = plugin.getDiscordClient()
    fun handleJDA(context: CommandContext<CommandSource?>): Int {
        val messagesConfig = plugin.getMessageConfigManager().get()
        val messageFormat = messagesConfig.getTable("command.jda").getString("format")

        val jdaStatus = discordClient?.status
        val jdaStatusName = jdaStatus?.name ?: "access unavailable to status object."
        val jdaLatency = discordClient?.gatewayPing.toString()

        val message = PrefixedMessageBuilder().make(
            plugin,
            PlaceholderFormatter.format(
                messageFormat,
                mapOf(
                    "discordClientStatus" to jdaStatusName,
                    "discordLatency" to jdaLatency
                )
            )
        )

        context.source?.sendMessage(message)
        return Command.SINGLE_SUCCESS
    }

    fun handleReconnect(context: CommandContext<CommandSource?>): Int {
        val messagesConfig = plugin.getMessageConfigManager().get()
        val messageFormat = messagesConfig.getTable("command.jda.reconnect").getString("format")
        val message = PrefixedMessageBuilder().make(
            plugin,
            messageFormat
        )
        context.source?.sendMessage(message)
        return Command.SINGLE_SUCCESS
    }

    fun handleCancelRequests(context: CommandContext<CommandSource?>): Int {
        val messagesConfig = plugin.getMessageConfigManager().get()

        val discordClient = plugin.getDiscordClient()

        if (discordClient == null) {
            val messageFormat = messagesConfig.getTable("command.jda.cancel-requests").getString("failed")
            val message = PrefixedMessageBuilder().make(
                plugin,
                messageFormat
            )
            context.source?.sendMessage(message)
            return Command.SINGLE_SUCCESS
        }

        val cancelled = discordClient.cancelRequests()

        val messageFormat = messagesConfig.getTable("command.jda.cancel-requests").getString("success")
        val message = PrefixedMessageBuilder().make(
            plugin,
            PlaceholderFormatter.format(
                messageFormat,
                mapOf(
                    "cancelledRequestCount" to cancelled.toString()
                )
            )
        )
        context.source?.sendMessage(message)
        return Command.SINGLE_SUCCESS
    }
}
