package cc.crab55e.metsChat

import cc.crab55e.metsChat.command.TestBrigadierCommand.createBrigadierCommand
import com.google.inject.Inject
import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.minimessage.MiniMessage
import org.slf4j.Logger
import java.nio.file.Path


@Plugin(
    id = "metschat", name = "MetsChat", version = BuildConstants.VERSION
)
class MetsChat @Inject constructor(
    private val logger: Logger,
    private val server: ProxyServer,
    @DataDirectory private val dataDirectory: Path
) {
    private val configManager = ConfigManager(this, dataDirectory)

    fun getLogger(): Logger {
        return logger
    }
    fun getServer(): ProxyServer {
        return server
    }
    fun getConfigManager(): ConfigManager {
        return configManager
    }
    fun getDataDirectory(): Path {
        return dataDirectory
    }
    fun getCommandManager(): CommandManager {
        return server.commandManager
    }

    @Subscribe
    fun onPlayerChat(event: PlayerChatEvent) {
        val sender = event.player
        val message = event.message

        val senderServerName = sender.currentServer.get().serverInfo.name

        val mm = MiniMessage.miniMessage()

        server.allPlayers.forEach playerLoop@{
            val receiver = it
            val receiverServerName = receiver.currentServer.get().serverInfo.name
            val crossServerMessageFormat = getConfigManager().getConfig().getString(
                "cross-server-message-format",
                "<hover:show_text:'from %s'>\\<<yellow>*</yellow>%s></hover> %s"
            )

            if (receiverServerName == senderServerName) return@playerLoop
            receiver.sendMessage(
                mm.deserialize(
                    String.format(
                        "<hover:show_text:'from %s'>\\<<yellow>*</yellow>%s></hover> %s",
                        senderServerName,
                        sender.username,
                        message
                    )
                )
            )
        }

    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        logger.info("Initializing")
        val commandManager: CommandManager = server.commandManager

        val commandMeta = commandManager.metaBuilder("test")
            .aliases("otherAlias", "anotherAlias")
            .plugin(this)
            .build()

        val commandToRegister = createBrigadierCommand(server)

        commandManager.register(commandMeta, commandToRegister)

        logger.info("Initialized.")
    }
}
