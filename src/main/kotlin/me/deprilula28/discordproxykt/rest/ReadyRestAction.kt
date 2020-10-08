package me.deprilula28.discordproxykt.rest

import me.deprilula28.discordproxykt.DiscordProxyKt
import java.util.concurrent.CompletableFuture

open class ReadyRestAction<T>(private val value: T, override val bot: DiscordProxyKt): IRestAction<T> {
    override fun request(): CompletableFuture<T> = CompletableFuture.completedFuture(value)
}