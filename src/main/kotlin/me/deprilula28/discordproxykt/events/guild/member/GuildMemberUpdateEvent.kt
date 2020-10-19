package me.deprilula28.discordproxykt.events.guild.member

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.guild.Member
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.parsing
import me.deprilula28.discordproxykt.entities.UnavailableField

class GuildMemberUpdateEvent(override val map: JsonObject, override val bot: DiscordProxyKt): GuildMemberEvent {
    /**
     * @throws [UnavailableField] When you call [Member.mute] and [Member.deaf] under the member.
     */
    override val guildSnowflake: Snowflake by parsing(JsonElement::asSnowflake, "guild_id")
    override val member: Member = Member(guild, map, bot)
    
    override suspend fun internalHandle() {
        guild.upgrade().getIfAvailable()?.apply { cachedMembers.find { member.user.snowflake == it.user.snowflake }?.map = member.map }
    }
}
