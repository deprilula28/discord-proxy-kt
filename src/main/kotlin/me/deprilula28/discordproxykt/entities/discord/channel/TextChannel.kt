package me.deprilula28.discordproxykt.entities.discord.channel

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.assertPermissions
import me.deprilula28.discordproxykt.builder.MessageBuilder
import me.deprilula28.discordproxykt.builder.MessageConversion
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.entities.PartialEntity
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.Timestamp
import me.deprilula28.discordproxykt.entities.discord.*
import me.deprilula28.discordproxykt.entities.discord.message.Emoji
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
        fun new(guild: PartialGuild, id: Snowflake): PartialTextChannel
            = object: PartialTextChannel {
                override val snowflake: Snowflake = id
                override val bot: DiscordProxyKt = guild.bot
                override val internalGuild: PartialGuild = guild
                override fun upgrade(): IRestAction<TextChannel>
                    = bot.request(RestEndpoint.GET_CHANNEL.path(snowflake.id), { TextChannel(guild, this as JsonObject, bot) })
                override fun toString(): String = "Channel(partial, $type, $guild, $snowflake.id)"
            }
    }
    
    override val type: ChannelType
        get() = ChannelType.TEXT
    
    override fun upgrade(): IRestAction<TextChannel>
    
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
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message)).clearReactions()"))
    fun clearReactionsById(message: String) = fetchMessage(Snowflake(message)).clearReactions()
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message.toString())).clearReactions()"))
    fun clearReactionsById(message: Long) = fetchMessage(Snowflake(message.toString())).clearReactions()
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message)).clearReactions(emote)"))
    fun clearReactionsById(message: String, emote: String) = fetchMessage(Snowflake(message)).clearReactions(emote)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message)).clearReactions(emote)"))
    fun clearReactionsById(message: String, emote: Emoji) = fetchMessage(Snowflake(message)).clearReactions(emote)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message.toString())).clearReactions(emote)"))
    fun clearReactionsById(message: Long, emote: String) = fetchMessage(Snowflake(message.toString())).clearReactions(emote)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMessage(Snowflake(message.toString())).clearReactions(emote)"))
    fun clearReactionsById(message: Long, emote: Emoji) = fetchMessage(Snowflake(message.toString())).clearReactions(emote)
    
    override fun fetchMessage(message: Snowflake): PartialMessage
        = PartialMessage.new(this as PartialMessageChannel, message)
    
    override val asMention: String
        get() = "<#${snowflake.id}>"
}

/**
 * a text channel within a server
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
open class TextChannel(override val internalGuild: PartialGuild, map: JsonObject, bot: DiscordProxyKt):
    Entity(map, bot), MessageChannel, GuildChannel, PartialTextChannel, EntityManager<TextChannel>
{
    /**
     * the channel topic (0-1024 characters)
     */
    var topic: String by parsing(JsonElement::asString, {
        if (it.length !in 0 .. 1024) throw InvalidRequestException("Channel topic must be between 0 and 1024 characters in length")
        Json.encodeToJsonElement(it)
    })
    /**
     * whether the channel is nsfw
     */
    var nsfw: Boolean by parsing(JsonElement::asBoolean, Json::encodeToJsonElement)
    /**
     * amount of seconds a user has to wait before sending another message (0-21600); bots, as well as users with the permission manage_messages or manage_channel, are unaffected
     */
    var rateLimitPerUser: Int? by parsingOpt(JsonElement::asInt, Json::encodeToJsonElement, "rate_limit_per_user")
    
    override val lastPinTimestamp: Timestamp? by parsingOpt(JsonElement::asTimestamp, "last_pin_timestamp")
    override val guild: PartialGuild by ::internalGuild
    override val lastMessage: PartialMessage by parsing({ fetchMessage(asSnowflake()) }, "last_message_id")
    
    override var name: String by parsing(JsonElement::asString, {
        if (it.length !in 2 .. 100) throw InvalidRequestException("Channel name must be between 2 and 100 characters in length")
        Json.encodeToJsonElement(it)
    })
    override var position: Int by parsing(JsonElement::asInt, Json::encodeToJsonElement)
    override var category: PartialCategory? by parsingOpt(
        { PartialCategory.new(guild, asSnowflake()) },
        { it?.run { JsonPrimitive(snowflake.id) } ?: JsonNull },
        "parent_id",
    )
    override var permissions: List<PermissionOverwrite> by parsing({
        (this as JsonArray).map { it.asPermissionOverwrite(this@TextChannel, guild, bot) }
    }, { Json.encodeToJsonElement(it.map(PermissionOverwrite::toObject)) }, "permission_overwrites")
    
    val fetchWebhooks: IRestAction<List<Webhook>>
        get() = IRestAction.coroutine(guild.bot) {
            assertPermissions(this, Permissions.MANAGE_WEBHOOKS)
            bot.request(RestEndpoint.GET_CHANNEL_WEBHOOKS.path(snowflake.id),
                        { (this as JsonArray).map { Webhook(it as JsonObject, bot) } }).await()
        }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("nsfw"))
    fun isNSFW(): Boolean = nsfw
    
    // Permission checking
    override fun bulkDelete(messages: Collection<PartialMessage>)
        = IRestAction.coroutine(guild.bot) {
            assertPermissions(this, Permissions.MANAGE_MESSAGES)
            super.bulkDelete(messages).await()
        }
    
    override fun bulkDelete(vararg messages: PartialMessage)
        = IRestAction.coroutine(guild.bot) {
            assertPermissions(this, Permissions.MANAGE_MESSAGES)
            super.bulkDelete(*messages).await()
        }
    
    override fun send(message: MessageConversion): IRestAction<Message>
        = IRestAction.coroutine(guild.bot) {
            if (message is MessageBuilder && message.map["tts"]?.asBoolean() == true)
                assertPermissions(this, Permissions.SEND_MESSAGES, Permissions.SEND_TTS_MESSAGES)
            else assertPermissions(this, Permissions.SEND_MESSAGES)
            super.send(message).await()
        }
    
    override fun upgrade(): IRestAction<TextChannel> = IRestAction.ProvidedRestAction(bot, this)
    
    override fun fetchMessage(message: Snowflake): PartialMessage
        = PartialMessage.new(this as GuildChannel, message)
    
    override val changes: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
    
    /**
     * Requests that this guild gets edited based on the altered fields.
     */
    override fun edit(): IRestAction<TextChannel> {
        if (changes.isEmpty()) throw InvalidRequestException(
            "No changes have been made to this channel, yet `edit()` was called.")
        return IRestAction.coroutine(guild.bot) {
            assertPermissions(this, Permissions.MANAGE_CHANNELS)
            bot.request(RestEndpoint.MODIFY_CHANNEL.path(snowflake.id),
                        { this@TextChannel.apply { map = this@request as JsonObject } }) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }.await()
        }
    }
    
    fun createCopy(guild: PartialGuild) = TextChannelBuilder(guild, bot).apply {
        name = this@TextChannel.name
        position = this@TextChannel.position
        permissions = this@TextChannel.permissions
        category = this@TextChannel.category
        topic = this@TextChannel.topic
        nsfw = this@TextChannel.nsfw
        rateLimitPerUser = this@TextChannel.rateLimitPerUser
    }
    fun createCopy() = createCopy(guild)
    
    fun webhookBuilder(): WebhookBuilder = WebhookBuilder(this, bot)
    
    override fun toString(): String = "Channel($type, $guild, $name, ${snowflake.id})"
    
    fun canTalk() = IRestAction.coroutine(bot) {
        fetchPermissions(guild.fetchSelfMember.await()).await().contains(Permissions.SEND_MESSAGES)
    }
    fun canTalk(member: Member) = IRestAction.coroutine(bot) {
        fetchPermissions(member).await().contains(Permissions.SEND_MESSAGES)
    }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("webhookBuilder().apply { this@apply.name = name }"))
    fun createWebhook(name: String) = webhookBuilder().apply { this@apply.name = name }
    @Deprecated("JDA Compatibility Field", ReplaceWith("fetchWebhooks"))
    fun retrieveWebhooks() = fetchWebhooks
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("rateLimitPerUser"))
    val slowmode: Int? by ::rateLimitPerUser
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("category?.upgrade()?.complete()"))
    val parent: Category?
        get() = category?.upgrade()?.complete()
}

class TextChannelBuilder(guild: PartialGuild, bot: DiscordProxyKt):
    TextChannel(guild, JsonObject(MapNotReady()), bot), EntityBuilder<TextChannel>
{
    /**
     * Creates a text channel based on altered fields and returns it as a rest action.<br>
     * The values of the fields in the builder itself will be updated, making it usable as a TextChannel.
     */
    override fun create(): IRestAction<TextChannel> {
        if (!changes.containsKey("name")) throw InvalidRequestException("Channels require at least a name.")
        changes["type"] = JsonPrimitive(0)
        return IRestAction.coroutine(guild.bot) {
            assertPermissions(this, Permissions.MANAGE_GUILD)
            bot.request(
                RestEndpoint.CREATE_GUILD_CHANNEL.path(internalGuild.snowflake.id),
                { this@TextChannelBuilder.apply { map = this@request as JsonObject } },
            ) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }.await()
        }
    }
    
    override fun toString(): String = "Channel(builder)"
}
