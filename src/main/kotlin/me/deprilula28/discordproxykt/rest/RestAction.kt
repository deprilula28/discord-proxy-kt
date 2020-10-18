package me.deprilula28.discordproxykt.rest

import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.RestException
import me.deprilula28.discordproxykt.cache.DiscordRestCache
import java.io.InputStream
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.ConcurrentHashMap

open class RestAction<T: Any>(
    override val bot: DiscordProxyKt,
    private val path: RestEndpoint.Path,
    private val constructor: JsonElement.(DiscordProxyKt) -> T,
    private val postData: (() -> String)? = null,
    private val bodyType: RestEndpoint.BodyType = RestEndpoint.BodyType.JSON,
): IRestAction<T> {
    companion object {
        const val DISCORD_PATH: String = "https://discord.com/api/v7"
        val routeRateLimits = ConcurrentHashMap<RestEndpoint.Path, RateLimitBucket>()
        
        data class RateLimitBucket(
            var limit: Int,
            var remaining: Int,
            var resetEpoch: Long,
            var waitTime: Long,
            var timingJob: Job?,
        ) {
            fun resetTimer() {
                resetEpoch = System.currentTimeMillis() + waitTime
                remaining = limit
            }
            
            fun loadFromHeaders(res: HttpResponse<InputStream>): Boolean {
                val limitHeader = res.headers().firstValue("X-RateLimit-Limit")
                if (!limitHeader.isPresent) return false
                limit = limitHeader.get().toInt()
                remaining = res.headers().firstValue("X-RateLimit-Remaining").get().toInt()
                waitTime = (res.headers().firstValue("X-RateLimit-Reset-After").get().toDouble() * 1000).toLong() + 100 /* Hard coded epsilon for slight disrepancies */
                resetEpoch = System.currentTimeMillis() + waitTime
                return true
            }
        }
    }
    
    private suspend fun request(rateLimit: Pair<RestEndpoint.Path, RateLimitBucket>?): T {
        val builder = HttpRequest.newBuilder()
            .uri(URI(DISCORD_PATH + path.url))
            .header("Authorization", bot.authorization)
            .header("Content-Type", bodyType.typeStr)
            .header("User-Agent", "DiscordBot (https://github.com/deprilula28/discord-proxy-kt v0.0.1)")
        
        if (postData == null) builder.method(path.endpoint.method.toString(), HttpRequest.BodyPublishers.noBody())
        else builder.method(path.endpoint.method.toString(), HttpRequest.BodyPublishers.ofByteArray(postData.invoke().encodeToByteArray()))
        
        val req = builder.build()
        val res = bot.client.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream()).await()
        // TODO Streamed parsing of this
        val contents = withContext(Dispatchers.IO) { res.body().readBytes() }.toString(Charsets.UTF_8)
        val code = res.statusCode()
        
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
        
        if (code >= 400) {
            when (code) {
                403 -> throw InsufficientPermissionsException(listOf()) // Missing permissions
                493 -> {
                    // TODO Handle global rate limits here
                    handleRateLimit()
                    return await()
                }
            }
            throw RestException(req.uri().toString(), contents, code)
        }
        handleRateLimit()
    
        // 204 No Content
        val obj = constructor(if (code == 204) JsonNull else Json.decodeFromString(JsonElement.serializer(), contents), bot)
        if (path.endpoint.method == RestEndpoint.Method.GET) bot.cache.store(path, DiscordRestCache.StoreType.REQUEST, obj)
        return obj
    }
    
    /**
     * Sends this REST action and returns a Java future.
     */
    override suspend fun await(): T {
        // Cache
        val endpoint = path.endpoint
        if (endpoint.method == RestEndpoint.Method.GET) {
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
        println("Performing request at $path")
        return deferred?.run { await() } ?: request(null)
    }
    
    override fun getIfAvailable(): T? {
        if (path.endpoint.method == RestEndpoint.Method.GET) {
            val item = runBlocking { bot.cache.retrieve<T>(path) }
            if (item != null) return item
        }
        return null
    }
}
