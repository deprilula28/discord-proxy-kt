package me.deprilula28.discordproxykt.events.guild.channel

import me.deprilula28.discordproxykt.entities.discord.channel.PartialGuildChannel
import me.deprilula28.discordproxykt.events.guild.GuildEvent

interface GuildChannelEvent: GuildEvent {
    val channel: PartialGuildChannel
}
