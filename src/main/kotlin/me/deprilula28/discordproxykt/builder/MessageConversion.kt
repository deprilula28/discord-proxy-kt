package me.deprilula28.discordproxykt.builder

import me.deprilula28.discordproxykt.rest.RestEndpoint

interface MessageConversion {
    fun toMessage(): Pair<String, RestEndpoint.BodyType>
}