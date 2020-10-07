package events.message

import me.deprilula28.discordproxykt.JdaProxySpectacles
import me.deprilula28.discordproxykt.entities.Snowflake
import events.GenericEvent
import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.entities.asSnowflake

open class GenericMessageEvent(private val map: JsonObject, bot: JdaProxySpectacles): GenericEvent(bot) {
    val id: Snowflake by lazy { map["id"]!!.asSnowflake() }
    val channel: Snowflake by lazy { map["channel_id"]!!.asSnowflake() }
    val guild: Snowflake? by lazy { map["guild_id"]?.asSnowflake() }
}
