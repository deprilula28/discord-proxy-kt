package me.deprilula28.discordproxykt

import me.deprilula28.discordproxykt.entities.discord.*
import me.deprilula28.discordproxykt.entities.discord.channel.GuildChannel
import me.deprilula28.discordproxykt.rest.IRestAction
import me.deprilula28.discordproxykt.rest.InsufficientPermissionsException
import me.deprilula28.discordproxykt.rest.bitSetToEnumSet
import java.util.*
import java.util.concurrent.CompletableFuture

// Permission Utilities
inline fun <reified T: Any> assertPermissions(guild: PartialGuild, vararg perm: Permissions, crossinline then: () -> IRestAction<T>): IRestAction<T> {
    val cachedPerms = (guild as? Guild)?.cachedUserPermissions
    return cachedPerms?.run {
        checkPerms(perm, this)
        then()
    } ?: useSelfMemberRoles(guild) { _, roles ->
        var bitSet = 0L
        roles.forEach { el -> bitSet = bitSet and el.permissionsRaw }
        val enumSet = bitSet.bitSetToEnumSet(Permissions.values())
        checkPerms(perm, enumSet)
        then().request()
    }
}

inline fun <reified T: Any> assertPermissions(channel: GuildChannel, vararg perm: Permissions, crossinline then: () -> IRestAction<T>): IRestAction<T> {
    return useSelfMemberRoles(channel.guild) { member, roles ->
        val roleSnowflakes = roles.map { it.snowflake to it }.toMap()
        var bitSet = 0L
        roles.forEach { el -> bitSet = bitSet and el.permissionsRaw }
        // Fast path for admin
        val adminCode = 1L shl Permissions.ADMINISTRATOR.ordinal
        if ((bitSet and adminCode) == adminCode) then().request()
        else {
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
            then().request()
        }
    }
}

fun checkPerms(expected: Array<out Permissions>, actual: EnumSet<Permissions>) {
    if (actual.contains(Permissions.ADMINISTRATOR)) return // Has full bypass
    val lackingPerms = expected.filter { !actual.contains(it) }
    if (lackingPerms.isNotEmpty()) throw InsufficientPermissionsException(lackingPerms)
}

inline fun <reified T: Any> useSelfMemberRoles(guild: PartialGuild, crossinline then: (Member, List<Role>) -> CompletableFuture<T>): IRestAction<T> {
    val future = guild.bot.selfUser.request().thenCompose { guild.fetchMember(it.snowflake).request() }.thenCompose { member ->
        member.fetchRoles.request().thenCompose {
            then(member, it)
        }
    }
    val resNow = future.getNow(null)
    return resNow?.run { IRestAction.ProvidedRestAction(guild.bot, this) } ?: IRestAction.FuturesRestAction(guild.bot) { future }
}
