package cc.crab55e.metsChat.event

import cc.crab55e.metsChat.MetsChat

class BackendMessage(
    private val plugin: MetsChat
) {
    private val logger = plugin.getLogger()
    fun onBackendMessageReceived(message: String) {
        logger.info(message)
    }
}