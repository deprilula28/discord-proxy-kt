package entities.discord

import entities.Snowflake
import java.util.*

class PermissionOverwrite(
    val id: Snowflake,
    val user: Boolean,
    val allow: EnumSet<Permissions>,
    val deny: EnumSet<Permissions>,
)