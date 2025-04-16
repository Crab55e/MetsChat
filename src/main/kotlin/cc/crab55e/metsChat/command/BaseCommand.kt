package cc.crab55e.metsChat.command

import cc.crab55e.metsChat.BuildConstants
import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.PrefixedMessageBuilder
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource

class BaseCommand(
    private val plugin: MetsChat
) {
    fun handleBase(context: CommandContext<CommandSource>): Int {
        val source = context.source
        val message = PrefixedMessageBuilder().make(plugin, "Running MetsChat ${BuildConstants.VERSION}")
        source.sendMessage(message)
        return Command.SINGLE_SUCCESS
    }
}
