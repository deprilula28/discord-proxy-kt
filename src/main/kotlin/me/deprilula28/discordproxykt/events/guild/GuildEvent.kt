package me.deprilula28.discordproxykt.events.guild

import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.Guild
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.events.Event
import me.deprilula28.discordproxykt.rest.RestEndpoint

interface GuildEvent: Event {
    val guildSnowflake: Snowflake
    val guild: PartialGuild
        get() = bot.fetchGuild(guildSnowflake)
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.id"))
    val guildId: String
        get() = guildSnowflake.id
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.idLong"))
    val guildIdLong: Long
        get() = guildSnowflake.idLong
}
