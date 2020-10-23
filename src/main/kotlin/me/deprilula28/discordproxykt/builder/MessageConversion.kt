package me.deprilula28.discordproxykt.builder

import io.ktor.client.request.*

interface MessageConversion {
    fun toMessage(request: HttpRequestBuilder): Any
    
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun build() {}
}