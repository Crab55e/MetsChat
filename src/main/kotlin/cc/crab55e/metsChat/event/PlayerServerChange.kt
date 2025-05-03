package cc.crab55e.metsChat.event

import cc.crab55e.metsChat.MetsChat
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.player.ServerConnectedEvent

class PlayerServerChange(
    private val plugin: MetsChat
) {
    private val logger = plugin.getLogger()
    @Subscribe
    fun onServerChange(event: ServerConnectedEvent) {
        if (!event.previousServer.isPresent) return
        // PlayerJoinに任せるぜ！
        logger.info("Player Server Change Fireeeeeeeeeeeee")
    }
}