package me.deprilula28.discordproxykt.entities.discord.message

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.User
import me.deprilula28.discordproxykt.rest.asBoolean
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.asString
import java.net.URLEncoder

// https://discord.com/developers/docs/resources/emoji#emoji-object
interface Emoji {
    fun toUriPart(): String
}

class ReactionEmoji(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), Emoji {
    override fun toUriPart(): String = snowflake.id
}
class UnicodeEmoji(val name: String): Emoji {
    override fun toUriPart(): String = URLEncoder.encode(name)
}

class GuildEmoji(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), Emoji {
    /**
     * emoji name
     */
    val name: String by lazy { (map["name"] ?: throw UnavailableField()).asString() }
    /**
     * roles this emoji is whitelisted to
     */
    val roles: List<Snowflake> by lazy { (map["roles"] as JsonArray).map { it.asSnowflake() } }
    /**
     * user that created this emoji
     */
    val user: User by lazy { User(map["user"] as JsonObject, bot) }
    /**
     * whether this emoji must be wrapped in colons
     */
    val requireColons: Boolean by lazy { map["require_colons"]!!.asBoolean() }
    /**
     * whether this emoji is managed
     */
    val managed: Boolean by lazy { map["managed"]!!.asBoolean() }
    /**
     * whether this emoji is animated
     */
    val animated: Boolean by lazy { map["animated"]!!.asBoolean() }
    /**
     * whether this emoji can be used, may be false due to loss of Server Boosts
     */
    val available: Boolean by lazy { map["available"]!!.asBoolean() }
    
    override fun toUriPart(): String = URLEncoder.encode(name)
}