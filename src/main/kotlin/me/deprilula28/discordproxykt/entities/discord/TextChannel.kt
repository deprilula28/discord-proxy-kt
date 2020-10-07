package me.deprilula28.discordproxykt.entities.discord

import me.deprilula28.discordproxykt.JdaProxySpectacles
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.entities.*

// https://discord.com/developers/docs/resources/channel#channel-object-channel-structure
interface MessageChannel {
    val rateLimitPerUser: Int?
    val lastMessageId: Snowflake
}

interface GuildChannel {
    val guildSnowflake: Snowflake
    val position: Int
    val name: String
    val permissions: List<PermissionOverwrite>
}

class TextChannel(map: JsonObject, bot: JdaProxySpectacles): Entity(map, bot), MessageChannel, GuildChannel {
    val topic: String by lazy { map["topic"]!!.asString() }
    val nsfw: Boolean by lazy { map["nsfw"]!!.asBoolean() }
    val lastPinTimestamp: Timestamp? by lazy { map["last_pin_timestamp"]?.asTimestamp() }
    override val rateLimitPerUser: Int? by lazy { map["rate_limit_per_user"]?.asInt() }
    override val lastMessageId: Snowflake by lazy { map["last_message_id"]!!.asSnowflake() }
    override val guildSnowflake: Snowflake by lazy { map["guild_id"]!!.asSnowflake() }
    override val position: Int by lazy { map["position"]!!.asInt() }
    override val name: String by lazy { map["name"]!!.asString() }
    
    override val permissions: List<PermissionOverwrite> by lazy {
        (map["permission_overwrites"]!! as JsonArray).map {
            PermissionOverwrite(it as JsonObject, bot)
        }
    }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("rateLimitPerUser"))
    val slowmode: Int? by ::rateLimitPerUser
}
