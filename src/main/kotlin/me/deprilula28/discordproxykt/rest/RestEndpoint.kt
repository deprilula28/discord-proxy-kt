package me.deprilula28.discordproxykt.rest

import io.ktor.http.*

enum class RestEndpoint(private val path: String, val method: HttpMethod) {
    // Channel
    GET_CHANNEL("/channels/%s", HttpMethod.Get),
    MODIFY_CHANNEL("/channels/%s", HttpMethod.Patch),
    DELETE_CHANNEL("/channels/%s", HttpMethod.Delete),
    GET_CHANNEL_WEBHOOKS("/channels/%s/webhooks", HttpMethod.Get),
    GET_CHANNEL_MESSAGES("/channel/%s/messages", HttpMethod.Get),
    GET_CHANNEL_MESSAGE("/channels/%s/messages/%s", HttpMethod.Get),
    CREATE_MESSAGE("/channels/%s/messages", HttpMethod.Post),
    CROSSPOST_MESSAGE("/channels/%s/messages/%s/crosspost", HttpMethod.Post),
    CREATE_REACTION("/channels/%s/messages/%s/reactions/%s/@me", HttpMethod.Put),
    DELETE_OWN_REACTION("/channels/%s/messages/%s/reactions/%s/@me", HttpMethod.Delete),
    DELETE_USER_REACTION("/channels/%s/messages/%s/reactions/%s/%s", HttpMethod.Delete),
    GET_REACTIONS("/channels/%s/messages/%s/reactions/%s", HttpMethod.Get),
    DELETE_ALL_REACTIONS("/channels/%s/messages/%s/reactions", HttpMethod.Delete),
    DELETE_ALL_REACTIONS_FOR_EMOJI("/channels/%s/messages/%s/reactions/%s", HttpMethod.Delete),
    EDIT_MESSAGE("/channels/%s/messages/%s", HttpMethod.Patch),
    DELETE_MESSAGE("/channels/%s/messages/%s", HttpMethod.Delete),
    BULK_DELETE_MESSAGES("/channels/%s/messages/bulk-delete", HttpMethod.Post),
    EDIT_CHANNEL_PERMISSIONS("/channels/%s/permissions/%s", HttpMethod.Put),
    GET_CHANNEL_INVITES("/channels/%s/invites", HttpMethod.Get),
    CREATE_CHANNEL_INVITE("/channels/%s/invites", HttpMethod.Post),
    DELETE_CHANNEL_PERMISSION("/channels/%s/permissions/%s", HttpMethod.Delete),
    FOLLOW_NEWS_CHANNEL("/channels/%s/followers", HttpMethod.Post),
    TRIGGER_TYPING_INDICATOR("/channels/%s/typing", HttpMethod.Post),
    GET_PINNED_MESSAGES("/channels/%s/pins", HttpMethod.Get),
    ADD_PINNED_CHANNEL_MESSAGE("/channels/%s/pins/%s", HttpMethod.Put),
    DELETE_PINNED_CHANNEL_MESSAGE("/channels/%s/pins/%s", HttpMethod.Delete),
    GROUP_DM_ADD_RECIPIENT("/channels/%s/recipients/%s", HttpMethod.Put),
    GROUP_DM_REMOVE_RECIPIENT("/channels/%s/recipients/%s", HttpMethod.Delete),
    
    // Emoji
    GET_GUILD_EMOJIS("/guilds/%s/emojis", HttpMethod.Get),
    GET_GUILD_EMOJI("/guilds/%s/emojis/%s", HttpMethod.Get),
    CREATE_GUILD_EMOJI("/guilds/%s/emojis", HttpMethod.Post),
    MODIFY_GUILD_EMOJI("/guilds/%s/emojis/%s", HttpMethod.Patch),
    DELETE_GUILD_EMOJI("/guilds/%s/emojis/%s", HttpMethod.Delete),
    
    // Guild
    CREATE_GUILD("/guilds", HttpMethod.Post),
    GET_GUILD("/guilds/%s", HttpMethod.Get),
    GET_GUILD_PREVIEW("/guilds/%s/preview", HttpMethod.Get),
    MODIFY_GUILD("/guilds/%s", HttpMethod.Patch),
    DELETE_GUILD("/guilds/%s", HttpMethod.Delete),
    GET_GUILD_CHANNELS("/guilds/%s/channels", HttpMethod.Get),
    CREATE_GUILD_CHANNEL("/guilds/%s/channels", HttpMethod.Post),
    MODIFY_GUILD_CHANNEL_POSITIONS("/guilds/%s/channels", HttpMethod.Patch),
    GET_GUILD_WEBHOOKS("/guilds/%s/webhooks", HttpMethod.Get),
    GET_GUILD_MEMBER("/guilds/%s/members/%s", HttpMethod.Get),
    LIST_GUILD_MEMBERS("/guilds/%s/members", HttpMethod.Get),
    ADD_GUILD_MEMBER("/guilds/%s/members/%s", HttpMethod.Put),
    REMOVE_GUILD_MEMBER("/guilds/%s/members/%s", HttpMethod.Delete),
    MODIFY_GUILD_MEMBER("/guilds/%s/members/%s", HttpMethod.Patch),
    MODIFY_CURRENT_USER_NICK("/guilds/%s/members/@me/nick", HttpMethod.Patch),
    GET_GUILDS_BANS("/guilds/%s/bans", HttpMethod.Get),
    GET_GUILDS_BAN("/guilds/%s/bans/%s", HttpMethod.Get),
    CREATE_GUILD_BAN("/guilds/%s/bans/%s", HttpMethod.Put),
    REMOVE_GUILD_BAN("/guilds/%s/bans/%s", HttpMethod.Delete),
    GET_GUILD_PRUNE_COUNT("/guilds/%s/prune", HttpMethod.Get),
    BEGIN_GUILD_PRUNE_COUNT("/guilds/%s/prune", HttpMethod.Post),
    GET_GUILD_VOICE_REGIONS("/guilds/%s/regions", HttpMethod.Get),
    GET_GUILD_INVITES("/guilds/%s/invites", HttpMethod.Get),
    GET_GUILD_INTEGRATIONS("/guilds/%s/integrations", HttpMethod.Get),
    CREATE_GUILD_NITEGRATION("/guilds/%s/integrations", HttpMethod.Post),
    MODIFY_GUILD_INTEGRATION("/guilds/%s/integrations/%s", HttpMethod.Patch),
    DELETE_GUILD_INTEGRATION("/guilds/%s/integrations/%s", HttpMethod.Delete),
    SYNC_GUILD_INTEGRATION("/guilds/%s/integrations/%s/sync", HttpMethod.Post),
    GET_GUILD_AUDIT_LOGS("/guilds/%s/audit-logs", HttpMethod.Get),
    
    // User
    GET_CURRENT_USER("/users/@me", HttpMethod.Get),
    GET_USER("/users/%s", HttpMethod.Get),
    MODIFY_CURRENT_USER("/users/@me", HttpMethod.Patch),
    GET_CURRENT_USER_GUILDS("/users/@me/guilds", HttpMethod.Get),
    LEAVE_GUILD("/users/@me/guilds/%s", HttpMethod.Delete),
    GET_USER_DMS("/users/@me/channels", HttpMethod.Get),
    CREATE_DM("/users/@me/channels", HttpMethod.Post),
    GET_USER_CONNECTIONS("/users/@me/connections", HttpMethod.Get),
    
    // Webhook
    CREATE_WEBHOOK("/channels/%s/webhooks", HttpMethod.Post),
    GET_WEBHOOK("/webhooks/%s", HttpMethod.Get),
    GET_WEBHOOK_TOKEN("/webhooks/%s/%s", HttpMethod.Get),
    DELETE_WEBHOOK("/webhooks/%s", HttpMethod.Delete),
    DELETE_WEBHOOK_TOKEN("/webhooks/%s/%s", HttpMethod.Delete),
    MODIFY_WEBHOOK("/webhooks/%s", HttpMethod.Patch),
    MODIFY_WEBHOOK_TOKEN("/webhooks/%s/%s", HttpMethod.Patch),
    EXECUTE_WEBHOOK("/webhooks/%s/%s", HttpMethod.Post),
    
    // Role
    ADD_GUILD_MEMBER_ROLE("/guilds/%s/members/%s/roles/%s", HttpMethod.Put),
    REMOVE_GUILD_MEMBER_ROLE("/guilds/%s/members/%s/roles/%s", HttpMethod.Delete),
    GET_GUILD_ROLES("/guilds/%s/roles", HttpMethod.Get),
    CREATE_GUILD_ROLE("/guilds/%s/roles", HttpMethod.Post),
    MODIFY_GUILD_ROLE_POSITIONS("/guilds/%s/roles", HttpMethod.Patch),
    MODIFY_GUILD_ROLE("/guilds/%s/roles/%s", HttpMethod.Patch),
    DELETE_GUILD_ROLE("/guilds/%s/roles/%s", HttpMethod.Delete),
    
    // Widget
    GET_GUILD_WIDGET_SETTINGS("/guilds/%s/widget", HttpMethod.Get),
    MODIFY_GUILD_WIDGET("/guilds/%s/widget", HttpMethod.Patch),
    GET_GUILD_WIDGET("/guilds/%s/widget.json", HttpMethod.Get),
    GET_GUILD_VANITY_URL("/guilds/%s/widget-url", HttpMethod.Get),
    GET_GUILD_WIDGET_IMAGE("/guilds/%s/widget.png", HttpMethod.Get),
    
    // Other
    GET_INVITE("/invites/%s", HttpMethod.Get),
    DELETE_INVITE("/invites/%s", HttpMethod.Delete),
    GET_VOICE_REGIONS("/voice/regions", HttpMethod.Get);
    
    data class Path(val url: String, val endpoint: RestEndpoint)
    
    enum class BodyType(val typeStr: String) {
        JSON("application/json"),
        FORM("multipart/form-data"),
    }
    
    fun path(vararg parts: String) = Path(String.format(path, *parts), this)
    fun path(getParameters: List<Pair<String?, String>>, vararg parts: String)
        = Path(String.format(path, *parts) + "?" + getParameters.joinToString("&") {
                (key, value) -> if (key == null) value else "$key=$value"
        }, this)
}
