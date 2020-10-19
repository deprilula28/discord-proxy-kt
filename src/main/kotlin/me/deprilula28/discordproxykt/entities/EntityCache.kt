package me.deprilula28.discordproxykt.entities

class EntityCache<T>(private val internal: MutableList<T> = mutableListOf()): MutableList<T> by internal {

}
