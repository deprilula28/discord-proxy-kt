package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*

// https://discord.com/developers/docs/resources/channel#channel-object-channel-structure
interface MessageChannel {
    val lastMessageId: Snowflake
}

interface GuildChannel {
    val guildSnowflake: Snowflake
    val position: Int
    val name: String
    val permissions: List<PermissionOverwrite>
    val categorySnowflake: Snowflake?
}

class VoiceChannel(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), GuildChannel {
    val bitrate: Int by map.delegateJson(JsonElement::asInt)
    val userLimit: Int by map.delegateJson(JsonElement::asInt, "user_limit")
    override val guildSnowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "guild_id")
    override val position: Int by map.delegateJson(JsonElement::asInt)
    override val name: String by map.delegateJson(JsonElement::asString)
    override val categorySnowflake: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake, "parent_id")
    
    override val permissions: List<PermissionOverwrite> by map.delegateJson({
        (this as JsonArray).map {
            PermissionOverwrite(it as JsonObject, bot)
        }
    }, "permission_overwrites")
}

class Category(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), GuildChannel {
    override val guildSnowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "guild_id")
    override val position: Int by map.delegateJson(JsonElement::asInt)
    override val name: String by map.delegateJson(JsonElement::asString)
    override val categorySnowflake: Snowflake? by ::snowflake
    
    override val permissions: List<PermissionOverwrite> by map.delegateJson({
        (this as JsonArray).map {
            PermissionOverwrite(it as JsonObject, bot)
        }
    }, "permission_overwrites")
}

class PrivateChannel(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), MessageChannel {
    override val lastMessageId: Snowflake by map.delegateJson(JsonElement::asSnowflake, "last_message_id")
}
