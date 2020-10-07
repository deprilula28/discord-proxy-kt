package me.deprilula28.discordproxykt.entities.discord.message

import me.deprilula28.discordproxykt.JdaProxySpectacles
import me.deprilula28.discordproxykt.entities.Entity
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.entities.asBoolean
import me.deprilula28.discordproxykt.entities.asInt
import me.deprilula28.discordproxykt.entities.asString

// https://discord.com/developers/docs/resources/channel#attachment-object
class Attachment(map: JsonObject, bot: JdaProxySpectacles): Entity(map, bot) {
    val filename: String by lazy { map["filename"]!!.asString() }
    val sourceUrl: String by lazy { map["url"]!!.asString() }
    val proxyUrl: String by lazy { map["proxy_url"]!!.asString() }
    val size: Int by lazy { map["size"]!!.asInt() }
    val width: Int? by lazy { map["width"]?.asInt() }
    val height: Int? by lazy { map["height"]?.asInt() }
}
