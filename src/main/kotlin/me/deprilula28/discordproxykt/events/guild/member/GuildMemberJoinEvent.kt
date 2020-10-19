package me.deprilula28.discordproxykt.events.guild.member

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.guild.Member
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.parsing

class GuildMemberJoinEvent(override val map: JsonObject, override val bot: DiscordProxyKt): GuildMemberEvent {
    override val member: Member = Member(guild, map, bot)
    override val guildSnowflake: Snowflake by parsing(JsonElement::asSnowflake, "guild_id")
    
    override suspend fun internalHandle() {
        guild.upgrade().getIfAvailable()?.apply { cachedMembers.add(member) }
    }
}