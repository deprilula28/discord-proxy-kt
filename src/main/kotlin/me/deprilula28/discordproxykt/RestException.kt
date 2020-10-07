package me.deprilula28.discordproxykt

class RestException(val endpoint: String, val body: String, val errCode: Int): Exception(
    "Failed request at $endpoint ($errCode):\n$body")
