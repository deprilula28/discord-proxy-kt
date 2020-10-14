package me.deprilula28.discordproxykt.events.guild

import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.events.Event

interface GuildEvent: Event {
    val snowflake: Snowflake
    val guild: PartialGuild
        get() = bot.fetchGuild(snowflake)
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.id"))
    val guildId: String
        get() = snowflake.id
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.idLong"))
    val guildIdLong: Long
        get() = snowflake.idLong
}