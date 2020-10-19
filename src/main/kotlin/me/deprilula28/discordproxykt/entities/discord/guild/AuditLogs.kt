package me.deprilula28.discordproxykt.entities.discord.guild

import kotlinx.serialization.json.JsonObject
import me.deprilula28.discordproxykt.DiscordProxyKt
import me.deprilula28.discordproxykt.entities.Entity

class AuditLogEntry(map: JsonObject, bot: DiscordProxyKt): Entity(map, bot) {
    // TODO This
    // https://discord.com/developers/docs/resources/audit-log#audit-log-object
}
