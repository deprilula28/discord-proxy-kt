package me.deprilula28.discordproxykt.entities.discord

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Entity
import me.deprilula28.discordproxykt.entities.UnavailableField
import me.deprilula28.discordproxykt.entities.discord.channel.PartialTextChannel
import me.deprilula28.discordproxykt.rest.*
import java.awt.image.RenderedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

class Webhook(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot), EntityManager<Webhook> {
    /**
     * @return false (Incoming Webhook) Incoming Webhooks can post messages to channels with a generated token
     * @return true (Channel Follower Webhook) Channel Follower Webhooks are internal webhooks used with
     * Channel Following to post new messages into channels
     */
    val following: Boolean by map.delegateJson({ asInt() == 2 }, "type")
    /**
     * the guild id this webhook is for
     */
    val guild: PartialGuild.Upgradeable? by map.delegateJsonNullable({ bot.fetchGuild(asSnowflake()) }, "guild_id")
    /**
     * the channel id this webhook is for
     */
    var channel: PartialTextChannel.Upgradeable? by map.delegateJsonMutable(
        { PartialTextChannel.new(guild ?: throw UnavailableField(), asSnowflake()) },
        { Json.encodeToJsonElement((it ?: throw InvalidRequestException("Cannot set channel to null")).snowflake.id) },
        "channel_id",
    )
    /**
     * 	the user this webhook was created by (not returned when getting a webhook with its token)
     */
    val user: User? by map.delegateJsonNullable({ User(this as JsonObject, bot) })
    
    var name: String by map.delegateJsonMutable(
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
     * Requests that this guild gets edited based on the altered fields.<br>
     * This object will not be updated to reflect the changes, rather a new Webhook object is returned from the RestAction.
     */
    override fun edit(): IRestAction<Webhook> {
        if (changes.isEmpty()) throw InvalidRequestException("No changes have been made to this webhook, yet `edit()` was called.")
        return bot.request(RestEndpoint.MODIFY_WEBHOOK.path(snowflake.id), { Webhook(this as JsonObject, bot) }) {
            val res = Json.encodeToString(changes)
            changes.clear()
            res
        }
    }
    
    @Deprecated("JDA Compatibility Field", ReplaceWith(""))
    val manager: Webhook?
        get() = this
}
