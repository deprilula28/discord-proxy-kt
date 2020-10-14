package me.deprilula28.discordproxykt.events.guild.member

import me.deprilula28.discordproxykt.entities.discord.PartialMember
import me.deprilula28.discordproxykt.entities.discord.PartialUser
import me.deprilula28.discordproxykt.entities.discord.User
import me.deprilula28.discordproxykt.events.guild.GuildEvent

interface GuildMemberEvent: GuildEvent {
    val member: PartialMember
    
    val user: PartialUser
        get() = member.user
}
