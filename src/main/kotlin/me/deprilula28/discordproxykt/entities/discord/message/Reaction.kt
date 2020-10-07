package me.deprilula28.discordproxykt.entities.discord.message

import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.JdaProxySpectacles
import me.deprilula28.discordproxykt.entities.asBoolean
import me.deprilula28.discordproxykt.entities.asInt

// https://discord.com/developers/docs/resources/channel#reaction-object
class Reaction(private val map: JsonObject, val bot: JdaProxySpectacles) {
    val count: Int by lazy { map["count"]!!.asInt() }
    val me: Boolean by lazy { map["me"]!!.asBoolean() }
    val emote: Emoji by lazy {
        val obj = map["emoji"] as JsonObject
        val id = obj["id"]
        if (id == null) UnicodeEmoji(obj) else ReactionEmoji(obj, bot)
    }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("bot"))
    val jda: JdaProxySpectacles by ::bot
}
