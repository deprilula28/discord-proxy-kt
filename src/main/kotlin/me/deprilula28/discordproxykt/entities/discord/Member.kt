package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*

/**
 * a user within a guild
 * <br>
 * https://discord.com/developers/docs/resources/guild#guild-member-object
 */
class Member(map: JsonObject, bot: DiscordProxyKt, readyUser: User? = null): Entity(map, bot) {
    /**
     * the user this guild member represents
     */
    val user: User by lazy { readyUser ?: User(map["user"] as JsonObject, bot) }
    /**
     * this users guild nickname
     */
    val nick: String? by map.delegateJsonNullable(JsonElement::asString)
    /**
     * array of role object ids
     */
    val roles: List<Snowflake> by map.delegateJson({ (this as JsonArray).map { it.asSnowflake() } })
    /**
     * when the user joined the guild
     */
    val joinedAt: Timestamp by map.delegateJson(JsonElement::asTimestamp, "joined_at")
    /**
     * when the user started boosting the guild
     */
    val premiumSince: Timestamp? by map.delegateJsonNullable(JsonElement::asTimestamp, "premium_since")
    /**
     * whether the user is deafened in voice channels
     */
    val deaf: Boolean by map.delegateJson(JsonElement::asBoolean)
    /**
     * whether the user is muted in voice channels
     */
    val mute: Boolean by map.delegateJson(JsonElement::asBoolean)
}