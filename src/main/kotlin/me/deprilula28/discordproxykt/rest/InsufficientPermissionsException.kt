package me.deprilula28.discordproxykt.rest

import me.deprilula28.discordproxykt.entities.discord.Permissions
import java.lang.Exception

class InsufficientPermissionsException(val permissions: List<Permissions>):
    Exception("Insufficient permissions! Lacking: ${permissions.joinToString(", ")}")
{}
