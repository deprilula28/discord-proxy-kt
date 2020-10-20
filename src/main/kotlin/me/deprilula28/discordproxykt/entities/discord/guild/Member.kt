package me.deprilula28.discordproxykt.entities.discord.guild

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.assertPermissions
import me.deprilula28.discordproxykt.entities.PartialEntity
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.Timestamp
import me.deprilula28.discordproxykt.entities.discord.PartialUser
import me.deprilula28.discordproxykt.entities.discord.Permissions
import me.deprilula28.discordproxykt.entities.discord.User
import me.deprilula28.discordproxykt.entities.discord.channel.PartialVoiceChannel
import me.deprilula28.discordproxykt.rest.*
import java.awt.Color
import java.time.OffsetDateTime
import java.util.*

/**
 * This type is used for operations when an ID of a [Member] is known.<br>
 * If the data is also known it will implement [Member], and [upgrade] is a no-op.<br>
 * If it isn't known, [upgrade] will be a request to get the data from Discord.
 */
interface PartialMember: PartialEntity {
    val guild: PartialGuild
    val user: PartialUser
    
    fun upgrade(): IRestAction<Member>
    
    private suspend fun assertRolePerms(role: PartialRole) {
        assertPermissions(guild, Permissions.MANAGE_ROLES)
        (role as? Role)?.run {
            val roles = guild.fetchSelfMember.await().fetchRoles.await()
            val highest = roles.maxByOrNull { it.position }
            if (highest == null || this.position > highest.position) throw PermissionHierarchyException(this, highest)
        }
    }
    
    fun add(role: PartialRole): IRestAction<Unit>
        = IRestAction.coroutine(bot) {
            assertRolePerms(role)
            bot.coroutineRequest(RestEndpoint.ADD_GUILD_MEMBER_ROLE.path(guild.snowflake.id, user.snowflake.id, role.snowflake.id), { Unit })
        }
    
    fun remove(role: PartialRole): IRestAction<Unit>
        = IRestAction.coroutine(bot) {
            assertRolePerms(role)
            bot.coroutineRequest(RestEndpoint.REMOVE_GUILD_MEMBER_ROLE.path(guild.snowflake.id, user.snowflake.id, role.snowflake.id), { Unit })
        }
    
    fun kick(): IRestAction<Unit>
        = IRestAction.coroutine(bot) {
            assertPermissions(guild, Permissions.KICK_MEMBERS)
            bot.coroutineRequest(RestEndpoint.REMOVE_GUILD_MEMBER.path(guild.snowflake.id, user.snowflake.id), { Unit })
        }
    
    fun ban(days: Int = 7): IRestAction<Unit> {
        if (days !in 0 .. 7) throw InvalidRequestException("Message deletion days on ban must be from 0 to 7")
        return IRestAction.coroutine(bot) {
            assertPermissions(guild, Permissions.BAN_MEMBERS)
            bot.coroutineRequest(RestEndpoint.CREATE_GUILD_BAN.path(guild.snowflake.id, user.snowflake.id), { Unit }) {
                Json.encodeToString(mapOf("delete_message_days" to JsonPrimitive(days)))
            }
        }
    }
    
    fun unban(): IRestAction<Unit>
        = IRestAction.coroutine(bot) {
            assertPermissions(guild, Permissions.BAN_MEMBERS)
            bot.request(RestEndpoint.REMOVE_GUILD_BAN.path(guild.snowflake.id, user.snowflake.id), { Unit })
        }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("ban(days)"))
    fun ban(days: Int = 7, reason: String?) = ban(days)
    @Deprecated("JDA Compatibility Function", ReplaceWith("kick()"))
    fun kick(member: String) = kick()
}

// Warning: Provide readyUser for MESSAGE_CREATE and MESSAGE_UPDATE as Discord won't include the field!
/**
 * a user within a guild
 * <br>
 * https://discord.com/developers/docs/resources/guild#guild-member-object
 */
class Member(override val guild: PartialGuild, override var map: JsonObject, override val bot: DiscordProxyKt, private val readyUser: User? = null): PartialMember, EntityManager<Member> {
    /**
     * the user this guild member represents
     */
    override val user: User
        get() = readyUser ?: User(map["user"] as JsonObject, bot)
    /**
     * this users guild nickname
     */
    var nick: String? by parsingOpt(JsonElement::asString, Json::encodeToJsonElement)
    /**
     * array of role object ids
     */
    val roles: List<PartialRole> by parsing(
        { (this as JsonArray).map { guild.fetchRole(it.asSnowflake()) } },
        { Json.encodeToJsonElement(it.map { sn -> sn.snowflake.id }) }
    )
    /**
     * when the user joined the guild
     */
    val joinedAt: Timestamp by parsing(JsonElement::asTimestamp, "joined_at")
    /**
     * when the user started boosting the guild
     */
    val premiumSince: Timestamp? by parsingOpt(JsonElement::asTimestamp, "premium_since")
    /**
     * whether the user is deafened in voice channels
     */
    var deaf: Boolean by parsing(JsonElement::asBoolean, Json::encodeToJsonElement)
    /**
     * whether the user is muted in voice channels
     */
    var mute: Boolean by parsing(JsonElement::asBoolean, Json::encodeToJsonElement)
    
    override val changes: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
    
    /**
     * To finish the procedure, the rest action needs to be called.
     */
    fun move(channel: PartialVoiceChannel?): Member {
        changes["channel_id"] = Json.encodeToJsonElement(channel?.snowflake?.id)
        return this
    }
    
    override fun upgrade(): IRestAction<Member> = IRestAction.ProvidedRestAction(bot, this)
    
    /**
     * The same as fetching all values of `roles`, but more optimized
     */
    val fetchRoles: IRestAction<List<Role>>
        get() {
            val roleSnowflakes = roles.map { it.snowflake }
            return IRestAction.coroutine(bot) {
                guild.fetchRoles.await().filter { it.snowflake in roleSnowflakes }
            }
        }
    
    /**
     * Requests that this guild gets edited based on the altered fields.
     */
    override fun edit(): IRestAction<Member> {
        if (changes.isEmpty()) throw InvalidRequestException("No changes have been made to this member, yet `edit()` was called.")
        val permissions = mutableListOf<Permissions>()
        if (changes.containsKey("nick")) permissions.add(Permissions.MANAGE_NICKNAMES)
        if (changes.containsKey("roles")) permissions.add(Permissions.MANAGE_ROLES)
        if (changes.containsKey("mute")) permissions.add(Permissions.MUTE_MEMBERS)
        if (changes.containsKey("deaf")) permissions.add(Permissions.DEAFEN_MEMBERS)
        if (changes.containsKey("channel_id")) permissions.add(Permissions.MOVE_MEMBERS)
        
        return IRestAction.coroutine(bot) {
            assertPermissions(guild, *permissions.toTypedArray())
            bot.coroutineRequest(
                RestEndpoint.MODIFY_GUILD_MEMBER.path(guild.snowflake.id, user.snowflake.id),
                { this@Member.apply { map = this@coroutineRequest as JsonObject } }
            ) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
    
    fun setRoles(newRoles: Collection<Role>) {
        (newRoles - roles).forEach { role -> add(role) }
        (roles - newRoles).forEach { role -> remove(role) }
    }
    
    fun hasAuthorityOver(other: Member): IRestAction<Boolean>
        = IRestAction.coroutine(bot) {
            if (this.user.snowflake == other.user.snowflake) return@coroutine false
            val otherRoles = other.fetchRoles.await()
            val selfRoles = fetchRoles.await()
            (selfRoles.maxByOrNull { it.position }?.position ?: 0) > (otherRoles.maxByOrNull { it.position }?.position ?: 0)
        }
    
    fun hasAuthorityOver(role: Role): IRestAction<Boolean>
        = IRestAction.coroutine(bot) {
            fetchRoles.await().maxByOrNull { it.position }?.position ?: 0 > role.position
        }
    
    val fetchColor: IRestAction<Color>
        get() = IRestAction.coroutine(bot) {
            fetchRoles.await().filter { it.color != Color.black }.maxByOrNull { it.position }?.color ?: Color.black
        }
    
    val fetchPermissions: IRestAction<EnumSet<Permissions>>
        get() = IRestAction.coroutine(bot) {
            var bitSet = 0L
            fetchRoles.await().forEach { el -> bitSet = bitSet or el.permissionsRaw }
            bitSet.bitSetToEnumSet(Permissions.values())
        }
    
    override val snowflake: Snowflake
        get() = user.snowflake
    
    override fun toString(): String = "Member($user, $guild)"
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("guild.owner == member"))
    val owner: Boolean
        get() {
            return if (guild is Guild) guild.owner.user.snowflake == user.snowflake
            else guild.upgrade().complete().owner.user.snowflake == user.snowflake
        }
    @Deprecated("JDA Compatibility Field", ReplaceWith("joinedAt.offsetDateTime"))
    val timeJoined: OffsetDateTime
        get() = joinedAt.offsetDateTime
    @Deprecated("JDA Compatibility Field", ReplaceWith("premiumSince.offsetDateTime"))
    val timeBoosted: OffsetDateTime?
        get() = premiumSince?.offsetDateTime
    @Deprecated("JDA Compatibility Field", ReplaceWith("listOf()"))
    val activity: List<Nothing>
        get() = listOf()
    @Deprecated("JDA Compatibility Field", ReplaceWith("color"))
    val color: Color
        get() = fetchColor.complete()
    @Deprecated("JDA Compatibility Field", ReplaceWith("color.rgb"))
    val colorRaw: Int
        get() = fetchColor.complete().rgb
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("hasAuthorityOver(member).complete()"))
    fun canInteract(member: Member) = hasAuthorityOver(member).complete()
    @Deprecated("JDA Compatibility Function", ReplaceWith("hasAuthorityOver(role).complete()"))
    fun canInteract(role: Role) = hasAuthorityOver(role).complete()
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("apply { nick = nickname }.edit()"))
    fun modifyNickname(nickname: String?) = apply { nick = nickname }.edit()
    @Deprecated("JDA Compatibility Function", ReplaceWith("apply { deaf = value }.edit()"))
    fun deafen(value: Boolean = true) = apply { deaf = value }.edit()
    @Deprecated("JDA Compatibility Function", ReplaceWith("apply { mute = value }.edit()"))
    fun mute(value: Boolean = true) = apply { mute = value }.edit()
    @Deprecated("JDA Compatibility Function", ReplaceWith("true"))
    fun hasTimeJoined() = true
}