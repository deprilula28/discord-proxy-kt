package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.message.GuildEmoji
import me.deprilula28.discordproxykt.rest.IRestAction
import me.deprilula28.discordproxykt.rest.InvalidRequestException
import me.deprilula28.discordproxykt.rest.RestAction
import me.deprilula28.discordproxykt.rest.RestEndpoint
import java.awt.image.RenderedImage
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO

/**
 * https://discord.com/developers/docs/resources/voice#voice-region-object
 */
class VoiceRegion(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot) {
    /**
     * name of the region
     */
    val name: String by map.delegateJson(JsonElement::asString)
    /**
     * true if this is a vip-only server
     * <br>
     * "VIP" regions are now known as 368kbps regions for boosted servers
     */
    val vip: Boolean by map.delegateJson(JsonElement::asBoolean)
    /**
     * true for a single server that is closest to the current user's client
     */
    val optimal: Boolean by map.delegateJson(JsonElement::asBoolean)
    /**
     * whether this is a deprecated voice region (avoid switching to these)
     */
    val deprecated: Boolean by map.delegateJson(JsonElement::asBoolean)
    /**
     * whether this is a custom voice region (used for events/etc)
     */
    val custom: Boolean by map.delegateJson(JsonElement::asBoolean)
}

/**
 * https://discord.com/developers/docs/resources/guild#ban-object
 */
data class Ban(private val map: JsonObject, private val bot: DiscordProxyKt) {
    val user: User by map.delegateJson({ User(this as JsonObject, bot) })
    val reason: String by map.delegateJson(JsonElement::asString)
}

interface PartialGuild: IPartialEntity {
    val fetchRegions: IRestAction<List<VoiceRegion>>
        get() = RestAction(
            bot, { (this as JsonArray).map { VoiceRegion(it as JsonObject, bot) } },
            RestEndpoint.GET_GUILD_VOICE_REGIONS, snowflake.id,
        )
    
    val fetchChannels: IRestAction<List<GuildChannel>>
        get() = RestAction(
            bot, { (this as JsonArray).mapNotNull(::parseChannel) },
            RestEndpoint.GET_GUILD_CHANNELS, snowflake.id,
        )
    
    fun getMember(user: Snowflake): PartialMember.Upgradeable
        = object: PartialMember.Upgradeable,
            RestAction<Member>(bot, { Member(this as JsonObject, bot) }, RestEndpoint.GET_GUILD_MEMBER, snowflake.id, user.id) {
                override val user: PartialUser by lazy { bot.users[user] }
            }
    
    val fetchEmojis: IRestAction<List<GuildEmoji>>
        get() = RestAction(
            bot, { (this as JsonArray).map { GuildEmoji(it as JsonObject, bot) } },
            RestEndpoint.GET_GUILD_EMOJIS, snowflake.id,
        )
    
    fun fetchEmoji(emoji: Snowflake): RestAction<GuildEmoji>
        = RestAction(
            bot, { GuildEmoji(this as JsonObject, bot) },
            RestEndpoint.GET_GUILD_EMOJI, snowflake.id, emoji.id
        )
    
    val fetchVanityCode: IRestAction<String>
        get() = RestAction(bot, { (this as JsonPrimitive).content }, RestEndpoint.GET_GUILD_VANITY_URL, snowflake.id)
    
    val fetchBans: IRestAction<List<Ban>>
        get() = RestAction(
            bot, { (this as JsonArray).map { Ban(it as JsonObject, bot) } },
            RestEndpoint.GET_GUILDS_BANS, snowflake.id,
        )
    
    fun fetchBan(user: PartialUser): RestAction<Ban>
            = RestAction(
        bot, { Ban(this as JsonObject, bot) },
        RestEndpoint.GET_GUILDS_BAN, snowflake.id, user.snowflake.id
    )
    
    fun addMember(accessToken: String, user: PartialUser): IRestAction<Member>
        = RestAction(
            bot, { Member(this as JsonObject, it) },
            RestEndpoint.ADD_GUILD_MEMBER, snowflake.id, user.snowflake.id,
        )
    
    fun retrievePrunableMemberCount(days: Int)
            = RestAction(bot, { asInt() }, RestEndpoint.GET_GUILD_PRUNE_COUNT, snowflake.id)
    
    interface Upgradeable: PartialGuild, IRestAction<Guild>
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("false"))
    val loaded: Boolean
        get() = false
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchEmojis"))
    fun retrieveEmotes() = fetchEmojis
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchEmoji(Snowflake(id))"))
    fun retrieveEmoteById(id: String) = fetchEmoji(Snowflake(id))
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchEmoji(Snowflake(id.toString()))"))
    fun retrieveEmoteById(id: Long) = fetchEmoji(Snowflake(id.toString()))
    @Deprecated("JDA Compatibility Function", ReplaceWith("emote"))
    fun retrieveEmote(emote: GuildEmoji) = fetchEmoji(emote.snowflake)
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchBans"))
    fun retrieveBanList() = fetchBans
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchBan(bot.users[Snowflake(id)])"))
    fun retrieveBanById(id: String) = fetchBan(bot.users[Snowflake(id)])
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchBan(bot.users[Snowflake(id.toString())])"))
    fun retrieveBanById(id: Long) = fetchBan(bot.users[Snowflake(id.toString())])
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchBan(user)"))
    fun retrieveBan(user: PartialUser) = fetchBan(user)
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("/*no-op*/"))
    fun pruneMemberCache() = println("Warning: pruneMemberCache() is a no-op!")
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("/*no-op*/"))
    fun unloadMember(id: Long) = println("Warning: unloadMember(long) is a no-op!")
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("addMember"))
    fun addMember(accessToken: String, user: String)
            = addMember(accessToken, bot.users[Snowflake(user)])
    @Deprecated("JDA Compatibility Function", ReplaceWith("addMember"))
    fun addMember(accessToken: String, user: Long)
            = addMember(accessToken, bot.users[Snowflake(user.toString())])
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("vanityUrl"))
    fun retrieveVanityUrl() = fetchVanityCode
    
    @Deprecated("JDA Compatbility Field", ReplaceWith("\"https://discord.gg/\" + vanityCode"))
    val vanityUrl: String?
        get() = fetchVanityCode.request().get()?.run { "https://discord.gg/$this" }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("regions"))
    fun retrieveRegions() = RestAction(
        bot,
        {
            val enumSet = EnumSet.noneOf(Region::class.java)
            (this as JsonArray).forEach {
                val region = VoiceRegion(it as JsonObject, bot)
                enumSet.add(Region.valueOf((if (region.vip) "VIP_" else "") + region.name.toUpperCase()))
            }
            enumSet
        },
        RestEndpoint.GET_GUILD_VOICE_REGIONS,
        snowflake.id,
    )
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("regions"))
    fun retrieveRegions(includeDeprecated: Boolean) = retrieveRegions()
    
    // NOT PUBLIC API
    fun parseChannel(it: JsonElement): GuildChannel? {
        val obj = it as JsonObject
        return when (val type = obj["type"]!!.asInt()) {
            0 -> TextChannel(obj, bot)
            2 -> VoiceChannel(obj, bot)
            4 -> Category(obj, bot)
            else -> {
                println("Invalid channel type received for guild ${snowflake.id}: $type")
                null
            }
        }
    }
    
    // The bullshit JDA ones
    // I'm not gonna do anything for these as the guild members intent is assumed to be off for this library
    @Deprecated("JDA Compatibility Function", ReplaceWith("isMember(user)"))
    fun isMember(user: User): Boolean = false
    @Deprecated("JDA Compatibility Function", ReplaceWith("this[user]"))
    fun getMember(user: User) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith("this[user]"))
    fun getMember(userId: String) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith("this[user]"))
    fun getMember(userIdLong: Long) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getMemberByTag(tag: String) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getMemberByTag(tag: String, discrim: String) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getMembersByName(tag: String, ignoreCase: Boolean) = listOf<Member>()
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getMembersByNickname(tag: String, ignoreCase: Boolean) = listOf<Member>()
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getMembersByEffectiveName(tag: String, ignoreCase: Boolean) = listOf<Member>()
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getMembersWithRoles(vararg roles: Role) = listOf<Member>()
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getMembersWithRoles(roles: Collection<Role>) = listOf<Member>()
    
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getGuildChannelById(id: String) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getGuildChannelById(idLong: Long) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getGuildChannelById(channelType: ChannelType, id: String) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getGuildChannelById(channelType: ChannelType, idLong: Long) = null
    
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getCategoryById(id: String) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getCategoryById(idLong: Long) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getCategoriesByName(name: String, ignoreCase: Boolean) = null
    @Deprecated("JDA Compatibility Field", ReplaceWith(""))
    val categories: List<Nothing>
        get() = listOf()
    
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getStoreChannelById(id: String) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getStoreChannelById(idLong: Long) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getStoreChannelsByName(name: String, ignoreCase: Boolean) = null
    @Deprecated("JDA Compatibility Field", ReplaceWith(""))
    val storeChannels: List<Nothing>
        get() = listOf()
    
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getTextChannelById(id: String) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getTextChannelById(idLong: Long) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getTextChannelsByName(name: String, ignoreCase: Boolean) = null
    @Deprecated("JDA Compatibility Field", ReplaceWith(""))
    val textChannels: List<Nothing>
        get() = listOf()
    
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getVoiceChannelById(id: String) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getVoiceChannelById(idLong: Long) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getVoiceChannelsByName(name: String, ignoreCase: Boolean) = null
    @Deprecated("JDA Compatibility Field", ReplaceWith(""))
    val voiceChannels: List<Nothing>
        get() = listOf()
    
    @Deprecated("JDA Compatibility Field", ReplaceWith(""))
    val channels: List<Nothing>
        get() = listOf()
    @Deprecated("JDA Compatibility Field", ReplaceWith(""))
    fun getChannels(boolean: Boolean) = listOf<Nothing>()
    
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getRoleById(id: String) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getRoleById(idLong: Long) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getRolesByName(name: String, ignoreCase: Boolean) = null
    
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getEmoteById(id: String) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getEmoteById(idLong: Long) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun getEmotesByName(name: String, ignoreCase: Boolean) = null
    @Deprecated("JDA Compatibility Field", ReplaceWith(""))
    val emotes: List<Nothing>
        get() = listOf()
    
}

/**
 * Guilds in Discord represent an isolated collection of users and channels, and are often referred to as "servers" in the UI.
 */
class Guild(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), EntityManager<Guild>, PartialGuild {
    /**
     * guild name (2-100 characters, excluding trailing and leading whitespace)
     */
    var name: String by map.delegateJsonMutable(JsonElement::asString, Json::encodeToJsonElement)
    /**
     * id of owner
     */
    var owner: PartialMember.Upgradeable by map.delegateJsonMutable(
        { getMember(asSnowflake()) },
        {
            // TODO Check if self is owner
            Json.encodeToJsonElement(it.user.snowflake.id)
        },
        "owner_id"
    )
    /**
     * application id of the guild creator if it is bot-created
     */
    val applicationSnowflake: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake, "application_id")
    
    /**
     * roles in the guild
     */
    val roles: List<Role> by map.delegateJson({ (this as JsonArray).map { Role(it as JsonObject, bot) } })
    /**
     * custom guild emojis
     */
    val emojis: List<GuildEmoji> by map.delegateJson({ (this as JsonArray).map { GuildEmoji(it as JsonObject, bot) } })
    
    /**
     * the description for the guild, if the guild is discoverable
     */
    val description: String? by map.delegateJsonNullable(JsonElement::asString)
    /**
     * discovery splash hash; only present for guilds with the "DISCOVERABLE" feature
     */
    val discoverableSplash: String? by map.delegateJsonNullable(JsonElement::asString, "discovery_splash")
    /**
     * banner hash
     */
    val banner: String? by map.delegateJsonNullable(JsonElement::asString)
    /**
     * the id of the channel where guilds with the "PUBLIC" feature can display rules and/or guidelines
     */
    val rulesChannelSnowflake: Snowflake? by map.delegateJsonNullable(JsonElement::asSnowflake, "rules_channel_id")
    /**
     * 	the preferred locale of a guild with the "PUBLIC" feature; used in server discovery and notices from Discord; defaults to "en-US"
     */
    var locale: String by map.delegateJsonMutable(JsonElement::asString, Json::encodeToJsonElement, "preferred_locale")
    /**
     * 	the id of the channel where admins and moderators of guilds with the "PUBLIC" feature receive notices from Discord
     */
    var publicUpdatesChannel: PartialTextChannel.Upgradeable? by map.delegateJsonMutableNullable(
        ::delegateChannel,
        { Json.encodeToJsonElement(it?.snowflake?.id) },
        "public_updates_channel_id"
    )
    
    /**
     * icon hash
     */
    val icon: String? by map.delegateJsonNullable(JsonElement::asString)
    /**
     * splash hash
     */
    val iconSplash: String? by map.delegateJsonNullable(JsonElement::asString, "splash")
    
    /**
     * voice region for the guild
     */
    var region: Region by map.delegateJsonMutable(
        { Region.valueOf(asString().toUpperCase().replace("-", "_")) },
        { Json.encodeToJsonElement(it.toString()) },
    )
    /**
     * voice region for the guild
     */
    val regionRaw: String by map.delegateJson(JsonElement::asString, "region")
    /**
     * id of afk channel
     */
    var afkChannel: PartialVoiceChannel.Upgradeable? by map.delegateJsonMutableNullable(
        {
            val afkSnowflake = asSnowflake()
            object: PartialVoiceChannel.Upgradeable {
                override val snowflake: Snowflake = afkSnowflake
                override val bot: DiscordProxyKt = bot
        
                override fun request(): CompletableFuture<VoiceChannel> =
                        fetchChannels.request().thenApply { it.find { ch -> ch.snowflake == snowflake } as VoiceChannel }
            }
        },
        { Json.encodeToJsonElement(it?.snowflake?.id) },
        "afk_channel_id",
    )
    
    /**
     * afk timeout in seconds
     */
    var afkTimeout: Timeout by map.delegateJsonMutable(
        { Timeout.valueOf("SECONDS_${asInt()}") },
        { Json.encodeToJsonElement(it.ordinal) },
        "afk_timeout",
    )
    
    /**
     * true if the server widget is enabled
     */
    val widgetEnabled: Boolean? by map.delegateJsonNullable(JsonElement::asBoolean, "widget_enabled")
    /**
     * the channel id that the widget will generate an invite to, or null if set to no invite
     */
    val widgetChannelSnowflake: PartialTextChannel.Upgradeable? by map.delegateJsonNullable(::delegateChannel, "widget_channel_id")
    
    /**
     * true if the user is the owner of the guild
     * <br>
     * Only sent under "GET Current User Guilds" endpoint, relative to current user
     */
    val userIsOwner: Boolean? by map.delegateJsonNullable(JsonElement::asBoolean, "owner")
    /**
     * total permissions for the user in the guild (excludes overrides)
     * <br>
     * Only sent under "GET Current User Guilds" endpoint, relative to current user
     */
    val userPermissions: EnumSet<Permissions>? by map.delegateJsonNullable({ asLong().bitSetToEnumSet(Permissions.values()) }, "permissions")
    
    /**
     * when this guild was joined at
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val joinedAt: Timestamp? by map.delegateJsonNullable(JsonElement::asTimestamp, "joined_at")
    /**
     * true if this is considered a large guild
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val large: Boolean? by map.delegateJsonNullable(JsonElement::asBoolean)
    /**
     * true if this guild is unavailable due to an outage
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val unavailable: Boolean? by map.delegateJsonNullable(JsonElement::asBoolean)
    /**
     * total number of members in this guild
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val instantMemberCount: Int? by map.delegateJsonNullable(JsonElement::asInt, "member_count")
    /**
     * 	approximate number of members in this guild
     */
    val approxMemberCount: Int? by map.delegateJsonNullable(JsonElement::asInt, "approximate_member_count")
    /**
     * 	approximate number of non-offline members in this guild
     */
    val approxPresenceCount: Int? by map.delegateJsonNullable(JsonElement::asInt, "approximate_presence_count")
    /**
     * users in the guild
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val members: List<Member>? by map.delegateJsonNullable({ (this as JsonArray).map { Member(it as JsonObject, bot) } })
    /**
     * channels in the guild
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val cachedChannels: List<GuildChannel>? by map.delegateJsonNullable({ (this as JsonArray).mapNotNull(::parseChannel) }, "channels")
    
    /**
     * the maximum number of presences for the guild (the default value, currently 25000, is in effect when null is returned)
     */
    val maxPresences: Int? by map.delegateJsonNullable(JsonElement::asInt, "max_presences")
    /**
     * the maximum number of members for the guild
     */
    val maxMembers: Int? by map.delegateJsonNullable(JsonElement::asInt, "max_members")
    /**
     * the maximum amount of users in a video channel
     */
    val maxVideoChannelUsers: Int? by map.delegateJsonNullable(JsonElement::asInt, "max_video_channel_users")
    /**
     * the vanity url code for the guild
     */
    val vanityCode: String? by map.delegateJsonNullable(JsonElement::asString, "vanity_url_code")
    
    /**
     * verification level required for the guild
     */
    var verificationLevel: VerificationLevel by map.delegateJsonMutable(
        { VerificationLevel.values()[asInt()] },
        { Json.encodeToJsonElement(it.ordinal) },
        "verification_level",
    )
    
    /**
     * default message notifications level
     */
    var defaultNotificationLevel: NotificationLevel by map.delegateJsonMutable(
        { NotificationLevel.values()[asInt()] },
        { Json.encodeToJsonElement(it.ordinal) },
        "default_message_notifications",
    )
    
    /**
     * explicit content filter level
     */
    var explicitContentLevel: ExplicitContentFilterLevel by map.delegateJsonMutable(
        { ExplicitContentFilterLevel.values()[asInt()] },
        { Json.encodeToJsonElement(it.ordinal) },
        "explicit_content_filter",
    )
    
    // This is encoded as a list of strings instead of a bitset for god knows why
    /**
     * enabled guild features
     */
    val features: EnumSet<Features> by map.delegateJson({
        val set = EnumSet.noneOf(Features::class.java)
        (this as JsonArray).forEach { set.add(Features.valueOf(it.asString())) }
        set
    })
    
    /**
     * required MFA level for the guild
     */
    val requiredMFALevel: MFALevel by map.delegateJson({ MFALevel.values()[asInt()] }, "mfa_level")
    
    /**
     * premium tier (Server Boost level)
     */
    val boostTier: BoostTier by map.delegateJson({ BoostTier.values()[asInt()] }, "premium_tier")
    /**
     * the number of boosts this guild currently has
     */
    val boosters: Int? by map.delegateJsonNullable(JsonElement::asInt, "premium_subscription_count")
    
    /**
     * the id of the channel where guild notices such as welcome messages and boost events are posted
     */
    val systemChannel: PartialTextChannel.Upgradeable? by map.delegateJsonMutableNullable(
        ::delegateChannel,
        { Json.encodeToJsonElement(it?.snowflake?.id) },
        "system_channel_id",
    )
    
    /**
     * system channel flags
     */
    val systemChannelFlags: EnumSet<SystemChannelFlags> by map.delegateJson({ asLong().bitSetToEnumSet(SystemChannelFlags.values()) }, "system_channel_flags")
    
    val memberCount: Int by lazy { instantMemberCount ?: approxMemberCount ?: -1 }
    val iconUrl: String? by lazy {
        if (icon == null) null
        else "https://cdn.discordapp.com/icons/${snowflake.id}/$icon.${if (icon!!.startsWith("a_")) "gif" else "png"}"
    }
    
    override val edits: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
    
    /**
     * Requests that this guild gets edited based on the altered fields.<br>
     * This object will not be updated to reflect the changes, rather a new Guild object is returned from the RestAction.
     */
    override fun request(): CompletableFuture<Guild>
        = RestAction(bot, { Guild(this as JsonObject, bot) }, RestEndpoint.MODIFY_GUILD, snowflake.id) { Json.encodeToString(edits) }.request()
    
    /**
     * To finish the procedure, the rest action needs to be called.
     */
    fun setIcon(image: RenderedImage, format: String): Guild {
        if (image.width > 1024 || image.height > 1024) throw InvalidRequestException("Neither image dimension may exceed 1024!")
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, format, baos)
        edits["icon"] = JsonPrimitive("data:image/jpeg;base64,${Base64.getEncoder().encodeToString(baos.toByteArray())}")
        return this
    }
    
    /**
     * To finish the procedure, the rest action needs to be called.
     */
    fun setSplash(image: RenderedImage, format: String): Guild {
        if (Features.INVITE_SPLASH !in features) throw InvalidRequestException("Cannot change splash without the INVITE_SPLASH feature!")
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, format, baos)
        edits["splash"] = JsonPrimitive("data:image/jpeg;base64,${Base64.getEncoder().encodeToString(baos.toByteArray())}")
        return this
    }
    
    /**
     * To finish the procedure, the rest action needs to be called.
     */
    fun setBanner(image: RenderedImage, format: String): Guild {
        if (Features.BANNER !in features) throw InvalidRequestException("Cannot change banner without the BANNER feature!")
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, format, baos)
        edits["banner"] = JsonPrimitive("data:image/jpeg;base64,${Base64.getEncoder().encodeToString(baos.toByteArray())}")
        return this
    }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("iconSplash"))
    val splashId: String? by ::iconSplash
    @Deprecated("JDA Compatibility Field", ReplaceWith("icon"))
    val iconId: String? by ::icon
    @Deprecated("JDA Compatibility Field", ReplaceWith("banner"))
    val bannerId: String? by ::banner
    @Deprecated("JDA Compatibility Field", ReplaceWith("https://cdn.discordapp.com/banners/\${snowflake.id}/\$banner.png"))
    val bannerUrl: String?
        get() = banner?.run { "https://cdn.discordapp.com/banners/${snowflake.id}/$banner.png" }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("owner.snowflake.id"))
    val ownerId: String by lazy { owner.user.snowflake.id }
    @Deprecated("JDA Compatibility Field", ReplaceWith("owner.snowflake.idLong"))
    val ownerIdLong: Long by lazy { owner.user.snowflake.idLong }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("boostTier.bitrate"))
    val maxBitrate: Int by boostTier::bitrate
    @Deprecated("JDA Compatibility Field", ReplaceWith("boostTier.fileSize"))
    val maxFileSize: Long by boostTier::fileSize
    @Deprecated("JDA Compatibility Field", ReplaceWith("boostTier.emotes"))
    val maxEmotes: Int by boostTier::emotes
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("bot.guilds[snowflake]"))
    fun retrieveMetaData(): IRestAction<Guild> = bot.guilds[snowflake]
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("this[selfUser]"))
    val selfMember: Member
        get() = getMember(bot.selfUser.request().get().snowflake).request().get()
    
    private fun delegateChannel(json: JsonElement): PartialTextChannel.Upgradeable {
        val systemSnowflake = json.asSnowflake()
        return object: PartialTextChannel.Upgradeable {
            override val snowflake: Snowflake = systemSnowflake
            override val bot: DiscordProxyKt = this@Guild.bot
            
            override fun request(): CompletableFuture<TextChannel> =
                    fetchChannels.request().thenApply { it.find { ch -> ch.snowflake == snowflake } as TextChannel }
        }
    }
}

@Deprecated("JDA Compatibility Class", ReplaceWith("Int"))
enum class ChannelType {
    TEXT,
    PRIVATE,
    VOICE,
    GROUP,
    CATEGORY,
    STORE,
}

enum class Region {
    AMSTERDAN, VIP_AMSTERDAN,
    BRAZIL, VIP_BRAZIL,
    EUROPE,  VIP_EUROPE,
    EU_CENTRAL,  VIP_EU_CENTRAL,
    EU_WEST, VIP_EU_WEST,
    FRANKFURT,  VIP_FRANKFURT,
    HONG_KONG,  VIP_HONG_KONG,
    JAPAN, VIP_JAPAN,
    SOUTH_KOREA, VIP_SOUTH_KOREA,
    LONDON, VIP_LONDON,
    RUSSIA,  VIP_RUSSIA,
    INDIA,  VIP_INDIA,
    SINGAPORE, VIP_SINGAPORE,
    SOUTH_AFRICA, VIP_SOUTH_AFRICA,
    SYDNEY, VIP_SYDNEY,
    US_CENTRAL, VIP_US_CENTRAL,
    US_EAST, VIP_US_EAST,
    US_SOUTH, VIP_US_SOUTH,
    US_WEST, VIP_US_WEST,
}

/**
 * https://discord.com/developers/docs/resources/guild#guild-object-system-channel-flags
 */
enum class SystemChannelFlags {
    /**
     * Suppress member join notifications
     */
    SUPPRESS_JOIN_NOTIFICATIONS,
    /**
     * Suppress server boost notifications
     */
    SUPPRESS_PREMIUM_SUBSCRIPTIONS,
}

/**
 * https://discord.com/developers/docs/resources/guild#guild-object-premium-tier
 */
enum class BoostTier(val bitrate: Int, val emotes: Int, val fileSize: Long) {
    NONE(96000, 50, 8_388_608L),
    TIER_1(128000, 100, 8_388_608L),
    TIER_2(256000, 150, 52_428_800L),
    TIER_3(384000, 250, 104_857_600L),
}

/**
 * https://discord.com/developers/docs/resources/guild#guild-object-mfa-level
 */
enum class MFALevel {
    NONE,
    ELEVATED
}

/**
 * https://discord.com/developers/docs/resources/guild#guild-object-explicit-content-filter-level
 */
enum class ExplicitContentFilterLevel {
    OFF,
    NO_ROLE,
    ALL,
}

/**
 * https://discord.com/developers/docs/resources/guild#guild-object-default-message-notification-level
 */
enum class NotificationLevel {
    ALL_MESSAGES,
    MENTIONS_ONLY
}

/**
 * https://discord.com/developers/docs/resources/guild#guild-object-verification-level
 */
enum class VerificationLevel {
    /**
     * Unrestricted
     */
    NONE,
    /**
     * Must have verified email on account
     */
    LOW,
    /**
     * Must be registered on Discord for longer than 5 minutes
     */
    MEDIUM,
    /**
     * (╯°□°）╯︵ ┻━┻ - must be a member of the server for longer than 10 minutes
     */
    HIGH,
    /**
     * ┻━┻ ミヽ(ಠ 益 ಠ)ﾉ彡 ┻━┻ - must have a verified phone number
     */
    VERY_HIGH
}

enum class Timeout(val seconds: Int) {
    SECONDS_60(60),
    SECONDS_300(300),
    SECONDS_900(900),
    SECONDS_1800(1800),
    SECONDS_3600(3600);
}

/**
 * https://discord.com/developers/docs/resources/guild#guild-object-guild-features
 */
enum class Features {
    /**
     * Guild has access to set an invite splash background
     */
    INVITE_SPLASH,
    /**
     * Guild has access to set 384kbps bitrate in voice (previously VIP voice servers)
     */
    VIP_REGIONS,
    /**
     * Guild has access to set a vanity URL
     */
    VANITY_URL,
    /**
     * Guild is verified
     */
    VERIFIED,
    /**
     * Guild is partnered
     */
    PARTNERED,
    /**
     * Guild is public
     */
    PUBLIC,
    /**
     * Guild has access to use commerce features (i.e. create store channels)
     */
    COMMERCE,
    /**
     * Guild has access to create news channels
     */
    NEWS,
    /**
     * Guild is able to be discovered in the directory
     */
    DISCOVERABLE,
    /**
     * Guild is able to be featured in the directory
     */
    FEATURABLE,
    /**
     * Guild has access to set an animated guild icon
     */
    ANIMATED_ICON,
    /**
     * Guild has access to set a guild banner image
     */
    BANNER,
    /**
     * Guild cannot be public
     */
    PUBLIC_DISABLED,
    /**
     * Guild has enabled the welcome screen
     */
    WELCOME_SCREEN_ENABLED,
}
