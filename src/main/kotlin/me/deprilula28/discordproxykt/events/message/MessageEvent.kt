package me.deprilula28.discordproxykt.events.message

import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.UnavailableField
import me.deprilula28.discordproxykt.entities.discord.channel.*
import me.deprilula28.discordproxykt.events.Event

interface MessageEvent: Event {
    val snowflake: Snowflake
    val channel: PartialMessageChannel
    
    val channelType: ChannelType
        get() = channel.type
    
    /**
     * @throws [UnavailableField] If the [channel] is not a [TextChannel].
     */
    val textChannel: TextChannel
        get() = channel as? TextChannel ?: throw UnavailableField()
    
    /**
     * @throws [UnavailableField] If the [channel] is not a [PrivateChannel].
     */
    val privateChannel: PrivateChannel
        get() = channel as? PrivateChannel ?: throw UnavailableField()
    
    fun isFromType(type: ChannelType) = channelType == type
    fun isFromGuild() = channel is PartialGuildChannel
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.id"))
    val messageId: String
        get() = snowflake.id
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.idLong"))
    val messageIdLong: Long
        get() = snowflake.idLong
}
