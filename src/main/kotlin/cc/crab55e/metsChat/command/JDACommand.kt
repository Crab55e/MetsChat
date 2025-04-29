package cc.crab55e.metsChat.command

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.PrefixedMessageBuilder
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource

class JDACommand(
    private val plugin: MetsChat
) {
    private val discordClient = plugin.getDiscordClient()
    fun handleJDA(context: CommandContext<CommandSource?>): Int {

        val jdaStatus = discordClient?.status
        val jdaStatusName = jdaStatus?.name ?: "access unavailable to status object."
        val jdaLatency = discordClient?.gatewayPing.toString()
        val stringMessage = """
            MetsChat / JDA
            Status: $jdaStatusName,
            Latency: $jdaLatency
        """.trimIndent()
        val message = PrefixedMessageBuilder().make(plugin, stringMessage)
        context.source?.sendMessage(message)
        return Command.SINGLE_SUCCESS
    }
    fun handleReconnect(context: CommandContext<CommandSource?>): Int {
        val message = PrefixedMessageBuilder().make(plugin, "Unavailable feature.")
        context.source?.sendMessage(message)
        return Command.SINGLE_SUCCESS
    }
}
