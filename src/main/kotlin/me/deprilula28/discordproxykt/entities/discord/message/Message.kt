package me.deprilula28.discordproxykt.entities.discord.message

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.assertPermissions
import me.deprilula28.discordproxykt.builder.MessageBuilder
import me.deprilula28.discordproxykt.builder.MessageConversion
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.*
import me.deprilula28.discordproxykt.entities.discord.channel.*
import me.deprilula28.discordproxykt.rest.*
import java.time.OffsetDateTime
import java.util.*

interface PartialMessage: PartialEntity {
    val channelRaw: Snowflake

    companion object {
        fun new(channel: PartialMessageChannel, id: Snowflake): PartialMessage
            = object: PartialMessage {
                override val bot: DiscordProxyKt = channel.bot
                override val channelRaw: Snowflake = channel.snowflake
                override val snowflake: Snowflake = id
        
                override fun upgrade(): IRestAction<Message>
                    = bot.request(
                        RestEndpoint.GET_CHANNEL_MESSAGE.path(channel.snowflake.id, id.id),
                        { Message(this as JsonObject, channel.bot) },
                    )
            }
        
        // Includes permission check for reading the message in the guild
        // TODO make this less of a spaghetti mess
        fun new(channel: GuildChannel, id: Snowflake): PartialMessage
            = object: PartialMessage {
                override val channelRaw: Snowflake = channel.snowflake
                override val bot: DiscordProxyKt = channel.bot
                override val snowflake: Snowflake = id
    
                override fun upgrade(): IRestAction<Message>
                    = IRestAction.coroutine(channel.bot) {
                        assertPermissions(channel, Permissions.READ_MESSAGE_HISTORY)
                        RestAction(channel.bot, RestEndpoint.GET_CHANNEL_MESSAGE.path(channel.snowflake.id, id.id),
                                   { Message(this as JsonObject, channel.bot) }).await()
                    }
            }
    }
    
    fun upgrade(): IRestAction<Message>
    
    fun fetchReactions(emoji: Emoji): PaginatedAction<User>
        = PaginatedAction(bot, { User(this as JsonObject, bot) }, RestEndpoint.GET_REACTIONS, channelRaw.id, snowflake.id, emoji.toUriPart())
    
    fun edit(message: MessageConversion): IRestAction<Message>
        = bot.request(RestEndpoint.EDIT_MESSAGE.path(channelRaw.id, snowflake.id), { Message(this as JsonObject, bot) }) {
            message.toMessage().first
        }
    
    fun delete(): IRestAction<Unit> = bot.request(RestEndpoint.DELETE_MESSAGE.path(channelRaw.id, snowflake.id), { Unit })
    fun pin(): IRestAction<Unit> = bot.request(RestEndpoint.ADD_PINNED_CHANNEL_MESSAGE.path(channelRaw.id, snowflake.id), { Unit })
    fun unpin(): IRestAction<Unit> = bot.request(RestEndpoint.DELETE_PINNED_CHANNEL_MESSAGE.path(channelRaw.id, snowflake.id), { Unit })
    
    fun addReaction(emoji: Emoji): IRestAction<Unit>
        = bot.request(RestEndpoint.CREATE_REACTION.path(channelRaw.id, snowflake.id, emoji.toUriPart()), { Unit })
    fun removeReaction(emoji: Emoji): IRestAction<Unit>
        = bot.request(RestEndpoint.DELETE_OWN_REACTION.path(channelRaw.id, snowflake.id, emoji.toUriPart()), { Unit })
    fun removeReaction(emoji: Emoji, user: PartialUser): IRestAction<Unit>
        = bot.request(RestEndpoint.DELETE_USER_REACTION.path(channelRaw.id, snowflake.id, emoji.toUriPart(), user.snowflake.id), { Unit })
    
    fun clearReactions(): IRestAction<Unit>
            = bot.request(RestEndpoint.DELETE_ALL_REACTIONS.path(channelRaw.id, snowflake.id), { Unit })
    fun clearReactions(emoji: Emoji): IRestAction<Unit>
            = bot.request(RestEndpoint.DELETE_ALL_REACTIONS_FOR_EMOJI.path(channelRaw.id, snowflake.id, emoji.toUriPart()), { Unit })
    
    fun edit(content: String) = edit(MessageBuilder().setContent(content))
    fun addReaction(unicode: String) = addReaction(UnicodeEmoji(unicode))
    fun removeReaction(unicode: String) = removeReaction(UnicodeEmoji(unicode))
    fun removeReaction(unicode: String, user: PartialUser) = removeReaction(UnicodeEmoji(unicode), user)
    fun clearReactions(unicode: String) = clearReactions(UnicodeEmoji(unicode))
    fun fetchReactions(unicode: String) = fetchReactions(UnicodeEmoji(unicode))
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("edit(text)"))
    fun editMessage(text: String) = edit(text)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchReactions(unicode)"))
    fun retrieveReactionUsers(unicode: String) = fetchReactions(unicode)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchReactions(emoji)"))
    fun retrieveReactionUsers(emoji: Emoji) = fetchReactions(emoji)
    @Deprecated("JDA Compatibility Function", ReplaceWith("edit(embed)"))
    fun editMessage(embed: MessageConversion) = edit(embed)
    @Deprecated("JDA Compatibility Function", ReplaceWith("edit(String.format(str, *format))"))
    fun editMessageFormat(str: String, vararg format: Any) = edit(String.format(str, *format))
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchReactions(unicode)"))
    fun getReactionByUnicode(unicode: String) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchReactions(Snowflake(id))"))
    fun getReactionById(id: String) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchReactions(Snowflake(id.toString()))"))
    fun getReactionById(id: Long) = null
}

/**
 * https://discord.com/developers/docs/resources/channel#message-object
 */
class Message(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), PartialMessage {
    /**
     * id of the channel the message was sent in
     */
    override val channelRaw: Snowflake by parsing(JsonElement::asSnowflake, "channel_id")
    /**
     * id of the guild the message was sent in
     */
    val guild: PartialGuild? by parsingOpt({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    /**
     * the author of this message (not guaranteed to be a valid user, see below)
     */
    val author: User by parsing({ User(this as JsonObject, bot) })
    /**
     * member properties for this message's author
     */
    val member: Member? by parsingOpt(
        { if (guild != null) Member(guild ?: throw UnavailableField(), this as JsonObject, bot, author) else null })
    /**
     * contents of the message
     */
    val content: String by parsing(JsonElement::asString)
    /**
     * used for validating a message was sent
     */
    val nonce: String? by parsingOpt(JsonElement::asString)
    /**
     * when this message was sent
     */
    val timestamp: Timestamp by parsing(JsonElement::asTimestamp)
    /**
     * when this message was edited (or null if never)
     */
    val editTimestamp: Timestamp? by parsingOpt(JsonElement::asTimestamp, "edited_timestamp")
    /**
     * whether this was a TTS message
     */
    val tts: Boolean by parsing(JsonElement::asBoolean)
    /**
     * whether this message mentions everyone
     */
    val mentionEveryone: Boolean by parsing(JsonElement::asBoolean, "mention_everyone")
    /**
     * users specifically mentioned in the message
     */
    val mentions: List<User> by parsing({ (this as JsonArray).map { User(it as JsonObject, bot) } })
    /**
     * roles specifically mentioned in this message
     */
    val mentionRoles: List<PartialRole> by parsing({
        guild?.run {
            (this@parsing as JsonArray).map {
                PartialRole.new(this, it.asSnowflake())
            }
        } ?: listOf()
    }, "mention_roles")
    /**
     * channels specifically mentioned in this message
     */
    val mentionChannels: List<PartialTextChannel>? by parsingOpt({
        if (guild == null) null
        else (this as JsonArray?)?.map {
            PartialTextChannel.new(guild!!,
                                   (it as JsonObject)["id"]!!.asSnowflake())
        }
    }, "mention_channels")
    /**
     * any attached files
     */
    val attachments: List<Attachment> by parsing({ (this as JsonArray).map { Attachment(it as JsonObject, bot) } })
    /**
     * any embedded content
     */
    val embeds: List<Embed> by parsing({ (this as JsonArray).map { Embed(it as JsonObject, bot) } })
    /**
     * reactions to the message
     */
    val reactions: List<Reaction>? by parsingOpt(
        { (this as JsonArray).map { Reaction(it as JsonObject, bot) } })
    /**
     * whether this message is pinned
     */
    val pinned: Boolean by parsing(JsonElement::asBoolean)
    /**
     * if the message is generated by a webhook, this is the webhook's id
     */
    val webhookId: Snowflake? by parsingOpt(JsonElement::asSnowflake)
    /**
     * type of message
     */
    val type: Type by parsing({ Type.values()[asInt()] })
    /**
     * message flags ORd together, describes extra features of the message
     */
    val flags: EnumSet<Flags> by parsing({ asLong().bitSetToEnumSet(Flags.values()) })
    
    // JDA Compatibility
    fun getMentions(vararg types: MentionType): List<Mentionable> = types.flatMap { it.method(this@Message) }
    @Suppress("UNCHECKED_CAST") fun isMentioned(mentionable: Mentionable, vararg types: List<MentionType>): Boolean
            = getMentions(*types.ifEmpty { MentionType.values() } as Array<out MentionType>).contains(mentionable)
    
    /**
     * @returns PartialTextChannel if this is a text channel.
     * @throws UnavailableField if there is no valid guild.
     */
    val textChannel: PartialTextChannel? by lazy { PartialTextChannel.new(guild ?: throw UnavailableField(), channelRaw) }
    /**
     * @returns PartialTextChannel if this is a text channel.
     * @throws UnavailableField if there is no valid guild.
     */
    val privateChannel: PartialPrivateChannel? by lazy {
        if (guild != null) throw UnavailableField()
        PartialPrivateChannel.new(channelRaw, bot)
    }
    
    interface Mentionable {
        val asMention: String
    }
    
    enum class MentionType(val method: Message.() -> List<Mentionable>) {
        USER({ mentions }),
        ROLE({ mentionRoles }),
        CHANNEL({ mentionChannels ?: listOf() }),
    }
    
    enum class Flags {
        CROSSPOSTED, IS_CROSSPOST, SUPPRESS_EMBEDS, SOURCE_MESSAGE_DELETED, URGENT,
    }
    
    enum class Type {
        DEFAULT,
        RECIPIENT_ADD,
        RECIPIENT_REMOVE,
        CALL,
        CHANNEL_NAME_CHANGE,
        CHANNEL_ICON_CHANGE,
        CHANNEL_PINNED_MESSAGE,
        GUILD_MEMBER_JOIN,
        USER_PREMIUM_GUILD_SUBSCRIPTION,
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_1,
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_2,
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_3,
        CHANNEL_FOLLOW_ADD,
        GUILD_DISCOVERY_DISQUALIFIED,
        GUILD_DISCOVERY_REQUALIFIED,
    }
    
    // I'm not deprecating this because what the hell is this API, Discord
    fun surpressEmbeds() = edit(MessageBuilder().setFlags(flags.apply { add(Flags.SUPPRESS_EMBEDS) }))
    val surpressedEmbeds: Boolean
        get() = flags.contains(Flags.SUPPRESS_EMBEDS)
    
    val jumpUrl: String
        get() = "https://discord.com/channels/${guild?.snowflake?.id ?: "@me"}/${channelRaw.id}/${snowflake.id}"
    
    override fun upgrade(): IRestAction<Message> = IRestAction.ProvidedRestAction(bot, this)
    
    override fun edit(message: MessageConversion)
            = bot.request(RestEndpoint.EDIT_MESSAGE.path(channelRaw.id, channelRaw.id), {
        this@Message.apply { map = this@request as JsonObject }
    }) {
        message.toMessage().first
    }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("mentions"))
    val mentionedUsers: List<User> by ::mentions
    @Deprecated("JDA Compatibility Function", ReplaceWith("mentionEveryone"))
    fun mentionsEveryone() = mentionEveryone
    @Deprecated("JDA Compatibility Field", ReplaceWith("mentionChannels"))
    val mentionedChannels: List<MessageChannel>
        get() = mentionChannels?.map { it.upgrade().complete() } ?: listOf()
    @Deprecated("JDA Compatibility Field", ReplaceWith("mentionRoles"))
    val mentionedRoles: List<Role>
        get() = mentionRoles.map { it.upgrade().complete() }
    @Deprecated("JDA Compatibility Field", ReplaceWith("editTimestamp.offsetDateTime"))
    val timeEdited: OffsetDateTime?
        get() = editTimestamp?.offsetDateTime
    @Deprecated("JDA Compatibility Field", ReplaceWith("editTimestamp != null"))
    val edited: Boolean
        get() = editTimestamp != null
    @Deprecated("JDA Compatibility Field", ReplaceWith("content"))
    val contentRaw: String
        get() = content
    @Deprecated("JDA Compatibility Field", ReplaceWith("content"))
    val contentDisplay: String
        get() = content
    @Deprecated("JDA Compatibility Field", ReplaceWith("content.replace(Regex(\"(\\\\*)|(__)|(~)|(\\\\|\\\\|)|(^> )\"), \"\")"))
    val contentStripped: String
        get() = content.replace(Regex("(\\*)|(__)|(~)|(\\|\\|)|(^> )"), "")
    @Deprecated("JDA Compatibility Field", ReplaceWith("textChannel?.upgrade()?.complete()?.category?.upgrade()?.complete()"))
    val category: Category?
        get() = textChannel?.upgrade()?.complete()?.category?.upgrade()?.complete()
}
object Everyone: Message.Mentionable {
    override val asMention: String = "@everyone"
}
