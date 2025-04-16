package cc.crab55e.metsChat;

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.minimessage.MiniMessage
import org.slf4j.Logger

@Plugin(
    id = "metschat", name = "MetsChat", version = BuildConstants.VERSION
)
class MetsChat @Inject constructor(val logger: Logger, val server: ProxyServer) {

    @Subscribe
    fun onPlayerChat(event: PlayerChatEvent) {
        val sender = event.player;
        val message = event.message;

        val senderServerName = sender.currentServer.get().serverInfo.name;

        val mm = MiniMessage.miniMessage();

        server.allPlayers.forEach playerLoop@{
            val receiver = it;
            val receiverServerName = receiver.currentServer.get().serverInfo.name;

            if (receiverServerName == senderServerName) return@playerLoop  // continue的な処理！

            receiver.sendMessage(
                mm.deserialize(
                    String.format(
                        "<hover:show_text:'from %s'>\\<<yellow>*</yellow>%s></hover> %s",
                        senderServerName,
                        sender.username,
                        message
                    )
                )
            )
        }

    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        logger.info("Initializing")
        logger.info("Initialized.")
    }
}
