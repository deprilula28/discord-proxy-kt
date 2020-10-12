package me.deprilula28.discordproxykt.events.message

import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.PartialMessageChannel
import me.deprilula28.discordproxykt.entities.discord.message.Message

class MessageUpdateEvent(map: JsonObject, override val bot: DiscordProxyKt): GenericMessageEvent {
    val message: Message = Message(map, bot)
    
    override val id: Snowflake by lazy { message.snowflake }
    override val channel: PartialMessageChannel.Upgradeable by lazy {
        // kotlin aint smart enough to figure out types on its own
        if (message.guild == null) message.privateChannel as PartialMessageChannel.Upgradeable
        else message.textChannel as PartialMessageChannel.Upgradeable
    }
}
