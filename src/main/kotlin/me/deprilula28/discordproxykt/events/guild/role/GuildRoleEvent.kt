package me.deprilula28.discordproxykt.events.guild.role

import me.deprilula28.discordproxykt.entities.discord.PartialRole
import me.deprilula28.discordproxykt.entities.discord.PartialUser
import me.deprilula28.discordproxykt.events.guild.GuildEvent

interface GuildRoleEvent: GuildEvent {
    val role: PartialRole
}
