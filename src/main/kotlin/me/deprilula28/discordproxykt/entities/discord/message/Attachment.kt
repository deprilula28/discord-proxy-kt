package me.deprilula28.discordproxykt.entities.discord.message

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.rest.asInt
import me.deprilula28.discordproxykt.rest.asString
import me.deprilula28.discordproxykt.rest.parsing
import me.deprilula28.discordproxykt.rest.parsingOpt

/**
 * https://discord.com/developers/docs/resources/channel#attachment-object
 */
class Attachment(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot) {
    /**
     * name of file attached
     */
    val filename: String by parsing(JsonElement::asString)
    /**
     * source url of file
     */
    val sourceUrl: String by parsing(JsonElement::asString, "url")
    /**
     * a proxied url of file
     */
    val proxyUrl: String by parsing(JsonElement::asString, "proxy_url")
    /**
     * size of file in bytes
     */
    val size: Int by parsing(JsonElement::asInt)
    /**
     * width of file (if image)
     */
    val width: Int? by parsingOpt(JsonElement::asInt)
    /**
     * height of file (if image)
     */
    val height: Int? by parsingOpt(JsonElement::asInt)
}
