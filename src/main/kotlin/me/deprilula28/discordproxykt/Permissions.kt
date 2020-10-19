package me.deprilula28.discordproxykt

import me.deprilula28.discordproxykt.entities.discord.*
import me.deprilula28.discordproxykt.entities.discord.channel.GuildChannel
import me.deprilula28.discordproxykt.rest.IRestAction
import me.deprilula28.discordproxykt.rest.InsufficientPermissionsException
import me.deprilula28.discordproxykt.rest.bitSetToEnumSet
import java.util.*
import java.util.concurrent.CompletableFuture

// Permission Utilities
suspend fun assertPermissions(guild: PartialGuild, vararg perm: Permissions) {
    val cachedPerms = (guild as? Guild)?.cachedUserPermissions
    if (cachedPerms == null) {
        val (_, roles) = useSelfMemberRoles(guild)
        var bitSet = 0L
        roles.forEach { el -> bitSet = bitSet or el.permissionsRaw }
        val enumSet = bitSet.bitSetToEnumSet(Permissions.values())
        checkPerms(perm, enumSet)
    } else checkPerms(perm, cachedPerms)
}

suspend fun assertPermissions(channel: GuildChannel, vararg perm: Permissions) {
    val (member, roles) = useSelfMemberRoles(channel.guild)
    val roleSnowflakes = roles.map { it.snowflake to it }.toMap()
    var bitSet = 0L
    roles.forEach { el -> bitSet = bitSet or el.permissionsRaw }
    // Fast path for admin
    val adminCode = 1L shl Permissions.ADMINISTRATOR.ordinal
    if ((bitSet and adminCode) == adminCode) return
    var rolesAllow = 0L
    var rolesDeny = 0L
    var everyoneAllow = 0L
    var everyoneDeny = 0L
    var memberAllow = 0L
    var memberDeny = 0L
    channel.permissions.forEach {
        if (it is RoleOverride) {
            if (!roleSnowflakes.containsKey(it.snowflake)) return@forEach
            rolesAllow = rolesAllow or it.allowRaw
            rolesDeny = rolesDeny or it.denyRaw
        } else if (it.snowflake == channel.guild.snowflake) {
            everyoneAllow = it.allowRaw
            everyoneDeny = it.denyRaw
        } else if (it is MemberOverride && it.snowflake == member.user.snowflake) {
            memberAllow = it.allowRaw
            memberDeny = it.denyRaw
        }
    }
    bitSet = bitSet and (Long.MAX_VALUE xor everyoneDeny)
    bitSet = bitSet or everyoneAllow
    bitSet = bitSet and (Long.MAX_VALUE xor rolesDeny)
    bitSet = bitSet or rolesAllow
    bitSet = bitSet and (Long.MAX_VALUE xor memberDeny)
    bitSet = bitSet or memberAllow
    
    checkPerms(perm, bitSet.bitSetToEnumSet(Permissions.values()))
}

fun checkPerms(expected: Array<out Permissions>, actual: EnumSet<Permissions>) {
    if (actual.contains(Permissions.ADMINISTRATOR)) return // Has full bypass
    val lackingPerms = expected.filter { !actual.contains(it) }
    if (lackingPerms.isNotEmpty()) throw InsufficientPermissionsException(lackingPerms)
}

suspend fun useSelfMemberRoles(guild: PartialGuild): Pair<Member, List<Role>> {
    val member = guild.fetchSelfMember.await()
    val roles = member.fetchRoles.await()
    return member to roles
}
