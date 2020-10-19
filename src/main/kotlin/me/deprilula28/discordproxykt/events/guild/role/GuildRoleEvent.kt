package me.deprilula28.discordproxykt.events.guild.role

import me.deprilula28.discordproxykt.entities.discord.guild.PartialRole
import me.deprilula28.discordproxykt.events.guild.GuildEvent

interface GuildRoleEvent: GuildEvent {
    val role: PartialRole
}
