package cc.crab55e.metsChat.util

class Models {
    @JvmInline
    value class Snowflake(val id: String) {
        init {
            require(id.matches(Regex("^\\d+$"))) { "Invalid Discord ID: $id" }
        }
    }
}