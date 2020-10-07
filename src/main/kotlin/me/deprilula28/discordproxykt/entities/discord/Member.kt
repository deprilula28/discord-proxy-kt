package me.deprilula28.discordproxykt.entities.discord

import me.deprilula28.discordproxykt.JdaProxySpectacles
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.entities.*

// https://discord.com/developers/docs/resources/guild#guild-member-object
class Member(map: JsonObject, bot: JdaProxySpectacles, readyUser: User? = null): Entity(map, bot) {
    val user by lazy { readyUser ?: User(map["user"] as JsonObject, bot) }
    val nick: String? by lazy { map["nick"]?.asString() }
    val roles: List<Snowflake> by lazy { (map["roles"] as JsonArray).map { it.asSnowflake() } }
    val joinedAt: Timestamp by lazy { map["joined_at"]!!.asTimestamp() }
    val premiumSince: Timestamp? by lazy { map["premium_since"]?.asTimestamp() }
    val deaf: Boolean by lazy { map["nick"]!!.asBoolean() }
    val mute: Boolean by lazy { map["mute"]!!.asBoolean() }
}