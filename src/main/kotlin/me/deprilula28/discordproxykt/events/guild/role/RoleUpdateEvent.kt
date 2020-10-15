package me.deprilula28.discordproxykt.events.guild.role

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.*
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.parsing

class RoleUpdateEvent(override val map: JsonObject, override val bot: DiscordProxyKt): GuildRoleEvent {
    override val guildSnowflake: Snowflake by parsing(JsonElement::asSnowflake, "guild_id")
    override val role: PartialRole by parsing({ guild.fetchRole(asSnowflake()) }, "role_id")
}
