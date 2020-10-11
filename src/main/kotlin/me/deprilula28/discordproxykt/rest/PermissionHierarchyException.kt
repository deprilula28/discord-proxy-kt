package me.deprilula28.discordproxykt.rest

import me.deprilula28.discordproxykt.entities.discord.Role

class PermissionHierarchyException(val requiredLevel: Role, val currentLevel: Role?):
    Exception("Below the required permission hierarchy required! Expected: $requiredLevel, Actual: ${currentLevel?.toString() ?: "None"}")
{}
