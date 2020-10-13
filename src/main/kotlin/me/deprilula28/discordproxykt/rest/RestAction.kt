package me.deprilula28.discordproxykt.rest

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.RestException
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

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
            var remaining: Int,
            var resetEpochSecs: Long,
            var waitingFuture: CompletableFuture<Unit>? = null,
        )
    }
    
    private fun createRequest(bucket: RateLimitBucket): CompletableFuture<T> {
        val request = HttpRequest.newBuilder()
            .uri(URI(DISCORD_PATH + path.url))
            .method(path.endpoint.method.toString(), postData?.run { HttpRequest.BodyPublishers.ofString(this()) }
                    ?: HttpRequest.BodyPublishers.noBody())
            .header("Authorization", bot.authorization)
            .header("Content-Type", bodyType.typeStr)
            .header("User-Agent", "DiscordBot (https://github.com/deprilula28/discord-proxy-kt v0.0.1)")
            .build()
        return bot.client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).thenApply {
            val rateLimitRemaining = it.headers().firstValue("X-RateLimit-Remaining").get().toInt()
            val rateLimitReset = it.headers().firstValue("X-RateLimit-Reset").get().toLong()
            synchronized(bucket) {
                bucket.remaining = rateLimitRemaining
                bucket.resetEpochSecs = rateLimitReset
            }
        
            if (it.statusCode() != 200)
                throw RestException(
                    request.uri().toString(),
                    it.body().readAllBytes().toString(Charsets.UTF_8),
                    it.statusCode()
                )
            val res = Json.decodeFromString(JsonElement.serializer(), it.body().toString())
            if (path.endpoint.method == RestEndpoint.Method.GET) bot.cache.store(path, res)
            constructor(res, bot)
        }
    }
    
    /**
     * Sends this REST action and returns a Java future.
     */
    override fun request(): CompletableFuture<T> {
        val endpoint = path.endpoint
        if (endpoint.method == RestEndpoint.Method.GET) {
            val item = bot.cache.retrieve<T>(path)
            if (item != null) return CompletableFuture.completedFuture(item)
        }
        val bucket = routeRateLimits[endpoint] ?: RateLimitBucket(1, -1L)
        if (!routeRateLimits.containsKey(endpoint)) routeRateLimits[endpoint] = bucket
        
        synchronized(bucket) {
            val time = bucket.resetEpochSecs
            // Join in the waiting future
            if (bucket.waitingFuture != null) return bucket.waitingFuture!!.thenCompose { createRequest(bucket) }
            if (bucket.remaining == 0) {
                val wait = CompletableFuture.supplyAsync { Thread.sleep(System.currentTimeMillis() - (time * 1000)) }
                bucket.waitingFuture = wait
                return wait.thenCompose { createRequest(bucket) }
            }
            bucket.remaining --
        }
        
        return createRequest(bucket)
    }
    
    override fun getIfAvailable(): T? {
        if (path.endpoint.method == RestEndpoint.Method.GET) {
            val item = bot.cache.retrieve<T>(path)
            if (item != null) return item
        }
        return null
    }
}
