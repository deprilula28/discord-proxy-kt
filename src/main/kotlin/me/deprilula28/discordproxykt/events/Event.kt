package me.deprilula28.discordproxykt.events

import me.deprilula28.discordproxykt.entities.Parse

interface Event: Parse {
    /**
     * Internal handling of the event for special activities.
     */
    suspend fun internalHandle() {}
}
