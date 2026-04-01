package cc.crab55e.metsChat.discord

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.MarkdownParser
import cc.crab55e.metsChat.util.PlaceholderFormatter
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import okhttp3.internal.toHexString

class MessageReceived(private val plugin: MetsChat) : ListenerAdapter() {
    private val mm = MiniMessage.miniMessage()

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return // ボット自身のメッセージを無視

        val config = plugin.getConfigManager().get()
        val messagesConfig = plugin.getMessageConfigManager().get()

        val fromDiscordTableKey = "message-share.from-discord"
        val fromDiscordTable = config.getTable(fromDiscordTableKey) ?: return
        
        if (!fromDiscordTable.getBoolean("enabled", false)) return

        val discordGeneralTable = config.getTable("discord.general") ?: return
        val defaultChannelId = discordGeneralTable.getString("default-channel-id") ?: ""
        
        var validChannelId = fromDiscordTable.getString("channel-id") ?: ""
        if (validChannelId.isEmpty()) validChannelId = defaultChannelId

        val validChannelIds = fromDiscordTable.getList<String>("included-channel-ids") ?: mutableListOf()
        if (validChannelId.isNotEmpty()) {
            validChannelIds.add(validChannelId)
        }

        if (event.channel.id !in validChannelIds) return

        val message: Message = event.message
        val fromDiscordMessagesTable = messagesConfig.getTable(fromDiscordTableKey) ?: return

        val parseMarkdown = fromDiscordMessagesTable.getBoolean("parse-markdown", false)
        val minecraftMessageContent = if (parseMarkdown) {
            MarkdownParser.discordToMiniMessage(message.contentRaw)
        } else {
            message.contentDisplay
        }

        var roleColorHex = event.member?.color?.rgb?.toHexString() ?: "ffffffff"
        if (roleColorHex.length >= 2) roleColorHex = roleColorHex.drop(2)

        val allRoleNamesSeparator = fromDiscordMessagesTable.getString("all-role-names-separator") ?: ","
        val allRoleNames = event.member?.roles?.joinToString(allRoleNamesSeparator) { it.name } ?: ""

        val referencedMessage = event.message.referencedMessage
        val messageIsReply = referencedMessage != null
        val replyUserName = if (messageIsReply) referencedMessage?.author?.effectiveName ?: "" else ""
        val replyMessage = if (messageIsReply) referencedMessage?.contentRaw ?: "" else ""

        val minecraftMessageFormat = if (!messageIsReply) {
            fromDiscordMessagesTable.getString("format") ?: ""
        } else {
            fromDiscordMessagesTable.getString("reply-format") ?: ""
        }

        // 1. DiscordメッセージをComponent に変換
        val messageComponent = Component.text(minecraftMessageContent)

        // 2. {message} を一旦削ったフォーマットを用意
        val placeholderToken = "%%MESSAGE%%"
        val formatWithToken = minecraftMessageFormat.replace("{message}", placeholderToken)

        // 3. {authorName}, {roleColorHex} などの置換を実行
        val formatted = PlaceholderFormatter.format(
            formatWithToken,
            mapOf(
                "authorName" to event.author.effectiveName,
                "roleColorHex" to roleColorHex,
                "allRoleNames" to allRoleNames,
                "discordMessageUrl" to event.jumpUrl,
                "replyUserName" to replyUserName,
                "replyMessage" to replyMessage
            )
        )

        // 4. tokenで分割 → 前後を Component 化
        val parts = formatted.split(placeholderToken, limit = 2)
        val header = if (parts.isNotEmpty()) mm.deserialize(parts[0]) else Component.empty()
        val tail = if (parts.size > 1) mm.deserialize(parts[1]) else Component.empty()

        // 5. 最後に Component を合体
        val minecraftMessage = header.append(messageComponent).append(tail)

        plugin.getServer().allPlayers.forEach { player ->
            player.sendMessage(minecraftMessage)
        }
    }
}