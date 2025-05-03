package cc.crab55e.metsChat.discord

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.MarkdownParser
import cc.crab55e.metsChat.util.PlaceholderFormatter
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.kyori.adventure.text.minimessage.MiniMessage
import okhttp3.internal.toHexString


class MessageReceived(private val plugin: MetsChat) : ListenerAdapter() {
    private val logger = plugin.getLogger()
    private val mm = MiniMessage.miniMessage()
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val config = plugin.getConfigManager().get()
        val messagesConfig = plugin.getMessageConfigManager().get()

        val fromDiscordTableKey = "message-share.from-discord"
        val fromDiscordTable = config.getTable(fromDiscordTableKey)
        if (!fromDiscordTable.getBoolean("enabled")) return

        val discordGeneralTableKey = "discord.general"
        val discordGeneralTable = config.getTable(discordGeneralTableKey)

        val defaultChannelId = discordGeneralTable.getString("default-channel-id")
        var validChannelId = fromDiscordTable.getString("channel-id")
        if (validChannelId == "") validChannelId = defaultChannelId

        val validChannelIds = fromDiscordTable.getList<String>("included-channel-ids")
        validChannelIds.add(validChannelId)

        if (!validChannelIds.contains(event.channel.id)) return

        val message: Message = event.message
        // TODO: メッセージがreplyだったら

        val fromDiscordMessagesTable = messagesConfig.getTable(fromDiscordTableKey)

        val parseMarkdown = fromDiscordMessagesTable.getBoolean("parse-markdown")
        val minecraftMessageContent: String
        if (parseMarkdown) {
            minecraftMessageContent = MarkdownParser.discordToMiniMessage(message.contentRaw)
        } else {
            minecraftMessageContent = message.contentDisplay
        }
        var roleColorHex = event.member?.color?.rgb?.toHexString() ?: "ffffffff" // 下2桁は下で消えるのでok
        roleColorHex = roleColorHex.drop(2)
        val allRoleNamesSeparator = fromDiscordMessagesTable.getString("all-role-names-separator")
        var allRoleNames = ""
        event.member?.roles?.forEach{allRoleNames += it.name + allRoleNamesSeparator}
        allRoleNames = allRoleNames.removeSuffix(allRoleNamesSeparator)


        val minecraftMessageFormat = fromDiscordMessagesTable.getString("format")
        val minecraftMessage = mm.deserialize( PlaceholderFormatter.format(
            minecraftMessageFormat,
            mapOf(
                "authorName" to event.author.effectiveName,
                "message" to minecraftMessageContent,
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