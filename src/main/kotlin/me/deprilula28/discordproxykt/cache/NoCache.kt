package me.deprilula28.discordproxykt.cache

class NoCache: Cache {
    override fun store(key: Any, value: Any): Boolean = false
    override fun <T: Any> retrieve(key: Any): T? = null
}