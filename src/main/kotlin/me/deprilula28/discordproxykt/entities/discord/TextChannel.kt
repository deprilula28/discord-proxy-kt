package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.entities.discord.message.PartialMessage
import me.deprilula28.discordproxykt.rest.IRestAction
import me.deprilula28.discordproxykt.rest.RestAction
import me.deprilula28.discordproxykt.rest.RestEndpoint

/**
 * This type is used for operations when an ID of a {@link me.deprilula28.discordproxykt.entities.discord.TextChannel TextChannel} is known.
 * <br>
 * If it is {@link me.deprilula28.discordproxykt.entities.discord.PartialTextChannel$Upgradeable Upgradeable},
 * you can get data of a text channel by calling `await()` or `request()`.
 */
interface PartialTextChannel: IPartialEntity, Message.Mentionable {
    /// Retrieve a message by the given ID.
    /// <br>
    /// This will check the cache and return a PartialMessage with a free request if possible.
    operator fun get(message: Snowflake): PartialMessage.Upgradeable
        = object: PartialMessage.Upgradeable,
            RestAction<Message>(bot, { Message(this as JsonObject, bot) }, RestEndpoint.GET_CHANNEL_MESSAGE, snowflake.id, message.id) {
            override val snowflake: Snowflake = message
        }
    
    fun bulkDelete(messages: Collection<Message>) = bulkDeleteSnowflake(messages.map { it.snowflake })
    fun bulkDeleteSnowflake(messages: Collection<Snowflake>)
            = RestAction(bot, { Unit }, RestEndpoint.BULK_DELETE_MESSAGES, snowflake.id) {
        Json.encodeToString(messages.map { it.id })
    }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("bulkDelete"))
    fun deleteMessages(messages: Collection<Message>) = bulkDeleteSnowflake(messages.map { it.snowflake })
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("bulkDeleteSnowflake"))
    fun deleteMessagesByIds(messages: Collection<String>) = bulkDeleteSnowflake(messages.map { Snowflake(it) })
    
    interface Upgradeable: PartialTextChannel, IRestAction<TextChannel>
    
    override val asMention: String
        get() = "<#${snowflake.id}>"
}

/**
 * a text channel within a server
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
class TextChannel(map: JsonObject, bot: DiscordProxyKt):
    Entity(map, bot),
    MessageChannel,
    GuildChannel,
    PartialTextChannel
{
    /**
     * the channel topic (0-1024 characters)
     */
    val topic: String by map.delegateJson(JsonElement::asString)
    /**
     * whether the channel is nsfw
     */
    val nsfw: Boolean by map.delegateJson(JsonElement::asBoolean)
    /**
     * amount of seconds a user has to wait before sending another message (0-21600); bots, as well as users with the permission manage_messages or manage_channel, are unaffected
     */
    val rateLimitPerUser: Int? by map.delegateJsonNullable(JsonElement::asInt, "rate_limit_per_user")
    
    override val name: String by map.delegateJson(JsonElement::asString)
    override val lastPinTimestamp: Timestamp? by map.delegateJsonNullable(JsonElement::asTimestamp, "last_pin_timestamp")
    override val lastMessageId: Snowflake by map.delegateJson(JsonElement::asSnowflake, "last_message_id")
    
    override val guildSnowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "guild_id")
    override val position: Int by map.delegateJson(JsonElement::asInt)
    override val categorySnowflake: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake, "parent_id")
    
    override val permissions: List<PermissionOverwrite> by map.delegateJson({
        (this as JsonArray).map {
            PermissionOverwrite(it as JsonObject, bot)
        }
    }, "permission_overwrites")
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("nsfw"))
    fun isNSFW(): Boolean = nsfw
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("rateLimitPerUser"))
    val slowmode: Int? by ::rateLimitPerUser
    
    // TODO
    // https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/entities/TextChannel.html#createCopy()
    // https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/entities/TextChannel.html#createCopy(net.dv8tion.jda.api.entities.Guild)
    // https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/entities/TextChannel.html#deleteWebhookById(java.lang.String)
}