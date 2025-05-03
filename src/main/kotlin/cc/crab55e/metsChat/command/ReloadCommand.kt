package cc.crab55e.metsChat.command

import cc.crab55e.metsChat.BuildConstants
import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.PlaceholderFormatter
import cc.crab55e.metsChat.util.PrefixedMessageBuilder
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource

class ReloadCommand(
    private val plugin: MetsChat
) {
    fun handleReload(context: CommandContext<CommandSource?>): Int {
        plugin.getConfigManager().reload()
        plugin.getMessageConfigManager().reload()

        val messagesConfig = plugin.getMessageConfigManager().get()
        val messageFormat = messagesConfig.getTable("command.reload").getString("format")
        val message = PrefixedMessageBuilder().make(
            plugin,
            messageFormat
        )

        context.source?.sendMessage(message)
        return Command.SINGLE_SUCCESS
    }
}
