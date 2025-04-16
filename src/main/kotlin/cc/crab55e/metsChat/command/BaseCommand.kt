package cc.crab55e.metsChat.command

import cc.crab55e.metsChat.BuildConstants
import cc.crab55e.metsChat.MetsChat
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.minimessage.MiniMessage

class BaseCommand(
    private val plugin: MetsChat
) {
    fun handleBase(context: CommandContext<CommandSource>): Int {
        val source = context.source
        val mm = MiniMessage.miniMessage()
        val prefix = plugin.getConfigManager().getConfig().getString(
            "plugin-message-prefix",
            "<green>[MetsChat]</green>"
        )
        val message = mm.deserialize(
            "$prefix Running MetsChat ${BuildConstants.VERSION}"
        )
        source.sendMessage(message)
        return Command.SINGLE_SUCCESS
    }
}
