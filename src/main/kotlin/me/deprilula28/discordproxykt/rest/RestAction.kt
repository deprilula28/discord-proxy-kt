package me.deprilula28.discordproxykt.rest

import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.RestException
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
        const val DISCORD_PATH: String = "https://discord.com/api/v8"
        val routeRateLimits = ConcurrentHashMap<RestEndpoint, RateLimitBucket>()
        
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
            
            fun loadFromHeaders(res: HttpResponse<InputStream>) {
                limit = res.headers().firstValue("X-RateLimit-Limit").get().toInt()
                remaining = res.headers().firstValue("X-RateLimit-Remaining").get().toInt()
                waitTime = (res.headers().firstValue("X-RateLimit-Reset-After").get().toDouble() * 1000).toLong() + 100 /* Hard coded epsilon for slight disrepancies */
                resetEpoch = System.currentTimeMillis() + waitTime
                println("Loaded from headers")
            }
        }
    }
    
    private suspend fun request(): Pair<T, HttpResponse<InputStream>> {
        val req = HttpRequest.newBuilder()
            .uri(URI(DISCORD_PATH + path.url))
            .method(path.endpoint.method.toString(), postData?.run { HttpRequest.BodyPublishers.ofString(this()) }
                    ?: HttpRequest.BodyPublishers.noBody())
            .header("Authorization", bot.authorization)
            .header("Content-Type", bodyType.typeStr)
            .header("User-Agent", "DiscordBot (https://github.com/deprilula28/discord-proxy-kt v0.0.1)")
            .build()
        val res = bot.client.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream()).await()
        // TODO Streamed parsing of this
        val contents = withContext(Dispatchers.IO) { res.body().readBytes() }.toString(Charsets.UTF_8)
        if (res.statusCode() >= 400)
            throw RestException(
                req.uri().toString(),
                contents,
                res.statusCode()
            )
    
        val json = Json.decodeFromString(JsonElement.serializer(), contents)
        if (path.endpoint.method == RestEndpoint.Method.GET) bot.cache.store(path, json)
        return constructor(json, bot) to res
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
        val bucket = routeRateLimits.getOrPut(endpoint) {
            println("Running the getOrPut internal function")
            val newBucket = RateLimitBucket(1, -1, -1, -1L, null)
            deferred = bot.scope.async {
                val (obj, res) = request()
                synchronized(newBucket) {
                    newBucket.loadFromHeaders(res)
                    val delayTime = newBucket.resetEpoch - System.currentTimeMillis()
                    newBucket.timingJob = bot.scope.launch {
                        delay(delayTime)
                        routeRateLimits.remove(endpoint)
                    }
                }
                obj
            }
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
        return deferred?.run { await() } ?: request().first
    }
    
    override fun getIfAvailable(): T? {
        if (path.endpoint.method == RestEndpoint.Method.GET) {
            val item = bot.cache.retrieve<T>(path)
            if (item != null) return item
        }
        return null
    }
}
