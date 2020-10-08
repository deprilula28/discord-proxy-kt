package me.deprilula28.discordproxykt.entities.discord.message

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*

// https://discord.com/developers/docs/resources/channel#attachment-object
class Attachment(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot) {
    val filename: String by map.delegateJson(JsonElement::asString)
    val sourceUrl: String by map.delegateJson(JsonElement::asString, "url")
    val proxyUrl: String by map.delegateJson(JsonElement::asString, "proxy_url")
    val size: Int by map.delegateJson(JsonElement::asInt)
    val width: Int? by map.delegateJsonNullable(JsonElement::asInt)
    val height: Int? by map.delegateJsonNullable(JsonElement::asInt)
}
