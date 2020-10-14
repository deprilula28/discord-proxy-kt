package me.deprilula28.discordproxykt.entities.discord.channel

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.entities.PartialEntity
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.Timestamp
import me.deprilula28.discordproxykt.entities.discord.message.PartialMessage
import me.deprilula28.discordproxykt.rest.*

interface PartialPrivateChannel: PartialMessageChannel, PartialEntity {
    companion object {
        fun new(id: Snowflake, bot: DiscordProxyKt): PartialPrivateChannel
            = object: PartialPrivateChannel {
                override val snowflake: Snowflake = id
                override val bot: DiscordProxyKt = bot
    
                override fun upgrade(): IRestAction<PrivateChannel>
                    = RestAction(bot, RestEndpoint.CREATE_DM.path(), { PrivateChannel(this as JsonObject, bot) }, {
                        Json.encodeToString("recipient_id" to JsonPrimitive(id.id))
                    }
                )
            }
    }
    
    override val type: ChannelType
        get() = ChannelType.PRIVATE
    
    fun upgrade(): IRestAction<PrivateChannel>
}

/**
 * a direct message between users
 * <br>
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
class PrivateChannel(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), MessageChannel, PartialPrivateChannel {
    override val lastPinTimestamp: Timestamp? by map.delegateJsonNullable(JsonElement::asTimestamp, "last_pin_timestamp")
    override val lastMessage: PartialMessage by map.delegateJson({ fetchMessage(asSnowflake()) }, "last_message_id")
    
    override fun upgrade(): IRestAction<PrivateChannel> = IRestAction.ProvidedRestAction(bot, this)
}
