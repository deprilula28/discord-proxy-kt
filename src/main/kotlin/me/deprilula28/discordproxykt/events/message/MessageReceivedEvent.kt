package events.message

import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.JdaProxySpectacles
import me.deprilula28.discordproxykt.entities.discord.message.Message

class MessageReceivedEvent(map: JsonObject, bot: JdaProxySpectacles): GenericMessageEvent(map, bot) {
    val message: Message = Message(map, bot)
}
