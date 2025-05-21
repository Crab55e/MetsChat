package cc.crab55e.metsChat.command

import cc.crab55e.metsChat.MetsChat
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource

object MetsChatCommand {
    fun create(plugin: MetsChat): BrigadierCommand {
        val baseCommand = BaseCommand(plugin)
        val reloadCommand = ReloadCommand(plugin)
        val jdaCommand = JDACommand(plugin)

        val root = LiteralArgumentBuilder.literal<CommandSource>("metschat")
            .requires { it.hasPermission("metschat.command.base") }
            .executes { baseCommand.handleBase(it) }
            .then(LiteralArgumentBuilder.literal<CommandSource>("reload")
                .requires { it.hasPermission("metschat.command.reload") }
                .executes { reloadCommand.handleReload(it) })
            .then(LiteralArgumentBuilder.literal<CommandSource>("jda")
                .requires { it.hasPermission("metschat.command.jda") }
                .executes { jdaCommand.handleJDA(it) }
                .then(LiteralArgumentBuilder.literal<CommandSource>("reconnect")
                    .requires {it.hasPermission("metschat.command.jda.reconnect")}
                    .executes {jdaCommand.handleReconnect(it)}
                )
                .then(LiteralArgumentBuilder.literal<CommandSource>("cancel-requests")
                    .requires {it.hasPermission("metschat.command.jda.cancel-requests")}
                    .executes {jdaCommand.handleCancelRequests(it)}
                )
            )

        return BrigadierCommand(root.build())
    }
}
