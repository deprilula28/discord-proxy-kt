package me.deprilula28.discordproxykt.entities.discord.channel

import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.ChannelType
import me.deprilula28.discordproxykt.entities.discord.PermissionOverwrite
import me.deprilula28.discordproxykt.rest.*

/**
 * an organizational category that contains up to 50 channels
 * <br>
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
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
    
    override val type: ChannelType
        get() = ChannelType.CATEGORY
}

