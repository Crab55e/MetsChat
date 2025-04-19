package cc.crab55e.metsChat.command

import cc.crab55e.metsChat.MetsChat
import com.mojang.brigadier.arguments.StringArgumentType
import com.velocitypowered.api.command.BrigadierCommand

object BaseBrigadierCommand {
    fun createBrigadierCommand(plugin: MetsChat): BrigadierCommand {
        val baseCommand = BaseCommand(plugin)
        val reloadCommand = ReloadCommand(plugin)

        val mainNode = BrigadierCommand.literalArgumentBuilder("metschat")
            .requires { source -> source.hasPermission("metschat.command.metschat") }
            .executes { baseCommand.handleBase(it) }
            .then(
                BrigadierCommand.requiredArgumentBuilder("argument", StringArgumentType.word())
                    .suggests { _, builder ->
                        builder.suggest("reload")
                        builder.buildFuture()
                    }
                    .executes { reloadCommand.handleReload(it) }
            )
            .build()

        return BrigadierCommand(mainNode)
    }
}
