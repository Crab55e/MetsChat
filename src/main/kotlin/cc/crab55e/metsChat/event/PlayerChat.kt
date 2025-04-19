package cc.crab55e.metsChat.event

import cc.crab55e.metsChat.MetsChat
import cc.crab55e.metsChat.util.PlaceholderFormatter
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import net.kyori.adventure.text.minimessage.MiniMessage

class ChatEventListener(
    plugin: MetsChat
) {
    private val server = plugin.getServer()
    private val config = plugin.getConfigManager().getConfig()

    @Subscribe
    fun onPlayerChat(event: PlayerChatEvent) {
        val sender = event.player
        val message = event.message
        val senderServerName = sender.currentServer.get().serverInfo.name

        val mm = MiniMessage.miniMessage()
        val minecraftTable = config.getTable("minecraft")
        val crossServerMessageFormat = minecraftTable.getString("cross-server-message-format")

        server.allPlayers.forEach playerLoop@{
            val receiver = it
            val receiverServerName = receiver.currentServer.get().serverInfo.name
            if (receiverServerName == senderServerName) return@playerLoop

            receiver.sendMessage(
                mm.deserialize(
                    PlaceholderFormatter.format(
                        crossServerMessageFormat,
                        mapOf(
                            "senderServer" to senderServerName,
                            "senderName" to sender.username,
                            "message" to message
                        )
                    )
                )
            )
        }
    }
}
