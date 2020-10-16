package me.deprilula28.discordproxykt.events.message

import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.Guild
import me.deprilula28.discordproxykt.entities.discord.channel.PartialMessageChannel
import me.deprilula28.discordproxykt.entities.discord.message.Message

class MessageReceivedEvent(override val map: JsonObject, override val bot: DiscordProxyKt): MessageEvent {
    val message: Message = Message(map, bot)
    
    override val messageSnowflake: Snowflake by message::snowflake
    override val channel: PartialMessageChannel
        get() = if (message.guild == null) message.privateChannel as PartialMessageChannel
        else message.textChannel as PartialMessageChannel
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("textChannel.guild.upgrade().complete()"))
    val guild: Guild
        get() = textChannel.guild.upgrade().complete()
    
    val author by message::author
    val member by message::member
    val webhookMessage: Boolean
        get() = message.webhookId != null
}
