package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.assertPermissions
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.entities.PartialEntity
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.rest.*
import java.awt.Color
import java.util.*

interface PartialRole: PartialEntity, Message.Mentionable {
    val guild: PartialGuild
    
    companion object {
        fun new(spawnGuild: PartialGuild, id: Snowflake): PartialRole
            = object: PartialRole {
                override val guild: PartialGuild = spawnGuild
                override val bot: DiscordProxyKt = spawnGuild.bot
                override val snowflake: Snowflake = id
    
                override fun upgrade(): IRestAction<Role>
                     = IRestAction.coroutine(spawnGuild.bot) {
                        spawnGuild.fetchRoles.await().find { role -> role.snowflake == id }!!
                    }
    
                override fun toString(): String = "Role(partial, ${snowflake.id})"
            }
    }
    
    fun upgrade(): IRestAction<Role>
    fun delete(): IRestAction<Unit> = bot.request(RestEndpoint.DELETE_GUILD_ROLE.path(guild.snowflake.id, snowflake.id), { Unit })
    
    override val asMention: String
        get() = "<@&${snowflake.id}>"
}

/**
 * Roles represent a set of permissions attached to a group of users. Roles have unique names, colors, and
 * can be "pinned" to the side bar, causing their members to be listed separately. Roles are unique per guild,
 * and can have separate permission profiles for the global context (guild) and channel context. The @everyone role has
 * the same ID as the guild it belongs to.
 * <br>
 * https://discord.com/developers/docs/topics/permissions#role-object-role-structure
 */
open class Role(override val guild: PartialGuild, map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), PartialRole, EntityManager<Role> {
    /**
     * role name
     */
    var name: String by parsing(JsonElement::asString, Json::encodeToJsonElement)
    /**
     * integer representation of hexadecimal color code
     */
    var color: Color by parsing(JsonElement::asColor, { JsonPrimitive((it.red shl 16) or (it.green shl 8) or it.blue) })
    /**
     * if this role is pinned in the user listing
     */
    var hoisted: Boolean by parsing(JsonElement::asBoolean, Json::encodeToJsonElement, "hoist")
    /**
     * position of this role
     */
    val position: Int by parsing(JsonElement::asInt)
    /**
     * permission bit set
     */
    val permissions: EnumSet<Permissions>
        get() = permissionsRaw.bitSetToEnumSet(Permissions.values())
    /**
     * permission bit set
     */
    var permissionsRaw: Long by parsing(JsonElement::asLong, Json::encodeToJsonElement, "permissions")
    /**
     * whether this role is managed by an integration
     */
    val managed: Boolean by parsing(JsonElement::asBoolean)
    /**
     * whether this role is mentionable
     */
    var mentionable: Boolean by parsing(JsonElement::asBoolean, Json::encodeToJsonElement)
    
    val publicRole: Boolean
        get() = position == -1
    
    override val changes: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
    
    /**
     * Requests that this guild gets edited based on the altered fields.
     */
    override fun edit(): IRestAction<Role> {
        if (changes.isEmpty()) throw InvalidRequestException("No changes have been made to this role, yet `edit()` was called.")
        return IRestAction.coroutine(guild.bot) {
            assertPermissions(guild, Permissions.MANAGE_ROLES)
            bot.request(
                RestEndpoint.MODIFY_GUILD_ROLE.path(guild.snowflake.id, snowflake.id),
                { this@Role.apply { map = this@request as JsonObject } },
            ) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }.await()
        }
    }
    
    override fun upgrade(): IRestAction<Role> = IRestAction.ProvidedRestAction(bot, this)
    
    override fun toString(): String = "Role(${snowflake.id}, guild = $guild)"
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("color.rgb"))
    val colorRaw: Int
        get() = color.rgb
    
    @Deprecated("JDA Compatibility Field", ReplaceWith(""))
    val manager: Role?
        get() = this
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("position > role.position"))
    fun canInteract(role: Role) = position > role.position
}

class RoleBuilder(guild: PartialGuild, bot: DiscordProxyKt):
    Role(guild, JsonObject(MapNotReady()), bot), EntityBuilder<Role>
{
    /**
     * Creates a Role based on altered fields and returns it as a rest action.<br>
     * The values of the fields in the builder itself will be updated, making it usable as a Role.
     */
    override fun create(): IRestAction<Role> {
        return IRestAction.coroutine(guild.bot) {
            assertPermissions(guild, Permissions.MANAGE_ROLES)
            bot.coroutineRequest(
                RestEndpoint.CREATE_GUILD_ROLE.path(guild.snowflake.id),
                { this@RoleBuilder.apply { map = this@coroutineRequest as JsonObject } },
            ) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
    
    override fun toString(): String = "Role(builder)"
}
