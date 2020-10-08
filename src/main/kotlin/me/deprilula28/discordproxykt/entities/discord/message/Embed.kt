package me.deprilula28.discordproxykt.entities.discord.message

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import java.awt.Color

/**
 * https://discord.com/developers/docs/resources/channel#embed-object
 */
class Embed(private val map: JsonObject, val bot: DiscordProxyKt) {
    @Deprecated("JDA Compatibility Field", ReplaceWith("bot")) val jda: DiscordProxyKt by ::bot
    
    /**
     * title of embed
     */
    val title: String? by map.delegateJsonNullable(JsonElement::asString)
    /**
     * type of embed (always "rich" for webhook embeds)
     */
    val type: Type? by map.delegateJsonNullable({ asString().run { Type.valueOf(this.toUpperCase()) } })
    /**
     * description of embed
     */
    val description: String? by map.delegateJsonNullable(JsonElement::asString)
    /**
     * url of embed
     */
    val url: String? by map.delegateJsonNullable(JsonElement::asString)
    /**
     * timestamp of embed content
     */
    val timestamp: Timestamp? by map.delegateJsonNullable(JsonElement::asTimestamp)
    /**
     * color code of the embed
     */
    val color: Color? by map.delegateJsonNullable(JsonElement::asColor)
    /**
     * fields information
     */
    val fields: List<Field>? by map.delegateJsonNullable({ (this as JsonArray).map { Field(it as JsonObject, bot) } })
    
    class Field(private val map: JsonObject, val bot: DiscordProxyKt) {
        /**
         * name of the field
         */
        val name: String by map.delegateJson(JsonElement::asString)
        /**
         * value of the field
         */
        val value: String by map.delegateJson(JsonElement::asString)
        /**
         * whether or not this field should display inline
         */
        val inline: Boolean by map.delegateJson(JsonElement::asBoolean)
    }
    
    /**
     * footer information
     */
    val footer: Footer? by map.delegateJsonNullable({ Footer(this as JsonObject, bot) })
    
    class Footer(private val map: JsonObject, val bot: DiscordProxyKt) {
        /**
         * footer text
         */
        val text: String by map.delegateJson(JsonElement::asString)
        /**
         * url of footer icon (only supports http(s) and attachments)
         */
        val sourceIconUrl: String? by map.delegateJsonNullable(JsonElement::asString, "icon_url")
        /**
         * a proxied url of footer icon
         */
        val proxyIconUrl: String? by map.delegateJsonNullable(JsonElement::asString, "proxy_icon_url")
    }
    
    /**
     * image information
     */
    val image: Image? by map.delegateJsonNullable({ Image(this as JsonObject, bot) })
    
    class Image(private val map: JsonObject, val bot: DiscordProxyKt) {
        /**
         * source url of image (only supports http(s) and attachments)
         */
        val sourceUrl: String? by map.delegateJsonNullable(JsonElement::asString, "url")
        /**
         * a proxied url of the image
         */
        val proxyUrl: String? by map.delegateJsonNullable(JsonElement::asString, "proxy_url")
        /**
         * width of image
         */
        val width: Int? by map.delegateJsonNullable({ asInt() })
        /**
         * height of image
         */
        val height: Int? by map.delegateJsonNullable({ asInt() })
    }
    
    /**
     * provider information
     */
    val provider: Provider? by map.delegateJsonNullable({ Provider(this as JsonObject, bot) })
    
    class Provider(private val map: JsonObject, val bot: DiscordProxyKt) {
        /**
         * name of provider
         */
        val name: String? by map.delegateJsonNullable(JsonElement::asString)
        /**
         * url of provider
         */
        val url: String? by map.delegateJsonNullable(JsonElement::asString)
    }
    
    /**
     * author information
     */
    val author: Author? by map.delegateJsonNullable({ Author(this as JsonObject, bot) })
    
    class Author(private val map: JsonObject, val bot: DiscordProxyKt) {
        /**
         * name of author
         */
        val name: String? by map.delegateJsonNullable(JsonElement::asString)
        /**
         * url of author
         */
        val url: String? by map.delegateJsonNullable(JsonElement::asString)
        /**
         * url of author icon (only supports http(s) and attachments)
         */
        val sourceIconUrl: String? by map.delegateJsonNullable(JsonElement::asString, "url")
        /**
         * a proxied url of author icon
         */
        val proxyIconUrl: String? by map.delegateJsonNullable(JsonElement::asString, "proxy_url")
    }
    
    enum class Type {
        /**
         * generic embed rendered from embed attributes
         */
        RICH,
        /**
         * image embed
         */
        IMAGE,
        /**
         * video embed
         */
        VIDEO,
        /**
         * animated gif image embed rendered as a video embed
         */
        GIFV,
        /**
         * article embed
         */
        ARTICLE,
        /**
         * link embed
         */
        LINK
    }
}