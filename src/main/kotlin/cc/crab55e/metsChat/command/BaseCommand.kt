package cc.crab55e.metsChat.command

import cc.crab55e.metsChat.BuildConstants
import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.PlaceholderFormatter
import cc.crab55e.metsChat.util.PrefixedMessageBuilder
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource

class BaseCommand(
    private val plugin: MetsChat
) {
    fun handleBase(context: CommandContext<CommandSource>): Int {
        val messagesConfig = plugin.getMessageConfigManager().get()
        val messageFormat = messagesConfig.getTable("command.base").getString("format")
        val message = PrefixedMessageBuilder().make(
            plugin,
            PlaceholderFormatter.format(
                messageFormat,
                mapOf(
                    "pluginVersion" to BuildConstants.VERSION
                )
            )
        )

        val source = context.source
        source.sendMessage(message)
        return Command.SINGLE_SUCCESS
    }
}
