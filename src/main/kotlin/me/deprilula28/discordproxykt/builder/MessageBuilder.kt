package me.deprilula28.discordproxykt.builder

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.utils.io.streams.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.entities.discord.guild.PartialRole
import me.deprilula28.discordproxykt.entities.discord.PartialUser
import me.deprilula28.discordproxykt.entities.discord.message.Everyone
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.rest.InvalidRequestException
import me.deprilula28.discordproxykt.rest.RestEndpoint
import me.deprilula28.discordproxykt.rest.toBitSet
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URLEncoder
import java.util.*

class MessageBuilder: MessageConversion {
    internal val map = mutableMapOf<String, JsonElement>()
    var file: Pair<InputStream, String>? = null
    
    fun setContent(text: String): MessageBuilder {
        if (text.length > 2000) throw InvalidRequestException("Message content size of 2000 characters was surpassed.")
        map["content"] = JsonPrimitive(text)
        return this
    }
    
    fun setTts(tts: Boolean): MessageBuilder {
        map["tts"] = JsonPrimitive(tts)
        return this
    }
    
    fun setAllowedMentions(roles: Collection<PartialRole>, users: Collection<PartialUser>, parse: EnumSet<AllowedMentionType>): MessageBuilder {
        map["allowed_mentions"] = Json.encodeToJsonElement(mapOf(
            "users" to JsonArray(users.map { JsonPrimitive(it.snowflake.id) }),
            "roles" to JsonArray(roles.map { JsonPrimitive(it.snowflake.id) }),
            "parse" to JsonArray(parse.map { JsonPrimitive(it.text) }),
        ))
        return this
    }
    
    fun setAllowedMentions(vararg mentionables: Message.Mentionable): MessageBuilder {
        val roles = mentionables.filterIsInstance(PartialRole::class.java)
        val users = mentionables.filterIsInstance(PartialUser::class.java)
        val set = EnumSet.noneOf(AllowedMentionType::class.java)
        if (roles.isNotEmpty()) set.add(AllowedMentionType.ROLE_MENTIONS)
        if (users.isNotEmpty()) set.add(AllowedMentionType.USER_MENTIONS)
        if (mentionables.contains(Everyone)) set.add(AllowedMentionType.EVERYONE_MENTIONS)
        
        return setAllowedMentions(roles, users, set)
    }
    
    enum class AllowedMentionType(val text: String) {
        ROLE_MENTIONS("roles"),
        USER_MENTIONS("users"),
        EVERYONE_MENTIONS("everyone"),
    }
    
    fun setEmbed(embed: EmbedBuilder): MessageBuilder {
        map["embed"] = embed.element
        return this
    }
    
    fun setFlags(flags: EnumSet<Message.Flags>): MessageBuilder {
        map["flags"] = JsonPrimitive(flags.toBitSet())
        return this
    }
    
    fun setFile(fileName: String, stream: InputStream): MessageBuilder {
        file = stream to fileName
        return this
    }
    
    fun setFile(fileName: String, bytes: ByteArray): MessageBuilder = setFile(fileName, ByteArrayInputStream(bytes))
    fun setFile(fileName: String, str: String): MessageBuilder = setFile(fileName, str.toByteArray())
    
    override fun toMessage(request: HttpRequestBuilder): Any {
        if (file != null) {
            val (stream, fileName) = file!!
            request.contentType(ContentType.MultiPart.FormData)
            request.header("Content-Disposition", fileName)
            
            return formData {
                append("payload_json", Json.encodeToString(map))
                appendInput("file") { stream.asInput() }
            }
        }
        return Json.encodeToString(map)
    }
}
