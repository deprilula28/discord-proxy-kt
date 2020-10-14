package me.deprilula28.discordproxykt.events.guild.invite

import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.channel.PartialCategory
import me.deprilula28.discordproxykt.entities.discord.channel.PartialTextChannel
import me.deprilula28.discordproxykt.entities.discord.channel.PartialVoiceChannel
import me.deprilula28.discordproxykt.events.guild.GuildEvent

interface GuildInviteEvent: GuildEvent {
    val code: String
    val channelSnowflake: Snowflake
    
    val url: String
        get() = "https://discord.gg/$code"
    @Deprecated("JDA Compatibility Field", ReplaceWith("guild.fetchCategory(channelSnowflake)"))
    val category: PartialCategory
        get() = guild.fetchCategory(channelSnowflake)
    @Deprecated("JDA Compatibility Field", ReplaceWith("guild.fetchTextChannel(channelSnowflake)"))
    val textChannel: PartialTextChannel
        get() = guild.fetchTextChannel(channelSnowflake)
    @Deprecated("JDA Compatibility Field", ReplaceWith("guild.fetchVoiceChannel(channelSnowflake)"))
    val voiceChannel: PartialVoiceChannel
        get() = guild.fetchVoiceChannel(channelSnowflake)
}
