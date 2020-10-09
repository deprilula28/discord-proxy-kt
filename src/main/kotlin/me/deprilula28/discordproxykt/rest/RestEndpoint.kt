package me.deprilula28.discordproxykt.rest

enum class RestEndpoint(private val path: String, val method: Method) {
    // Channel
    GET_CHANNEL("/channels/%s", Method.GET),
    MODIFY_CHANNEL("/channels/%s", Method.PATCH),
    DELETE_CHANNEL("/channels/%s", Method.DELETE),
    GET_CHANNEL_WEBHOOKS("/channels/%s/webhooks", Method.GET),
    GET_CHANNEL_MESSAGES("/channel/%s/messages", Method.GET),
    GET_CHANNEL_MESSAGE("/channels/%s/messages/%s", Method.GET),
    CREATE_MESSAGE("/channels/%s/messages", Method.POST),
    CROSSPOST_MESSAGE("/channels/%s/messages/%s/crosspost", Method.POST),
    CREATE_REACTION("/channels/%s/messages/%s/reactions/%s/@me", Method.PUT),
    DELETE_OWN_REACTION("/channels/%s/messages/%s/reactions/%s/@me", Method.DELETE),
    DELETE_USER_REACTION("/channels/%s/messages/%s/reactions/%s/%s", Method.DELETE),
    GET_REACTIONS("/channels/%s/messages/%s/reactions/%s", Method.GET),
    DELETE_ALL_REACTIONS("/channels/%s/messages/%s/reactions", Method.DELETE),
    DELETE_ALL_REACTIONS_FOR_EMOJI("/channels/%s/messages/%s/reactions/%s", Method.DELETE),
    EDIT_MESSAGE("/channels/%s/messages/%s", Method.PATCH),
    DELETE_MESSAGE("/channels/%s/messages/%s", Method.DELETE),
    BULK_DELETE_MESSAGES("/channels/%s/messages/bulk-delete", Method.POST),
    EDIT_CHANNEL_PERMISSIONS("/channels/%s/permissions/%s", Method.PUT),
    GET_CHANNEL_INVITES("/channels/%s/invites", Method.GET),
    CREATE_CHANNEL_INVITE("/channels/%s/invites", Method.POST),
    DELETE_CHANNEL_PERMISSION("/channels/%s/permissions/%s", Method.DELETE),
    FOLLOW_NEWS_CHANNEL("/channels/%s/followers", Method.POST),
    TRIGGER_TYPING_INDICATOR("/channels/%s/typing", Method.POST),
    GET_PINNED_MESSAGES("/channels/%s/pins", Method.GET),
    ADD_PINNED_CHANNEL_MESSAGE("/channels/%s/pins/%s", Method.PUT),
    DELETE_PINNED_CHANNEL_MESSAGE("/channels/%s/pins/%s", Method.DELETE),
    GROUP_DM_ADD_RECIPIENT("/channels/%s/recipients/%s", Method.PUT),
    GROUP_DM_REMOVE_RECIPIENT("/channels/%s/recipients/%s", Method.DELETE),
    
    // Emoji
    GET_GUILD_EMOJIS("/guilds/%s/emojis", Method.GET),
    GET_GUILD_EMOJI("/guilds/%s/emojis/%s", Method.GET),
    CREATE_GUILD_EMOJI("/guilds/%s/emojis", Method.POST),
    MODIFY_GUILD_EMOJI("/guilds/%s/emojis/%s", Method.PATCH),
    DELETE_GUILD_EMOJI("/guilds/%s/emojis/%s", Method.DELETE),
    
    // Guild
    CREATE_GUILD("/guilds", Method.POST),
    GET_GUILD("/guilds/%s?with_counts=true", Method.GET),
    GET_GUILD_PREVIEW("/guilds/%s/preview", Method.GET),
    MODIFY_GUILD("/guilds/%s", Method.PATCH),
    DELETE_GUILD("/guilds/%s", Method.DELETE),
    GET_GUILD_CHANNELS("/guilds/%s/channels", Method.GET),
    CREATE_GUILD_CHANNEL("/guilds/%s/channels", Method.POST),
    MODIFY_GUILD_CHANNEL_POSITIONS("/guilds/%s/channels", Method.PATCH),
    GET_GUILD_WEBHOOKS("/guilds/%s/webhooks", Method.GET),
    GET_GUILD_MEMBER("/guilds/%s/members/%s", Method.GET),
    LIST_GUILD_MEMBERS("/guilds/%s/members", Method.GET),
    ADD_GUILD_MEMBER("/guilds/%s/members/%s", Method.PUT),
    REMOVE_GUILD_MEMBER("/guilds/%s/members/%s", Method.DELETE),
    MODIFY_GUILD_MEMBER("/guilds/%s/members/%s", Method.PATCH),
    MODIFY_CURRENT_USER_NICK("/guilds/%s/members/@me/nick", Method.PATCH),
    GET_GUILDS_BANS("/guilds/%s/bans", Method.GET),
    GET_GUILDS_BAN("/guilds/%s/bans/%s", Method.GET),
    CREATE_GUILD_BAN("/guilds/%s/bans/%s", Method.PUT),
    REMOVE_GUILD_BAN("/guilds/%s/bans/%s", Method.DELETE),
    GET_GUILD_PRUNE_COUNT("/guilds/%s/prune?days=%s", Method.GET),
    BEGIN_GUILD_PRUNE_COUNT("/guilds/%s/prune", Method.POST),
    GET_GUILD_VOICE_REGIONS("/guilds/%s/regions", Method.GET),
    GET_GUILD_INVITES("/guilds/%s/invites", Method.GET),
    GET_GUILD_INTEGRATIONS("/guilds/%s/integrations", Method.GET),
    CREATE_GUILD_NITEGRATION("/guilds/%s/integrations", Method.POST),
    MODIFY_GUILD_INTEGRATION("/guilds/%s/integrations/%s", Method.PATCH),
    DELETE_GUILD_INTEGRATION("/guilds/%s/integrations/%s", Method.DELETE),
    SYNC_GUILD_INTEGRATION("/guilds/%s/integrations/%s/sync", Method.POST),
    GET_GUILD_AUDIT_LOGS("/guilds/%s/audit-logs", Method.GET),
    
    // User
    GET_CURRENT_USER("/users/@me", Method.GET),
    GET_USER("/users/%s", Method.GET),
    MODIFY_CURRENT_USER("/users/@me", Method.PATCH),
    GET_CURRENT_USER_GUILDS("/users/@me/guilds", Method.GET),
    LEAVE_GUILD("/users/@me/guilds/%s", Method.DELETE),
    GET_USER_DMS("/users/@me/channels", Method.GET),
    CREATE_DM("/users/@me/channels", Method.POST),
    GET_USER_CONNECTIONS("/users/@me/connections", Method.GET),
    
    // Webhook
    CREATE_WEBHOOK("/channels/%s/webhooks", Method.POST),
    GET_WEBHOOK("/webhooks/%s", Method.GET),
    GET_WEBHOOK_TOKEN("/webhooks/%s/%s", Method.GET),
    DELETE_WEBHOOK("/webhooks/%s", Method.DELETE),
    DELETE_WEBHOOK_TOKEN("/webhooks/%s/%s", Method.DELETE),
    MODIFY_WEBHOOK("/webhooks/%s", Method.PATCH),
    MODIFY_WEBHOOK_TOKEN("/webhooks/%s/%s", Method.PATCH),
    EXECUTE_WEBHOOK("/webhooks/%s/%s", Method.POST),
    
    // Role
    ADD_GUILD_MEMBER_ROLE("/guilds/%s/members/%s/roles/%s", Method.PUT),
    REMOVE_GUILD_MEMBER_ROLE("/guilds/%s/members/%s/roles/%s", Method.DELETE),
    GET_GUILD_ROLES("/guilds/%s/roles", Method.GET),
    CREATE_GUILD_ROLE("/guilds/%s/roles", Method.POST),
    MODIFY_GUILD_ROLE_POSITIONS("/guilds/%s/roles", Method.PATCH),
    MODIFY_GUILD_ROLE("/guilds/%s/roles/%s", Method.PATCH),
    DELETE_GUILD_ROLE("/guilds/%s/roles/%s", Method.DELETE),
    
    // Widget
    GET_GUILD_WIDGET_SETTINGS("/guilds/%s/widget", Method.GET),
    MODIFY_GUILD_WIDGET("/guilds/%s/widget", Method.PATCH),
    GET_GUILD_WIDGET("/guilds/%s/widget.json", Method.GET),
    GET_GUILD_VANITY_URL("/guilds/%s/widget-url", Method.GET),
    GET_GUILD_WIDGET_IMAGE("/guilds/%s/widget.png", Method.GET),
    
    // Other
    GET_INVITE("/invites/%s", Method.GET),
    DELETE_INVITE("/invites/%s", Method.DELETE),
    GET_VOICE_REGIONS("/voice/regions", Method.GET);
    
    enum class Method {
        GET,
        POST,
        PATCH,
        PUT,
        DELETE
    }
    
    fun path(vararg parts: String) = String.format(path, *parts)
}
