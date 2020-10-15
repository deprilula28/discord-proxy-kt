package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.assertPermissions
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.channel.GuildChannel
import me.deprilula28.discordproxykt.rest.*
import java.util.*

/**
 * https://discord.com/developers/docs/resources/channel#overwrite-object
 */
abstract class PermissionOverwrite(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), EntityManager<Unit> {
    abstract val channel: GuildChannel
    abstract val guild: PartialGuild
    open val role: PartialRole?
        get() = null
    open val member: PartialMember?
        get() = null
    
    /**
     * permission bit set
     */
    var allow: EnumSet<Permissions> by map.delegateJsonMutable(
        { asLong().bitSetToEnumSet(Permissions.values()) },
        { JsonPrimitive(it.toBitSet()) },
    )
    /**
     * permission bit set
     */
    var deny: EnumSet<Permissions> by map.delegateJsonMutable(
        { asLong().bitSetToEnumSet(Permissions.values()) },
        { JsonPrimitive(it.toBitSet()) },
    )
    
    val inherit: EnumSet<Permissions> by lazy {
        // Perform AND of complements
        // But really slowly cuz this isn't a function of enum set
        val set = EnumSet.noneOf(Permissions::class.java)
        Permissions.values().forEach { if (!allow.contains(it) && !deny.contains(it)) set.add(it) }
        set
    }
    
    val allowRaw: Long by map.delegateJson(JsonElement::asLong, "allow")
    val denyRaw: Long by map.delegateJson(JsonElement::asLong, "deny")
    
    val inheritRaw: Long
        get() = inherit.toBitSet()
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("is MemberOverride"))
    open fun isMemberOverride() = false
    @Deprecated("JDA Compatibility Field", ReplaceWith("is RoleOverride"))
    open fun isRoleOverride() = false
    
    override val changes: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
}

open class MemberOverride(
    override val guild: PartialGuild,
    override val channel: GuildChannel,
    map: JsonObject,
    bot: DiscordProxyKt,
): PermissionOverwrite(map, bot) {
    val user: PartialUser by lazy { bot.fetchUser(snowflake) }
    override val member: PartialMember by lazy { guild.fetchMember(snowflake) }
    
    override fun isMemberOverride(): Boolean = true
    
    override fun edit(): IRestAction<Unit> {
        if (changes.isEmpty()) throw InvalidRequestException("No changes have been made to this overwrite, yet `edit()` was called.")
        return bot.request(RestEndpoint.EDIT_CHANNEL_PERMISSIONS.path(snowflake.id, member.user.snowflake.id), { Json.encodeToString(changes) })
    }
}

// TODO All the crazy methods on https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/requests/restaction/PermissionOverrideAction.html
class MemberOverrideBuilder(
    guild: PartialGuild,
    channel: GuildChannel,
    bot: DiscordProxyKt
): MemberOverride(guild, channel, JsonObject(MapNotReady()), bot), EntityBuilder<MemberOverride> {
    lateinit var builderMember: Snowflake
    
    fun setMember(member: PartialMember): MemberOverrideBuilder {
        this.builderMember = member.user.snowflake
        return this
    }
    
    fun setMember(user: PartialUser): MemberOverrideBuilder {
        this.builderMember = user.snowflake
        return this
    }
    
    /**
     * Creates an MemberOverride based on altered fields and returns it as a rest action.<br>
     * The values of the fields in the builder itself will be updated, making it usable as a MemberOverride.
     */
    override fun create(): IRestAction<MemberOverride> {
        if (!changes.containsKey("id")) throw InvalidRequestException("Member overrides require the user ID.")
        changes["type"] = JsonPrimitive(1)
        return assertPermissions(channel, Permissions.MANAGE_ROLES) {
            bot.request(
                RestEndpoint.EDIT_CHANNEL_PERMISSIONS.path(channel.snowflake.id, builderMember.id),
                { this@MemberOverrideBuilder.apply { map = this@request as JsonObject } },
            ) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
}

open class RoleOverride(
    override val guild: PartialGuild,
    override val channel: GuildChannel,
    map: JsonObject,
    bot: DiscordProxyKt,
): PermissionOverwrite(map, bot) {
    override val role: PartialRole by lazy { guild.fetchRole(snowflake) }
    
    override fun isRoleOverride(): Boolean = true
    
    override fun edit(): IRestAction<Unit> {
        if (changes.isEmpty()) throw InvalidRequestException("No changes have been made to this overwrite, yet `edit()` was called.")
        return bot.request(RestEndpoint.EDIT_CHANNEL_PERMISSIONS.path(snowflake.id, role.snowflake.id), { Json.encodeToString(changes) })
    }
}

// TODO All the crazy methods on https://ci.dv8tion.net/job/JDA/javadoc/net/dv8tion/jda/api/requests/restaction/PermissionOverrideAction.html
class RoleOverrideBuilder(
    guild: PartialGuild,
    channel: GuildChannel,
    bot: DiscordProxyKt
): RoleOverride(guild, channel, JsonObject(MapNotReady()), bot), EntityBuilder<RoleOverride> {
    lateinit var builderRole: Snowflake
    
    fun setRole(role: PartialRole): RoleOverrideBuilder {
        this.builderRole = role.snowflake
        return this
    }
    
    /**
     * Creates an RoleOverride based on altered fields and returns it as a rest action.<br>
     * The values of the fields in the builder itself will be updated, making it usable as a RoleOverride.
     */
    override fun create(): IRestAction<RoleOverride> {
        if (!changes.containsKey("id")) throw InvalidRequestException("Role overrides require the role ID.")
        changes["type"] = JsonPrimitive(0)
        return assertPermissions(channel, Permissions.MANAGE_ROLES) {
            bot.request(
                RestEndpoint.EDIT_CHANNEL_PERMISSIONS.path(channel.snowflake.id, builderRole.id),
                { this@RoleOverrideBuilder.apply { map = this@request as JsonObject } },
            ) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
}
