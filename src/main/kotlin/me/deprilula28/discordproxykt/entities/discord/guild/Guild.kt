package me.deprilula28.discordproxykt.entities.discord.guild

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.assertPermissions
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.*
import me.deprilula28.discordproxykt.entities.discord.channel.*
import me.deprilula28.discordproxykt.entities.discord.message.GuildEmoji
import me.deprilula28.discordproxykt.rest.*
import me.deprilula28.discordproxykt.rest.IRestAction
import java.awt.image.RenderedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

/**
 * This type is used for operations when an ID of a [Guild] is known.<br>
 * If the data is also known it will implement [Guild], and [upgrade] is a no-op.<br>
 * If it isn't known, [upgrade] will be a request to get the data from Discord.
 */
interface PartialGuild: PartialEntity {
    companion object {
        fun new(snowflake: Snowflake, bot: DiscordProxyKt): PartialGuild {
            return object: PartialGuild {
                override val snowflake: Snowflake = snowflake
                override val bot: DiscordProxyKt = bot
                override fun upgrade(): IRestAction<Guild> = RestAction(
                    bot, RestEndpoint.GET_GUILD.path(snowflake.id),
                    { Guild(this as JsonObject, bot) },
                )
                override fun toString(): String = "Guild(partial, ${snowflake.id})"
            }
        }
    }
    
    fun upgrade(): IRestAction<Guild>
    
    val fetchRoles: IRestAction<List<Role>>
        get() = PaginatedAction(
            // This isn't actually paginated, but PaginatedAction supports a list format by default
            bot, { Role(this@PartialGuild, this as JsonObject, bot) },
            RestEndpoint.GET_GUILD_ROLES, snowflake.id,
        )
    
    fun fetchRole(role: Snowflake): PartialRole = PartialRole.new(this, role)
    
    val fetchAuditLogs: IRestAction<AuditLogEntry>
        get() = IRestAction.coroutine(bot) {
            assertPermissions(this, Permissions.VIEW_AUDIT_LOG)
            bot.coroutineRequest(
                RestEndpoint.GET_GUILD_AUDIT_LOGS.path(snowflake.id),
                { AuditLogEntry(this as JsonObject, bot) },
            )
        }
    
    val fetchInvites: IRestAction<List<ExtendedInvite>>
        get() = IRestAction.coroutine(bot) {
            assertPermissions(this, Permissions.MANAGE_GUILD)
            bot.coroutineRequest(RestEndpoint.GET_GUILD_INVITES.path(snowflake.id),
                        { (this as JsonArray).map { ExtendedInvite(it as JsonObject, bot) } })
        }
    
    val fetchRegions: IRestAction<List<VoiceRegion>>
        get() = bot.request(RestEndpoint.GET_GUILD_VOICE_REGIONS.path(snowflake.id),
                            { (this as JsonArray).map { VoiceRegion(it as JsonObject, bot) } })
    
    val fetchChannels: IRestAction<List<GuildChannel>>
        get() = bot.request(RestEndpoint.GET_GUILD_CHANNELS.path(snowflake.id),
                            { (this as JsonArray).mapNotNull { it.asGuildChannel(bot, this@PartialGuild) } })
    
    fun fetchTextChannel(snowflake: Snowflake) = PartialTextChannel.new(this, snowflake)
    fun fetchVoiceChannel(snowflake: Snowflake) = PartialVoiceChannel.new(this, snowflake)
    fun fetchStoreChannel(snowflake: Snowflake) = PartialStoreChannel.new(this, snowflake)
    fun fetchNewsChannel(snowflake: Snowflake) = PartialNewsChannel.new(this, snowflake)
    fun fetchCategory(snowflake: Snowflake) = PartialCategory.new(this, snowflake)
    fun fetchChannel(snowflake: Snowflake) = PartialGuildChannel.new(this, snowflake)
    
    val fetchWebhooks: IRestAction<List<Webhook>>
        get() = IRestAction.coroutine(bot) {
            assertPermissions(this, Permissions.MANAGE_WEBHOOKS)
            bot.coroutineRequest(RestEndpoint.GET_GUILD_WEBHOOKS.path(snowflake.id),
                        { (this as JsonArray).map { Webhook(it as JsonObject, bot) } })
        }
    
    /**
     * Requires the GUILD_MEMBERS privileged intent
     */
    val fetchMembers: PaginatedAction<Member>
        get() = PaginatedAction(
            bot, { Member(this@PartialGuild, this as JsonObject, bot) },
            RestEndpoint.LIST_GUILD_MEMBERS, snowflake.id,
        )
    
    fun fetchMember(user: Snowflake): PartialMember = object: PartialMember {
        override val guild: PartialGuild = this@PartialGuild
        override val bot: DiscordProxyKt = this@PartialGuild.bot
        override val user: PartialUser by lazy { bot.fetchUser(user) }
        
        override fun upgrade(): IRestAction<Member>
            = RestAction(
                bot, RestEndpoint.GET_GUILD_MEMBER.path(snowflake.id, user.id),
                { Member(this@PartialGuild, this as JsonObject, bot) },
            )
        
        override fun toString(): String = "Member(partial, $user, $guild)"
    }
    
    val fetchEmojis: IRestAction<List<GuildEmoji>>
        get() = bot.request(RestEndpoint.GET_GUILD_EMOJIS.path(snowflake.id),
                            { (this as JsonArray).map { GuildEmoji(it as JsonObject, bot) } })
    
    fun fetchEmoji(emoji: Snowflake): IRestAction<GuildEmoji> = bot.request(
        RestEndpoint.GET_GUILD_EMOJI.path(snowflake.id, emoji.id), { GuildEmoji(this as JsonObject, bot) })
    
    val fetchVanityCode: IRestAction<String>
        get() = bot.request(RestEndpoint.GET_GUILD_VANITY_URL.path(snowflake.id), { (this as JsonPrimitive).content })
    
    val fetchBans: IRestAction<List<Ban>>
        get() = IRestAction.coroutine(bot) {
            assertPermissions(this, Permissions.BAN_MEMBERS)
            bot.request(
                RestEndpoint.GET_GUILDS_BANS.path(snowflake.id),
                { (this as JsonArray).map { Ban(it as JsonObject, bot) } },
            ).await()
        }
    
    fun fetchBan(user: PartialUser): IRestAction<Ban>
        = IRestAction.coroutine(bot) {
            assertPermissions(this, Permissions.BAN_MEMBERS)
            bot.request(
                RestEndpoint.GET_GUILDS_BAN.path(snowflake.id, user.snowflake.id),
                { Ban(this as JsonObject, bot) },
            ).await()
        }
    
    val fetchSelfMember: IRestAction<Member>
        get() = IRestAction.coroutine(bot) {
            fetchMember(bot.selfUser.await().snowflake).upgrade().await()
        }
    
    val fetchUserPermissions: IRestAction<EnumSet<Permissions>>
        get() = IRestAction.coroutine(bot) {
            // In constructed guilds, fetchRoles has no requests needed
            val selfUser = bot.selfUser.await()
            val selfMember = fetchMember(selfUser.snowflake).upgrade().await()
            val selfRoles = selfMember.fetchRoles.await()
            var set = 0L
            selfRoles.forEach { el -> set = set and el.permissionsRaw }
            set.bitSetToEnumSet(Permissions.values())
        }
    
    fun addMember(accessToken: String, user: PartialUser): IRestAction<Member> = IRestAction.coroutine(bot) {
        assertPermissions(this, Permissions.CREATE_INSTANT_INVITE)
        bot.coroutineRequest(
            RestEndpoint.ADD_GUILD_MEMBER.path(snowflake.id, user.snowflake.id),
            { Member(this@PartialGuild, this as JsonObject, it) },
        ) {
            Json.encodeToString("access_token" to JsonPrimitive(accessToken))
        }
    }
    
    fun retrievePrunableMemberCount(days: Int): IRestAction<Int> = IRestAction.coroutine(bot) {
        assertPermissions(this, Permissions.KICK_MEMBERS)
        bot.coroutineRequest(
            RestEndpoint.GET_GUILD_PRUNE_COUNT.path(listOf("days" to days.toString()), snowflake.id),
            { (this as JsonObject)["pruned"]!!.asInt() }
        )
    }
        
    fun prune(days: Int, vararg role: PartialRole): IRestAction<Unit>
        = IRestAction.coroutine(bot) {assertPermissions(this, Permissions.KICK_MEMBERS)
            bot.request(RestEndpoint.BEGIN_GUILD_PRUNE_COUNT.path(snowflake.id), { Unit }) {
                Json.encodeToString(mapOf(
                    "days" to JsonPrimitive(days),
                    "compute_prune_count" to JsonPrimitive(false),
                    "include_roles" to Json.encodeToString(role.map { it.snowflake.id }),
                ))
            }
        }
    
    fun leave(): IRestAction<Unit> = bot.request(RestEndpoint.LEAVE_GUILD.path(snowflake.id), { Unit })
    
    fun delete(): IRestAction<Unit> = bot.request(RestEndpoint.DELETE_GUILD.path(snowflake.id), { Unit })
    
    fun textChannelBuilder(): TextChannelBuilder = TextChannelBuilder(this, bot)
    fun voiceChannelBuilder(): VoiceChannelBuilder = VoiceChannelBuilder(this, bot)
    fun categoryBuilder(): CategoryBuilder = CategoryBuilder(this, bot)
    fun roleBuilder(): RoleBuilder = RoleBuilder(this, bot)
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("false"))
    val loaded: Boolean
        get() = false
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("delete()"))
    fun delete(mfa: String) = delete()
    @Deprecated("JDA Compatibility Function", ReplaceWith("prune(days, *role)"))
    fun prune(days: Int, bool: Boolean, vararg role: PartialRole) = prune(days, *role)
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.kick()"))
    fun kick(member: PartialMember) = member.kick()
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMember(Snowflake(member)).kick()"))
    fun kick(member: String) = fetchMember(Snowflake(member)).kick()
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.kick()"))
    fun kick(member: PartialMember, reason: String?) = member.kick()
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMember(Snowflake(member)).kick()"))
    fun kick(member: String, reason: String?) = fetchMember(Snowflake(member)).kick()
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMember(Snowflake(member)).ban(days)"))
    fun ban(member: String, days: Int = 7) = fetchMember(Snowflake(member)).ban(days)
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.ban(days)"))
    fun ban(member: PartialMember, days: Int = 7) = member.ban(days)
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.ban(days)"))
    fun ban(member: PartialMember, days: Int = 7, reason: String?) = member.ban(days)
    @Deprecated("JDA Compatibility Function", ReplaceWith("ban(fetchMember(Snowflake(member)))"))
    fun ban(member: String, days: Int = 7, reason: String?) = fetchMember(Snowflake(member)).ban(days)
    @Deprecated("JDA Compatibility Function", ReplaceWith("unban(fetchMember(Snowflake(member)))"))
    fun unban(member: String) = fetchMember(Snowflake(member)).unban()
    @Deprecated("JDA Compatibility Function", ReplaceWith("unban(fetchMember(Snowflake(member)))"))
    fun unban(member: PartialMember) = member.unban()
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("textChannelBuilder().apply { this@apply.name = name }"))
    fun createTextChannel(name: String) = textChannelBuilder().apply { this@apply.name = name }
    @Deprecated("JDA Compatibility Field", ReplaceWith("voiceChannelBuilder().apply { this@apply.name = name }"))
    fun createVoiceChannel(name: String) = voiceChannelBuilder().apply { this@apply.name = name }
    @Deprecated("JDA Compatibility Field", ReplaceWith("categoryBuilder().apply { this@apply.name = name }"))
    fun createCategory(name: String) = categoryBuilder().apply { this@apply.name = name }
    @Deprecated("JDA Compatibility Field", ReplaceWith("roleBuilder()"))
    fun createRole() = roleBuilder()
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.add(role)"))
    fun addRoleToMember(member: PartialMember, role: PartialRole) = member.add(role)
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.add(role)"))
    fun addRoleToMember(member: String, role: PartialRole) = fetchMember(Snowflake(member)).add(role)
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.add(role)"))
    fun addRoleToMember(member: Long, role: PartialRole) = fetchMember(Snowflake(member.toString())).add(role)
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.remove(role)"))
    fun removeRoleFromMember(member: PartialMember, role: PartialRole) = member.remove(role)
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.remove(role)"))
    fun removeRoleFromMember(member: String, role: PartialRole) = fetchMember(Snowflake(member)).remove(role)
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.remove(role)"))
    fun removeRoleFromMember(member: Long, role: PartialRole) = fetchMember(Snowflake(member.toString())).remove(role)
    @Deprecated("JDA Compatibility Function", ReplaceWith("add.forEach { member.add(it) }\nremove.forEach { member.remove(it) }"))
    fun modifyMemberRoles(member: Member, add: Collection<Role>, remove: Collection<Role>) {
        add.forEach { member.add(it) }
        remove.forEach { member.remove(it) }
    }
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.setRoles(roles)"))
    fun modifyMemberRoles(member: Member, roles: Collection<Role>) = member.setRoles(roles)
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.setRoles(roles)"))
    fun modifyMemberRoles(member: Member, vararg roles: Role) = member.setRoles(roles.toList())
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchAuditLogs"))
    fun retrieveAuditLogs() = fetchAuditLogs
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchInvites"))
    fun retrieveInvites() = fetchInvites
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchEmojis"))
    fun retrieveEmotes() = fetchEmojis
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchEmoji(Snowflake(id))"))
    fun retrieveEmoteById(id: String) = fetchEmoji(Snowflake(id))
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchEmoji(Snowflake(id.toString()))"))
    fun retrieveEmoteById(id: Long) = fetchEmoji(Snowflake(id.toString()))
    @Deprecated("JDA Compatibility Function", ReplaceWith("emote"))
    fun retrieveEmote(emote: GuildEmoji) = fetchEmoji(emote.snowflake)
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMembers"))
    fun retrieveMembers() = fetchMembers
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMember(Snowflake(id))"))
    fun retrieveMemberById(id: String) = fetchMember(Snowflake(id))
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMember(Snowflake(id))"))
    fun retrieveMemberById(id: String, cache: Boolean) = fetchMember(Snowflake(id))
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMember(Snowflake(id.toString()))"))
    fun retrieveMemberById(id: Long) = fetchMember(Snowflake(id.toString()))
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMember(Snowflake(id.toString()))"))
    fun retrieveMemberById(id: Long, cache: Boolean) = fetchMember(Snowflake(id.toString()))
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMember(user.snowflake)"))
    fun retrieveMember(user: PartialUser) = fetchMember(user.snowflake)
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMember(user.snowflake)"))
    fun retrieveMember(user: PartialUser, cache: Boolean) = fetchMember(user.snowflake)
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.move(channel).edit()"))
    fun moveVoiceMember(member: Member, channel: PartialVoiceChannel?) = member.move(channel).edit()
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.move(null).edit()"))
    fun kickVoiceMember(member: Member) = member.move(null).edit()
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.apply { nick = nickname }.edit()"))
    fun modifyNickname(member: Member, nickname: String?) = member.apply { nick = nickname }.edit()
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.apply { deaf = value }.edit()"))
    fun deafen(member: Member, value: Boolean = true) = member.apply { deaf = value }.edit()
    @Deprecated("JDA Compatibility Function", ReplaceWith("member.apply { mute = value }.edit()"))
    fun mute(member: Member, value: Boolean = true) = member.apply { mute = value }.edit()

    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchBans"))
    fun retrieveBanList() = fetchBans
    @Deprecated("JDA Compatibility Function", ReplaceWith("bot.fetchUser(Snowflake(id))"))
    fun retrieveBanById(id: String) = fetchBan(bot.fetchUser(Snowflake(id)))
    @Deprecated("JDA Compatibility Function", ReplaceWith("bot.fetchUser(Snowflake(id.toString()))"))
    fun retrieveBanById(id: Long) = fetchBan(bot.fetchUser(Snowflake(id.toString())))
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchBan(user)"))
    fun retrieveBan(user: PartialUser) = fetchBan(user)
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("/*no-op*/"))
    fun pruneMemberCache() = println("Warning: pruneMemberCache() is a no-op!")
    @Deprecated("JDA Compatibility Function", ReplaceWith("true"))
    fun checkVerification() = true
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("/*no-op*/"))
    fun unloadMember(id: Long) = println("Warning: unloadMember(long) is a no-op!")
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("addMember(accessToken, bot.fetchUser(Snowflake(user)))"))
    fun addMember(accessToken: String, user: String)
            = addMember(accessToken, bot.fetchUser(Snowflake(user)))
    @Deprecated("JDA Compatibility Function", ReplaceWith("addMember(accessToken, bot.fetchUser(Snowflake(user.toString())))"))
    fun addMember(accessToken: String, user: Long)
            = addMember(accessToken, bot.fetchUser(Snowflake(user.toString())))
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("vanityUrl"))
    fun retrieveVanityUrl() = fetchVanityCode
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("\"https://discord.gg/\" + vanityCode"))
    val vanityUrl: String?
        get() = "https://discord.gg/${fetchVanityCode.complete()}"
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("regions"))
    fun retrieveRegions()
        = bot.request(RestEndpoint.GET_GUILD_VOICE_REGIONS.path(snowflake.id), {
            val enumSet = EnumSet.noneOf(Region::class.java)
            (this as JsonArray).forEach {
                val region = VoiceRegion(it as JsonObject, bot)
                enumSet.add(Region.valueOf((if (region.vip) "VIP_" else "") + region.name.toUpperCase()))
            }
            enumSet
        })
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("regions"))
    fun retrieveRegions(includeDeprecated: Boolean) = retrieveRegions()
    
    /*
    TODO from JDA:
    Guild#retrieveMembers(Collection<User>)
    Guild#retrieveMembersByIds(Collection<String>)
    Guild#retrieveMembersByIds(String...)
    Guild#retrieveMembersByIds(Collection<Long>)
    Guild#retrieveMembersByIds(long...)
    Guild#retrieveMembers(boolean, Collection<User>)
    Guild#retrieveMembersByIds(boolean, Collection<String>)
    Guild#retrieveMembersByIds(boolean, String...)
    Guild#retrieveMembersByIds(boolean, Collection<Long>)
    Guild#retrieveMembersByIds(boolean, long...)
    Guild#retrieveMembersByPrefix(String, int)
    */
}

/**
 * Guilds in Discord represent an isolated collection of users and channels, and are often referred to as "servers" in the UI.
 */
open class Guild(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), EntityManager<Guild>, PartialGuild {
    /**
     * guild name (2-100 characters, excluding trailing and leading whitespace)
     */
    var name: String by parsing(JsonElement::asString, Json::encodeToJsonElement)
    /**
     * id of owner
     */
    var owner: PartialMember by parsing({ fetchMember(asSnowflake()) }, { Json.encodeToJsonElement(it.user.snowflake.id) }, "owner_id")
    /**
     * application id of the guild creator if it is bot-created
     */
    val applicationSnowflake: Snowflake? by parsingOpt(JsonElement::asSnowflake, "application_id")
    /**
     * custom guild emojis
     */
    val emojis: List<GuildEmoji> by parsing({ (this as JsonArray).map { GuildEmoji(it as JsonObject, bot) } })
    
    /**
     * the description for the guild, if the guild is discoverable
     */
    val description: String? by parsingOpt(JsonElement::asString)
    /**
     * discovery splash hash; only present for guilds with the "DISCOVERABLE" feature
     */
    val discoverableSplash: String? by parsingOpt(JsonElement::asString, "discovery_splash")
    /**
     * banner hash
     */
    val banner: String? by parsingOpt(JsonElement::asString)
    /**
     * the id of the channel where guilds with the "PUBLIC" feature can display rules and/or guidelines
     */
    val rulesChannel: PartialTextChannel? by parsingOpt({ fetchTextChannel(asSnowflake()) }, "rules_channel_id")
    /**
     * 	the preferred locale of a guild with the "PUBLIC" feature; used in server discovery and notices from Discord; defaults to "en-US"
     */
    var locale: String by parsing(JsonElement::asString, Json::encodeToJsonElement, "preferred_locale")
    /**
     * 	the id of the channel where admins and moderators of guilds with the "PUBLIC" feature receive notices from Discord
     */
    var publicUpdatesChannel: PartialTextChannel? by parsingOpt(::delegateChannel, {
        Json.encodeToJsonElement(it?.snowflake?.id)
    }, "public_updates_channel_id")
    
    /**
     * icon hash
     */
    val icon: String? by parsingOpt(JsonElement::asString)
    /**
     * splash hash
     */
    val iconSplash: String? by parsingOpt(JsonElement::asString, "splash")
    
    /**
     * voice region for the guild
     */
    var region: Region by parsing(
        { Region.valueOf(asString().toUpperCase().replace("-", "_")) },
        { Json.encodeToJsonElement(it.toString()) },
    )
    /**
     * voice region for the guild
     */
    val regionRaw: String by parsing(JsonElement::asString, "region")
    /**
     * id of afk channel
     */
    var afkChannel: PartialVoiceChannel? by parsingOpt(
        { PartialVoiceChannel.new(this@Guild, asSnowflake()) },
        { Json.encodeToJsonElement(it?.snowflake?.id) },
        "afk_channel_id",
    )
    
    /**
     * afk timeout in seconds
     */
    var afkTimeout: Timeout by parsing(
        { Timeout.valueOf("SECONDS_${asInt()}") },
        { Json.encodeToJsonElement(it.ordinal) },
        "afk_timeout",
    )
    
    /**
     * true if the server widget is enabled
     */
    val widgetEnabled: Boolean? by parsingOpt(JsonElement::asBoolean, "widget_enabled")
    /**
     * the channel id that the widget will generate an invite to, or null if set to no invite
     */
    val widgetChannel: PartialTextChannel? by parsingOpt(::delegateChannel, "widget_channel_id")
    
    /**
     * true if the user is the owner of the guild
     * <br>
     * Only sent under "GET Current User Guilds" endpoint, relative to current user
     */
    val userIsOwner: Boolean? by parsingOpt(JsonElement::asBoolean, "owner")
    /**
     * total permissions for the user in the guild (excludes overrides)
     * <br>
     * Only sent under "GET Current User Guilds" endpoint, relative to current user
     */
    val cachedUserPermissions: EnumSet<Permissions>? by parsingOpt({ asLong().bitSetToEnumSet(Permissions.values()) }, "permissions")
    
    /**
     * when this guild was joined at
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val joinedAt: Timestamp? by parsingOpt(JsonElement::asTimestamp, "joined_at")
    /**
     * true if this is considered a large guild
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val large: Boolean? by parsingOpt(JsonElement::asBoolean)
    /**
     * true if this guild is unavailable due to an outage
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val unavailable: Boolean? by parsingOpt(JsonElement::asBoolean)
    /**
     * total number of members in this guild
     * <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val instantMemberCount: Int? by parsingOpt(JsonElement::asInt, "member_count")
    /**
     * 	approximate number of members in this guild
     */
    val approxMemberCount: Int? by parsingOpt(JsonElement::asInt, "approximate_member_count")
    /**
     * 	approximate number of non-offline members in this guild
     */
    val approxPresenceCount: Int? by parsingOpt(JsonElement::asInt, "approximate_presence_count")
    
    /**
     * users in the guild <br>
     * Only sent when under the event where the guild becomes available to the bot <br>
     * Requires the GUILD_MEMBERS privileged intent
     */
    val cachedMembers: EntityCache<Member> by lazy {
        EntityCache(map["members"]?.run {
            (this as JsonArray).map { Member(this@Guild, it as JsonObject, bot) }.toMutableList()
        } ?: mutableListOf())
    }
    /**
     * channels in the guild <br>
     * Only sent when under the event where the guild becomes available to the bot
     */
    val cachedChannels: EntityCache<GuildChannel> by lazy {
        EntityCache(map["channels"]?.run {
            (this as JsonArray).mapNotNull { it.asGuildChannel(bot, this@Guild) }.toMutableList()
        } ?: mutableListOf())
    }
    /**
     * roles in the guild
     */
    val cachedRoles: EntityCache<Role> by lazy {
        EntityCache(map["roles"]?.run {
            (this as JsonArray).map { Role(this@Guild, it as JsonObject, bot) }.toMutableList()
        } ?: mutableListOf())
    }
    
    /**
     * the maximum number of presences for the guild (the default value, currently 25000, is in effect when null is returned)
     */
    val maxPresences: Int? by parsingOpt(JsonElement::asInt, "max_presences")
    /**
     * the maximum number of members for the guild
     */
    val maxMembers: Int? by parsingOpt(JsonElement::asInt, "max_members")
    /**
     * the maximum amount of users in a video channel
     */
    val maxVideoChannelUsers: Int? by parsingOpt(JsonElement::asInt, "max_video_channel_users")
    /**
     * the vanity url code for the guild
     */
    val vanityCode: String? by parsingOpt(JsonElement::asString, "vanity_url_code")
    
    /**
     * verification level required for the guild
     */
    var verificationLevel: VerificationLevel by parsing(
        { VerificationLevel.values()[asInt()] },
        { Json.encodeToJsonElement(it.ordinal) },
        "verification_level",
    )
    
    /**
     * default message notifications level
     */
    var defaultNotificationLevel: NotificationLevel by parsing(
        { NotificationLevel.values()[asInt()] },
        { Json.encodeToJsonElement(it.ordinal) },
        "default_message_notifications",
    )
    
    /**
     * explicit content filter level
     */
    var explicitContentLevel: ExplicitContentFilterLevel by parsing(
        { ExplicitContentFilterLevel.values()[asInt()] },
        { Json.encodeToJsonElement(it.ordinal) },
        "explicit_content_filter",
    )
    
    // This is encoded as a list of strings instead of a bitset for god knows why
    /**
     * enabled guild features
     */
    val features: EnumSet<Features> by parsing({
        val set = EnumSet.noneOf(Features::class.java)
        (this as JsonArray).forEach {
            set.add(Features.valueOf(it.asString()))
        }
        set
    })
    
    /**
     * required MFA level for the guild
     */
    val requiredMFALevel: MFALevel by parsing({ MFALevel.values()[asInt()] }, "mfa_level")
    
    /**
     * premium tier (Server Boost level)
     */
    val boostTier: BoostTier by parsing({ BoostTier.values()[asInt()] }, "premium_tier")
    /**
     * the number of boosts this guild currently has
     */
    val boosters: Int? by parsingOpt(JsonElement::asInt, "premium_subscription_count")
    
    /**
     * the id of the channel where guild notices such as welcome messages and boost events are posted
     */
    val systemChannel: PartialTextChannel? by parsingOpt(
        ::delegateChannel,
        { Json.encodeToJsonElement(it?.snowflake?.id) },
        "system_channel_id",
    )
    
    /**
     * system channel flags
     */
    val systemChannelFlags: EnumSet<SystemChannelFlags> by parsing({ asLong().bitSetToEnumSet(
        SystemChannelFlags.values()) }, "system_channel_flags")
    
    val memberCount: Int by lazy { instantMemberCount ?: approxMemberCount ?: -1 }
    val iconUrl: String? by lazy {
        if (icon == null) null
        else "https://cdn.discordapp.com/icons/${snowflake.id}/$icon.${if (icon!!.startsWith("a_")) "gif" else "png"}"
    }
    
    override val changes: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
    
    /**
     * Requests that this guild gets edited based on the altered fields.
     */
    override fun edit(): IRestAction<Guild> {
        if (changes.isEmpty()) throw InvalidRequestException(
            "No changes have been made to this guild, yet `edit()` was called.")
        return IRestAction.coroutine(bot) {
            assertPermissions(this, Permissions.MANAGE_GUILD)
            bot.coroutineRequest(RestEndpoint.MODIFY_GUILD.path(snowflake.id),
                        { this@Guild.apply { map = this@coroutineRequest as JsonObject } }) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
    
    /**
     * To finish the procedure, the rest action needs to be called.
     */
    fun setIcon(image: RenderedImage, format: String): Guild {
        if (image.width > 1024 || image.height > 1024) throw InvalidRequestException("Neither image dimension may exceed 1024!")
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, format, baos)
        changes["icon"] = JsonPrimitive("data:image/jpeg;base64,${Base64.getEncoder().encodeToString(baos.toByteArray())}")
        return this
    }
    
    /**
     * To finish the procedure, the rest action needs to be called.
     */
    fun setSplash(image: RenderedImage, format: String): Guild {
        if (Features.INVITE_SPLASH !in features) throw InvalidRequestException("Cannot change splash without the INVITE_SPLASH feature!")
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, format, baos)
        changes["splash"] = JsonPrimitive("data:image/jpeg;base64,${Base64.getEncoder().encodeToString(baos.toByteArray())}")
        return this
    }
    
    /**
     * To finish the procedure, the rest action needs to be called.
     */
    fun setBanner(image: RenderedImage, format: String): Guild {
        if (Features.BANNER !in features) throw InvalidRequestException("Cannot change banner without the BANNER feature!")
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, format, baos)
        changes["banner"] = JsonPrimitive("data:image/jpeg;base64,${Base64.getEncoder().encodeToString(baos.toByteArray())}")
        return this
    }
    
    private fun delegateChannel(json: JsonElement): PartialTextChannel
        = PartialTextChannel.new(this, json.asSnowflake())
    
    override fun upgrade(): IRestAction<Guild> = IRestAction.ProvidedRestAction(bot, this)
    
    /*
        Properties below override PartialGuild to use the additional information this type has available
     */
    override fun fetchMember(user: Snowflake): PartialMember
        = cachedMembers.run { find { it.user.snowflake == user } } ?: super.fetchMember(user)
    
    override val fetchUserPermissions: IRestAction<EnumSet<Permissions>>
        get() = cachedUserPermissions?.run { IRestAction.ProvidedRestAction(bot, this) } ?: super.fetchUserPermissions
    
    override val fetchRoles: IRestAction<List<Role>>
        get() = IRestAction.ProvidedRestAction(bot, cachedRoles)
    
    override fun toString(): String = "Guild($name, ${snowflake.id})"
    
    @Deprecated("JDA Compatibility Field", ReplaceWith(""))
    val manager: Guild?
        get() = this
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("owner"))
    fun retrieveOwner() = owner
    @Deprecated("JDA Compatibility Function", ReplaceWith("owner"))
    fun retrieveOwner(cache: Boolean) = owner
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("iconSplash"))
    val splashId: String? by ::iconSplash
    @Deprecated("JDA Compatibility Field", ReplaceWith("icon"))
    val iconId: String? by ::icon
    @Deprecated("JDA Compatibility Field", ReplaceWith("banner"))
    val bannerId: String? by ::banner
    @Deprecated("JDA Compatibility Field", ReplaceWith("unavailable != true"))
    val available: Boolean by lazy { unavailable != true } // This isn't just !unavailable as it may also be null
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
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("bot.fetchGuild(snowflake).upgrade()"))
    fun retrieveMetaData(): IRestAction<Guild> = bot.fetchGuild(snowflake).upgrade()
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.any { it.user == user } ?: false"))
    fun isMember(user: User): Boolean
            = cachedMembers.any { it.user == user }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.find { it.user == user }"))
    fun getMember(user: User)
            = cachedMembers.find { it.user == user }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.find { it.user.snowflake.id == userId }"))
    fun getMember(userId: String)
            = cachedMembers.find { it.user.snowflake.id == userId }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.find { it.user.snowflake.idLong == userIdLong }"))
    fun getMember(userIdLong: Long)
            = cachedMembers.find { it.user.snowflake.idLong == userIdLong }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.find { \"\${it.user.username}#\${it.user.discriminator}\" == tag }"))
    fun getMemberByTag(tag: String)
            = cachedMembers.find { "${it.user.username}#${it.user.discriminator}" == tag }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.find { it.user.username == tag && it.user.discriminator == discrim }"))
    fun getMemberByTag(tag: String, discrim: String)
            = cachedMembers.find { it.user.username == tag && it.user.discriminator == discrim }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.filter { it.user.username.equals(tag, ignoreCase) } ?: listOf()"))
    fun getMembersByName(tag: String, ignoreCase: Boolean)
            = cachedMembers.filter { it.user.username.equals(tag, ignoreCase) }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.filter { it.nick?.equals(tag, ignoreCase) ?: false } ?: listOf()"))
    fun getMembersByNickname(tag: String, ignoreCase: Boolean)
            = cachedMembers.filter { it.nick?.equals(tag, ignoreCase) ?: false }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.filter { it.nick?.equals(tag, ignoreCase) ?: it.user.username.equals(tag, ignoreCase) } ?: listOf()"))
    fun getMembersByEffectiveName(tag: String, ignoreCase: Boolean)
            = cachedMembers.filter { it.nick?.equals(tag, ignoreCase) ?: it.user.username.equals(tag, ignoreCase) }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.filter { mem -> roles.all { mem.roles.contains(it.snowflake) } } ?: listOf()"))
    fun getMembersWithRoles(vararg roles: Role)
            = cachedMembers.filter { mem -> roles.all { mem.roles.contains(it) } }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.filter { mem -> roles.all { mem.roles.contains(it.snowflake) } } ?: listOf()"))
    fun getMembersWithRoles(roles: Collection<Role>)
            = cachedMembers.filter { mem -> roles.all { mem.roles.contains(it) } }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.find { it.snowflake.id == id }"))
    fun getGuildChannelById(id: String)
            = cachedChannels.find { it.snowflake.id == id }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.find { it.snowflake.idLong == idLong }"))
    fun getGuildChannelById(idLong: Long)
            = cachedChannels.find { it.snowflake.idLong == idLong }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.find { it.type == channelType && it.snowflake.id == id }"))
    fun getGuildChannelById(channelType: ChannelType, id: String)
            = cachedChannels.find { it.type == channelType && it.snowflake.id == id }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.find { it.type == channelType && it.snowflake.idLong == idLong }"))
    fun getGuildChannelById(channelType: ChannelType, idLong: Long)
            = cachedChannels.find { it.type == channelType && it.snowflake.idLong == idLong }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.mapNotNull { it as? Category }?.find { it.snowflake.id == id }"))
    fun getCategoryById(id: String)
            = cachedChannels.mapNotNull { it as? Category }.find { it.snowflake.id == id }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.mapNotNull { it as? Category }?.find { it.snowflake.idLong == idLong }"))
    fun getCategoryById(idLong: Long)
            = cachedChannels.mapNotNull { it as? Category }.find { it.snowflake.idLong == idLong }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.mapNotNull { it as? Category }?.find { it.name.equals(name, ignoreCase) }"))
    fun getCategoriesByName(name: String, ignoreCase: Boolean)
            = cachedChannels.mapNotNull { it as? Category }.find { it.name.equals(name, ignoreCase) }
    @Deprecated("JDA Compatibility Field", ReplaceWith("null"))
    val categories
        get() = cachedChannels.mapNotNull { it as? Category }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("null"))
    fun getStoreChannelById(id: String) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith("null"))
    fun getStoreChannelById(idLong: Long) = null
    @Deprecated("JDA Compatibility Function", ReplaceWith("null"))
    fun getStoreChannelsByName(name: String, ignoreCase: Boolean) = null
    @Deprecated("JDA Compatibility Field", ReplaceWith("null"))
    val storeChannels: List<Nothing>
        get() = listOf()
    @Deprecated("JDA Compatibility Field", ReplaceWith("null"))
    val voiceStates: List<Nothing>
        get() = listOf()
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.mapNotNull { it as? TextChannel }?.find { it.snowflake.id == id }"))
    fun getTextChannelById(id: String)
            = cachedChannels?.mapNotNull { it as? TextChannel }?.find { it.snowflake.id == id }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.mapNotNull { it as? TextChannel }?.find { it.snowflake.idLong == idLong }"))
    fun getTextChannelById(idLong: Long)
            = cachedChannels?.mapNotNull { it as? TextChannel }?.find { it.snowflake.idLong == idLong }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.mapNotNull { it as? TextChannel }?.find { it.name.equals(name, ignoreCase) }"))
    fun getTextChannelsByName(name: String, ignoreCase: Boolean)
            = cachedChannels?.mapNotNull { it as? TextChannel }?.find { it.name.equals(name, ignoreCase) }
    @Deprecated("JDA Compatibility Field", ReplaceWith("null"))
    val textChannels
        get() = cachedChannels?.mapNotNull { it as? TextChannel }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.mapNotNull { it as? VoiceChannel }?.find { it.snowflake.id == id }"))
    fun getVoiceChannelById(id: String)
            = cachedChannels?.mapNotNull { it as? VoiceChannel }?.find { it.snowflake.id == id }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.mapNotNull { it as? VoiceChannel }?.find { it.snowflake.idLong == idLong }"))
    fun getVoiceChannelById(idLong: Long)
            = cachedChannels?.mapNotNull { it as? VoiceChannel }?.find { it.snowflake.idLong == idLong }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.mapNotNull { it as? VoiceChannel }?.find { it.name.equals(name, ignoreCase) }"))
    fun getVoiceChannelsByName(name: String, ignoreCase: Boolean)
            = cachedChannels?.mapNotNull { it as? VoiceChannel }?.find { it.name.equals(name, ignoreCase) }
    @Deprecated("JDA Compatibility Field", ReplaceWith("null"))
    val voiceChannels
        get() = cachedChannels?.mapNotNull { it as? VoiceChannel }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("cachedChannels"))
    val channels
        get() = cachedChannels
    @Deprecated("JDA Compatibility Field", ReplaceWith("cachedChannels"))
    fun getChannels(boolean: Boolean) = cachedChannels
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedRoles.find { it.snowflake.id == id }"))
    fun getRoleById(id: String) = cachedRoles.find { it.snowflake.id == id }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedRoles.find { it.snowflake.idLong == idLong }"))
    fun getRoleById(idLong: Long) = cachedRoles.find { it.snowflake.idLong == idLong }
    @Deprecated("JDA Compatibility Function", ReplaceWith("null"))
    fun getRolesByName(name: String, ignoreCase: Boolean) = cachedRoles.find { it.name.equals(name, ignoreCase) }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedRoles.find { it.snowflake.id == id }"))
    fun getEmoteById(id: String) = emojis.find { it.snowflake.id == id }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedRoles.find { it.snowflake.id == id }"))
    fun getEmoteById(idLong: Long) = emojis.find { it.snowflake.idLong == idLong }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedRoles.find { it.snowflake.id == id }"))
    fun getEmotesByName(name: String, ignoreCase: Boolean) = emojis.find { it.name.equals(name, ignoreCase) }
    @Deprecated("JDA Compatibility Field", ReplaceWith("emojis"))
    val emotes
        get() = emojis
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("apply { owner = member }.edit()"))
    fun transferOwnership(member: PartialMember): IRestAction<Guild> = apply { owner = member }.edit()
}

class GuildBuilder(bot: DiscordProxyKt):
    Guild(JsonObject(MapNotReady()), bot), EntityBuilder<Guild>
{
    /**
     * Creates a guild based on altered fields and returns it as a rest action.<br>
     * The values of the fields in the builder itself will be updated, making it usable as a Guild.
     */
    override fun create(): IRestAction<Guild> {
        if (!changes.containsKey("name")) throw InvalidRequestException("Guilds require at least a name.")
        return IRestAction.coroutine(bot) {
            assertPermissions(this, Permissions.MANAGE_GUILD)
            bot.coroutineRequest(RestEndpoint.CREATE_GUILD.path(),
                        { this@GuildBuilder.apply { map = this@coroutineRequest as JsonObject } }) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
    override fun toString(): String = "GuildBuilder"
}

