The last Discord library (although, probably not)

# Warning

This project is a work in progress. We have barely anything working as of now. You can watch it though if you are interested! :^)

# Introduction

## Spectacles & Decoupling

By using the [Spectacles Gateway](https://github.com/spec-tacles/gateway), discord-proxy-kt can decouple the connection to Discord and handling shards. You can cheaply restart and create more workers that use the library, without needing to worry about IDENTIFY times and sharding, allowing for much more flexibility and scalability.

To communicate with Spectacles, [RabbitMQ](https://www.rabbitmq.com/) is used.

## Caching

A big concern when scaling is caching. This library is completely agnostic to your particular caching system, as we use a `Cache` interface that you can write yourself, or you can use one of the solutions we have implemented.

## JDA Migration

We are trying to keep the API similar to JDA, to allow for easy migration (when possible, it should just require re-importing everything). When we feel using JDA's way is worse, we will keep the cross-functionality functions and fields, but annotate them with `@Deprecated` to suggest our way.

## Kotlin Notes

While the library is written in Kotlin, and uses some of its language native features, we want to keep Java support in mind:
- `RestAction`: In Java, you can use `CompletableFuture` through `.request()`, and Kotlin coroutines through `.await()`.

# Contributing

Feel free to contribute! We are looking to cover [the entire Discord API](https://discord.com/developers/docs/), as well as all of the JDA backwards support.
