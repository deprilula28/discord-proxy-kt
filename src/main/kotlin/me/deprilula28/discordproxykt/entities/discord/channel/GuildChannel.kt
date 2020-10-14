package me.deprilula28.discordproxykt.entities.discord.channel

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.deprilula28.discordproxykt.assertPermissions
import me.deprilula28.discordproxykt.entities.PartialEntity
import me.deprilula28.discordproxykt.entities.discord.*
import me.deprilula28.discordproxykt.rest.IRestAction
import me.deprilula28.discordproxykt.rest.RestEndpoint
import me.deprilula28.discordproxykt.rest.toBitSet
import java.util.*

/**
 * WARNING:<br>
 * This type will not have permission checking ahead of time!<br>
 * If your bot causes more than 10,000 permission errors within 10 minutes, your IP address
 * will be "temporarily restricted from accessing the Discord API". (Interpret that as you will, Discord
 * isn't more specific than that)<br>
 * You can use the RestAction this type implements to get a full type, with permission checking.
 */
interface PartialGuildChannel: PartialEntity {
    val fetchInvites: IRestAction<List<ExtendedInvite>>
        get() = bot.request(
            RestEndpoint.GET_CHANNEL_INVITES.path(snowflake.id),
            { (this as JsonArray).map { ExtendedInvite(it as JsonObject, bot) } },
        )
    
    fun delete(): IRestAction<Unit> = bot.request(RestEndpoint.DELETE_CHANNEL.path(snowflake.id), { Unit })
}

/**
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
interface GuildChannel: PartialGuildChannel {
    /**
     * the id of the guild
     */
    val guild: PartialGuild
    /**
     * the type of channel; only conversion between text and news is supported and only in guilds with the "NEWS" feature
     */
    val type: ChannelType
    /**
     * sorting position of the channel<br>
     * Note: Discord does not assure this will be continuous. There may be two channels under the same position value,
     * in which case the client will sort them by the snowflake bigint.
     */
    val position: Int
    /**
     * the name of the channel (2-100 characters)
     */
    val name: String
    /**
     * explicit permission overwrites for members and roles
     */
    val permissions: List<PermissionOverwrite>
    /**
     * id of the parent category for a channel (each parent category can contain up to 50 channels)
     */
    val category: PartialCategory?
    
    override val fetchInvites: IRestAction<List<ExtendedInvite>>
        get() = assertPermissions(this, Permissions.MANAGE_CHANNELS) { super.fetchInvites }
    
    override fun delete() = assertPermissions(this, Permissions.MANAGE_CHANNELS) { super.delete() }
    
    fun getPermissionOverride(member: PartialMember)
        = permissions.find { it.snowflake == member.user.snowflake } as MemberOverride?
    fun getPermissionOverride(role: PartialRole)
        = permissions.find { it.snowflake == role.snowflake } as RoleOverride?
    
    val memberPermissionOverrides: List<MemberOverride>
        get() = permissions.filterIsInstance(MemberOverride::class.java)
    val rolePermissionOverrides: List<RoleOverride>
        get() = permissions.filterIsInstance(RoleOverride::class.java)
    
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    val manager
        get() = this
    @Deprecated("JDA Compatibility Field", ReplaceWith("position"))
    val positionRaw: Int
        get() = position
    @Deprecated("JDA Compatibility Field", ReplaceWith("listOf()"))
    val members: List<Member>
        get() = listOf()
}
