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
                        = IRestAction.FuturesRestAction(guild.bot) {
                    guild.fetchChannels.request().thenApply {
                        it.find { ch -> ch.snowflake == id } as TextChannel
                    }
                }
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
    override val guild: PartialGuild by ::internalGuild
    override val lastMessage: PartialMessage by map.delegateJson({ fetchMessage(asSnowflake()) }, "last_message_id")
    
    override val name: String by map.delegateJson(JsonElement::asString)
    override val position: Int by map.delegateJson(JsonElement::asInt)
    override val category: PartialCategory? by map.delegateJsonNullable({ PartialCategory.new(guild, asSnowflake()) }, "parent_id")
    override val permissions: List<PermissionOverwrite> by map.delegateJson({
        (this as JsonArray).map { it.asPermissionOverwrite(this@TextChannel, guild, bot) }
    }, "permission_overwrites")
    
    val fetchWebhooks: IRestAction<List<Webhook>>
        get() = assertPermissions(this, Permissions.MANAGE_WEBHOOKS) {
            bot.request(
                RestEndpoint.GET_CHANNEL_WEBHOOKS.path(snowflake.id),
                { (this as JsonArray).map { Webhook(it as JsonObject, bot) } }
            )
        }
    
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
    
    override fun upgrade(): IRestAction<TextChannel> = IRestAction.ProvidedRestAction(bot, this)
    
    override fun fetchMessage(message: Snowflake): PartialMessage
        = PartialMessage.new(this as GuildChannel, message)
    
    override val changes: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
    
    /**
     * Requests that this guild gets edited based on the altered fields.
     */
    override fun edit(): IRestAction<TextChannel> {
        if (changes.isEmpty()) throw InvalidRequestException("No changes have been made to this channel, yet `edit()` was called.")
        return assertPermissions(this, Permissions.MANAGE_CHANNELS) {
            bot.request(RestEndpoint.MODIFY_CHANNEL.path(snowflake.id), { this@TextChannel.apply { map = this@request as JsonObject } }) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
    
    fun createCopy(guild: PartialGuild) = TextChannelBuilder(guild, bot).apply {
        // TODO fill this with copying fields
    }
    fun createCopy() = createCopy(guild)
    
    fun webhookBuilder(): WebhookBuilder = WebhookBuilder(this, bot)
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("webhookBuilder().apply { this@apply.name = name }"))
    fun createWebhook(name: String) = webhookBuilder().apply { this@apply.name = name }
    @Deprecated("JDA Compatibility Field", ReplaceWith("fetchWebhooks"))
    fun retrieveWebhooks() = fetchWebhooks
    
    // TODO canTalk(), canTalk(Member) and also some permission checking stuff
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("rateLimitPerUser"))
    val slowmode: Int? by ::rateLimitPerUser
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("category?.upgade()?.request()?.get()"))
    val parent: Category?
        get() = category?.upgrade()?.request()?.get()
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
        return assertPermissions(this, Permissions.MANAGE_GUILD) {
            bot.request(
                RestEndpoint.CREATE_GUILD_CHANNEL.path(internalGuild.snowflake.id),
                { this@TextChannelBuilder.apply { map = this@request as JsonObject } },
            ) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
}
