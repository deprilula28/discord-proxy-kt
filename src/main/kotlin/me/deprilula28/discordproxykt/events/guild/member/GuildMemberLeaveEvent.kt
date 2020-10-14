package me.deprilula28.discordproxykt.events.guild.member

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.Guild
import me.deprilula28.discordproxykt.entities.discord.Member
import me.deprilula28.discordproxykt.entities.discord.PartialMember
import me.deprilula28.discordproxykt.entities.discord.User
import me.deprilula28.discordproxykt.events.guild.GuildEvent
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.delegateJson

class GuildMemberLeaveEvent(map: JsonObject, override val bot: DiscordProxyKt): GuildEvent {
    val user: User by map.delegateJson({ User(this as JsonObject, bot) })
    override val snowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "guild_id")
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("null"))
    val member: Member? = null
}