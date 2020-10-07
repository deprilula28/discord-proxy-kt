package me.deprilula28.discordproxykt.rest

enum class RestEndpoint(private val path: String, val method: String) {
    // Audit Log
    GET_GUILD_AUDIT_LOGS("/guilds/%s/audit-logs", "GET"),
    
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
    GROUP_DM_REMOVE_RECIPIENT("/channels/%s/recipients/%s", "DELETE");
    
    fun path(vararg parts: String) = String.format(path, *parts)
}
