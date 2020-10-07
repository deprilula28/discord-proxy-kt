package me.deprilula28.discordproxykt.entities.discord.message

import me.deprilula28.discordproxykt.DiscordProxyKt
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.entities.*
import java.awt.Color

// https://discord.com/developers/docs/resources/channel#embed-object
class Embed(private val map: JsonObject, val bot: DiscordProxyKt) {
    @Deprecated("JDA Compatibility Field", ReplaceWith("bot")) val jda: DiscordProxyKt by ::bot
    
    val type: Type? by lazy { map["type"]?.asString()?.run { Type.valueOf(this.toUpperCase()) } }
    val title: String? by lazy { map["title"]?.asString() }
    val description: String? by lazy { map["description"]?.asString() }
    val url: String? by lazy { map["url"]?.asString() }
    val timestamp: Timestamp? by lazy { map["timestamp"]?.asTimestamp() }
    val color: Color? by lazy { map["color"]?.asColor() }
    
    val fields: List<Field>? by lazy { map["fields"]?.run { (this as JsonArray).map { Field(it as JsonObject, bot) } } }
    
    class Field(private val map: JsonObject, val bot: DiscordProxyKt) {
        val name: String by lazy { map["name"]!!.asString() }
        val value: String by lazy { map["value"]!!.asString() }
        val inline: Boolean by lazy { map["inline"]!!.asBoolean() }
    }
    
    val footer: Footer? by lazy { map["footer"]?.run { Footer(this as JsonObject, bot) } }
    
    class Footer(private val map: JsonObject, val bot: DiscordProxyKt) {
        val text: String by lazy { map["text"]!!.asString() }
        val sourceIconUrl: String? by lazy { map["icon_url"]?.asString() }
        val proxyIconUrl: String? by lazy { map["proxy_icon_url"]?.asString() }
    }
    
    val image: Image? by lazy { map["image"]?.run { Image(this as JsonObject, bot) } }
    
    class Image(private val map: JsonObject, val bot: DiscordProxyKt) {
        val sourceUrl: String? by lazy { map["url"]?.asString() }
        val proxyUrl: String? by lazy { map["proxy_url"]?.asString() }
        val width: Int? by lazy { map["width"]?.asInt() }
        val height: Int? by lazy { map["height"]?.asInt() }
    }
    
    val provider: Provider? by lazy { map["provider"]?.run { Provider(this as JsonObject, bot) } }
    
    class Provider(private val map: JsonObject, val bot: DiscordProxyKt) {
        val name: String? by lazy { map["name"]?.asString() }
        val url: String? by lazy { map["url"]?.asString() }
    }
    
    val author: Author? by lazy { map["author"]?.run { Author(this as JsonObject, bot) } }
    
    class Author(private val map: JsonObject, val bot: DiscordProxyKt) {
        val name: String? by lazy { map["name"]?.asString() }
        val url: String? by lazy { map["url"]?.asString() }
        val sourceIconUrl: String? by lazy { map["icon_url"]?.asString() }
        val proxyIconUrl: String? by lazy { map["proxy_icon_url"]?.asString() }
    }
    
    enum class Type {
        RICH, IMAGE, VIDEO, GIFV, ARTICLE, LINK
    }
}