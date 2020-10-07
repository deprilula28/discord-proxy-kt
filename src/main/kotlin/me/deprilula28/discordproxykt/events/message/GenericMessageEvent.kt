package me.deprilula28.discordproxykt.events.message

import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.events.GenericEvent

interface GenericMessageEvent: GenericEvent {
    val id: Snowflake
    val channel: Snowflake
    val guild: Snowflake?
}
