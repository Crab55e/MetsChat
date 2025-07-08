package cc.crab55e.metsChat

import cc.crab55e.metsChat.command.MetsChatCommand
import cc.crab55e.metsChat.discord.ButtonInteraction
import cc.crab55e.metsChat.discord.MessageReceived
import cc.crab55e.metsChat.event.*
import cc.crab55e.metsChat.gateway.BackendMessage
import cc.crab55e.metsChat.gateway.BackendSupportServer
import cc.crab55e.metsChat.gateway.HeartbeatTask
import cc.crab55e.metsChat.gateway.HeartbeatTracker
import cc.crab55e.metsChat.gateway.event.Timeout
import cc.crab55e.metsChat.util.*

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
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder

import org.slf4j.Logger
import java.nio.file.Path
import java.util.concurrent.TimeUnit


@Plugin(
    id = "metschat", name = "MetsChat", version = BuildConstants.VERSION
)

class MetsChat @Inject constructor(
    private val logger: Logger, private val server: ProxyServer, @DataDirectory private val dataDirectory: Path
) {
    private var discordClient: JDA? = null
    private val configManager = ConfigManager(this, dataDirectory)
    private val messageConfigManager = MessageConfigManager(this, dataDirectory)
    private val backendSupportConfigManager = BackendSupportConfigManager(this, dataDirectory)
    private val heartbeatTracker = HeartbeatTracker(this)
    private val heartbeatTimeoutEvent = Timeout(this)

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

    fun getBackendSupportConfigManager(): BackendSupportConfigManager {
        return backendSupportConfigManager
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

    fun getHeartbeatTracker(): HeartbeatTracker {
        return heartbeatTracker
    }

    fun getHeartbeatTimeoutEvent(): Timeout {
        return heartbeatTimeoutEvent
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        logger.info("Initializing...")

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
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_EXPRESSIONS,
            GatewayIntent.SCHEDULED_EVENTS
        )
            .addEventListeners(
                MessageReceived(this),
                ButtonInteraction(this)
            )
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .build()

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

                val runtime = Runtime.getRuntime()
                val mb = 1024 * 1024
                val placeholders = mapOf(
                    "proxyVersion" to server.version.version,
                    "maxRamMB" to (runtime.maxMemory() / mb).toString(),
                    "usedRamMB" to ((runtime.totalMemory() - runtime.freeMemory()) / mb).toString(),
                    "discordLatency" to discordClient!!.gatewayPing.toString()
                )

                val formattedStrings = listOf("title", "desc", "content").associateWith { key ->
                    PlaceholderFormatter.format(initializeNotifyMessagesTable.getString(key), placeholders)
                }

                val embed = EmbedBuilder().setTitle(formattedStrings["title"]).setDescription(formattedStrings["desc"])
                    .setColor(ColorCodeToColor(initializeNotifyMessagesTable.getString("color")).color).build()

                val message = MessageCreateBuilder().addEmbeds(embed).setContent(formattedStrings["content"]).build()

                initializeNotifyChannel.sendMessage(message).queue()
            } else logger.warn("failed to get the initialize notify channel")
        } else logger.info("disabled initialize notify to discord.")

        val eventManager = server.eventManager
        eventManager.register(this, ChatEventListener(this))
        eventManager.register(this, PlayerJoin(this))
        eventManager.register(this, PlayerLeave(this))
        eventManager.register(this, PlayerServerChange(this))

        val commandManager = server.commandManager
        val commandMeta = commandManager.metaBuilder("metschat").aliases("mchat").plugin(this).build()

        commandManager.register(commandMeta, MetsChatCommand.create(this))

        val backendSupportConfig = backendSupportConfigManager.get()
        val backendSupportGeneralTableKey = "general"
        val backendSupportGeneralTable = backendSupportConfig.getTable(backendSupportGeneralTableKey)
        if (backendSupportGeneralTable.getBoolean("enabled")) {

            val backendSupportServerTableKey = "general.server-setting"
            val backendSupportServerTable = backendSupportConfig.getTable(backendSupportServerTableKey)
            val backendSupportServerPort = backendSupportServerTable.getLong("port")

            val backendSupportServer = BackendSupportServer(
                this,
                backendSupportServerPort.toInt(),
                BackendMessage(this)
            )
            backendSupportServer.start()
            logger.info("BackendSupport Server listening on $backendSupportServerPort")

            val gatewayTimeoutCheckInterval = backendSupportConfig.getTable("gateway.timeout").getLong("check-interval")

            server.scheduler.buildTask(this, HeartbeatTask(this))
                .repeat(gatewayTimeoutCheckInterval, TimeUnit.SECONDS)
                .schedule()
        } else logger.info("backend support is disabled.")
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
                val embed = EmbedBuilder().setTitle(shutdownNotifyMessagesTable.getString("title"))
                    .setDescription(shutdownNotifyMessagesTable.getString("desc"))
                    .setColor(ColorCodeToColor(shutdownNotifyMessagesTable.getString("color")).color).build()
                val message =
                    MessageCreateBuilder().addEmbeds(embed).setContent(shutdownNotifyMessagesTable.getString("content"))
                        .build()
                try {
                    shutdownNotifyChannel.sendMessage(message).complete()
                } catch (e: Exception) {
                    logger.error("Failed to sent shutdown-notify to discord: $e")
                }
            } else logger.warn("failed to get the shutdown notify channel")
        } else logger.info("disabled shutdown notify to discord.")

        discordClient?.shutdown()
        logger.info("Disabled.")

    }
}
