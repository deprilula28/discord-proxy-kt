package me.deprilula28.discordproxykt.rest

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.PartialEntity
import me.deprilula28.discordproxykt.entities.Snowflake

open class PaginatedAction<T: PartialEntity>(
    override val bot: DiscordProxyKt,
    private val path: RestEndpoint,
    private vararg val pathParts: String,
    private val constructor: JsonElement.(DiscordProxyKt) -> T,
): IRestAction<List<T>> {
    private var after: Snowflake? = null
    var limit: Int? = null
    
    override suspend fun await(): List<T> {
        val map = mutableListOf<Pair<String, String>>()
        after?.apply { map.add("after" to id) }
        limit?.apply { map.add("limit" to toString()) }
        val list = bot.coroutineRequest(path.path(map, *pathParts), { (this as JsonArray).map { it.constructor(bot) } })
        after = list.last().snowflake
        return list
    }
    
    override suspend fun getIfAvailable(): List<T>? = null
// TODO Test pagination
}
