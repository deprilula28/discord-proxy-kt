package me.deprilula28.discordproxykt.rest

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.RestException
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture

// TODO Support rate limits
open class RestAction<T: Any>(
    override val bot: DiscordProxyKt,
    private val constructor: JsonElement.(DiscordProxyKt) -> T,
    private val endpoint: RestEndpoint,
    private vararg val pathParts: String,
    private val postData: (() -> String)? = null,
): IRestAction<T> {
    companion object {
        const val DISCORD_PATH: String = "https://discord.com/api/v8/"
    }
    
    /// Send the REST action and return a Java future
    override fun request(): CompletableFuture<T> {
        val path = endpoint.path(*pathParts)
        if (endpoint.method == RestEndpoint.Method.GET) {
            val item = bot.cache.retrieve<T>(path)
            if (item != null) return CompletableFuture.completedFuture(item)
        }
        val request = HttpRequest.newBuilder()
            .uri(URI(DISCORD_PATH + path))
            .method(endpoint.method.toString(), postData?.run { HttpRequest.BodyPublishers.ofString(this()) }
                    ?: HttpRequest.BodyPublishers.noBody())
            .header("Authorization", bot.authorization)
            .header("User-Agent", "discord-proxy-kt worker")
            .build()
        return bot.client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).thenApply {
            if (it.statusCode() != 200)
                throw RestException(
                    request.uri().toString(),
                    it.body().readAllBytes().toString(Charsets.UTF_8),
                    it.statusCode()
                )
            constructor(Json.decodeFromString(JsonElement.serializer(), it.body().toString()), bot)
        }
    }
}
