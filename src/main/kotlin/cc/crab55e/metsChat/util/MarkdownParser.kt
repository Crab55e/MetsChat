package cc.crab55e.metsChat.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class MarkdownParser {
    companion object {
        private val lessThanSafePatterns = mapOf(
            Regex("__\\*\\*\\*(.+?)\\*\\*\\*__") to { match: MatchResult ->
                "<underlined><bold><italic>${match.groupValues[1]}</italic></bold></underlined>"
            },
            Regex("__\\*\\*(.+?)\\*\\*__") to { match: MatchResult ->
                "<underlined><bold>${match.groupValues[1]}</bold></underlined>"
            },
            Regex("__\\*(.+?)\\*__") to { match: MatchResult ->
                "<underlined><italic>${match.groupValues[1]}</italic></underlined>"
            },
            Regex("\\*\\*\\*(.+?)\\*\\*\\*") to { match: MatchResult ->
                "<bold><italic>${match.groupValues[1]}</italic></bold>"
            },
            Regex("__(.+?)__") to { match: MatchResult ->
                "<underlined>${match.groupValues[1]}</underlined>"
            },
            Regex("\\*\\*(.+?)\\*\\*") to { match: MatchResult ->
                "<bold>${match.groupValues[1]}</bold>"
            },
            Regex("\\*(.+?)\\*") to { match: MatchResult ->
                "<italic>${match.groupValues[1]}</italic>"
            },
            Regex("_(.+?)_") to { match: MatchResult ->
                "<italic>${match.groupValues[1]}</italic>"
            },
            Regex("~~(.+?)~~") to { match: MatchResult ->
                "<strikethrough>${match.groupValues[1]}</strikethrough>"
            },
            Regex("\\|\\|(.+?)\\|\\|") to { match: MatchResult ->
                "<hover:show_text:'${match.groupValues[1]}'><obfuscated>${match.groupValues[1]}</obfuscated></hover>"
            },

            Regex("\\[(.+?)]\\((.+?)\\)") to { match: MatchResult ->
                "<hover:show_text:'${match.groupValues[2]}'><click:open_url:'${match.groupValues[2]}'><underlined><color:aqua>${match.groupValues[1]}</color></underlined></click></hover>"
            },
            Regex("^>\\u0020(.*)", RegexOption.MULTILINE) to { match: MatchResult ->
                "<gray>></gray> ${match.groupValues[1]}"
            },
            Regex("```([a-zA-Z0-9]+\\n)?(.+)```", setOf( RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)) to { match: MatchResult ->
                val lang = match.groupValues[1].ifEmpty { "unset" }
                val code = match.groupValues[2]
                "<hover:show_text:'Lang: $lang'><click:copy_to_clipboard:'${code}'><color:#cccccc><shadow:#1f1f1fff>${code}</shadow></color></click></hover>"
            },
            Regex("``(.+?)``") to { match: MatchResult ->
                "<click:copy_to_clipboard:'${match.groupValues[1]}'><hover:show_text:'Copy'><shadow:#1f1f1fff><color:#cccccc>${match.groupValues[1]}</color></shadow></hover></click>"
            },
            Regex("`(.+?)`") to { match: MatchResult ->
                "<click:copy_to_clipboard:'${match.groupValues[1]}'><hover:show_text:'Copy'><shadow:#1f1f1fff><color:#cccccc>${match.groupValues[1]}</color></shadow></hover></click>"
            },
            Regex("`/(.+?)`") to { match: MatchResult ->
                "<click:run_command:'/${match.groupValues[1]}'><hover:show_text:'Run as command.'><shadow:#1f1f1fff><color:#cccccc>/${match.groupValues[1]}</color></shadow></hover></click>"
            }
        )

        private val lessThanUnSafePatterns = mapOf(
            Regex("<@!?(\\d+)>") to { match: MatchResult ->
                val userId = match.groupValues[1]
                "<hover:show_text:'ID: $userId'><click:copy_to_clipboard:'$userId'><color:#5865f2>@USER</color></click></hover>"
            },
            Regex("<#(\\d+)>") to { match: MatchResult ->
                val channelId = match.groupValues[1]
                "<hover:show_text:'ID: $channelId'><click:copy_to_clipboard:'$channelId'><color:#5865f2>#CHANNEL</color></click></hover>"
            },
            Regex("<@&(\\d+)>") to { match: MatchResult ->
                val roleId = match.groupValues[1]
                "<hover:show_text:'ID: $roleId'><click:copy_to_clipboard:'$roleId'><color:#5865f2>@ROLE</color></click></hover>"
            },
            Regex("</(.+):(\\d+)>") to { match: MatchResult ->
                val name = match.groupValues[1]
                val commandId = match.groupValues[2]
                "<hover:show_text:'Copy'><click:copy_to_clipboard:'/$name'><color:#5865f2>/$name</color></click></hover>"
            },
            Regex("<a?:(.+):(\\d+)>") to { match: MatchResult ->
                val name = match.groupValues[1]
                val id = match.groupValues[2]
                "<hover:show_text:'Unsupported emoji\nName: $name\nID: $id'><click:copy_to_clipboard:'$name'><red>⬜</red></click></hover>"
            },
            Regex("<t:(\\d+):.?>") to { match: MatchResult ->
                val timestamp = match.groupValues[1].toLong()
                val instant = Instant.ofEpochSecond(timestamp)
                val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.JAPAN)
                val formattedTime = dateTime.format(formatter)
                "<hover:show_text:'Unix timestamp: $timestamp'><click:copy_to_clipboard:'$timestamp'><color:#5865f2>$formattedTime</color></click></hover>"
            },
            Regex("<id:(customize|browse|guide|linked-roles)?>") to { match: MatchResult ->
                val type = match.groupValues[1]
                "<hover:show_text:'Type: $type'><color:#5865f2>🔗GUILD_NAVIGATION</color></hover>"
            }
        )

        @JvmStatic
        fun discordToMiniMessage(input: String): String {
            var parsedInput = input
            val lessThanKey = UUID.randomUUID().toString()

            parsedInput = parsedInput.replace("<", lessThanKey)
            for ((regex, replacer) in lessThanSafePatterns) {
                parsedInput = regex.replace(parsedInput, replacer)
            }
            parsedInput = parsedInput.replace(lessThanKey, "<")

            for ((regex, replacer) in lessThanUnSafePatterns) {
                parsedInput = regex.replace(parsedInput, replacer)
            }

            val result = parsedInput

            return result
        }
    }
}
