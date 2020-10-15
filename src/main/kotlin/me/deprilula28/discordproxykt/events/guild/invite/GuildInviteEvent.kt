package me.deprilula28.discordproxykt.events.guild.invite

import me.deprilula28.discordproxykt.entities.discord.channel.PartialCategory
import me.deprilula28.discordproxykt.entities.discord.channel.PartialGuildChannel
import me.deprilula28.discordproxykt.entities.discord.channel.PartialTextChannel
import me.deprilula28.discordproxykt.entities.discord.channel.PartialVoiceChannel
import me.deprilula28.discordproxykt.events.guild.GuildEvent

interface GuildInviteEvent: GuildEvent {
    val code: String
    val channel: PartialGuildChannel
    
    val url: String
        get() = "https://discord.gg/$code"
    @Deprecated("JDA Compatibility Field", ReplaceWith("guild.fetchCategory(channel.snowflake)"))
    val category: PartialCategory
        get() = guild.fetchCategory(channel.snowflake)
    @Deprecated("JDA Compatibility Field", ReplaceWith("guild.fetchTextChannel(channel.snowflake)"))
    val textChannel: PartialTextChannel
        get() = guild.fetchTextChannel(channel.snowflake)
    @Deprecated("JDA Compatibility Field", ReplaceWith("guild.fetchVoiceChannel(channel.snowflake)"))
    val voiceChannel: PartialVoiceChannel
        get() = guild.fetchVoiceChannel(channel.snowflake)
}
