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

# Library Standards

## Delegated Properties

When the library parses the JSON for a type, it doesn't parse all of the entity fields at once ahead of time, instead keeping a map of all the keys to their unparsed values as the only data each entity keeps.

The fields under each entity will use `map.delegateJson`, that will create a getter method lazily delegating to the entity's map.

## Entity Mutability

If an entity has a respective "Modify" endpoint, it will be set to inherit `EntityManager<T>` where T is itself. This will save a mutable map of `changes` done to the entity.

The mutable (in the API sense) fields in the entity will have `map.delegateJsonMutable` instead of delegateJson, and the `var` assignment instead of `val`. This won't actually make it a mutable field (there is no underlying field), it merely makes a setter to alter the changes map.

When all the changes are applied, the `EntityManager#edit` function is called returning an action that updates the entity. Once Discord returns the edited entity map, the delegated map is changed to match it.

## Fetching and Partial Entities

Fetch methods should provide an immediate partial entity, that may be used to call the API with functions that only use the entity's ID (and other immediate data) to perform actions.

The partial entities can be upgraded through an API request for the final entity using `PartialEntity#upgrade`.

# Contributing

Feel free to contribute! We are looking to cover [the entire Discord API](https://discord.com/developers/docs/), as well as all of the JDA backwards support.
