package me.deprilula28.discordproxykt.builder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import me.deprilula28.discordproxykt.entities.Timestamp
import me.deprilula28.discordproxykt.rest.InvalidRequestException
import me.deprilula28.discordproxykt.rest.RestEndpoint
import java.awt.Color
import java.net.URL

class EmbedBuilder: MessageConversion {
    private val map = mutableMapOf<String, JsonElement>()
    
    fun setTitle(text: String): EmbedBuilder {
        if (text.length > 256) throw InvalidRequestException("Embed title size limit of 256 characters was surpassed.")
        map["title"] = JsonPrimitive(text)
        return this
    }
    
    fun setDescription(text: String): EmbedBuilder {
        if (text.length > 2048) throw InvalidRequestException("Embed description size limit of 2048 characters was surpassed.")
        map["description"] = JsonPrimitive(text)
        return this
    }
    
    fun setUrl(text: String): EmbedBuilder {
        map["url"] = JsonPrimitive(text)
        return this
    }
    
    fun setTimestamp(stamp: Timestamp): EmbedBuilder {
        map["timestamp"] = JsonPrimitive(stamp.toString())
        return this
    }
    
    fun setColor(color: Color): EmbedBuilder {
        map["color"] = JsonPrimitive(color.rgb)
        return this
    }
    
    fun setFooter(footer: Footer): EmbedBuilder {
        if (footer.text.length > 2048) throw InvalidRequestException("Embed footer text size limit of 2048 characters was surpassed.")
        map["footer"] = Json.encodeToJsonElement(footer)
        return this
    }
    
    @Serializable
    data class Footer(
        val text: String,
        @SerialName("icon_url") val iconUrl: String? = null,
    )
    
    fun setImage(image: URL): EmbedBuilder {
        map["image"] = Json.encodeToJsonElement(mapOf("url" to JsonPrimitive(image.toString())))
        return this
    }
    
    fun setImage(image: String) = setImage(URL(image))// Does URI checking
    
    fun setThumbnail(thumbnail: URL): EmbedBuilder {
        map["thumbnail"] = Json.encodeToJsonElement(mapOf("url" to JsonPrimitive(thumbnail.toString())))
        return this
    }
    
    fun setThumbnail(thumbnail: String) = setImage(URL(thumbnail))// Does URI checking
    
    fun setProvider(provider: Provider): EmbedBuilder {
        map["provider"] = Json.encodeToJsonElement(provider)
        return this
    }
    
    @Serializable
    data class Provider(
        @SerialName("name") val name: String? = null,
        @SerialName("url") val url: String? = null,
    )
    
    fun setAuthor(author: Author): EmbedBuilder {
        if (author.name?.run { length > 256 } == true) throw InvalidRequestException("Embed author name of 256 characters was surpassed.")
        map["author"] = Json.encodeToJsonElement(author)
        return this
    }
    
    @Serializable
    data class Author(
        @SerialName("name") val name: String? = null,
        @SerialName("url") val url: String? = null,
    )
    
    val fields = mutableListOf<JsonElement>()
    fun addField(field: Field): EmbedBuilder {
        if (field.name.length > 256) throw InvalidRequestException("Embed field name limit of 256 characters was surpassed.")
        if (field.value.length > 1024) throw InvalidRequestException("Embed field description limit of 1024 characters was surpassed.")
        if (fields.size == 25) throw InvalidRequestException("Field amount is limited to 25.")
        fields.add(Json.encodeToJsonElement(field))
        return this
    }
    
    @Serializable
    data class Field(
        val name: String,
        val value: String,
        val inline: Boolean = false,
    )
    
    override fun toMessage(): Pair<String, RestEndpoint.BodyType> {
        if (fields.isNotEmpty()) map["fields"] = Json.encodeToJsonElement(fields)
        return Json.encodeToString(mapOf(
            "embed" to map
        )) to RestEndpoint.BodyType.JSON
    }
    
    val element: JsonElement
        get() {
            if (fields.isNotEmpty()) map["fields"] = Json.encodeToJsonElement(fields)
            return Json.encodeToJsonElement(map)
        }
    
    @Deprecated("JDA Compatibility Function", ReplaceWith(""))
    fun build() = this
}
