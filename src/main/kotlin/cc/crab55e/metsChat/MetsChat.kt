package cc.crab55e.metsChat

import cc.crab55e.metsChat.command.BaseBrigadierCommand.createBrigadierCommand
import cc.crab55e.metsChat.discord.MessageReceived
import com.google.inject.Inject
import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.kyori.adventure.text.minimessage.MiniMessage
import okhttp3.internal.notify
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
            val config = getConfigManager().getConfig()
            val minecraftTable = config.getTable("minecraft")
            val crossServerMessageFormat = minecraftTable.getString("cross-server-message-format")

            if (receiverServerName == senderServerName) return@playerLoop
            receiver.sendMessage(
                mm.deserialize(
                    String.format(
                        crossServerMessageFormat,
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

        val commandMeta = commandManager.metaBuilder("metschat")
            .aliases("mchat")
            .plugin(this)
            .build()

        val commandToRegister = createBrigadierCommand(this)

        commandManager.register(commandMeta, commandToRegister)

        val botToken: String
        val discordBotTokenTable = getConfigManager().getConfig().getTable("discord.bot-token")
        val discordBotTokenType = discordBotTokenTable.getString("type")
        val discordBotTokenValue = discordBotTokenTable.getString("value")
        if (discordBotTokenType == "system-environ" ) {
            botToken = System.getenv(discordBotTokenValue)
        } else if (discordBotTokenType == "raw-string") {
            botToken = discordBotTokenValue
        } else {
            logger.error("$discordBotTokenType is Invalid bot token type in ${getConfigManager().getConfigFileName()}")
            this.server.shutdown()
            return
        }

        val discordAPI = JDABuilder.createDefault(
            botToken,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MEMBERS
        ).build()
        discordAPI.addEventListener(MessageReceived(this))

        logger.info("Initialized.")
    }
}
