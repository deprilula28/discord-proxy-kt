package me.deprilula28.discordproxykt.rest

import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import me.deprilula28.discordproxykt.DiscordProxyKt
import java.util.concurrent.CompletableFuture

interface IRestAction<T> {
    val bot: DiscordProxyKt
    
    fun request(): CompletableFuture<T>
    
    /// Kotlin coroutine await function
    suspend fun await(): T = request().await()
    
    // JDA Compatibility
    @Deprecated("JDA Compatibility Function", ReplaceWith("request()", "java.util.concurrent.CompletableFuture"))
    fun queue() {
        bot.scope.launch { await() }
    }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("request()", "java.util.concurrent.CompletableFuture"))
    fun queue(func: (T) -> Unit) {
        bot.scope.launch { func(await()) }
    }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("request()", "java.util.concurrent.CompletableFuture"))
    fun queue(func: (T) -> Unit, err: (Exception) -> Unit) {
        bot.scope.launch {
            try {
                func(await())
            } catch (exception: Exception) {
                err(exception)
            }
        }
    }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith("request()", "java.util.concurrent.CompletableFuture"))
    fun complete(): T = request().get()
}