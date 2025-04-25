package cc.crab55e.metsChat.util

import cc.crab55e.metsChat.MetsChat
import com.google.gson.Gson
import com.google.gson.internal.bind.util.ISO8601Utils
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.components.Component
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.util.*

class WebhookWrapper(var discordClient: JDA, var webhookURL: String, var plugin: MetsChat) {
    fun send(message: Message): Response {
        val client = OkHttpClient()
        val messageMap = parseMessage(message)
        val messageJson = Gson().toJson(messageMap)
        plugin.getLogger().info(messageJson)
        val body = messageJson.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(webhookURL)
            .post(body)
            .build()

        client.newCall(request).execute().use { res ->
            return res
        }
    }
    fun parseMessage(m: Message): Map<String, Any?> {
        val message = mutableMapOf<String, Any?>()

        // TODO: これを良い感じにする、んでもってjsonにしてsend内でpostするぜ
        m.content?.let { message["content"] = it }
        m.username?.let { message["username"] = it }
        m.avatarURL?.let { message["avatar_url"] = it }
        m.tts?.let { message["tts"] = it }
        if (!m.embeds.isNullOrEmpty()) {
            message["embeds"] = m.embeds.map { embed ->
                mutableMapOf<String, Any?>().apply {
                    embed.title?.let { title -> this["title"] = title }
                    this["type"] = "rich" // webhookのembedは必ずrichらしい
                    embed.description?.let {desc -> this["description"] = desc}
                    embed.url?.let {url -> this["url"] = url}
                    embed.timestamp?.let {timestamp -> this["timestamp"] = ISO8601Utils.format(Date.from(timestamp.toInstant()))}
                    embed.color?.let {color -> this["color"] = color.colorSpace.numComponents}
                }
            }
        }
        return message
    }
}

class Message(
    val content: String? = null,
    val username: String? = null,
    val avatarURL: String? = null,
    val tts: Boolean? = null,
    val embeds: Array<MessageEmbed>? = null,
    val allowedMentions: Array<AllowedMention>? = null,
    val components: Array<Component>? = null, // files and payload_json is not supported.
    val attachments: Array<Attachment>? = null,
    val flags: Int? = null,
    val threadName: String? = null,
    val appliedTags: Array<Int>? = null,
    val poll: Poll? = null
) {
    init {
        require(listOf(content, embeds, components, poll).any { it != null }) {
            throw IllegalArgumentException("Messageの作成時は、content, embeds, stickerIds, components, pollから1つ以上の引数を指定しなければなりません")
        }
    }
}

class AllowedMention (
    val parse: String? = null,
    val roles: Array<Int>? = null,
    val users: Array<Int>? = null,
    val repliedUser: Boolean? = null
) {
    val allowedMentionTypes = listOf("roles", "users", "everyone")
    init {
        if (!allowedMentionTypes.contains(parse)) throw IllegalArgumentException("parse引数はroles users everyoneのいずれか1つでなければなりません")
    }
}
class Poll(
    val question: PollMedia,
    val answers: Array<PollAnswer>,
    val duration: Int? = null,
    val allowMultiSelect: Boolean? = null,
    val layoutType: Int? = null,
)
class PollMedia(
    val text: String,
    val emoji: Emoji? = null,
)
class PollAnswer(
    val answerId: Int,
    val pollMedia: PollMedia
)