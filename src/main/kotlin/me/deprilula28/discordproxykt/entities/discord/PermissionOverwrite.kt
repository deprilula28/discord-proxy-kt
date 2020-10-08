package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import java.util.*

// https://discord.com/developers/docs/resources/channel#overwrite-object
class PermissionOverwrite(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot) {
    val user: Boolean by map.delegateJson({ asInt() == 1 })
    val allow: EnumSet<Permissions> by map.delegateJson({ asLong().bitSetToEnumSet(Permissions.values()) })
    val deny: EnumSet<Permissions> by map.delegateJson({ asLong().bitSetToEnumSet(Permissions.values()) })
}
