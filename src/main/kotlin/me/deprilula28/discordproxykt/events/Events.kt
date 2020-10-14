package me.deprilula28.discordproxykt.events

import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.events.message.MessageReceivedEvent
import me.deprilula28.discordproxykt.events.message.MessageUpdateEvent

object Events {
    val MESSAGE_CREATE = Event("MESSAGE_CREATE", ::MessageReceivedEvent, MessageReceivedEvent::class.java)
    val MESSAGE_UPDATE = Event("MESSAGE_UPDATE", ::MessageUpdateEvent, MessageUpdateEvent::class.java)
    
    data class Event<T>(val eventName: String, val constructor: (JsonObject, DiscordProxyKt) -> T, val clazz: Class<T>)
}