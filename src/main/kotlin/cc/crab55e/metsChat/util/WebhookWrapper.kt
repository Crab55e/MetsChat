package cc.crab55e.metsChat.util

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.Models.Snowflake
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

class WebhookWrapper(var webhookURL: String, var plugin: MetsChat) {
    fun send(message: String): Response {
        val client = OkHttpClient()
        val body = message.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(webhookURL)
            .post(body)
            .build()

        client.newCall(request).execute().use { res ->
            return res
        }

    }
    fun send(message: Message): Response {
        val client = OkHttpClient()
        val messageMap = parseMessage(message)
        val messageJson = Gson().toJson(messageMap)
        val body = messageJson.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(webhookURL)
            .post(body)
            .build()

        client.newCall(request).execute().use { res ->
            return res
        }
    }

    private fun parseMessage(m: Message): Map<String, Any?> {
        val message = mutableMapOf<String, Any?>()

        m.content?.let { message["content"] = it }
        m.username?.let { message["username"] = it }
        m.avatarURL?.let { message["avatar_url"] = it }
        m.tts?.let { message["tts"] = it }

        if (!m.embeds.isNullOrEmpty()) {
            message["embeds"] = m.embeds.map { embed ->
                val embedMap = mutableMapOf<String, Any?>()
                embed.title?.let { embedMap["title"] = it }
                embedMap["type"] = "rich"
                embed.description?.let { embedMap["description"] = it }
                embed.url?.let { embedMap["url"] = it }
                embed.timestamp?.let {
                    embedMap["timestamp"] = ISO8601Utils.format(Date.from(it.toInstant()))
                }
                embed.color?.let { embedMap["color"] = it.rgb and 0xFFFFFF }

                embed.footer?.let { footer ->
                    embedMap["footer"] = mapOf(
                        "text" to footer.text?.toString(),
                        "icon_url" to footer.iconUrl?.toString(),
                        "proxy_icon_url" to footer.proxyIconUrl?.toString()
                    )
                }

                embed.image?.let { image ->
                    embedMap["image"] = mapOf(
                        "url" to image.url?.toString(),
                        "proxy_url" to image.proxyUrl?.toString(),
                        "height" to image.height,
                        "width" to image.width
                    )
                }

                embed.thumbnail?.let { thumbnail ->
                    embedMap["thumbnail"] = mapOf(
                        "url" to thumbnail.url?.toString(),
                        "proxy_url" to thumbnail.proxyUrl?.toString(),
                        "height" to thumbnail.height,
                        "width" to thumbnail.width
                    )
                }

                embed.videoInfo?.let { video ->
                    embedMap["video"] = mapOf(
                        "url" to video.url?.toString(),
                        "proxy_url" to video.proxyUrl?.toString(),
                        "height" to video.height,
                        "width" to video.width
                    )
                }

                embed.siteProvider?.let { provider ->
                    embedMap["provider"] = mapOf(
                        "name" to provider.name?.toString(),
                        "url" to provider.url?.toString()
                    )
                }

                embed.author?.let { author ->
                    embedMap["author"] = mapOf(
                        "name" to author.name?.toString(),
                        "url" to author.url?.toString(),
                        "icon_url" to author.iconUrl?.toString(),
                        "proxy_icon_url" to author.proxyIconUrl?.toString()
                    )
                }

                if (!embed.fields.isNullOrEmpty()) {
                    embedMap["fields"] = embed.fields.map { field ->
                        val fieldMap = mutableMapOf<String, Any?>()
                        field.name?.let { fieldMap["name"] = it }
                        field.value?.let { fieldMap["value"] = it }
                        fieldMap["inline"] = field.isInline
                    }
                }

                embedMap
            }
        }

        if (m.allowedMentions != null) {
            message["allowed_mentions"] = mapOf(
                "parse" to m.allowedMentions.parse,
                "roles" to m.allowedMentions.roles?.map { it.toString() },
                "users" to m.allowedMentions.users?.map { it.toString() },
                "replied_user" to m.allowedMentions.repliedUser
            )
        }

        m.components?.let {
            message["components"] = arrayOf(
                mapOf(
                    "type" to 2,
                    "label" to "Componentsはめんどいのでサポートしていない、使いたくなったらそのうちサポートするよ",
                    "disabled" to true
                )
            )
        }

        if (!m.attachments.isNullOrEmpty()) {
            message["attachments"] = m.attachments.map { attachment ->
                val attachmentMap = mutableMapOf<String, Any?>()
                attachment.id.let { attachmentMap["id"] = it }
                attachment.fileName.let {
                    attachmentMap["filename"] = it
                    attachmentMap["title"] = it
                }
                attachment.description.let { attachmentMap["description"] = it }
                attachment.contentType.let { attachmentMap["content_type"] = it }
                attachment.size.let { attachmentMap["size"] = it }
                attachment.url.let { attachmentMap["url"] = it }
                attachment.proxyUrl.let { attachmentMap["proxy_url"] = it }
                attachment.height.let { attachmentMap["height"] = it }
                attachment.width.let { attachmentMap["width"] = it }
                attachment.isEphemeral.let { attachmentMap["ephemeral"] = it }
                attachment.duration.let { attachmentMap["duration_secs"] = it }
                attachment.waveform.let { attachmentMap["waveform"] = it }
                // FIXME: attachment flags はどうしたら読み取れるんだ？

            }
        }
        m.flags?.let { message["flags"] = it }
        m.threadName?.let { message["thread_name"] = it }
        m.appliedTags?.let { message["applied_tags"] = it.map { it.toString() } }
        if (m.poll != null) {
            message["poll"] = mapOf(
                "question" to mapOf(
                    "text" to m.poll.question.text,
                    "emoji" to mapOf(
                        "name" to m.poll.question.emoji?.name
                    )
                ),
                "answers" to m.poll.answers.map { answer ->
                    mapOf(
                        "answer_id" to answer.answerId,
                        "poll_media" to mapOf(
                            "text" to answer.pollMedia.text,
                            "emoji" to mapOf(
                                "name" to answer.pollMedia.emoji?.name
                            )
                        )
                    )
                },
                "duration" to m.poll.duration,
                "allow_multiselect" to m.poll.allowMultiSelect,
                "layout_type" to m.poll.layoutType
            )
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
    val allowedMentions: AllowedMentions? = null,
    val components: Array<Component>? = null, // files and payload_json is not supported.
    val attachments: Array<Attachment>? = null,
    val flags: Int? = null,
    val threadName: String? = null,
    val appliedTags: Array<Snowflake>? = null,
    val poll: Poll? = null
) {
    init {
        require(listOf(content, embeds, components, poll).any { it != null }) {
            throw IllegalArgumentException("Messageの作成時は、content, embeds, stickerIds, components, pollから1つ以上の引数を指定しなければなりません")
        }
    }
}

class AllowedMentions(
    val parse: Array<String>? = null,
    var roles: Array<Snowflake>? = null,
    val users: Array<Snowflake>? = null,
    val repliedUser: Boolean? = null
) {
    init {
        parse?.forEach {
            if (it in listOf("roles", "users", "everyone")) {
                throw IllegalArgumentException("Invalid argument value $it of parse")
            }
        }
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