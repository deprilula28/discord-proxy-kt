package me.deprilula28.discordproxykt.entities

import kotlinx.serialization.json.JsonObject

class UnavailableField(val fieldName: String? = null, val available: JsonObject? = null):
    Exception("Field ${fieldName ?: ""} is not available under this context." + available?.run { " Available ones are: $this" })
