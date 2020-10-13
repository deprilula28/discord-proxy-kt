package me.deprilula28.discordproxykt.entities.discord.message

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.rest.asBoolean
import me.deprilula28.discordproxykt.rest.asInt
import me.deprilula28.discordproxykt.rest.asString
import me.deprilula28.discordproxykt.rest.delegateJson

/**
 * https://discord.com/developers/docs/resources/channel#reaction-object
 */
class Reaction(private val map: JsonObject, val bot: DiscordProxyKt) {
    /**
     * times this emoji has been used to react
     */
    val count: Int by map.delegateJson(JsonElement::asInt)
    /**
     * whether the current user reacted using this emoji
     */
    val me: Boolean by map.delegateJson(JsonElement::asBoolean)
    /**
     * emoji information
     */
    val emoji: Emoji by map.delegateJson({
        val obj = this as JsonObject
        val id = obj["id"]
        if (id == null || id == JsonNull || id.toString() == "null") UnicodeEmoji(obj["name"]!!.asString()) else ReactionEmoji(obj, bot)
    })
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("bot")) val jda: DiscordProxyKt by ::bot
}
