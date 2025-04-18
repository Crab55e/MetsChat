package cc.crab55e.metsChat.util

import cc.crab55e.metsChat.MetsChat
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

class PrefixedMessageBuilder {
    fun make(plugin: MetsChat, message: String): Component {
        val mm = MiniMessage.miniMessage()
        val config = plugin.getConfigManager().getConfig()
        val generalTable = config.getTable("general")
        val prefix = generalTable.getString(
            "plugin-message-prefix",
            "<green>[MetsChat]</green> "
        )
        val result = mm.deserialize(prefix + message)
        return result
    }
}