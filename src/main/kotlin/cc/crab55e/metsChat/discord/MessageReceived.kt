package cc.crab55e.metsChat.discord

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.PlaceholderFormatter
import com.moandjiezana.toml.Toml
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.kyori.adventure.text.minimessage.MiniMessage


class MessageReceived(private val plugin: MetsChat) : ListenerAdapter() {
    private val logger = plugin.getLogger()
    private val config = plugin.getConfigManager().getConfig()
    private val channelIdsTable = config.getTable("discord.channel-ids")
    private val toMinecraftTable = config.getTable("discord.message-share.to-minecraft")
    private val mm = MiniMessage.miniMessage()
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val message: Message = event.message
        val content: String = message.contentRaw

        val centralChatChannelId = channelIdsTable.getString("central-chat")
        val minecraftMessageFormat = toMinecraftTable.getString("format")

        if (message.channelId != centralChatChannelId) return
        if (event.author.isBot) return
        if (!toMinecraftTable.getBoolean("enabled")) return

        val markdownParsedMessage = "content"
        val roleColorHex = "ff2222"
        val allRoleNames = "admin, op, member"

        val minecraftMessage = mm.deserialize( PlaceholderFormatter.format(
            minecraftMessageFormat,
            mapOf(
                "authorName" to event.author.name,
                "markdownParsedMessage" to markdownParsedMessage,
                "roleColorHex" to roleColorHex,
                "allRoleNames" to allRoleNames,
                "discordMessageUrl" to event.jumpUrl
            )
        ))


        plugin.getServer().allPlayers.forEach playerLoop@{
            val receiver = it
            receiver.sendMessage(minecraftMessage)
        }

    }
}