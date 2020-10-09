package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.rest.asInt
import me.deprilula28.discordproxykt.rest.asLong
import me.deprilula28.discordproxykt.rest.bitSetToEnumSet
import me.deprilula28.discordproxykt.rest.delegateJson
import java.util.*

/**
 * https://discord.com/developers/docs/resources/channel#overwrite-object
 */
class PermissionOverwrite(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot) {
    /**
     * either 0 (role) or 1 (member)
     */
    val user: Boolean by map.delegateJson({ asInt() == 1 }, "type")
    /**
     * permission bit set
     */
    val allow: EnumSet<Permissions> by map.delegateJson({ asLong().bitSetToEnumSet(Permissions.values()) })
    /**
     * permission bit set
     */
    val deny: EnumSet<Permissions> by map.delegateJson({ asLong().bitSetToEnumSet(Permissions.values()) })
}
