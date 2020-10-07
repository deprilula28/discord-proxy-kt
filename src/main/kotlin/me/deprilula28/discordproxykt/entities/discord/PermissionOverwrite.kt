package me.deprilula28.discordproxykt.entities.discord

import me.deprilula28.discordproxykt.JdaProxySpectacles
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.entities.asInt
import me.deprilula28.discordproxykt.entities.asLong
import me.deprilula28.discordproxykt.entities.bitSetToEnumSet
import java.util.*

// https://discord.com/developers/docs/resources/channel#overwrite-object
class PermissionOverwrite(map: JsonObject, bot: JdaProxySpectacles): Entity(map, bot) {
    val user: Boolean by lazy { map["type"]!!.asInt() == 1 }
    val allow: EnumSet<Permissions> by lazy { map["allow"]!!.asLong().bitSetToEnumSet(Permissions.values()) }
    val deny: EnumSet<Permissions> by lazy { map["deny"]!!.asLong().bitSetToEnumSet(Permissions.values()) }
}
