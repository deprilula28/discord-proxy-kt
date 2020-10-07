package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
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
    val categorySnowflake: Snowflake?
}

class TextChannel(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), MessageChannel, GuildChannel {
    val topic: String by map.delegateJson(JsonElement::asString)
    val nsfw: Boolean by map.delegateJson(JsonElement::asBoolean)
    val lastPinTimestamp: Timestamp? by map.delegateJsonNullable(JsonElement::asTimestamp, "last_pin_timestamp")
    override val rateLimitPerUser: Int? by map.delegateJsonNullable(JsonElement::asInt, "rate_limit_per_user")
    override val lastMessageId: Snowflake by map.delegateJson(JsonElement::asSnowflake, "last_message_id")
    override val guildSnowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "guild_id")
    override val position: Int by map.delegateJson(JsonElement::asInt)
    override val name: String by map.delegateJson(JsonElement::asString)
    override val categorySnowflake: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake, "parent_id")
    
    override val permissions: List<PermissionOverwrite> by map.delegateJson({
        (this as JsonArray).map {
            PermissionOverwrite(it as JsonObject, bot)
        }
    }, "permission_overwrites")
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("rateLimitPerUser"))
    val slowmode: Int? by ::rateLimitPerUser
}
