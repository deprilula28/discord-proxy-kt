package me.deprilula28.discordproxykt.entities

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.rest.IRestAction
import me.deprilula28.discordproxykt.rest.RestAction
import me.deprilula28.discordproxykt.rest.asSnowflake
import me.deprilula28.discordproxykt.rest.delegateJson

/**
 * Types deriving this only need the ID to run the included functions.<br>
 * They should have an upgrade method that returns a rest action of the full object.
  */
interface PartialEntity {
    val snowflake: Snowflake
    val bot: DiscordProxyKt
}

/// Classes deriving this will be Discord entities, with an ID and delegated fields.
open class Entity(protected var map: JsonObject, override val bot: DiscordProxyKt): PartialEntity {
    override val snowflake: Snowflake by map.delegateJson(JsonElement::asSnowflake, "id")
    
    @Deprecated("JDA Compatibility Field", ReplaceWith("bot")) val jda: DiscordProxyKt by ::bot
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.id")) val id: String by snowflake::id
    @Deprecated("JDA Compatibility Field", ReplaceWith("snowflake.idLong")) val idLong: Long by snowflake::idLong
}
