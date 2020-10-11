package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.rest.*
import java.time.OffsetDateTime

// TODO Fill this
interface PartialMember {
    val guild: PartialGuild
    val user: PartialUser
    val bot: DiscordProxyKt
    
    private inline fun <reified T: Any> assertRolePerms(role: PartialRole, crossinline func: () -> IRestAction<T>): IRestAction<T> {
        return (guild as? Guild)?.run {
            assertPermissions(Permissions.MANAGE_ROLES) {
                (role as? Role)?.run {
                    assertAboveRole(position) { func() }
                } ?: func()
            }
        } ?: func()
    }
    
    fun add(role: PartialRole): IRestAction<Unit>
        = assertRolePerms(role) {
            RestAction(bot, { Unit }, RestEndpoint.ADD_GUILD_MEMBER_ROLE, guild.snowflake.id, user.snowflake.id, role.snowflake.id)
        }
    
    fun remove(role: PartialRole): IRestAction<Unit>
        = assertRolePerms(role) {
            RestAction(bot, { Unit }, RestEndpoint.REMOVE_GUILD_MEMBER_ROLE, guild.snowflake.id, user.snowflake.id, role.snowflake.id)
        }
    
    fun kick(): IRestAction<Unit>
            = guild.assertPermissions(Permissions.KICK_MEMBERS) {
        RestAction(bot, { Unit }, RestEndpoint.REMOVE_GUILD_MEMBER, guild.snowflake.id, user.snowflake.id)
    }
    
    fun ban(days: Int = 7): IRestAction<Unit> {
        if (days !in 0 .. 7) throw InvalidRequestException("Message deletion days on ban must be from 0 to 7")
        return guild.assertPermissions(Permissions.BAN_MEMBERS) {
            RestAction(bot, { Unit }, RestEndpoint.CREATE_GUILD_BAN, guild.snowflake.id, user.snowflake.id) {
                Json.encodeToString(mapOf("delete_message_days" to JsonPrimitive(days)))
            }
        }
    }
    
    fun unban(): IRestAction<Unit>
            = guild.assertPermissions(Permissions.BAN_MEMBERS) {
        RestAction(bot, { Unit }, RestEndpoint.REMOVE_GUILD_BAN, guild.snowflake.id, user.snowflake.id)
    }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("ban(days)"))
    fun ban(days: Int = 7, reason: String?) = ban(days)
    @Deprecated("JDA Compatibility Function", ReplaceWith("kick()"))
    fun kick(member: String) = kick()
    
    interface Upgradeable: PartialMember, IRestAction<Member>
}

// Warning: Provide readyUser for MESSAGE_CREATE and MESSAGE_UPDATE as Discord won't include the field!
/**
 * a user within a guild
 * <br>
 * https://discord.com/developers/docs/resources/guild#guild-member-object
 */
class Member(override val guild: PartialGuild, private val map: JsonObject, override val bot: DiscordProxyKt, readyUser: User? = null): PartialMember, EntityManager<Member> {
    /**
     * the user this guild member represents
     */
    override val user: User by lazy { readyUser ?: User(map["user"] as JsonObject, bot) }
    /**
     * this users guild nickname
     */
    var nick: String? by map.delegateJsonMutableNullable(JsonElement::asString, Json::encodeToJsonElement)
    /**
     * array of role object ids
     */
    val roles: List<PartialRole.Upgradeable> by map.delegateJsonMutable(
        { (this as JsonArray).map { guild.fetchRole(it.asSnowflake()) } },
        { Json.encodeToJsonElement(it.map { sn -> sn.snowflake.id }) }
    )
    /**
     * when the user joined the guild
     */
    val joinedAt: Timestamp by map.delegateJson(JsonElement::asTimestamp, "joined_at")
    /**
     * when the user started boosting the guild
     */
    val premiumSince: Timestamp? by map.delegateJsonNullable(JsonElement::asTimestamp, "premium_since")
    /**
     * whether the user is deafened in voice channels
     */
    var deaf: Boolean by map.delegateJsonMutable(JsonElement::asBoolean, Json::encodeToJsonElement)
    /**
     * whether the user is muted in voice channels
     */
    var mute: Boolean by map.delegateJsonMutable(JsonElement::asBoolean, Json::encodeToJsonElement)
    
    override val changes: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
    
    /**
     * To finish the procedure, the rest action needs to be called.
     */
    fun move(channel: PartialVoiceChannel?): Member {
        changes["channel_id"] = Json.encodeToJsonElement(channel?.snowflake?.id)
        return this
    }
    
    /**
     * The same as fetching all values of `roles`, but more optimized
     */
    val fetchRoles: IRestAction<List<Role>>
        get() {
            val roleSnowflakes = roles.map { it.snowflake }
            return guild.fetchRoles.map { finalRoles ->
                finalRoles.filter { it.snowflake in roleSnowflakes }
            }
        }
    
    /**
     * Requests that this guild gets edited based on the altered fields.<br>
     * This object will not be updated to reflect the changes, rather a new Webhook object is returned from the RestAction.
     */
    override fun edit(): IRestAction<Member> {
        if (changes.isEmpty()) throw InvalidRequestException("No changes have been made to this webhook, yet `edit()` was called.")
        return RestAction(bot, { Member(guild, this as JsonObject, bot) }, RestEndpoint.MODIFY_GUILD_MEMBER, guild.snowflake.id, user.snowflake.id) {
            val res = Json.encodeToString(changes)
            changes.clear()
            res
        }
    }
    
    fun setRoles(newRoles: Collection<Role>) {
        (newRoles - roles).forEach { role -> add(role) }
        (roles - newRoles).forEach { role -> remove(role) }
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
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("apply { nick = nickname }.edit()"))
    fun modifyNickname(nickname: String?) = apply { nick = nickname }.edit()
    @Deprecated("JDA Compatibility Function", ReplaceWith("apply { deaf = value }.edit()"))
    fun deafen(value: Boolean = true) = apply { deaf = value }.edit()
    @Deprecated("JDA Compatibility Function", ReplaceWith("apply { mute = value }.edit()"))
    fun mute(value: Boolean = true) = apply { mute = value }.edit()
    @Deprecated("JDA Compatibility Function", ReplaceWith("true"))
    fun hasTimeJoined() = true
}