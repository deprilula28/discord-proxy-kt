package me.deprilula28.discordproxykt.builder

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import me.deprilula28.discordproxykt.entities.discord.guild.PartialRole
import me.deprilula28.discordproxykt.entities.discord.PartialUser
import me.deprilula28.discordproxykt.entities.discord.message.Everyone
import me.deprilula28.discordproxykt.entities.discord.message.Message
import me.deprilula28.discordproxykt.rest.InvalidRequestException
import me.deprilula28.discordproxykt.rest.RestEndpoint
import me.deprilula28.discordproxykt.rest.toBitSet
import java.util.*

// TODO File
class MessageBuilder: MessageConversion {
    internal val map = mutableMapOf<String, JsonElement>()
    
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
    
    override fun toMessage(): Pair<String, RestEndpoint.BodyType> {
        return Json.encodeToString(map) to RestEndpoint.BodyType.JSON
    }
}
