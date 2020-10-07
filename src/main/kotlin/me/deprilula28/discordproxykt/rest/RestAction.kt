package me.deprilula28.discordproxykt.rest

import me.deprilula28.discordproxykt.JdaProxySpectacles
import me.deprilula28.discordproxykt.RestException
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.io.InputStream
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture

// TODO Support rate limits
open class RestAction<T>(
    private val bot: JdaProxySpectacles,
    endpoint: RestEndpoint,
    postData: (() -> InputStream)? = null,
    private val constructor: (JsonObject, JdaProxySpectacles) -> T,
    vararg pathParts: String
) {
    companion object {
        const val DISCORD_PATH: String = "https://discord.com/api/v8/"
    }

    private val request = HttpRequest.newBuilder()
        .uri(URI(DISCORD_PATH + endpoint.path(*pathParts)))
        .method(endpoint.method, postData?.run { HttpRequest.BodyPublishers.ofInputStream(this) } ?: HttpRequest.BodyPublishers.noBody())
        .header("Authorization", bot.authorization)
        .header("User-Agent", "JDAProxySpectacles Worker")
        .build()

    /// Send the REST action and return a Java future
    fun request(): CompletableFuture<T> {
        return bot.client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).thenApply {
            if (it.statusCode() != 200) throw RestException(request.uri().toString(), it.body().readAllBytes().toString(Charsets.UTF_8), it.statusCode())
            constructor(Json.decodeFromString(JsonObject.serializer(), it.body().toString()), bot)
        }
    }
    
    /// Kotlin coroutine await function
    suspend fun await(): T = request().await()

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