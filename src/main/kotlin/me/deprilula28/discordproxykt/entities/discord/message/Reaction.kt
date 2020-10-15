package me.deprilula28.discordproxykt.entities.discord.message

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Parse
import me.deprilula28.discordproxykt.rest.asBoolean
import me.deprilula28.discordproxykt.rest.asInt
import me.deprilula28.discordproxykt.rest.asString
import me.deprilula28.discordproxykt.rest.parsing

/**
 * https://discord.com/developers/docs/resources/channel#reaction-object
 */
class Reaction(override val map: JsonObject, override val bot: DiscordProxyKt): Parse {
    /**
     * times this emoji has been used to react
     */
    val count: Int by parsing(JsonElement::asInt)
    /**
     * whether the current user reacted using this emoji
     */
    val me: Boolean by parsing(JsonElement::asBoolean)
    /**
     * emoji information
     */
    val emoji: Emoji by parsing({
                                         val obj = this as JsonObject
                                         val id = obj["id"]
                                         if (id == null || id == JsonNull || id.toString() == "null") UnicodeEmoji(
                                             obj["name"]!!.asString()) else ReactionEmoji(obj, bot)
                                     })
}
