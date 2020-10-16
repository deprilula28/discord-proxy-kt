package me.deprilula28.discordproxykt.entities.discord.channel

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.assertPermissions
import me.deprilula28.discordproxykt.entities.*
import me.deprilula28.discordproxykt.entities.discord.PartialGuild
import me.deprilula28.discordproxykt.entities.discord.PermissionOverwrite
import me.deprilula28.discordproxykt.entities.discord.Permissions
import me.deprilula28.discordproxykt.rest.*

/**
 * an organizational category that contains up to 50 channels
 * <br>
 * Channel documentation:
 * https://discord.com/developers/docs/resources/channel
 */
interface PartialCategory: PartialEntity, PartialGuildChannel {
    companion object {
        fun new(guild: PartialGuild, id: Snowflake): PartialCategory
                = object: PartialCategory {
            override val snowflake: Snowflake = id
            override val bot: DiscordProxyKt = guild.bot
            override fun upgrade(): IRestAction<Category>
                = bot.request(RestEndpoint.GET_CHANNEL.path(snowflake.id), { Category(this as JsonObject, bot) })
        }
    }
    
    override val type: ChannelType
        get() = ChannelType.CATEGORY
    
    override fun upgrade(): IRestAction<Category>
}


open class Category(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), GuildChannel, PartialCategory, EntityManager<Category> {
    override val guild: PartialGuild by parsing({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    override val position: Int by parsing(JsonElement::asInt)
    override val name: String by parsing(JsonElement::asString)
    override val category: PartialCategory?
        get() = this
    override fun upgrade(): IRestAction<Category> = IRestAction.ProvidedRestAction(bot, this)
    
    override val permissions: List<PermissionOverwrite> by parsing({
        (this as JsonArray).map {
            asPermissionOverwrite(this@Category,
                                  guild, bot)
        }
    }, "permission_overwrites")
    
    override val type: ChannelType
        get() = ChannelType.CATEGORY
    
    override val changes: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
    
    fun createCopy(guild: PartialGuild) = CategoryBuilder(guild, bot).apply {
        // TODO fill this with copying fields
    }
    fun createCopy() = createCopy(guild)
    
    /**
     * Requests that this guild gets edited based on the altered fields.
     */
    override fun edit(): IRestAction<Category> {
        if (changes.isEmpty()) throw InvalidRequestException("No changes have been made to this channel, yet `edit()` was called.")
        return IRestAction.coroutine(guild.bot) {
            assertPermissions(this, Permissions.MANAGE_CHANNELS)
            bot.coroutineRequest(RestEndpoint.MODIFY_CHANNEL.path(snowflake.id), { this@Category.apply { map = this@coroutineRequest as JsonObject } }) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
}

class CategoryBuilder(private val internalGuild: PartialGuild, bot: DiscordProxyKt):
    Category(JsonObject(MapNotReady()), bot), EntityBuilder<Category>
{
    /**
     * Creates a voice channel based on altered fields and returns it as a rest action.<br>
     * The values of the fields in the builder itself will be updated, making it usable as a Category.
     */
    override fun create(): IRestAction<Category> {
        if (!changes.containsKey("name")) throw InvalidRequestException("Channels require at least a name.")
        changes["type"] = JsonPrimitive(4)
        return IRestAction.coroutine(guild.bot) {
            assertPermissions(this, Permissions.MANAGE_GUILD)
            bot.coroutineRequest(
                RestEndpoint.CREATE_GUILD_CHANNEL.path(internalGuild.snowflake.id),
                { this@CategoryBuilder.apply { map = this@coroutineRequest as JsonObject } },
            ) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
}
