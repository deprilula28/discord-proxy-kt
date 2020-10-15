package me.deprilula28.discordproxykt.events.message

import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.channel.PartialMessageChannel
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.delegateJson

class MessageUpdateEvent(map: JsonObject, override val bot: DiscordProxyKt): MessageEvent {
    val message: Message = Message(map, bot)
    
    override val snowflake: Snowflake by message::snowflake
    override val channel: PartialMessageChannel
        get() = if (message.guild == null) message.privateChannel as PartialMessageChannel
        else message.textChannel as PartialMessageChannel
    
    val guild: PartialGuild by map.delegateJson({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    
    val author by message::author
    val member by message::member
    val webhookMessage: Boolean
        get() = message.webhookId != null
}
