package me.deprilula28.discordproxykt.entities.discord.channel

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.entities.IPartialEntity
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.Timestamp
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.entities.discord.message.PartialMessage
import me.deprilula28.discordproxykt.rest.RestAction
import me.deprilula28.discordproxykt.rest.RestEndpoint

/**
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
interface MessageChannel {
    /**
     * when the last pinned message was pinned. This may be null in events such as GUILD_CREATE when a message is not pinned.
     */
    val lastPinTimestamp: Timestamp?
    
    /**
     * the id of the last message sent in this channel (may not point to an existing or valid message)
     */
    val lastMessageId: Snowflake
}

interface PartialMessageChannel: IPartialEntity {
    operator fun get(message: Snowflake): PartialMessage.Upgradeable = PartialMessage.new(this, message)
    
    val pinnedMessages: RestAction<List<Message>>
        get() = bot.request(RestEndpoint.GET_PINNED_MESSAGES.path(snowflake.id), { (this as JsonArray).map {
            Message(it as JsonObject, bot)
        } })
    
    fun typing() = bot.request(RestEndpoint.TRIGGER_TYPING_INDICATOR.path(snowflake.id), { Unit })
    
    
    
    interface Upgradeable: PartialMessageChannel
}