package me.deprilula28.discordproxykt.rest

import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import me.deprilula28.discordproxykt.DiscordProxyKt
import java.util.concurrent.CompletableFuture

interface IRestAction<T> {
    val bot: DiscordProxyKt
    
    companion object {
        fun <T> future(bot: DiscordProxyKt, func: CompletableFuture<T>): IRestAction<T>
            = object: IRestAction<T> {
                override val bot: DiscordProxyKt = bot
                override fun request(): CompletableFuture<T> = func
                override fun getIfAvailable(): T? = func.getNow(null)
            }
    }
    
    fun request(): CompletableFuture<T>
    fun getIfAvailable(): T?
    
    open class LazyFutureAction<T>(override val bot: DiscordProxyKt, futureProvider: () -> CompletableFuture<T>): IRestAction<T> {
        private val lazyFuture = lazy(futureProvider)
        override fun request() = lazyFuture.value
        override fun getIfAvailable(): T? = if (lazyFuture.isInitialized()) lazyFuture.value.getNow(null) else null
    }
    
    open class ProvidedRestAction<T>(override val bot: DiscordProxyKt, private val value: T): IRestAction<T> {
        private val future by lazy { CompletableFuture.completedFuture(value) }
        override fun request(): CompletableFuture<T> = future
        override fun getIfAvailable(): T? = value
    }
    
    // TODO Find a way to optimize these
    fun <V: Any> map(func: (T) -> V): IRestAction<V> = LazyFutureAction(bot) { this@IRestAction.request().thenApply(func) }
    fun <V: Any> flatMap(func: (T) -> CompletableFuture<V>): IRestAction<V> = LazyFutureAction(bot) { this@IRestAction.request().thenCompose(func) }
    
    /// Kotlin coroutine await function
    suspend fun await(): T = request().await()
    
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
    fun complete(): T = request().get()
}