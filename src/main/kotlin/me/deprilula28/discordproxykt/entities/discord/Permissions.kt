package me.deprilula28.discordproxykt.entities.discord

// https://discord.com/developers/docs/topics/permissions#permissions
enum class Permissions {
    /**
     * Allows creation of instant invites
     */
    CREATE_INSTANT_INVITE,
    /**
     * Allows kicking members
     */
    KICK_MEMBERS,
    /**
     * Allows banning members
     */
    BAN_MEMBERS,
    /**
     * Allows all permissions and bypasses channel permission overwrites
     */
    ADMINISTRATOR,
    /**
     * Allows management and editing of channels
     */
    MANAGE_CHANNELS,
    /**
     * Allows management and editing of the guild
     */
    MANAGE_GUILD,
    /**
     * Allows for the addition of reactions to messages
     */
    ADD_REACTIONS,
    /**
     * Allows for viewing of audit logs
     */
    VIEW_AUDIT_LOG,
    /**
     * Allows for using priority speaker in a voice channel
     */
    PRIORITY_SPEAKER,
    /**
     * Allows the user to go live
     */
    STREAM,
    /**
     * Allows guild members to view a channel, which includes reading messages in text channels
     */
    VIEW_CHANNEL,
    /**
     * Allows for sending messages in a channel
     */
    SEND_MESSAGES,
    /**
     * Allows for sending of /tts messages
     */
    SEND_TTS_MESSAGES,
    /**
     * Allows for deletion of other users messages
     */
    MANAGE_MESSAGES,
    /**
     * Links sent by users with this permission will be auto-embedded
     */
    EMBED_LINKS,
    /**
     * Allows for uploading images and files
     */
    ATTACH_FILES,
    /**
     * Allows for reading of message history
     */
    READ_MESSAGE_HISTORY,
    /**
     * Allows for using the @everyone tag to notify all users in a channel, and the @here tag to notify all online users in a channel
     */
    MENTION_EVERYONE,
    /**
     * Allows the usage of custom emojis from other servers
     */
    USE_EXTERNAL_EMOJIS,
    /**
     * Allows for viewing guild insights
     */
    VIEW_GUILD_INSIGHTS,
    /**
     * Allows for joining of a voice channel
     */
    CONNECT,
    /**
     * Allows for speaking in a voice channel
     */
    SPEAK,
    /**
     * Allows for muting members in a voice channel
     */
    MUTE_MEMBERS,
    /**
     * Allows for deafening of members in a voice channel
     */
    DEAFEN_MEMBERS,
    /**
     * Allows for moving of members between voice channels
     */
    MOVE_MEMBERS,
    /**
     * Allows for using voice-activity-detection in a voice channel
     */
    USE_VAD,
    /**
     * Allows for modification of own nickname
     */
    CHANGE_NICKNAME,
    /**
     * Allows for modification of other users nicknames
     */
    MANAGE_NICKNAMES,
    /**
     * Allows management and editing of roles
     */
    MANAGE_ROLES,
    /**
     * Allows management and editing of webhooks
     */
    MANAGE_WEBHOOKS,
    /**
     * Allows management and editing of emojis
     */
    MANAGE_EMOJIS
}
