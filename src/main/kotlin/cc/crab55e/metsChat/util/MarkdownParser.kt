package cc.crab55e.metsChat.util

import java.util.*

class MarkdownParser {
    companion object {
        private val patterns = mapOf(
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

        @JvmStatic
        fun discordToMiniMessage(input: String): String {
            var parsedInput = input
            val lessThanKey = UUID.randomUUID().toString()
            parsedInput = parsedInput.replace("<", lessThanKey)
            for ((regex, replacer) in patterns) {
                parsedInput = regex.replace(parsedInput, replacer)
            }
            val result = parsedInput.replace(lessThanKey, "<")
            return result
        }
    }
}