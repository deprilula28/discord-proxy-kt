package me.deprilula28.discordproxykt.rest

import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.RestException
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

open class RestAction<T: Any>(
    override val bot: DiscordProxyKt,
    private val path: RestEndpoint.Path,
    private val constructor: JsonElement.(DiscordProxyKt) -> T,
    private val postData: (() -> String)? = null,
    private val bodyType: RestEndpoint.BodyType = RestEndpoint.BodyType.JSON,
): IRestAction<T> {
    companion object {
        const val DISCORD_PATH: String = "https://discord.com/api/v8/"
        val routeRateLimits = ConcurrentHashMap<RestEndpoint, RateLimitBucket>()
        
        data class RateLimitBucket(
            var limit: Int,
            var remaining: Int,
            var resetEpochSecs: Long,
            var resetAt: Long,
        )
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
        
        // Rate Limits
        val bucket = routeRateLimits[endpoint] ?: RateLimitBucket(1, 1, -1L, -1L)
        if (!routeRateLimits.containsKey(endpoint)) routeRateLimits[endpoint] = bucket
        
        val time = synchronized(bucket) {
            if (bucket.remaining == 0) bucket.resetEpochSecs
            else {
                bucket.remaining --
                -1
            }
        }
        val delayTime = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(time)
        if (time > 0 && delayTime > 0) {
            delay(delayTime)
            synchronized(bucket) { bucket.remaining = bucket.limit }
            return await() // Re-run the rate limit check at this point
        }
        
        // Request
        val req = HttpRequest.newBuilder()
            .uri(URI(DISCORD_PATH + path.url))
            .method(path.endpoint.method.toString(), postData?.run { HttpRequest.BodyPublishers.ofString(this()) }
                    ?: HttpRequest.BodyPublishers.noBody())
            .header("Authorization", bot.authorization)
            .header("Content-Type", bodyType.typeStr)
            .header("User-Agent", "DiscordBot (https://github.com/deprilula28/discord-proxy-kt v0.0.1)")
            .build()
        val res = bot.client.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream()).await()
        val rateLimitLimit = res.headers().firstValue("X-RateLimit-Limit").get().toInt()
        val rateLimitRemaining = res.headers().firstValue("X-RateLimit-Remaining").get().toInt()
        val rateLimitReset = res.headers().firstValue("X-RateLimit-Reset").get().toLong()
        synchronized(bucket) {
            bucket.limit = rateLimitLimit
            bucket.remaining = rateLimitRemaining
            bucket.resetEpochSecs = rateLimitReset
        }
    
        if (res.statusCode() >= 400)
            throw RestException(
                req.uri().toString(),
                res.body().readAllBytes().toString(Charsets.UTF_8),
                res.statusCode()
            )
        
        val json = Json.decodeFromString(JsonElement.serializer(), res.body().toString())
        if (path.endpoint.method == RestEndpoint.Method.GET) bot.cache.store(path, json)
        return constructor(json, bot)
    }
    
    override fun getIfAvailable(): T? {
        if (path.endpoint.method == RestEndpoint.Method.GET) {
            val item = bot.cache.retrieve<T>(path)
            if (item != null) return item
        }
        return null
    }
}
