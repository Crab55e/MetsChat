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
import java.util.*


class MessageReceived(private val plugin: MetsChat) : ListenerAdapter() {
    private val logger = plugin.getLogger()
    private val mm = MiniMessage.miniMessage()
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return
        // 自身のwebhook or jda.selfUserだったらreturn的な条件でもよかったかもしれない

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
                "discordMessageUrl" to event.jumpUrl
            )
        )

        // 4. tokenで分割 → 前後を Component 化
        val parts = formatted.split(placeholderToken, limit = 2)
        val header = Component.text(parts[0])
        val tail = if (parts.size > 1) Component.text(parts[1]) else Component.empty()

        // 5. 最後に Component を合体
        val minecraftMessage = header.append(messageComponent).append(tail)



        plugin.getServer().allPlayers.forEach playerLoop@{
            val receiver = it
            receiver.sendMessage(minecraftMessage)
        }

    }
}