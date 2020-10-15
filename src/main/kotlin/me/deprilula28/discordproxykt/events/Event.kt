package me.deprilula28.discordproxykt.events

import me.deprilula28.discordproxykt.entities.Parse

interface Event: Parse {
    fun handle() {}
}
