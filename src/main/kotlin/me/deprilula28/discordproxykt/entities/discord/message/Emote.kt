package me.deprilula28.discordproxykt.entities.discord.message

import me.deprilula28.discordproxykt.DiscordProxyKt
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.User

// https://discord.com/developers/docs/resources/emoji#emoji-object
interface Emoji

class ReactionEmoji(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), Emoji
class UnicodeEmoji(private val map: JsonObject): Emoji {
    val name: String by lazy { map["name"]!!.asString() }
}

class GuildEmoji(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot) {
    val name: String by lazy { (map["name"] ?: throw UnavailableField()).asString() }
    val roles: List<Snowflake> by lazy { (map["mention_roles"] as JsonArray).map { it.asSnowflake() } }
    val user: User by lazy { User(map["user"] as JsonObject, bot) }
    val requireColons: Boolean by lazy { map["require_colons"]!!.asBoolean() }
    val managed: Boolean by lazy { map["managed"]!!.asBoolean() }
    val animated: Boolean by lazy { map["animated"]!!.asBoolean() }
    val available: Boolean by lazy { map["available"]!!.asBoolean() }
}