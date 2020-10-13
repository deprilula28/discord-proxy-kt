package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.channel.GuildChannel
import me.deprilula28.discordproxykt.rest.*
import java.util.*

/**
 * https://discord.com/developers/docs/resources/channel#overwrite-object
 */
abstract class PermissionOverwrite(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot) {
    abstract val channel: GuildChannel
    abstract val guild: PartialGuild.Upgradeable
    open val role: PartialRole?
        get() = null
    open val member: PartialMember?
        get() = null
    
    /**
     * permission bit set
     */
    val allow: EnumSet<Permissions> by map.delegateJson({ asLong().bitSetToEnumSet(Permissions.values()) })
    /**
     * permission bit set
     */
    val deny: EnumSet<Permissions> by map.delegateJson({ asLong().bitSetToEnumSet(Permissions.values()) })
    
    val inherit: EnumSet<Permissions> by lazy {
        // Perform AND of complements
        // But really slowly cuz this isn't a function of enum set
        val set = EnumSet.noneOf(Permissions::class.java)
        Permissions.values().forEach { if (!allow.contains(it) && !deny.contains(it)) set.add(it) }
        set
    }
    
    val allowRaw: Long
        get() = allow.toBitSet()
    
    val denyRaw: Long
        get() = deny.toBitSet()
    
    val inheritRaw: Long
        get() = inherit.toBitSet()
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("is MemberOverride"))
    open fun isMemberOverride() = false
    @Deprecated("JDA Compatibility Field", ReplaceWith("is RoleOverride"))
    open fun isRoleOverride() = false
}

open class MemberOverride(
    override val guild: PartialGuild.Upgradeable,
    override val channel: GuildChannel,
    map: JsonObject,
    bot: DiscordProxyKt,
): PermissionOverwrite(map, bot) {
    val user: PartialUser by lazy { bot.fetchUser(snowflake) }
    override val member: PartialMember by lazy { guild.fetchMember(snowflake) }
    
    override fun isMemberOverride(): Boolean = true
}

open class RoleOverride(
    override val guild: PartialGuild.Upgradeable,
    override val channel: GuildChannel,
    map: JsonObject,
    bot: DiscordProxyKt,
): PermissionOverwrite(map, bot) {
    override val role: PartialRole by lazy { guild.fetchRole(snowflake) }
    
    override fun isRoleOverride(): Boolean = true
}
