package entities

class Snowflake(val id: Long) {
    override fun toString(): String = id.toString()
}