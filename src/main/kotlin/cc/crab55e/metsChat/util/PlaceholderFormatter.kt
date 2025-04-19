package cc.crab55e.metsChat.util

class PlaceholderFormatter {
    companion object {
        @JvmStatic
        fun format(template: String, values: Map<String, String>): String {
            var result = template
            for ((key, value) in values) {
                result = result.replace("{$key}", value)
            }
            return result
        }
    }
}