package me.deprilula28.discordproxykt.events.guild.invite

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.ExtendedInvite
import me.deprilula28.discordproxykt.entities.discord.channel.PartialGuildChannel
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.delegateJson

class GuildInviteCreateEvent(map: JsonObject, override val bot: DiscordProxyKt): GuildInviteEvent {
    override val snowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "guild_id")
    val invite: ExtendedInvite = ExtendedInvite(map, bot, guild)
    
    override val code: String by invite::code
    override val channel: PartialGuildChannel
        get() = invite.channel!! // This should never be null in a guild invite creation
}
