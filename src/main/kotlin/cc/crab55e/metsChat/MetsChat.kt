package cc.crab55e.metsChat

import cc.crab55e.metsChat.command.MetsChatCommand
import cc.crab55e.metsChat.discord.MessageReceived
import cc.crab55e.metsChat.event.ChatEventListener
import cc.crab55e.metsChat.event.PlayerJoin
import cc.crab55e.metsChat.event.PlayerLeave
import cc.crab55e.metsChat.util.ColorCodeToColor

import com.google.inject.Inject
import com.velocitypowered.api.command.CommandManager
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
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
    private var discordClient: JDA? = null
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
    fun getDiscordClient(): JDA? {
        discordClient?.awaitReady()
        return discordClient
    }

    fun getDataDirectory(): Path {
        return dataDirectory
    }

    fun getCommandManager(): CommandManager {
        return server.commandManager
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        logger.info("Initializing")

        val botToken: String
        val discordBotTokenTable = getConfigManager().getConfig().getTable("discord.bot-token")
        val discordBotTokenType = discordBotTokenTable.getString("type")
        val discordBotTokenValue = discordBotTokenTable.getString("value")
        if (discordBotTokenType == "system-environ") {
            botToken = System.getenv(discordBotTokenValue)
        } else if (discordBotTokenType == "raw-string") {
            botToken = discordBotTokenValue
        } else {
            logger.error("$discordBotTokenType is Invalid bot token type in ${getConfigManager().getConfigFileName()}")
            this.server.shutdown()
            return
        }

        discordClient = JDABuilder.createDefault(
            botToken,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MEMBERS
        ).addEventListeners(
            MessageReceived(this)
        ).build()

        discordClient!!.awaitReady()

        val config = configManager.getConfig()
        val channelIdsTable = config.getTable("discord.channel-ids")
        val bootMessageChannelId = channelIdsTable.getString("boot-message")

        val bootMessageChannel = discordClient!!.getChannelById(TextChannel::class.java, bootMessageChannelId)

        if (bootMessageChannel != null) {
            val bootMessagesTable = config.getTable("discord.message-share.to-discord.boot-message")
            val embed = EmbedBuilder()
                .setColor(ColorCodeToColor(bootMessagesTable.getString("initialize-color")).color)
                .setTitle(bootMessagesTable.getString("initialize")) // FIXME: なぜかここら辺一体呼び出されない。
                .build()

            bootMessageChannel.sendMessageEmbeds(embed).queue()
        }

        val eventManager = server.eventManager
        eventManager.register(this, ChatEventListener(this))
        eventManager.register(this, PlayerJoin(this))
        eventManager.register(this, PlayerLeave(this))

        val commandManager = server.commandManager
        val commandMeta = commandManager.metaBuilder("metschat")
            .aliases("mchat")
            .plugin(this)
            .build()

        commandManager.register(commandMeta, MetsChatCommand.create(this))

        logger.info("Initialized.")
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        logger.info("Disabling...")
        val config = configManager.getConfig()
        val channelIdsTable = config.getTable("discord.channel-ids")
        val bootMessageChannelId = channelIdsTable.getString("boot-message")
        // FIXME: なぜかほぼ確で送られてこん
        discordClient!!.awaitReady()
        logger.info("JDA STATS: ${discordClient!!.status}")
        logger.info("JDA: $discordClient")
        val bootMessageChannel = discordClient!!.getChannelById(TextChannel::class.java, bootMessageChannelId)

        if (bootMessageChannel != null) {
            val bootMessagesTable = config.getTable("discord.message-share.to-discord.boot-message")
            val embed = EmbedBuilder()
                .setColor(ColorCodeToColor(bootMessagesTable.getString("shutdown-color")).color)
                .setTitle(bootMessagesTable.getString("shutdown"))
                .build()
            logger.info(embed.toString())

            bootMessageChannel.sendMessageEmbeds(embed).queue()
        }

        discordClient?.shutdown()

    }
}
