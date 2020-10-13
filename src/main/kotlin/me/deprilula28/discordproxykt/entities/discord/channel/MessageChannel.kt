package me.deprilula28.discordproxykt.entities.discord.channel

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.builder.EmbedBuilder
import me.deprilula28.discordproxykt.builder.MessageConversion
import me.deprilula28.discordproxykt.entities.PartialEntity
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.Timestamp
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.entities.discord.message.PartialMessage
import me.deprilula28.discordproxykt.builder.MessageBuilder
import me.deprilula28.discordproxykt.entities.discord.Guild
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.message.Emoji
import me.deprilula28.discordproxykt.rest.*

/**
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
interface PartialMessageChannel: PartialEntity {
    fun fetchMessage(message: Snowflake): PartialMessage.Upgradeable = PartialMessage.new(this, message)
    
    val fetchPins: RestAction<List<Message>>
        get() = bot.request(RestEndpoint.GET_PINNED_MESSAGES.path(snowflake.id), { (this as JsonArray).map {
            Message(it as JsonObject, bot)
        } })
    
    val fetchMessages: PaginatedAction<Message>
        get() = PaginatedAction(
            bot, { Message(this as  JsonObject, bot) },
            RestEndpoint.GET_CHANNEL_MESSAGES, snowflake.id,
        )
    
    fun typing() = bot.request(RestEndpoint.TRIGGER_TYPING_INDICATOR.path(snowflake.id), { Unit })
    
    fun send(message: MessageConversion): IRestAction<Message>
        = bot.request(RestEndpoint.CREATE_MESSAGE.path(snowflake.id), { Message(this as JsonObject, bot) }) {
            message.toMessage().first
        }
    
    fun send(content: String) = send(MessageBuilder().setContent(content))
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchPins"))
    fun retrievePinnedMessages() = fetchPins
    @Deprecated("JDA Compatibility Function", ReplaceWith("typing()"))
    fun sendTyping() = typing()
    @Deprecated("JDA Compatibility Function", ReplaceWith("send(text)"))
    fun sendMessage(text: String) = send(text)
    @Deprecated("JDA Compatibility Function", ReplaceWith("send(embed)"))
    fun sendMessage(message: MessageConversion) = send(message)
    @Deprecated("JDA Compatibility Function", ReplaceWith("send(String.format(format, args))"))
    fun sendMessageFormat(format: String, vararg args: String) = send(String.format(format, args))
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(id)).edit(text)"))
    fun editMessageById(id: String, text: String) = fetchMessage(Snowflake(id)).edit(text)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(id)).edit(text)"))
    fun editMessageById(id: Long, text: String) = fetchMessage(Snowflake(id.toString())).edit(text)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(id.toString())).edit(message)"))
    fun editMessageById(id: String, message: MessageConversion) = fetchMessage(Snowflake(id)).edit(message)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(id.toString())).edit(message)"))
    fun editMessageById(id: Long, message: MessageConversion) = fetchMessage(Snowflake(id.toString())).edit(message)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(id)).edit(String.format(format, args))"))
    fun editMessageFormatById(id: String, format: String, vararg args: String) = fetchMessage(Snowflake(id)).edit(String.format(format, args))
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(id.toString())).edit(String.format(format, args))"))
    fun editMessageFormatById(id: Long, format: String, vararg args: String) = fetchMessage(Snowflake(id.toString())).edit(String.format(format, args))
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(id))"))
    fun retrieveMessageById(id: String) = fetchMessage(Snowflake(id))
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(id.toString()))"))
    fun retrieveMessageById(id: Long) = fetchMessage(Snowflake(id.toString()))
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(id)).delete()"))
    fun deleteMessageById(id: String) = fetchMessage(Snowflake(id)).delete()
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(id.toString())).delete()"))
    fun deleteMessageById(id: Long) = fetchMessage(Snowflake(id.toString())).delete()
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(id)).pin()"))
    fun pinMessageById(id: String) = fetchMessage(Snowflake(id)).pin()
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(id.toString())).pin()"))
    fun pinMessageById(id: Long) = fetchMessage(Snowflake(id.toString())).pin()
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(id)).unpin()"))
    fun unpinMessageById(id: String) = fetchMessage(Snowflake(id)).unpin()
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(id.toString())).unpin()"))
    fun unpinMessageById(id: Long) = fetchMessage(Snowflake(id.toString())).unpin()
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message)).addReaction(emote)"))
    fun addReactionById(message: String, emote: String) = fetchMessage(Snowflake(message)).addReaction(emote)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message)).addReaction(emote)"))
    fun addReactionById(message: String, emote: Emoji) = fetchMessage(Snowflake(message)).addReaction(emote)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message.toString())).addReaction(emote)"))
    fun addReactionById(message: Long, emote: String) = fetchMessage(Snowflake(message.toString())).addReaction(emote)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message.toString())).addReaction(emote)"))
    fun addReactionById(message: Long, emote: Emoji) = fetchMessage(Snowflake(message.toString())).addReaction(emote)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message)).removeReaction(emote)"))
    fun removeReactionById(message: String, emote: String) = fetchMessage(Snowflake(message)).removeReaction(emote)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message)).removeReaction(emote)"))
    fun removeReactionById(message: String, emote: Emoji) = fetchMessage(Snowflake(message)).removeReaction(emote)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message.toString())).removeReaction(emote)"))
    fun removeReactionById(message: Long, emote: String) = fetchMessage(Snowflake(message.toString())).removeReaction(emote)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message.toString())).removeReaction(emote)"))
    fun removeReactionById(message: Long, emote: Emoji) = fetchMessage(Snowflake(message.toString())).removeReaction(emote)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message)).fetchReactions(emote)"))
    fun retrieveReactionUsersById(message: String, emote: Emoji) = fetchMessage(Snowflake(message)).fetchReactions(emote)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message)).fetchReactions(emote)"))
    fun retrieveReactionUsersById(message: String, emote: String) = fetchMessage(Snowflake(message)).fetchReactions(emote)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message.toString())).fetchReactions(emote)"))
    fun retrieveReactionUsersById(message: Long, emote: Emoji) = fetchMessage(Snowflake(message.toString())).fetchReactions(emote)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message.toString())).fetchReactions(emote)"))
    fun retrieveReactionUsersById(message: Long, emote: String) = fetchMessage(Snowflake(message.toString())).fetchReactions(emote)
    
    interface Upgradeable: PartialMessageChannel
}

interface MessageChannel {
    /**
     * when the last pinned message was pinned. This may be null in events such as GUILD_CREATE when a message is not pinned.
     */
    val lastPinTimestamp: Timestamp?
    
    /**
     * the id of the last message sent in this channel (may not point to an existing or valid message)
     */
    val lastMessage: PartialMessage.Upgradeable
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("lastMessage.snowflake.id"))
    val latestMessageId: String
        get() = lastMessage.snowflake.id
    @Deprecated("JDA Compatibility Field", ReplaceWith("lastMessage.snowflake.idLong"))
    val latestMessageIdLong: Long
        get() = lastMessage.snowflake.idLong
    @Deprecated("JDA Compatibility Function", ReplaceWith("true"))
    fun hasLatestMessage() = true
}
