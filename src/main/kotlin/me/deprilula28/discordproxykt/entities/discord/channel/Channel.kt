package me.deprilula28.discordproxykt.entities.discord.channel

interface Channel {
    val type: ChannelType
}

enum class ChannelType {
    TEXT,
    PRIVATE,
    VOICE,
    GROUP,
    CATEGORY,
    STORE,
}