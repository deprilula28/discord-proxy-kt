package me.deprilula28.discordproxykt.builder

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import me.deprilula28.discordproxykt.entities.Timestamp
import me.deprilula28.discordproxykt.rest.RestEndpoint
import java.awt.Color
import java.net.URL

class EmbedBuilder: MessageConversion {
    private val map = mutableMapOf<String, JsonElement>()
    
    fun setTitle(text: String): EmbedBuilder {
        map["title"] = JsonPrimitive(text)
        return this
    }
    
    fun setDescription(text: String): EmbedBuilder {
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
