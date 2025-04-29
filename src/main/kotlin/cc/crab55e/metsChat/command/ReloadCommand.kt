package cc.crab55e.metsChat.command

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.PrefixedMessageBuilder
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource

class ReloadCommand(
    private val plugin: MetsChat
) {
    fun handleReload(context: CommandContext<CommandSource?>): Int {
        plugin.getConfigManager().reloadConfig()
        val configFileName = plugin.getConfigManager().getConfigFileName()
        val message = PrefixedMessageBuilder().make(plugin, "Reloaded $configFileName")
        context.source?.sendMessage(message)
        return Command.SINGLE_SUCCESS
    }
}
