package me.deprilula28.discordproxykt.entities

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.delegateJson

/// Interfaces deriving this only need the ID to run the included functions.
interface IPartialEntity {
    val snowflake: Snowflake
    val bot: DiscordProxyKt
}

/// Classes deriving this will be Discord entities, with an ID and delegated fields.
open class Entity(private val map: JsonObject, override val bot: DiscordProxyKt): IPartialEntity {
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("bot")) val jda: DiscordProxyKt by ::bot
    
    override val snowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "id")
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.id")) val id: String by snowflake::id
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.idLong")) val idLong: Long by snowflake::idLong
}
