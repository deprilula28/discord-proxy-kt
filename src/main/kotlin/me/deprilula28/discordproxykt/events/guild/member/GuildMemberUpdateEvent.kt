package me.deprilula28.discordproxykt.events.guild.member

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.Member
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.delegateJson
import me.deprilula28.discordproxykt.entities.UnavailableField

class GuildMemberUpdateEvent(map: JsonObject, override val bot: DiscordProxyKt): GuildMemberEvent {
    /**
     * @throws [UnavailableField] When you call [Member.mute] and [Member.deaf] under the member.
     */
    override val member: Member = Member(guild, map, bot)
    override val snowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "guild_id")
}
