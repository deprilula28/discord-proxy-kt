package me.deprilula28.discordproxykt.entities.discord.message

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import java.awt.Color

// https://discord.com/developers/docs/resources/channel#embed-object
class Embed(private val map: JsonObject, val bot: DiscordProxyKt) {
    @Deprecated("JDA Compatibility Field", ReplaceWith("bot")) val jda: DiscordProxyKt by ::bot
    
    val type: Type? by map.delegateJsonNullable({ asString().run { Type.valueOf(this.toUpperCase()) } })
    val title: String? by map.delegateJsonNullable(JsonElement::asString)
    val description: String? by map.delegateJsonNullable(JsonElement::asString)
    val url: String? by map.delegateJsonNullable(JsonElement::asString)
    val timestamp: Timestamp? by map.delegateJsonNullable(JsonElement::asTimestamp)
    val color: Color? by map.delegateJsonNullable(JsonElement::asColor)
    
    val fields: List<Field>? by map.delegateJsonNullable({ (this as JsonArray).map { Field(it as JsonObject, bot) } })
    
    class Field(private val map: JsonObject, val bot: DiscordProxyKt) {
        val name: String by map.delegateJson(JsonElement::asString)
        val value: String by map.delegateJson(JsonElement::asString)
        val inline: Boolean by map.delegateJson(JsonElement::asBoolean)
    }
    
    val footer: Footer? by map.delegateJsonNullable({ Footer(this as JsonObject, bot) })
    
    class Footer(private val map: JsonObject, val bot: DiscordProxyKt) {
        val text: String by map.delegateJson(JsonElement::asString)
        val sourceIconUrl: String? by map.delegateJsonNullable(JsonElement::asString, "icon_url")
        val proxyIconUrl: String? by map.delegateJsonNullable(JsonElement::asString, "proxy_icon_url")
    }
    
    val image: Image? by map.delegateJsonNullable({ Image(this as JsonObject, bot) })
    
    class Image(private val map: JsonObject, val bot: DiscordProxyKt) {
        val sourceUrl: String? by map.delegateJsonNullable(JsonElement::asString, "url")
        val proxyUrl: String? by map.delegateJsonNullable(JsonElement::asString, "proxy_url")
        val width: Int? by map.delegateJsonNullable({ asInt() })
        val height: Int? by map.delegateJsonNullable({ asInt() })
    }
    
    val provider: Provider? by map.delegateJsonNullable({ Provider(this as JsonObject, bot) })
    
    class Provider(private val map: JsonObject, val bot: DiscordProxyKt) {
        val name: String? by map.delegateJsonNullable(JsonElement::asString)
        val url: String? by map.delegateJsonNullable(JsonElement::asString)
    }
    
    val author: Author? by map.delegateJsonNullable({ Author(this as JsonObject, bot) })
    
    class Author(private val map: JsonObject, val bot: DiscordProxyKt) {
        val name: String? by map.delegateJsonNullable(JsonElement::asString)
        val url: String? by map.delegateJsonNullable(JsonElement::asString)
        val sourceIconUrl: String? by map.delegateJsonNullable(JsonElement::asString, "url")
        val proxyIconUrl: String? by map.delegateJsonNullable(JsonElement::asString, "proxy_url")
    }
    
    enum class Type {
        RICH, IMAGE, VIDEO, GIFV, ARTICLE, LINK
    }
}