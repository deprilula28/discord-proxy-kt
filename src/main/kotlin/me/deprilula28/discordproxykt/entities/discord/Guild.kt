package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.entities.IPartialEntity
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.Timestamp
import me.deprilula28.discordproxykt.entities.discord.message.GuildEmoji
import me.deprilula28.discordproxykt.rest.*
import java.awt.image.RenderedImage
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
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

/**
 * https://discord.com/developers/docs/resources/invite#invite-object
 */
open class Invite(private val map: JsonObject, private val bot: DiscordProxyKt) {
    val code: String by map.delegateJson(JsonElement::asString)
    val guild: Guild? by map.delegateJsonNullable({ Guild(this as JsonObject, bot) })
    val channel: TextChannel by map.delegateJson({ TextChannel(guild!!, this as JsonObject, bot) })
    val inviter: User? by map.delegateJsonNullable({ User(this as JsonObject, bot) })
    val targetUser: User? by map.delegateJsonNullable({ User(this as JsonObject, bot) }, "target_user")
    val approxPresenceCount: Int? by map.delegateJsonNullable(JsonElement::asInt, "approximate_presence_count")
    val approxMemberCount: Int? by map.delegateJsonNullable(JsonElement::asInt, "approximate_member_count")
}

class ExtendedInvite(map: JsonObject, bot: DiscordProxyKt): Invite(map, bot) {
    val uses: Int by map.delegateJson(JsonElement::asInt, "uses")
    val maxUses: Int by map.delegateJson(JsonElement::asInt, "max_uses")
    val maxAge: Int by map.delegateJson(JsonElement::asInt, "max_age")
    val temporary: Boolean by map.delegateJson(JsonElement::asBoolean)
    val createdAt: Timestamp by map.delegateJson(JsonElement::asTimestamp, "created_at")
}

/**
 * Partial objects only need an ID.<br>
 * Working off this type means no permission checking will be done before requesting.<br>
 * You can get a full object by using {@link me.deprilula28.discordproxykt.rest.RestAction#request() request()} in this
 * type, if it isn't one already.
 */
interface PartialGuild: IPartialEntity {
    companion object {
        fun new(snowflake: Snowflake, bot: DiscordProxyKt): Upgradeable {
            return object : Upgradeable,
                    RestAction<Guild>(
                        bot, RestEndpoint.GET_GUILD.path(snowflake.id),
                        { Guild(this as JsonObject, bot) },
                    ) {
                override val snowflake: Snowflake
                    get() = snowflake
                override val bot: DiscordProxyKt
                    get() = bot
            }
        }
    }
    
    val fetchRoles: IRestAction<List<Role>>
        get() = PaginatedAction( // This isn't actually paginated, but PaginatedAction supports a list format by default
            bot, { Role(this@PartialGuild, this as JsonObject, bot) },
            RestEndpoint.GET_GUILD_ROLES, snowflake.id,
        )
    
    fun fetchRole(role: Snowflake): PartialRole.Upgradeable = PartialRole.new(this, role)
    
    val fetchAuditLogs: IRestAction<List<AuditLogEntry>>
        get() = assertPermissions(Permissions.VIEW_AUDIT_LOG) {
            PaginatedAction(
                bot, { AuditLogEntry(this as JsonObject, bot) },
                RestEndpoint.GET_GUILD_AUDIT_LOGS, snowflake.id,
            )
        }
    
    val fetchInvites: IRestAction<List<ExtendedInvite>>
        get() = assertPermissions(Permissions.MANAGE_GUILD) {
            bot.request(
                RestEndpoint.GET_GUILD_INVITES.path(snowflake.id),
                { (this as JsonArray).map { ExtendedInvite(it as JsonObject, bot) } }
            )
        }
    
    val fetchRegions: IRestAction<List<VoiceRegion>>
        get() = bot.request(
            RestEndpoint.GET_GUILD_VOICE_REGIONS.path(snowflake.id),
            { (this as JsonArray).map { VoiceRegion(it as JsonObject, bot) } }
        )
    
    val fetchChannels: IRestAction<List<GuildChannel>>
        get() = bot.request(
            RestEndpoint.GET_GUILD_CHANNELS.path(snowflake.id),
            { (this as JsonArray).mapNotNull(::parseChannel)  }
        )
    
    val fetchWebhooks: IRestAction<List<Webhook>>
        get() = assertPermissions(Permissions.MANAGE_WEBHOOKS) {
            bot.request(
                RestEndpoint.GET_GUILD_AUDIT_LOGS.path(snowflake.id),
                { (this as JsonArray).map { Webhook(it as JsonObject, bot) } }
            )
        }
    
    /**
     * Requires the GUILD_MEMBERS privileged intent
     */
    val fetchMembers: PaginatedAction<Member>
        get() = PaginatedAction(
            bot, { Member(this@PartialGuild, this as JsonObject, bot) },
            RestEndpoint.LIST_GUILD_MEMBERS, snowflake.id,
        )
    
    fun fetchMember(user: Snowflake): PartialMember.Upgradeable
        = object: PartialMember.Upgradeable,
            RestAction<Member>(bot, RestEndpoint.GET_GUILD_MEMBER.path(snowflake.id, user.id),
                               { Member(this@PartialGuild, this as JsonObject, bot) }
            ) {
                override val guild: PartialGuild = this@PartialGuild
                override val user: PartialUser by lazy { bot.users[user] }
            }
    
    val fetchEmojis: IRestAction<List<GuildEmoji>>
        get() = bot.request(
            RestEndpoint.GET_GUILD_EMOJIS.path(snowflake.id),
            { (this as JsonArray).map { GuildEmoji(it as JsonObject, bot) } }
        )
    
    fun fetchEmoji(emoji: Snowflake): IRestAction<GuildEmoji>
        = bot.request(
            RestEndpoint.GET_GUILD_EMOJI.path(snowflake.id, emoji.id),
            { GuildEmoji(this as JsonObject, bot) }
        )
    
    val fetchVanityCode: IRestAction<String>
        get() = bot.request(RestEndpoint.GET_GUILD_VANITY_URL.path(snowflake.id), { (this as JsonPrimitive).content })
    
    val fetchBans: IRestAction<List<Ban>>
        get() = bot.request(
            RestEndpoint.GET_GUILDS_BANS.path(snowflake.id),
            { (this as JsonArray).map { Ban(it as JsonObject, bot) } },
        )
    
    fun fetchBan(user: PartialUser): IRestAction<Ban>
        = bot.request(
            RestEndpoint.GET_GUILDS_BAN.path(snowflake.id, user.snowflake.id),
            { Ban(this as JsonObject, bot) },
        )
    
    val fetchSelfMember: IRestAction<Member>
        get() = IRestAction.FuturesRestAction(bot) {
            bot.selfUser.request().thenCompose { fetchMember(it.snowflake).request() }
        }
    
    val fetchUserPermissions: IRestAction<EnumSet<Permissions>>
        get() = IRestAction.FuturesRestAction(bot) {
            // In constructed guilds, fetchRoles has no requests needed
            fetchSelfMember.request().thenCompose { fetchRoles.request() }.thenApply { selfRoles ->
                var set = 0L
                selfRoles.forEach { el -> set = set and el.permissionsRaw }
                set.bitSetToEnumSet(Permissions.values())
            }
        }
    
    fun addMember(accessToken: String, user: PartialUser): IRestAction<Member>
        = assertPermissions(Permissions.CREATE_INSTANT_INVITE) {
            bot.request(
                RestEndpoint.ADD_GUILD_MEMBER.path(snowflake.id, user.snowflake.id),
                { Member(this@PartialGuild, this as JsonObject, it) },
            ) {
                Json.encodeToString(
                    "access_token" to JsonPrimitive(accessToken)
                )
            }
        }
    
    fun retrievePrunableMemberCount(days: Int): IRestAction<Int>
        = assertPermissions(Permissions.KICK_MEMBERS) {
            bot.request(
                RestEndpoint.DELETE_GUILD.path(listOf("days" to days.toString()), snowflake.id),
                { (this as JsonObject)["pruned"]!!.asInt() }
            )
        }
        
    fun prune(days: Int, vararg role: PartialRole): IRestAction<Unit>
        = assertPermissions(Permissions.KICK_MEMBERS) {
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
    
    interface Upgradeable: PartialGuild, IRestAction<Guild>
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("false"))
    val loaded: Boolean
        get() = false
    
    private fun checkPerms(expected: Array<out Permissions>, actual: EnumSet<Permissions>) {
        if (actual.contains(Permissions.ADMINISTRATOR)) return // Has full bypass
        val lackingPerms = expected.filter { !actual.contains(it) }
        if (lackingPerms.isNotEmpty()) throw InsufficientPermissionsException(lackingPerms)
    }
    
    fun <T: Any> assertPermissions(vararg perm: Permissions, then: () -> IRestAction<T>): IRestAction<T> {
        return fetchUserPermissions.getIfAvailable()?.run {
            checkPerms(perm, this)
            then()
        } ?: fetchUserPermissions.flatMap {
            checkPerms(perm, it)
            then().request()
        }
    }
    
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

    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMembers.request()"))
    // Note: they have a special return type for this called Task, and it's pretty similar to the
    // "CompletableFuture" this RestAction returns.
    fun loadMembers() = fetchMembers.request()
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMembers.request().filter(func)"))
    fun findMembers(func: (Member) -> Boolean): CompletableFuture<List<Member>>
            = fetchMembers.request().thenApply { it.filter(func) }
    @Deprecated("JDA Compatibility Function", ReplaceWith("fetchMembers.request().thenApply(func)"))
    fun findMembers(func: Consumer<Member>)
            = fetchMembers.request().thenApply { it.forEach(func) }
    
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
    @Deprecated("JDA Compatibility Function", ReplaceWith("true"))
    fun checkVerification() = true
    
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
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("\"https://discord.gg/\" + vanityCode"))
    val vanityUrl: String?
        get() = fetchVanityCode.request().get()?.run { "https://discord.gg/$this" }
    
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
    
    // NOT PUBLIC API
    fun parseChannel(it: JsonElement): GuildChannel? {
        val obj = it as JsonObject
        return when (val type = obj["type"]!!.asInt()) {
            0 -> TextChannel(this@PartialGuild, obj, bot)
            2 -> VoiceChannel(obj, bot)
            4 -> Category(obj, bot)
            else -> {
                println("Invalid channel type received for guild ${snowflake.id}: $type")
                null
            }
        }
    }
    
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
class Guild(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), EntityManager<Guild>, PartialGuild {
    /**
     * guild name (2-100 characters, excluding trailing and leading whitespace)
     */
    var name: String by map.delegateJsonMutable(JsonElement::asString, Json::encodeToJsonElement)
    /**
     * id of owner
     */
    var owner: PartialMember.Upgradeable by map.delegateJsonMutable(
        { fetchMember(asSnowflake()) },
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
    val cachedRoles: List<Role> by map.delegateJson({ (this as JsonArray).map { Role(this@Guild, it as JsonObject, bot) } }, "roles")
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
        { PartialVoiceChannel.new(this@Guild, asSnowflake()) },
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
    val widgetChannel: PartialTextChannel.Upgradeable? by map.delegateJsonNullable(::delegateChannel, "widget_channel_id")
    
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
    val cachedUserPermissions: EnumSet<Permissions>? by map.delegateJsonNullable({ asLong().bitSetToEnumSet(Permissions.values()) }, "permissions")
    
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
     * users in the guild <br>
     * Only sent when under the event where the guild becomes available to the bot <br>
     * Requires the GUILD_MEMBERS privileged intent
     */
    val cachedMembers: List<Member>? by map.delegateJsonNullable({ (this as JsonArray).map { Member(this@Guild, it as JsonObject, bot) } }, "members")
    /**
     * channels in the guild <br>
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
    
    override val changes: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
    
    /**
     * Requests that this guild gets edited based on the altered fields.<br>
     * This object will not be updated to reflect the changes, rather a new Guild object is returned from the RestAction.
     */
    override fun edit(): IRestAction<Guild> {
        if (changes.isEmpty()) throw InvalidRequestException("No changes have been made to this guild, yet `edit()` was called.")
        return assertPermissions(Permissions.MANAGE_GUILD) {
            bot.request(RestEndpoint.MODIFY_GUILD.path(snowflake.id), { Guild(this as JsonObject, bot) }) {
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
    
    private fun delegateChannel(json: JsonElement): PartialTextChannel.Upgradeable
        = PartialTextChannel.new(this, json.asSnowflake())
    
    /*
        Properties below override PartialGuild to use the additional information this type has available
     */
    
    // Ensure cached items are used instead of fetching when possible
    override val fetchUserPermissions: IRestAction<EnumSet<Permissions>>
        get() = cachedUserPermissions?.run { IRestAction.ProvidedRestAction(bot, this) } ?: super.fetchUserPermissions
    
    override val fetchRoles: IRestAction<List<Role>>
        get() = IRestAction.ProvidedRestAction(bot, cachedRoles)

    @Deprecated("JDA Compatibility Field", ReplaceWith(""))
    val manager: Guild?
        get() = this
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("edit().request()"))
    fun queue() = edit().request()
    
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
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("bot.guilds[snowflake]"))
    fun retrieveMetaData(): IRestAction<Guild> = bot.guilds[snowflake]
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.any { it.user == user } ?: false"))
    fun isMember(user: User): Boolean
            = cachedMembers?.any { it.user == user } ?: false
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.find { it.user == user }"))
    fun getMember(user: User)
            = cachedMembers?.find { it.user == user }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.find { it.user.snowflake.id == userId }"))
    fun getMember(userId: String)
            = cachedMembers?.find { it.user.snowflake.id == userId }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.find { it.user.snowflake.idLong == userIdLong }"))
    fun getMember(userIdLong: Long)
            = cachedMembers?.find { it.user.snowflake.idLong == userIdLong }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.find { \"\${it.user.username}#\${it.user.discriminator}\" == tag }"))
    fun getMemberByTag(tag: String)
            = cachedMembers?.find { "${it.user.username}#${it.user.discriminator}" == tag }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.find { it.user.username == tag && it.user.discriminator == discrim }"))
    fun getMemberByTag(tag: String, discrim: String)
            = cachedMembers?.find { it.user.username == tag && it.user.discriminator == discrim }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.filter { it.user.username.equals(tag, ignoreCase) } ?: listOf()"))
    fun getMembersByName(tag: String, ignoreCase: Boolean)
            = cachedMembers?.filter { it.user.username.equals(tag, ignoreCase) } ?: listOf()
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.filter { it.nick?.equals(tag, ignoreCase) ?: false } ?: listOf()"))
    fun getMembersByNickname(tag: String, ignoreCase: Boolean)
            = cachedMembers?.filter { it.nick?.equals(tag, ignoreCase) ?: false } ?: listOf()
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.filter { it.nick?.equals(tag, ignoreCase) ?: it.user.username.equals(tag, ignoreCase) } ?: listOf()"))
    fun getMembersByEffectiveName(tag: String, ignoreCase: Boolean)
            = cachedMembers?.filter { it.nick?.equals(tag, ignoreCase) ?: it.user.username.equals(tag, ignoreCase) } ?: listOf()
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.filter { mem -> roles.all { mem.roles.contains(it.snowflake) } } ?: listOf()"))
    fun getMembersWithRoles(vararg roles: Role)
            = cachedMembers?.filter { mem -> roles.all { mem.roles.contains(it.snowflake) } } ?: listOf()
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedMembers?.filter { mem -> roles.all { mem.roles.contains(it.snowflake) } } ?: listOf()"))
    fun getMembersWithRoles(roles: Collection<Role>)
            = cachedMembers?.filter { mem -> roles.all { mem.roles.contains(it.snowflake) } } ?: listOf()
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.find { it.snowflake.id == id }"))
    fun getGuildChannelById(id: String)
            = cachedChannels?.find { it.snowflake.id == id }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.find { it.snowflake.idLong == idLong }"))
    fun getGuildChannelById(idLong: Long)
            = cachedChannels?.find { it.snowflake.idLong == idLong }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.find { it.type == channelType && it.snowflake.id == id }"))
    fun getGuildChannelById(channelType: ChannelType, id: String)
            = cachedChannels?.find { it.type == channelType && it.snowflake.id == id }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.find { it.type == channelType && it.snowflake.idLong == idLong }"))
    fun getGuildChannelById(channelType: ChannelType, idLong: Long)
            = cachedChannels?.find { it.type == channelType && it.snowflake.idLong == idLong }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.mapNotNull { it as? Category }?.find { it.snowflake.id == id }"))
    fun getCategoryById(id: String)
            = cachedChannels?.mapNotNull { it as? Category }?.find { it.snowflake.id == id }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.mapNotNull { it as? Category }?.find { it.snowflake.idLong == idLong }"))
    fun getCategoryById(idLong: Long)
            = cachedChannels?.mapNotNull { it as? Category }?.find { it.snowflake.idLong == idLong }
    @Deprecated("JDA Compatibility Function", ReplaceWith("cachedChannels?.mapNotNull { it as? Category }?.find { it.name.equals(name, ignoreCase) }"))
    fun getCategoriesByName(name: String, ignoreCase: Boolean)
            = cachedChannels?.mapNotNull { it as? Category }?.find { it.name.equals(name, ignoreCase) }
    @Deprecated("JDA Compatibility Field", ReplaceWith("null"))
    val categories
        get() = cachedChannels?.mapNotNull { it as? Category }
    
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
    fun transferOwnership(member: PartialMember.Upgradeable): IRestAction<Guild> = apply { owner = member }.edit()
}

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
