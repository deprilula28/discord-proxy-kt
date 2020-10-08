package me.deprilula28.discordproxykt.events.message

import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.message.Message

class MessageUpdateEvent(map: JsonObject, override val bot: DiscordProxyKt): GenericMessageEvent {
    val message: Message = Message(map, bot)
    
    override val id: Snowflake by lazy { message.snowflake }
    override val channel: Snowflake by lazy { message.channelSnowflake }
    override val guild: Snowflake? by lazy { message.guildSnowflake }
}
