package me.deprilula28.discordproxykt.entities.discord.message

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.rest.*
import java.awt.Color

/**
 * https://discord.com/developers/docs/resources/channel#embed-object
 */
class Embed(override val map: JsonObject, override val bot: DiscordProxyKt): Parse {
    /**
     * title of embed
     */
    val title: String? by parsingOpt(JsonElement::asString)
    /**
     * type of embed (always "rich" for webhook embeds)
     */
    val type: Type? by parsingOpt({ asString().run { Type.valueOf(toUpperCase()) } })
    /**
     * description of embed
     */
    val description: String? by parsingOpt(JsonElement::asString)
    /**
     * url of embed
     */
    val url: String? by parsingOpt(JsonElement::asString)
    /**
     * timestamp of embed content
     */
    val timestamp: Timestamp? by parsingOpt(JsonElement::asTimestamp)
    /**
     * color code of the embed
     */
    val color: Color? by parsingOpt(JsonElement::asColor)
    /**
     * fields information
     */
    val fields: List<Field>? by parsingOpt({ (this as JsonArray).map { Field(it as JsonObject, bot) } })
    
    class Field(override val map: JsonObject, override val bot: DiscordProxyKt): Parse {
        /**
         * name of the field
         */
        val name: String by parsing(JsonElement::asString)
        /**
         * value of the field
         */
        val value: String by parsing(JsonElement::asString)
        /**
         * whether or not this field should display inline
         */
        val inline: Boolean by parsing(JsonElement::asBoolean)
    }
    
    /**
     * footer information
     */
    val footer: Footer? by parsingOpt({ Footer(this as JsonObject, bot) })
    
    class Footer(override val map: JsonObject, override val bot: DiscordProxyKt): Parse {
        /**
         * footer text
         */
        val text: String by parsing(JsonElement::asString)
        /**
         * url of footer icon (only supports http(s) and attachments)
         */
        val sourceIconUrl: String? by parsingOpt(JsonElement::asString, "icon_url")
        /**
         * a proxied url of footer icon
         */
        val proxyIconUrl: String? by parsingOpt(JsonElement::asString, "proxy_icon_url")
    }
    
    /**
     * image information
     */
    val image: Image? by parsingOpt({ Image(this as JsonObject, bot) })
    
    class Image(override val map: JsonObject, override val bot: DiscordProxyKt): Parse {
        /**
         * source url of image (only supports http(s) and attachments)
         */
        val sourceUrl: String? by parsingOpt(JsonElement::asString, "url")
        /**
         * a proxied url of the image
         */
        val proxyUrl: String? by parsingOpt(JsonElement::asString, "proxy_url")
        /**
         * width of image
         */
        val width: Int? by parsingOpt({ asInt() })
        /**
         * height of image
         */
        val height: Int? by parsingOpt({ asInt() })
    }
    
    /**
     * provider information
     */
    val provider: Provider? by parsingOpt({ Provider(this as JsonObject, bot) })
    
    class Provider(override val map: JsonObject, override val bot: DiscordProxyKt): Parse {
        /**
         * name of provider
         */
        val name: String? by parsingOpt(JsonElement::asString)
        /**
         * url of provider
         */
        val url: String? by parsingOpt(JsonElement::asString)
    }
    
    /**
     * author information
     */
    val author: Author? by parsingOpt({ Author(this as JsonObject, bot) })
    
    class Author(override val map: JsonObject, override val bot: DiscordProxyKt): Parse {
        /**
         * name of author
         */
        val name: String? by parsingOpt(JsonElement::asString)
        /**
         * url of author
         */
        val url: String? by parsingOpt(JsonElement::asString)
        /**
         * url of author icon (only supports http(s) and attachments)
         */
        val sourceIconUrl: String? by parsingOpt(JsonElement::asString, "url")
        /**
         * a proxied url of author icon
         */
        val proxyIconUrl: String? by parsingOpt(JsonElement::asString, "proxy_url")
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