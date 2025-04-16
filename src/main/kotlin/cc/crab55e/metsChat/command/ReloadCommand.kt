package cc.crab55e.metsChat.command

import cc.crab55e.metsChat.MetsChat
import com.mojang.brigadier.Command
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component

class ReloadCommand(
    private val plugin: MetsChat
) {
    fun handleReload(context: CommandContext<CommandSource?>): Int {
        val arg = context.getArgument("argument", String::class.java)

        if (arg.equals("reload", ignoreCase = true)) {
            plugin.getConfigManager().reloadConfig()
            context.source?.sendMessage(Component.text("Reloaded config!"))
        } else {
            plugin.getServer().getPlayer(arg).ifPresent { player ->
                player.sendMessage(Component.text("Hello!"))
            }
        }
        return Command.SINGLE_SUCCESS
    }
}
