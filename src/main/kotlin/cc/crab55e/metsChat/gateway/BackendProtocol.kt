package cc.crab55e.metsChat.gateway

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class BackendPayload(
    val signature: String,
    val message: JsonObject?
)

data class BaseBackendEvent(
    @SerializedName("event") val eventName: String,
    @SerializedName("server_id") val serverId: String,
    @SerializedName("timestamp") val timestamp: String? = null
)

data class GameEventPayload(
    @SerializedName("event") val eventName: String,
    @SerializedName("server_id") val serverId: String,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("json_component") val jsonComponent: String,
    @SerializedName("data") val data: EventData
)

data class EventData(
    @SerializedName("player") val player: JsonObject
)
