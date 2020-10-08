package me.deprilula28.discordproxykt.rest

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.RestException
import java.io.InputStream
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture

// TODO Support rate limits
open class RestAction<T>(
    override val bot: DiscordProxyKt,
    private val constructor: JsonObject.(DiscordProxyKt) -> T,
    endpoint: RestEndpoint,
    vararg pathParts: String,
    postData: (() -> String)? = null,
): IRestAction<T> {
    companion object {
        const val DISCORD_PATH: String = "https://discord.com/api/v8/"
    }
    
    private val request = HttpRequest.newBuilder()
        .uri(URI(DISCORD_PATH + endpoint.path(*pathParts)))
        .method(endpoint.method,
                postData?.run { HttpRequest.BodyPublishers.ofString(this()) } ?: HttpRequest.BodyPublishers.noBody())
        .header("Authorization", bot.authorization)
        .header("User-Agent", "JDAProxySpectacles Worker")
        .build()
    
    /// Send the REST action and return a Java future
    override fun request(): CompletableFuture<T> {
        return bot.client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream()).thenApply {
            if (it.statusCode() != 200) throw RestException(request.uri().toString(),
                                                            it.body().readAllBytes().toString(Charsets.UTF_8),
                                                            it.statusCode())
            constructor(Json.decodeFromString(JsonObject.serializer(), it.body().toString()), bot)
        }
    }
}
