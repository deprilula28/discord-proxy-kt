package me.deprilula28.discordproxykt.entities.discord.message

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.User
import me.deprilula28.discordproxykt.rest.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

// https://discord.com/developers/docs/resources/emoji#emoji-object
interface Emoji {
    fun toUriPart(): String
}

class ReactionEmoji(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), Emoji {
    override fun toUriPart(): String = snowflake.id
    val name: String? by parsingOpt(JsonElement::asString)
    override fun toString(): String = "Emoji(${snowflake.id}, $name)"
}
class UnicodeEmoji(val name: String): Emoji {
    override fun toUriPart(): String = URLEncoder.encode(name, StandardCharsets.UTF_8)
    override fun toString(): String = "Emoji($name)"
}

class GuildEmoji(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), Emoji {
    /**
     * emoji name
     */
    val name: String by parsing(JsonElement::asString)
    /**
     * roles this emoji is whitelisted to
     */
    val roles: List<Snowflake> by parsing({ (this as JsonArray).map { it.asSnowflake() } })
    /**
     * user that created this emoji
     */
    val user: User by parsing({ User(this as JsonObject, bot) })
    /**
     * whether this emoji must be wrapped in colons
     */
    val requireColons: Boolean by parsing(JsonElement::asBoolean, "require_colons")
    /**
     * whether this emoji is managed
     */
    val managed: Boolean by parsing(JsonElement::asBoolean)
    /**
     * whether this emoji is animated
     */
    val animated: Boolean by parsing(JsonElement::asBoolean)
    /**
     * whether this emoji can be used, may be false due to loss of Server Boosts
     */
    val available: Boolean by parsing(JsonElement::asBoolean)
    
    override fun toUriPart(): String = URLEncoder.encode(name, StandardCharsets.UTF_8)
    override fun toString(): String = "Emoji(${snowflake.id}, $name)"
}