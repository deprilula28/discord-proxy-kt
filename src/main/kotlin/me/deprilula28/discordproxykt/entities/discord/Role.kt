package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.JdaProxySpectacles
import me.deprilula28.discordproxykt.entities.*
import java.awt.Color
import java.util.*

// https://discord.com/developers/docs/topics/permissions#role-object-role-structure
class Role(map: JsonObject, bot: JdaProxySpectacles): Entity(map, bot) {
    val name: String by lazy { map["name"]!!.asString() }
    val color: Color by lazy { map["color"]!!.asColor() }
    val hoist: Boolean by lazy { map["hoist"]!!.asBoolean() }
    val position: Int by lazy { map["position"]!!.asInt() }
    val permissions: EnumSet<Permissions> by lazy { map["permissions"]!!.asLong().bitSetToEnumSet(Permissions.values()) }
    val managed: Boolean by lazy { map["managed"]!!.asBoolean() }
    val mentionable: Boolean by lazy { map["mentionable"]!!.asBoolean() }
}
