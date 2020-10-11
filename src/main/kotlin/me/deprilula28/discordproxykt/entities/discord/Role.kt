package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.entities.IPartialEntity
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.rest.*
import java.awt.Color
import java.util.*

// TODO Other methods
interface PartialRole: IPartialEntity, Message.Mentionable {
    val guild: PartialGuild
    
    companion object {
        fun new(spawnGuild: PartialGuild, id: Snowflake): Upgradeable {
            return object: Upgradeable,
                IRestAction.FuturesRestAction<Role>(
                    spawnGuild.bot,
                    // Get requests are cached, so this shouldn't run many times if cache is set up properly
                    { spawnGuild.fetchRoles.request().thenApply { it.find { role -> role.snowflake == id }!! } },
                ) {
                override val guild: PartialGuild = spawnGuild
                    override val snowflake: Snowflake = id
                }
        }
    }
    
    fun delete(): IRestAction<Unit> = RestAction(bot, { Unit }, RestEndpoint.DELETE_GUILD_ROLE, guild.snowflake.id, snowflake.id)
    
    override val asMention: String
        get() = "<@&${snowflake.id}>"
    
    interface Upgradeable: PartialRole, IRestAction<Role>
}

/**
 * Roles represent a set of permissions attached to a group of users. Roles have unique names, colors, a
 * nd can be "pinned" to the side bar, causing their members to be listed separately. Roles are unique per guild,
 * and can have separate permission profiles for the global context (guild) and channel context. The @everyone role has
 * the same ID as the guild it belongs to.
 * <br>
 * https://discord.com/developers/docs/topics/permissions#role-object-role-structure
 */
class Role(override val guild: PartialGuild, map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), PartialRole, EntityManager<Role> {
    /**
     * role name
     */
    var name: String by map.delegateJsonMutable(JsonElement::asString, Json::encodeToJsonElement)
    /**
     * integer representation of hexadecimal color code
     */
    var color: Color by map.delegateJsonMutable(JsonElement::asColor, { Json.encodeToJsonElement(it.rgb) })
    /**
     * if this role is pinned in the user listing
     */
    var hoisted: Boolean by map.delegateJsonMutable(JsonElement::asBoolean, Json::encodeToJsonElement, "hoist")
    /**
     * position of this role
     */
    val position: Int by map.delegateJson(JsonElement::asInt)
    /**
     * permission bit set
     */
    val permissions: EnumSet<Permissions> by lazy { permissionsRaw.bitSetToEnumSet(Permissions.values()) }
    /**
     * permission bit set
     */
    var permissionsRaw: Long by map.delegateJsonMutable(JsonElement::asLong, Json::encodeToJsonElement, "permissions")
    /**
     * whether this role is managed by an integration
     */
    val managed: Boolean by map.delegateJson(JsonElement::asBoolean)
    /**
     * whether this role is mentionable
     */
    var mentionable: Boolean by map.delegateJsonMutable(JsonElement::asBoolean, Json::encodeToJsonElement)
    
    val publicRole: Boolean
        get() = position == -1
    
    override val changes: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
    
    /**
     * Requests that this guild gets edited based on the altered fields.<br>
     * This object will not be updated to reflect the changes, rather a new Role object is returned from the RestAction.
     */
    override fun edit(): IRestAction<Role> {
        if (changes.isEmpty()) throw InvalidRequestException("No changes have been made to this role, yet `edit()` was called.")
        return guild.assertPermissions(Permissions.MANAGE_ROLES) {
            RestAction(bot, { Role(guild, this as JsonObject, bot) }, RestEndpoint.MODIFY_GUILD_ROLE,
                       guild.snowflake.id, snowflake.id) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("color.rgb"))
    val colorRaw: Int
        get() = color.rgb
    
    @Deprecated("JDA Compatibility Field", ReplaceWith(""))
    val manager: Role?
        get() = this
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("position > role.position"))
    fun canInteract(role: Role) = position > role.position
}
