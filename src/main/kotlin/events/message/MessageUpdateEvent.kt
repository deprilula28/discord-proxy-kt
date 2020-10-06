package events.message

import entities.discord.Member
import entities.message.Message
import entities.discord.User

class MessageUpdateEvent(val message: Message): GenericMessageEvent(message.id, message.channelId, message.guildId, message.bot) {
    val author: User
        get() = message.author
    
    val member: Member?
        get() = message.member
    
    val isWebhookMessage: Boolean
        get() = message.webhookId != null
}
