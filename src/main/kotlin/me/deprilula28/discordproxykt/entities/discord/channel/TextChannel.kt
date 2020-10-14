package me.deprilula28.discordproxykt.entities.discord.channel

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.assertPermissions
import me.deprilula28.discordproxykt.builder.MessageBuilder
import me.deprilula28.discordproxykt.builder.MessageConversion
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.entities.PartialEntity
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.Timestamp
import me.deprilula28.discordproxykt.entities.discord.ChannelType
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.PermissionOverwrite
import me.deprilula28.discordproxykt.entities.discord.Permissions
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.entities.discord.message.PartialMessage
import me.deprilula28.discordproxykt.rest.*

/**
 * This type is used for operations when an ID of a {@link me.deprilula28.discordproxykt.entities.discord.channel.TextChannel TextChannel} is known.
 * <br>
 * If it is {@link me.deprilula28.discordproxykt.entities.discord.channel.PartialTextChannel$Upgradeable Upgradeable},
 * you can get data of a text channel by calling `await()` or `request()`.
 */
interface PartialTextChannel: PartialMessageChannel, PartialGuildChannel, PartialEntity, Message.Mentionable {
    val internalGuild: PartialGuild
    
    companion object {
        fun new(guild: PartialGuild, id: Snowflake): Upgradeable
            = object: Upgradeable,
                    IRestAction.FuturesRestAction<TextChannel>(
                        guild.bot,
                        { guild.fetchChannels.request().thenApply { it.find { ch -> ch.snowflake == id } as TextChannel } }) {
                override val snowflake: Snowflake = id
                override val internalGuild: PartialGuild = guild
            }
    }
    
    fun bulkDelete(messages: Collection<PartialMessage>): IRestAction<Unit>
        = bot.request(RestEndpoint.BULK_DELETE_MESSAGES.path(snowflake.id), { Unit }) {
            Json.encodeToString(messages.map { it.snowflake.id })
        }
    
    fun bulkDelete(vararg messages: PartialMessage): IRestAction<Unit>
        = bot.request(RestEndpoint.BULK_DELETE_MESSAGES.path(snowflake.id), { Unit }) {
            Json.encodeToString(messages.map { it.snowflake.id })
        }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("bulkDelete(messages)"))
    fun deleteMessages(messages: Collection<PartialMessage>) = bulkDelete(messages)
    @Deprecated("JDA Compatibility Function", ReplaceWith("bulkDelete(messages.map { fetchMessage(Snowflake(it)) })"))
    fun deleteMessagesByIds(messages: Collection<String>) = bulkDelete(messages.map { fetchMessage(Snowflake(it)) })
    @Deprecated("JDA Compatibility Function", ReplaceWith("bulkDelete(messages.map { fetchMessage(Snowflake(it)) })"))
    fun purgeMessagesById(messages: Collection<String>) = bulkDelete(messages.map { fetchMessage(Snowflake(it)) })
    @Deprecated("JDA Compatibility Function", ReplaceWith("bulkDelete(messages.map { fetchMessage(Snowflake(it)) })"))
    fun purgeMessagesById(vararg messages: String) = bulkDelete(messages.map { fetchMessage(Snowflake(it)) })
    @Deprecated("JDA Compatibility Function", ReplaceWith("bulkDelete(messages)"))
    fun purgeMessages(messages: Collection<PartialMessage>) = bulkDelete(messages)
    @Deprecated("JDA Compatibility Function", ReplaceWith("bulkDelete(*messages)"))
    fun purgeMessages(vararg messages: PartialMessage) = bulkDelete(*messages)
    
    override fun fetchMessage(message: Snowflake): PartialMessage.Upgradeable
        = PartialMessage.new(this as PartialGuildChannel, message)
    
    interface Upgradeable: PartialTextChannel, PartialMessageChannel.Upgradeable, IRestAction<TextChannel>
    
    override val asMention: String
        get() = "<#${snowflake.id}>"
}

/**
 * a text channel within a server
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
class TextChannel(override val internalGuild: PartialGuild, map: JsonObject, bot: DiscordProxyKt):
    Entity(map, bot), MessageChannel, GuildChannel, PartialTextChannel
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
    
    override val lastPinTimestamp: Timestamp? by map.delegateJsonNullable(JsonElement::asTimestamp, "last_pin_timestamp")
    override val guild: PartialGuild.Upgradeable by map.delegateJson({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    override val lastMessage: PartialMessage.Upgradeable by map.delegateJson({ fetchMessage(asSnowflake()) }, "last_message_id")
    
    override val name: String by map.delegateJson(JsonElement::asString)
    override val position: Int by map.delegateJson(JsonElement::asInt)
    override val category: PartialCategory.Upgradeable? by map.delegateJsonNullable({ PartialCategory.new(guild, asSnowflake()) }, "parent_id")
    override val permissions: List<PermissionOverwrite> by map.delegateJson({
        (this as JsonArray).map { asPermissionOverwrite(this@TextChannel, guild, bot) }
    }, "permission_overwrites")
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("nsfw"))
    fun isNSFW(): Boolean = nsfw
    
    // Permission checking
    override fun bulkDelete(messages: Collection<PartialMessage>)
        = assertPermissions(this, Permissions.MANAGE_MESSAGES) { super.bulkDelete(messages) }
    
    override fun bulkDelete(vararg messages: PartialMessage)
        = assertPermissions(this, Permissions.MANAGE_MESSAGES) { super.bulkDelete(*messages) }
    
    override fun send(message: MessageConversion): IRestAction<Message>
        = if (message is MessageBuilder && message.map["tts"]?.asBoolean() == true)
            assertPermissions(this, Permissions.SEND_MESSAGES, Permissions.SEND_TTS_MESSAGES) { super.send(message) }
        else assertPermissions(this, Permissions.SEND_MESSAGES) { super.send(message) }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("rateLimitPerUser"))
    val slowmode: Int? by ::rateLimitPerUser
    
    override val type: ChannelType
        get() = ChannelType.TEXT
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("category?.request()?.get()"))
    val parent: Category?
        get() = category?.request()?.get()
}
