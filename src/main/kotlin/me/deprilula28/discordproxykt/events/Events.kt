package me.deprilula28.discordproxykt.events

import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.events.guild.*
import me.deprilula28.discordproxykt.events.guild.member.*
import me.deprilula28.discordproxykt.events.guild.invite.*
import me.deprilula28.discordproxykt.events.guild.role.*
import me.deprilula28.discordproxykt.events.message.*
import me.deprilula28.discordproxykt.events.message.reaction.*
import kotlin.properties.ReadOnlyProperty

object Events {
    val GUILD_CREATE by event(::GuildJoinEvent)
    val GUILD_DELETE by event(::GuildLeaveEvent)
    val GUILD_BAN_ADD by event(::GuildBanEvent)
    val GUILD_BAN_REMOVE by event(::GuildUnbanEvent)
    val GUILD_EMOJIS_UPDATE by event(::GuildUpdateEmojisEvent)
    
    val CHANNEL_CREATE by event(::ChannelCreateEvent)
    val CHANNEL_UPDATE by event(::ChannelUpdateEvent)
    val CHANNEL_DELETE by event(::ChannelDeleteEvent)
    
    val GUILD_MEMBER_ADD by event(::GuildMemberJoinEvent)
    val GUILD_MEMBER_REMOVE by event(::GuildMemberLeaveEvent)
    val GUILD_MEMBER_UPDATE by event(::GuildMemberUpdateEvent)
    val GUILD_ROLE_CREATE by event(::ChannelCreateEvent)
    val GUILD_ROLE_DELETE by event(::ChannelDeleteEvent)
    val GUILD_ROLE_UPDATE by event(::ChannelUpdateEvent)
    
    val INVITE_CREATE by event(::GuildInviteCreateEvent)
    val INVITE_DELETE by event(::GuildInviteDeleteEvent)
    
    val MESSAGE_CREATE by event(::MessageReceivedEvent)
    val MESSAGE_UPDATE by event(::MessageUpdateEvent)
    val MESSAGE_DELETE by event(::MessageDeleteEvent)
    val MESSAGE_DELETE_BULK by event(::MessageBulkDeleteEvent)
    val MESSAGE_REACTION_ADD by event(::MessageReactionAddEvent)
    val MESSAGE_REACTION_REMOVE by event(::MessageReactionRemoveEvent)
    val MESSAGE_REACTION_REMOVE_ALL by event(::MessageReactionRemoveAllEvent)
    val MESSAGE_REACTION_REMOVE_EMOJI by event(::MessageReactionRemoveEmojiEvent)
    
    private inline fun <reified T> event(noinline constructor: (JsonObject, DiscordProxyKt) -> T, ):
            ReadOnlyProperty<Any?, Event<T>>
        = ReadOnlyProperty{ _, property -> Event(property.name, constructor, T::class.java) }
    
    data class Event<T>(val eventName: String, val constructor: (JsonObject, DiscordProxyKt) -> T, val clazz: Class<T>)
}
