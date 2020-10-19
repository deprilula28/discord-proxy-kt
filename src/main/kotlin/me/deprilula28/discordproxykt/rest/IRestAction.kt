package me.deprilula28.discordproxykt.rest

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.deprilula28.discordproxykt.DiscordProxyKt
import java.util.concurrent.TimeUnit

interface IRestAction<T> {
    val bot: DiscordProxyKt
    
    companion object {
        fun <T> coroutine(bot: DiscordProxyKt, func: suspend () -> T): IRestAction<T>
            = object: IRestAction<T> {
                var res: T? = null
                override val bot: DiscordProxyKt = bot
                override suspend fun await(): T = res ?: func().apply { res = this }
                override suspend fun getIfAvailable(): T? = res
            }
    }
    
    open class ProvidedRestAction<T>(override val bot: DiscordProxyKt, private val value: T): IRestAction<T> {
        override suspend fun getIfAvailable(): T? = value
        override suspend fun await(): T = value
    }
    
    suspend fun await(): T
    
    suspend fun getIfAvailable(): T?
    
    fun queue() {
        queue({}, bot.defaultExceptionHandler)
    }
    
    fun queue(success: (T) -> Unit) {
        queue(success, bot.defaultExceptionHandler)
    }
    
    fun queue(success: (T) -> Unit, failure: (Exception) -> Unit) {
        bot.scope.launch {
            try {
                success(await())
            } catch (exception: Exception) {
                failure(exception)
            }
        }
    }
    
    fun queueAfter(delay: Long, unit: TimeUnit, ) {
        queueAfter(delay, unit, {}, bot.defaultExceptionHandler)
    }
    
    fun queueAfter(delay: Long, unit: TimeUnit, success: (T) -> Unit) {
        queueAfter(delay, unit, success, bot.defaultExceptionHandler)
    }
    
    fun queueAfter(delay: Long, unit: TimeUnit, success: (T) -> Unit, failure: (Exception) -> Unit) {
        bot.scope.launch {
            delay(unit.toMillis(delay))
            try {
                success(await())
            } catch (exception: Exception) {
                failure(exception)
            }
        }
    }
    
    // JDA Compatibility
    @Deprecated("JDA Compatibility Function", ReplaceWith("queue()"))
    fun complete(): T = runBlocking { await() }
}