package cc.crab55e.metsChat

import cc.crab55e.metsChat.command.BaseBrigadierCommand
import cc.crab55e.metsChat.discord.MessageReceived
import cc.crab55e.metsChat.event.ChatEventListener

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

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

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        logger.info("Initializing")

        val eventManager = server.eventManager
        eventManager.register(this, ChatEventListener(this))

        val commandManager = server.commandManager
        val commandMeta = commandManager.metaBuilder("metschat")
            .aliases("mchat")
            .plugin(this)
            .build()

        commandManager.register(commandMeta, BaseBrigadierCommand.createBrigadierCommand(this))

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
