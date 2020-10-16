package me.deprilula28.discordproxykt.rest

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.deprilula28.discordproxykt.DiscordProxyKt

interface IRestAction<T> {
    val bot: DiscordProxyKt
    
    companion object {
        fun <T> coroutine(bot: DiscordProxyKt, func: suspend () -> T): IRestAction<T>
            = object: IRestAction<T> {
                var res: T? = null
                override val bot: DiscordProxyKt = bot
                override suspend fun await(): T = res ?: func().apply { res = this }
                override fun getIfAvailable(): T? = res
            }
    }
    
    open class ProvidedRestAction<T>(override val bot: DiscordProxyKt, private val value: T): IRestAction<T> {
        override fun getIfAvailable(): T? = value
        override suspend fun await(): T = value
    }
    
    suspend fun await(): T
    
    fun getIfAvailable(): T?
    
    fun queue() {
        queue({}, bot.defaultExceptionHandler)
    }
    
    fun queue(func: (T) -> Unit) {
        queue(func, bot.defaultExceptionHandler)
    }
    
    fun queue(func: (T) -> Unit, err: (Exception) -> Unit) {
        bot.scope.launch {
            try {
                func(await())
            } catch (exception: Exception) {
                err(exception)
            }
        }
    }
    
    // JDA Compatibility
    @Deprecated("JDA Compatibility Function", ReplaceWith("queue()"))
    fun complete(): T = runBlocking { await() }
}