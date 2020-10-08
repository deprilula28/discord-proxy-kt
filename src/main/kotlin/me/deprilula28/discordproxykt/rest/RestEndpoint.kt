package me.deprilula28.discordproxykt.rest

enum class RestEndpoint(private val path: String, val method: String) {
    // Channel
    GET_CHANNEL("/channels/%s", "GET"),
    MODIFY_CHANNEL("/channels/%s", "PATCH"),
    DELETE_CHANNEL("/channels/%s", "DELETE"),
    GET_CHANNEL_MESSAGES("/channel/%s/messages", "GET"),
    GET_CHANNEL_MESSAGE("/channels/%s/messages/%s", "GET"),
    CREATE_MESSAGE("/channels/%s/messages", "POST"),
    CROSSPOST_MESSAGE("/channels/%s/messages/%s/crosspost", "POST"),
    CREATE_REACTION("/channels/%s/messages/%s/reactions/%s/@me", "PUT"),
    DELETE_OWN_REACTION("/channels/%s/messages/%s/reactions/%s/@me", "DELETE"),
    DELETE_USER_REACTION("/channels/%s/messages/%s/reactions/%s/%s", "DELETE"),
    GET_REACTIONS("/channels/%s/messages/%s/reactions/%s", "GET"),
    DELETE_ALL_REACTIONS("/channels/%s/messages/%s/reactions", "DELETE"),
    DELETE_ALL_REACTIONS_FOR_EMOJI("/channels/%s/messages/%s/reactions/%s", "DELETE"),
    EDIT_MESSAGE("/channels/%s/messages/%s", "PATCH"),
    DELETE_MESSAGE("/channels/%s/messages/%s", "DELETE"),
    BULK_DELETE_MESSAGES("/channels/%s/messages/bulk-delete", "POST"),
    EDIT_CHANNEL_PERMISSIONS("/channels/%s/permissions/%s", "PUT"),
    GET_CHANNEL_INVITES("/channels/%s/invites", "GET"),
    CREATE_CHANNEL_INVITE("/channels/%s/invites", "POST"),
    DELETE_CHANNEL_PERMISSION("/channels/%s/permissions/%s", "DELETE"),
    FOLLOW_NEWS_CHANNEL("/channels/%s/followers", "POST"),
    TRIGGER_TYPING_INDICATOR("/channels/%s/typing", "POST"),
    GET_PINNED_MESSAGES("/channels/%s/pins", "GET"),
    ADD_PINNED_CHANNEL_MESSAGE("/channels/%s/pins/%s", "PUT"),
    DELETE_PINNED_CHANNEL_MESSAGE("/channels/%s/pins/%s", "DELETE"),
    GROUP_DM_ADD_RECIPIENT("/channels/%s/recipients/%s", "PUT"),
    GROUP_DM_REMOVE_RECIPIENT("/channels/%s/recipients/%s", "DELETE"),
    
    // Emoji
    GET_GUILD_EMOJIS("/guilds/%s/emojis", "GET"),
    GET_GUILD_EMOJI("/guilds/%s/emojis/%s", "GET"),
    CREATE_GUILD_EMOJI("/guilds/%s/emojis", "POST"),
    MODIFY_GUILD_EMOJI("/guilds/%s/emojis/%s", "PATCH"),
    DELETE_GUILD_EMOJI("/guilds/%s/emojis/%s", "DELETE"),
    
    // Guild
    CREATE_GUILD("/guilds", "POST"),
    GET_GUILD("/guilds/%s", "GET"),
    GET_GUILD_PREVIEW("/guilds/%s/preview", "GET"),
    MODIFY_GUILD("/guilds/%s", "PATCH"),
    DELETE_GUILD("/guilds/%s", "DELETE"),
    GET_GUILD_CHANNELS("/guilds/%s/channels", "GET"),
    CREATE_GUILD_CHANNEL("/guilds/%s/channels", "POST"),
    MODIFY_GUILD_CHANNEL_POSITIONS("/guilds/%s/channels", "PATCH"),
    GET_GUILD_MEMBER("/guilds/%s/members/%s", "GET"),
    LIST_GUILD_MEMBERS("/guilds/%s/members", "GET"),
    ADD_GUILD_MEMBER("/guilds/%s/members/%s", "PUT"),
    REMOVE_GUILD_MEMBER("/guilds/%s/members/%s", "DELETE"),
    MODIFY_GUILD_MEMBER("/guilds/%s/members/%s", "PATCH"),
    MODIFY_CURRENT_USER_NICK("/guilds/%s/members/@me/nick", "PATCH"),
    GET_GUILDS_BANS("/guilds/%s/bans", "GET"),
    GET_GUILDS_BAN("/guilds/%s/bans/%s", "GET"),
    CREATE_GUILD_BAN("/guilds/%s/bans/%s", "PUT"),
    REMOVE_GUILD_BAN("/guilds/%s/bans/%s", "DELETE"),
    GET_GUILD_PRUNE_COUNT("/guilds/%s/prune", "GET"),
    BEGIN_GUILD_PRUNE_COUNT("/guilds/%s/prune", "POST"),
    GET_GUILD_VOICE_REGIONS("/guilds/%s/regions", "GET"),
    GET_GUILD_INVITES("/guilds/%s/invites", "GET"),
    GET_GUILD_INTEGRATIONS("/guilds/%s/integrations", "GET"),
    CREATE_GUILD_NITEGRATION("/guilds/%s/integrations", "POST"),
    MODIFY_GUILD_INTEGRATION("/guilds/%s/integrations/%s", "PATCH"),
    DELETE_GUILD_INTEGRATION("/guilds/%s/integrations/%s", "DELETE"),
    SYNC_GUILD_INTEGRATION("/guilds/%s/integrations/%s/sync", "POST"),
    GET_GUILD_AUDIT_LOGS("/guilds/%s/audit-logs", "GET"),
    
    // User
    GET_CURRENT_USER("/users/@me", "GET"),
    GET_USER("/users/%s", "GET"),
    MODIFY_CURRENT_USER("/users/@me", "PATCH"),
    GET_CURRENT_USER_GUILDS("/users/@me/guilds", "GET"),
    LEAVE_GUILD("/users/@me/guilds/%s", "DELETE"),
    GET_USER_DMS("/users/@me/channels", "GET"),
    CREATE_DM("/users/@me/channels", "POST"),
    GET_USER_CONNECTIONS("/users/@me/connections", "GET"),
    
    // Role
    ADD_GUILD_MEMBER_ROLE("/guilds/%s/members/%s/roles/%s", "PUT"),
    REMOVE_GUILD_MEMBER_ROLE("/guilds/%s/members/%s/roles/%s", "DELETE"),
    GET_GUILD_ROLES("/guilds/%s/roles", "GET"),
    CREATE_GUILD_ROLE("/guilds/%s/roles", "POST"),
    MODIFY_GUILD_ROLE_POSITIONS("/guilds/%s/roles", "PATCH"),
    MODIFY_GUILD_ROLE("/guilds/%s/roles/%s", "PATCH"),
    DELETE_GUILD_ROLE("/guilds/%s/roles/%s", "DELETE"),
    
    // Widget
    GET_GUILD_WIDGET_SETTINGS("/guilds/%s/widget", "GET"),
    MODIFY_GUILD_WIDGET("/guilds/%s/widget", "PATCH"),
    GET_GUILD_WIDGET("/guilds/%s/widget.json", "GET"),
    GET_GUILD_VANITY_URL("/guilds/%s/widget-url", "GET"),
    GET_GUILD_WIDGET_IMAGE("/guilds/%s/widget.png", "GET"),
    
    // Other
    GET_INVITE("/invites/%s", "GET"),
    DELETE_INVITE("/invites/%s", "DELETE"),
    GET_VOICE_REGIONS("/voice/regions", "GET");
    
    fun path(vararg parts: String) = String.format(path, *parts)
}
