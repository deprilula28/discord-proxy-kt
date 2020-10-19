package me.deprilula28.discordproxykt.entities.discord.channel

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.assertPermissions
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.guild.Features
import me.deprilula28.discordproxykt.entities.discord.guild.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.PermissionOverwrite
import me.deprilula28.discordproxykt.entities.discord.Permissions
import me.deprilula28.discordproxykt.rest.*

/**
 * This type is used for operations when an ID of a [StoreChannel] is known.<br>
 * If the data is also known it will implement [StoreChannel], and [upgrade] is a no-op.<br>
 * If it isn't known, [upgrade] will be a request to get the data from Discord.
 */
interface PartialStoreChannel: PartialEntity, PartialGuildChannel {
    companion object {
        fun new(guild: PartialGuild, id: Snowflake): PartialStoreChannel
            = object: PartialStoreChannel {
                override val snowflake: Snowflake = id
                override val bot: DiscordProxyKt = guild.bot
                
                override fun upgrade(): IRestAction<StoreChannel>
                    = bot.request(RestEndpoint.GET_CHANNEL.path(snowflake.id), { StoreChannel(this as JsonObject, bot) })
                override fun toString(): String = "Channel(partial, $type, $guild, $snowflake.id)"
            }
    }
    
    override val type: ChannelType
        get() = ChannelType.STORE
    
    override fun upgrade(): IRestAction<StoreChannel>
}


open class StoreChannel(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), GuildChannel, PartialStoreChannel, EntityManager<StoreChannel> {
    override val guild: PartialGuild by parsing({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    override fun upgrade(): IRestAction<StoreChannel> = IRestAction.ProvidedRestAction(bot, this)

    override var name: String by parsing(JsonElement::asString, {
        if (it.length !in 2 .. 100) throw InvalidRequestException("Channel name must be between 2 and 100 characters in length")
        Json.encodeToJsonElement(it)
    })
    override var position: Int by parsing(JsonElement::asInt, Json::encodeToJsonElement)
    override var permissions: List<PermissionOverwrite> by parsing({
        (this as JsonArray).map { it.asPermissionOverwrite(this@StoreChannel, guild, bot) }
    }, { Json.encodeToJsonElement(it.map(PermissionOverwrite::toObject)) }, "permission_overwrites")
    override var category: PartialCategory? by parsingOpt(
        { PartialCategory.new(guild, asSnowflake()) },
        { it?.run { JsonPrimitive(snowflake.id) } ?: JsonNull },
        "parent_id",
    )
    
    override val type: ChannelType
        get() = ChannelType.STORE
    
    override val changes: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
    
    fun createCopy(guild: PartialGuild) = CategoryBuilder(guild, bot).apply {
        name = this@StoreChannel.name
        position = this@StoreChannel.position
        permissions = this@StoreChannel.permissions
        category = this@StoreChannel.category
    }
    fun createCopy() = createCopy(guild)
    
    override fun toString(): String = "Channel($type, $guild, $name, ${snowflake.id})"
    
    /**
     * Requests that this guild gets edited based on the altered fields.
     */
    override fun edit(): IRestAction<StoreChannel> {
        if (changes.isEmpty()) throw InvalidRequestException("No changes have been made to this channel, yet `edit()` was called.")
        return IRestAction.coroutine(guild.bot) {
            assertPermissions(this, Permissions.MANAGE_CHANNELS)
            bot.coroutineRequest(RestEndpoint.MODIFY_CHANNEL.path(snowflake.id), { this@StoreChannel.apply { map = this@coroutineRequest as JsonObject } }) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
}

class StoreChannelBuilder(private val internalGuild: PartialGuild, bot: DiscordProxyKt):
    Category(JsonObject(MapNotReady()), bot), EntityBuilder<Category>
{
    /**
     * Creates a voice channel based on altered fields and returns it as a rest action.<br>
     * The values of the fields in the builder itself will be updated, making it usable as a [StoreChannel].
     */
    override fun create(): IRestAction<Category> {
        if (!changes.containsKey("name")) throw InvalidRequestException("Channels require at least a name.")
        changes["type"] = JsonPrimitive(6)
        return IRestAction.coroutine(internalGuild.bot) {
            assertPermissions(this, Permissions.MANAGE_GUILD)
            if (!internalGuild.upgrade().await().features.contains(Features.COMMERCE))
                throw InvalidRequestException("Cannot create store channel in a guild that doesn't have commerce")
            bot.coroutineRequest(
                RestEndpoint.CREATE_GUILD_CHANNEL.path(internalGuild.snowflake.id),
                { this@StoreChannelBuilder.apply { map = this@coroutineRequest as JsonObject } },
            ) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
    
    override fun toString(): String = "Channel(builder)"
}
