package me.deprilula28.discordproxykt.rest

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.RestException
import me.deprilula28.discordproxykt.cache.DiscordRestCache
import java.util.concurrent.ConcurrentHashMap

open class RestAction<T: Any>(
    override val bot: DiscordProxyKt,
    private val path: RestEndpoint.Path,
    private val constructor: JsonElement.(DiscordProxyKt) -> T,
    private val postData: (() -> String)? = null,
    private val bodyType: RestEndpoint.BodyType = RestEndpoint.BodyType.JSON,
): IRestAction<T> {
    companion object {
        const val DISCORD_PATH: String = "https://discord.com/api/v8"
        const val USER_AGENT: String = "DiscordBot (https://github.com/deprilula28/discord-proxy-kt v0.0.1)"
        val routeRateLimits = ConcurrentHashMap<RestEndpoint.Path, RateLimitBucket>()
        
        data class RateLimitBucket(
            var limit: Int,
            var remaining: Int,
            var resetEpoch: Long,
            var waitTime: Long,
            var timingJob: Job?,
        ) {
            fun loadFromHeaders(res: HttpResponse): Boolean {
                limit = (res.headers["X-RateLimit-Limit"] ?: return false).toInt()
                remaining = res.headers["X-RateLimit-Remaining"]!!.toInt()
                waitTime = (res.headers["X-RateLimit-Reset-After"]!!.toDouble() * 1000).toLong() + 100 /* Hard coded epsilon for slight disrepancies */
                resetEpoch = System.currentTimeMillis() + waitTime
                return true
            }
        }
    }
    
    private suspend fun request(rateLimit: Pair<RestEndpoint.Path, RateLimitBucket>?): T {
        val builder = HttpRequestBuilder()
        builder.url(DISCORD_PATH + path.url)
        builder.method = path.endpoint.method
        builder.header("Authorization", bot.authorization)
        builder.header("User-Agent", USER_AGENT)

        if (postData != null) {
            builder.header("Content-Type", bodyType.typeStr)
            builder.body = postData.invoke()
        } else if (path.endpoint.method != HttpMethod.Get) builder.body = "" // TODO Remove when https://github.com/ktorio/ktor/issues/1333
    
        val res = bot.client.request<HttpResponse>(builder)
        // TODO Streamed parsing of this
        val contents = withContext(Dispatchers.IO) { res.content.toByteArray() }.toString(Charsets.UTF_8)
        println("Perform request $path: $contents")
        val code = res.status
        
        fun handleRateLimit() {
            if (rateLimit != null) {
                val (endpoint, bucket) = rateLimit
                synchronized(bucket) {
                    if (bucket.loadFromHeaders(res)) {
                        val delayTime = bucket.resetEpoch - System.currentTimeMillis()
                        bucket.timingJob = bot.scope.launch {
                            delay(delayTime)
                            routeRateLimits.remove(endpoint)
                        }
                    } else routeRateLimits.remove(endpoint)
                }
            }
        }
        
        if (code.value >= 400) {
            when (code) {
                HttpStatusCode.Forbidden -> throw InsufficientPermissionsException(listOf())
                HttpStatusCode.TooManyRequests -> {
                    // TODO Handle global rate limits here
                    handleRateLimit()
                    return await()
                }
            }
            throw RestException(path.url, contents, code.value)
        }
        handleRateLimit()
    
        val obj = constructor(if (code == HttpStatusCode.NoContent) JsonNull else Json.decodeFromString(JsonElement.serializer(), contents), bot)
        if (path.endpoint.method == HttpMethod.Get) bot.cache.store(path, DiscordRestCache.StoreType.REQUEST, obj)
        return obj
    }
    
    /**
     * Sends this REST action and returns a Java future.
     */
    override suspend fun await(): T {
        // Cache
        val endpoint = path.endpoint
        if (endpoint.method == HttpMethod.Get) {
            val item = bot.cache.retrieve<T>(path)
            if (item != null) return item
        }
        
        // Rate Limiting
        var deferred: Deferred<T>? = null
        // TODO This lets many threads call it at once, which makes rate limiting not entirely thread safe
        val bucket = routeRateLimits.getOrPut(path) {
            val newBucket = RateLimitBucket(1, -1, -1, -1L, null)
            deferred = bot.scope.async { request(path to newBucket) }
            newBucket.timingJob = deferred
            newBucket
        }
        
        synchronized(bucket) {
            if (bucket.remaining == 0) {
                bucket.timingJob!!
            } else {
                bucket.remaining --
                null
            }
        }?.run {
            join()
            return@await await()
        }
        
        // Request
        return deferred?.run { await() } ?: request(null)
    }
    
    override suspend fun getIfAvailable(): T? {
        if (path.endpoint.method == HttpMethod.Get) {
            val item = bot.cache.retrieve<T>(path)
            if (item != null) return item
        }
        return null
    }
}
