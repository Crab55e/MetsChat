package cc.crab55e.metsChat

import cc.crab55e.metsChat.command.MetsChatCommand
import cc.crab55e.metsChat.discord.MessageReceived
import cc.crab55e.metsChat.event.ChatEventListener
import cc.crab55e.metsChat.event.PlayerJoin
import cc.crab55e.metsChat.event.PlayerLeave
import cc.crab55e.metsChat.util.ColorCodeToColor
import cc.crab55e.metsChat.util.ConfigManager
import cc.crab55e.metsChat.util.MessageConfigManager

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
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder

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
    private val messageConfigManager = MessageConfigManager(this, dataDirectory)

    fun getLogger(): Logger {
        return logger
    }

    fun getServer(): ProxyServer {
        return server
    }

    fun getConfigManager(): ConfigManager {
        return configManager
    }

    fun getMessageConfigManager(): MessageConfigManager {
        return messageConfigManager
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
        val discordBotTokenTable = getConfigManager().get().getTable("discord.bot-token")
        val discordBotTokenType = discordBotTokenTable.getString("type")
        val discordBotTokenValue = discordBotTokenTable.getString("value")
        if (discordBotTokenType == "system-environ") {
            botToken = System.getenv(discordBotTokenValue)
        } else if (discordBotTokenType == "raw-string") {
            botToken = discordBotTokenValue
        } else {
            logger.error("$discordBotTokenType is invalid type")
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

        val config = configManager.get()
        val initializeNotifyTableId = "message-share.to-discord.boot-notify.on-initialize"
        val initializeNotifyTable = config.getTable(initializeNotifyTableId)

        if (initializeNotifyTable.getBoolean("enabled")) {
            val defaultChannelId = config.getTable("discord.general").getString("default-channel-id")
            var initializeNotifyChannelId = initializeNotifyTable.getString("channel-id")
            if (initializeNotifyChannelId == "") initializeNotifyChannelId = defaultChannelId

            val initializeNotifyChannel =
                discordClient!!.getChannelById(TextChannel::class.java, initializeNotifyChannelId)

            if (initializeNotifyChannel != null) {
                val messagesConfig = messageConfigManager.get()
                val initializeNotifyMessagesTable = messagesConfig.getTable(initializeNotifyTableId)
                val embed = EmbedBuilder()
                    .setTitle(initializeNotifyMessagesTable.getString("title"))
                    .setDescription(initializeNotifyMessagesTable.getString("desc"))
                    .setColor(ColorCodeToColor(initializeNotifyMessagesTable.getString("color")).color)
                    .build()
                val message = MessageCreateBuilder()
                    .addEmbeds(embed)
                    .setContent(initializeNotifyMessagesTable.getString("content"))
                    .build()
                initializeNotifyChannel.sendMessage(message)
            }
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
        val config = configManager.get()
        val shutdownNotifyTableId = "message-share.to-discord.boot-notify.on-shutdown"
        val shutdownNotifyTable = config.getTable(shutdownNotifyTableId)

        if (shutdownNotifyTable.getBoolean("enabled")) {

            val defaultChannelId = config.getTable("discord.general").getString("default-channel-id")
            var shutdownNotifyChannelId = shutdownNotifyTable.getString("channel-id")
            if (shutdownNotifyChannelId == "") shutdownNotifyChannelId = defaultChannelId

            val shutdownNotifyChannel = discordClient!!.getChannelById(TextChannel::class.java, shutdownNotifyChannelId)

            if (shutdownNotifyChannel != null) {
                val messagesConfig = messageConfigManager.get()
                val shutdownNotifyMessagesTable = messagesConfig.getTable(shutdownNotifyTableId)
                val embed = EmbedBuilder()
                    .setTitle(shutdownNotifyMessagesTable.getString("title"))
                    .setDescription(shutdownNotifyMessagesTable.getString("desc"))
                    .setColor(ColorCodeToColor(shutdownNotifyMessagesTable.getString("color")).color)
                    .build()
                val message = MessageCreateBuilder()
                    .addEmbeds(embed)
                    .setContent(shutdownNotifyMessagesTable.getString("content"))
                    .build()
                shutdownNotifyChannel.sendMessage(message)
            }
        }

        discordClient?.shutdown()

    }
}
