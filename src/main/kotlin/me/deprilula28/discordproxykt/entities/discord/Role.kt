package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.rest.*
import java.awt.Color
import java.util.*

// TODO Other methods
interface PartialRole: IPartialEntity, Message.Mentionable {
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
class Role(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), PartialRole {
    /**
     * role name
     */
    val name: String by map.delegateJson(JsonElement::asString)
    /**
     * integer representation of hexadecimal color code
     */
    val color: Color by map.delegateJson(JsonElement::asColor)
    /**
     * if this role is pinned in the user listing
     */
    val hoist: Boolean by map.delegateJson(JsonElement::asBoolean)
    /**
     * position of this role
     */
    val position: Int by map.delegateJson(JsonElement::asInt)
    /**
     * permission bit set
     */
    val permissions: EnumSet<Permissions> by map.delegateJson({ asLong().bitSetToEnumSet(Permissions.values()) })
    /**
     * whether this role is managed by an integration
     */
    val managed: Boolean by map.delegateJson(JsonElement::asBoolean)
    /**
     * whether this role is mentionable
     */
    val mentionable: Boolean by map.delegateJson(JsonElement::asBoolean)
}
