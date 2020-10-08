package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.rest.IRestAction
import java.awt.Color
import java.util.*

// TODO Other methods
interface PartialRole: IPartialEntity, Message.Mentionable {
    override val asMention: String
        get() = "<@&${snowflake.id}>"
    
    interface Upgradeable: PartialRole, IRestAction<Role>
}

// https://discord.com/developers/docs/topics/permissions#role-object-role-structure
class Role(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), PartialRole {
    val name: String by map.delegateJson(JsonElement::asString)
    val color: Color by map.delegateJson(JsonElement::asColor)
    val hoist: Boolean by map.delegateJson(JsonElement::asBoolean)
    val position: Int by map.delegateJson(JsonElement::asInt)
    val permissions: EnumSet<Permissions> by map.delegateJson({ asLong().bitSetToEnumSet(Permissions.values()) })
    val managed: Boolean by map.delegateJson(JsonElement::asBoolean)
    val mentionable: Boolean by map.delegateJson(JsonElement::asBoolean)
}
