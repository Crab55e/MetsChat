package cc.crab55e.metsChat.event

import cc.crab55e.metsChat.MetsChat
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.json.JSONObject

class BackendMessage(
    private val plugin: MetsChat
) {
    private val logger = plugin.getLogger()
    fun onBackendMessageReceived(data: String) {
        logger.info(data)
        val jsonMessage = JSONObject(data)
        val displayMessageJson = jsonMessage.getJSONObject("data").getString("message_json")
        val displayMessage = GsonComponentSerializer.gson().deserialize(displayMessageJson);

    }
}