package me.deprilula28.discordproxykt.entities.discord.message

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.*
import me.deprilula28.discordproxykt.entities.discord.channel.PartialMessageChannel
import me.deprilula28.discordproxykt.entities.discord.channel.PartialPrivateChannel
import me.deprilula28.discordproxykt.entities.discord.channel.PartialTextChannel
import me.deprilula28.discordproxykt.rest.*
import java.time.OffsetDateTime
import java.util.*

// TODO This
interface PartialMessage: IPartialEntity {
    companion object {
        fun new(channel: PartialMessageChannel, id: Snowflake): Upgradeable
                = object: Upgradeable,
            RestAction<Message>(
                channel.bot,
                RestEndpoint.GET_CHANNEL_MESSAGE.path(channel.snowflake.id, id.id), { Message(this as JsonObject, channel.bot) }
            ) {
            override val snowflake: Snowflake = id
        }
        
        // Includes permission check for reading the message in the guild
        // TODO make this less of a spaghetti mess
        fun new(guild: PartialGuild, channel: PartialMessageChannel, id: Snowflake): Upgradeable
                = object: Upgradeable,
                IRestAction.FuturesRestAction<Message>(channel.bot, {
                    guild.fetchUserPermissions.request().thenCompose {
                        guild.checkPerms(arrayOf(Permissions.READ_MESSAGE_HISTORY), it)
                        RestAction(
                            channel.bot,
                            RestEndpoint.GET_CHANNEL_MESSAGE.path(channel.snowflake.id, id.id), { Message(this as JsonObject, channel.bot) }
                        ).request()
                    }
                }) {
            override val snowflake: Snowflake = id
        }
    }
    
    fun editMessage(text: CharSequence) {}
    fun editMessage(embed: Embed) {}
    fun editMessageFormat(str: String, vararg format: Any) = editMessage(String.format(str, *format))
    
    fun delete() {}
    fun pin() {}
    fun unpin() {}
    
    fun addReaction(emoji: Emoji) {}
    fun addReaction(unicode: String) {}
    
    fun clearReactions() {}
    fun clearReactions(emoji: Emoji) {}
    fun clearReactions(emoji: Emoji, user: User) {} // TODO Make into partial user
    fun clearReactions(unicode: String) {}
    fun clearReactions(unicode: String, user: User) {} // TODO Make into partial user
    
    interface Upgradeable: PartialMessage, IRestAction<Message>
}

/**
 * https://discord.com/developers/docs/resources/channel#message-object
 */
class Message(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), PartialMessage {
    /**
     * id of the channel the message was sent in
     */
    val channelRaw: Snowflake by map.delegateJson(JsonElement::asSnowflake, "channel_id")
    /**
     * id of the guild the message was sent in
     */
    val guild: PartialGuild.Upgradeable? by map.delegateJsonNullable({ PartialGuild.new(asSnowflake(), bot) }, "guild_id")
    /**
     * the author of this message (not guaranteed to be a valid user, see below)
     */
    val author: User by map.delegateJson({ User(this as JsonObject, bot) })
    /**
     * member properties for this message's author
     */
    val member: Member? by map.delegateJsonNullable({ if (guild != null) Member(guild ?: throw UnavailableField(), this as JsonObject, bot, author) else null })
    /**
     * contents of the message
     */
    val content: String by map.delegateJson(JsonElement::asString)
    /**
     * when this message was sent
     */
    val timestamp: Timestamp by map.delegateJson(JsonElement::asTimestamp)
    /**
     * when this message was edited (or null if never)
     */
    val editTimestamp: Timestamp? by map.delegateJsonNullable(JsonElement::asTimestamp, "edited_timestamp")
    /**
     * whether this was a TTS message
     */
    val tts: Boolean by map.delegateJson(JsonElement::asBoolean)
    /**
     * whether this message mentions everyone
     */
    val mentionEveryone: Boolean by map.delegateJson(JsonElement::asBoolean, "mention_everyone")
    /**
     * users specifically mentioned in the message
     */
    val mentions: List<User> by map.delegateJson({ (this as JsonArray).map { User(it as JsonObject, bot) } })
    /**
     * roles specifically mentioned in this message
     */
    val mentionRoles: List<PartialRole> = listOf()//TODO by map.delegateJson({ (this as JsonArray).map { it.asSnowflake() } }, "mention_roles")
    /**
     * channels specifically mentioned in this message
     */
    val mentionChannels: List<PartialTextChannel.Upgradeable>? by map.delegateJsonNullable({
        if (guild == null) null
        else (this as JsonArray?)?.map { PartialTextChannel.new(guild!!, (it as JsonObject)["id"]!!.asSnowflake()) }
    }, "mention_channels")
    /**
     * any attached files
     */
    val attachments: List<Attachment> by map.delegateJson({ (this as JsonArray).map { Attachment(it as JsonObject, bot) } })
    /**
     * any embedded content
     */
    val embeds: List<Embed> by map.delegateJson({ (this as JsonArray).map { Embed(it as JsonObject, bot) } })
    /**
     * reactions to the message
     */
    val reactions: List<Reaction>? by map.delegateJsonNullable({ (this as JsonArray).map { Reaction(it as JsonObject, bot) } })
    /**
     * whether this message is pinned
     */
    val pinned: Boolean by map.delegateJson(JsonElement::asBoolean)
    /**
     * if the message is generated by a webhook, this is the webhook's id
     */
    val webhookId: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake)
    /**
     * type of message
     */
    val type: Type by map.delegateJson({ Type.values()[asInt()] })
    /**
     * message flags ORd together, describes extra features of the message
     */
    val flags: EnumSet<Flags> by map.delegateJson({ asLong().bitSetToEnumSet(Flags.values()) })
    
    // JDA Compatibility
    fun getMentions(vararg types: MentionType): List<Mentionable> = types.flatMap { it.method(this@Message) }
    @Suppress("UNCHECKED_CAST") fun isMentioned(mentionable: Mentionable, vararg types: List<MentionType>): Boolean
            = getMentions(*types.ifEmpty { MentionType.values() } as Array<out MentionType>).contains(mentionable)
    
    /**
     * @returns PartialTextChannel.Upgradeable if this is a text channel.
     * @throws UnavailableField if there is no valid guild.
     */
    val textChannel: PartialTextChannel.Upgradeable? by lazy { PartialTextChannel.new(guild ?: throw UnavailableField(), channelRaw) }
    /**
     * @returns PartialTextChannel.Upgradeable if this is a text channel.
     * @throws UnavailableField if there is no valid guild.
     */
    val privateChannel: PartialPrivateChannel.Upgradeable? by lazy {
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
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("mentions"))
    val mentionedUsers: List<User> by ::mentions
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("mentionEveryone"))
    fun mentionsEveryone() = mentionEveryone
    
    //TODO Fetch these
    //    @Deprecated("JDA Compatibility Field", ReplaceWith("mentionChannels"))
    //    val mentionedChannels: List<MessageChannel>
    //        get() = mentionChannels
    //    @Deprecated("JDA Compatibility Field", ReplaceWith("mentionRoles"))
    //    val mentionedRoles: List<Role>
    //        get() = mentionRoles
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("editTimestamp.offsetDateTime"))
    val timeEdited: OffsetDateTime?
        get() = editTimestamp?.offsetDateTime
    
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
}