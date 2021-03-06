package me.deprilula28.discordproxykt.events.message.reaction

import me.deprilula28.discordproxykt.entities.discord.guild.Member
import me.deprilula28.discordproxykt.entities.discord.guild.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.PartialUser
import me.deprilula28.discordproxykt.entities.discord.message.PartialMessage
import me.deprilula28.discordproxykt.entities.discord.message.ReactionEmoji
import me.deprilula28.discordproxykt.events.message.MessageEvent

interface MessageReactionEvent: MessageEvent {
    val user: PartialUser
    val message: PartialMessage
    val guild: PartialGuild?
    val member: Member?
    val emoji: ReactionEmoji?
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("this"))
    val reaction: MessageReactionEvent
        get() = this
    @Deprecated("JDA Compatibility Field", ReplaceWith("emoji!!"))
    val reactionEmote: ReactionEmoji
        get() = emoji!!
    @Deprecated("JDA Compatibility Field", ReplaceWith("user.snowflake.id"))
    val userId: String
        get() = user.snowflake.id
    @Deprecated("JDA Compatibility Field", ReplaceWith("user.snowflake.idLong"))
    val userIdLong: Long
        get() = user.snowflake.idLong
    @Deprecated("JDA Compatibility Function", ReplaceWith("user.upgrade()"))
    fun retrieveUser() = user.upgrade()
    @Deprecated("JDA Compatibility Function", ReplaceWith("message.upgrade()"))
    fun retrieveMessage() = message.upgrade()
}
