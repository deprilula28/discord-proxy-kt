package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.entities.*

// https://discord.com/developers/docs/resources/guild#guild-member-object
class Member(map: JsonObject, bot: DiscordProxyKt, readyUser: User? = null): Entity(map, bot) {
    val user by lazy { readyUser ?: User(map["user"] as JsonObject, bot) }
    val nick: String? by map.delegateJsonNullable(JsonElement::asString)
    val roles: List<Snowflake> by map.delegateJson({ (this as JsonArray).map { it.asSnowflake() } })
    val joinedAt: Timestamp by map.delegateJson(JsonElement::asTimestamp, "joined_at")
    val premiumSince: Timestamp? by map.delegateJsonNullable(JsonElement::asTimestamp, "premium_since")
    val deaf: Boolean by map.delegateJson(JsonElement::asBoolean)
    val mute: Boolean by map.delegateJson(JsonElement::asBoolean)
}