package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.assertPermissions
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.entities.UnavailableField
import me.deprilula28.discordproxykt.entities.discord.channel.PartialTextChannel
import me.deprilula28.discordproxykt.entities.discord.channel.TextChannel
import me.deprilula28.discordproxykt.entities.discord.guild.PartialGuild
import me.deprilula28.discordproxykt.rest.*
import java.awt.image.RenderedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

open class Webhook(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), EntityManager<Webhook> {
    /**
     * @return false (Incoming Webhook) Incoming Webhooks can post messages to channels with a generated token
     * @return true (Channel Follower Webhook) Channel Follower Webhooks are internal webhooks used with
     * Channel Following to post new messages into channels
     */
    val following: Boolean by parsing({ asInt() == 2 }, "type")
    /**
     * the guild id this webhook is for
     */
    val guild: PartialGuild? by parsingOpt({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    /**
     * the channel id this webhook is for
     */
    var channel: PartialTextChannel? by parsing(
        { PartialTextChannel.new(guild ?: throw UnavailableField(), asSnowflake()) },
        { Json.encodeToJsonElement((it ?: throw InvalidRequestException("Cannot set channel to null")).snowflake.id) },
        "channel_id",
    )
    /**
     * 	the user this webhook was created by (not returned when getting a webhook with its token)
     */
    val user: User? by parsingOpt({ User(this as JsonObject, bot) })
    
    var name: String by parsing(
        { throw InvalidRequestException("Write-only field") },
        Json::encodeToJsonElement,
    )
    
    fun delete() = bot.request(RestEndpoint.DELETE_WEBHOOK.path(snowflake.id), { Unit })
    
    override val changes: MutableMap<String, JsonElement> by lazy(::mutableMapOf)
    
    /**
     * To finish the procedure, the rest action needs to be called.
     */
    fun setAvatar(image: RenderedImage, format: String): Webhook {
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, format, baos)
        changes["avatar"] = JsonPrimitive("data:image/jpeg;base64,${Base64.getEncoder().encodeToString(baos.toByteArray())}")
        return this
    }
    
    /**
     * Requests that this guild gets edited based on the altered fields.
     */
    override fun edit(): IRestAction<Webhook> {
        if (changes.isEmpty()) throw InvalidRequestException("No changes have been made to this webhook, yet `edit()` was called.")
        return bot.request(RestEndpoint.MODIFY_WEBHOOK.path(snowflake.id), { this@Webhook.apply { map = this@request as JsonObject } }) {
            val res = Json.encodeToString(changes)
            changes.clear()
            res
        }
    }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith(""))
    val manager: Webhook?
        get() = this
}

class WebhookBuilder(private val internalChannel: TextChannel, bot: DiscordProxyKt):
    Webhook(JsonObject(MapNotReady()), bot), EntityBuilder<Webhook>
{
    /**
     * Creates a webhook based on altered fields and returns it as a rest action.<br>
     * The values of the fields in the builder itself will be updated, making it usable as a Webhook.
     */
    override fun create(): IRestAction<Webhook> {
        if (!changes.containsKey("name")) throw InvalidRequestException("Webhooks require a name.")
        if (changes["name"]!!.asString() == "clyde") throw InvalidRequestException("Webhooks cannot be named Clyde.")
        return IRestAction.coroutine(bot) {
            assertPermissions(internalChannel, Permissions.MANAGE_CHANNELS)
            bot.coroutineRequest(
                RestEndpoint.CREATE_WEBHOOK.path(internalChannel.snowflake.id),
                { this@WebhookBuilder.apply { map = this@coroutineRequest as JsonObject } },
            ) {
                val res = Json.encodeToString(changes)
                changes.clear()
                res
            }
        }
    }
}
