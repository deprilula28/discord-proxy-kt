package me.deprilula28.discordproxykt

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.cache.DiscordRestCache
import me.deprilula28.discordproxykt.entities.Snowflake
import me.deprilula28.discordproxykt.entities.discord.PartialUser
import me.deprilula28.discordproxykt.entities.discord.User
import me.deprilula28.discordproxykt.entities.discord.channel.PartialPrivateChannel
import me.deprilula28.discordproxykt.entities.discord.guild.PartialGuild
import me.deprilula28.discordproxykt.rest.RestAction
import me.deprilula28.discordproxykt.rest.RestEndpoint

open class DiscordProxyKt internal constructor(
    val scope: CoroutineScope,
    val client: HttpClient,
    token: String?,
    val cache: DiscordRestCache,
    val defaultExceptionHandler: (Exception) -> Unit,
) {
    internal val authorization = "Bot $token"

    val selfUser: RestAction<User>
        get() = request(RestEndpoint.GET_CURRENT_USER.path(), { User(this as JsonObject, this@DiscordProxyKt) })
    
    fun fetchGuild(snowflake: Snowflake) = PartialGuild.new(snowflake, this)
    fun fetchUser(snowflake: Snowflake) = PartialUser.new(snowflake, this)
    fun fetchPrivateChannel(snowflake: Snowflake) = PartialPrivateChannel.new(snowflake, this)
    
    fun <T: Any> request(
        path: RestEndpoint.Path,
        constructor: JsonElement.(DiscordProxyKt) -> T,
        postData: ((HttpRequestBuilder) -> Any)? = null
    ) = RestAction(this, path, constructor, postData)
    
    suspend fun <T: Any> coroutineRequest(
        path: RestEndpoint.Path,
        constructor: JsonElement.(DiscordProxyKt) -> T,
        postData: ((HttpRequestBuilder) -> Any)? = null
    ) = RestAction(this, path, constructor, postData).await()
}
