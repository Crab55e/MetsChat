package cc.crab55e.metsChat.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.velocitypowered.api.proxy.Player
import java.util.*

class PlayerSkinTextureIdResolver(private val player: Player) {
    var textureId: String
    init {
        val senderProfilePropertiesJson = Base64.getDecoder().decode(player.gameProfileProperties[0].value).toString(Charsets.UTF_8)
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        val ProfileMap: Map<String, Any> = Gson().fromJson(senderProfilePropertiesJson, mapType)
        val ProfileMapTextures = ProfileMap["textures"] as? Map<*, *>
        val ProfileMapSkin = ProfileMapTextures?.get("SKIN") as? Map<*, *>
        val SkinUrl = ProfileMapSkin?.get("url") as? String
        // original: http://textures.minecraft.net/texture/bb20ec924eb2f949a6198f23ec4e38bcaf570d5f3b8f8003e7dcd28863007654
        textureId = SkinUrl?.split("/")?.last() ?: "undefined"
    }
}