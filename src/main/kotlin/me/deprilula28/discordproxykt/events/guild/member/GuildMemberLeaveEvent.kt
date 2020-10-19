package me.deprilula28.discordproxykt.events.guild.member

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.guild.Member
import me.deprilula28.discordproxykt.entities.discord.User
import me.deprilula28.discordproxykt.events.guild.GuildEvent
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.parsing

class GuildMemberLeaveEvent(override val map: JsonObject, override val bot: DiscordProxyKt): GuildEvent {
    val user: User by parsing({ User(this as JsonObject, bot) })
    override val guildSnowflake: Snowflake by parsing(JsonElement::asSnowflake, "guild_id")
    
    override suspend fun internalHandle() {
        guild.upgrade().getIfAvailable()?.apply { cachedMembers.removeIf { it.user.snowflake == user.snowflake } }
    }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("null"))
    val member: Member? = null
}