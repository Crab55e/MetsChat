package cc.crab55e.metsChat.discord

import cc.crab55e.metsChat.MetsChat
import com.moandjiezana.toml.Toml
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter


class MessageReceived(val plugin: MetsChat) : ListenerAdapter() {
    private val logger = plugin.getLogger()
    private val config = plugin.getConfigManager().getConfig()
    private val configDiscordTable: Toml = config.getTable("discord")
    private val centralForumChannelID = configDiscordTable.getString("central-forum-channel-id")
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val message: Message = event.message
        val content: String = message.contentRaw

        logger.info("Fired MessageReceivedEvent ${content}@${message.channelId} type ${message.channelType}")

        if (message.channelId != centralForumChannelID || message.channelType != ChannelType.FORUM) return
        if (event.author.isBot) return

        logger.info("starting main processing")

    }
}