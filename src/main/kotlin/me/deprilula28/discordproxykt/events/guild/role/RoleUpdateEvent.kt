package me.deprilula28.discordproxykt.events.guild.role

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.*
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.delegateJson

class RoleUpdateEvent(map: JsonObject, override val bot: DiscordProxyKt): GuildRoleEvent {
    override val snowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "guild_id")
    override val role: Role by map.delegateJson({ Role(guild, this as JsonObject, bot) })
}
