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

When the library parses the JSON for a type, it doesn't parse all the entity fields at once ahead of time, instead keeping a map of all the keys to their unparsed values as the only data each entity keeps.

The fields under each entity will use `parse` or `parseOpt`, that will create a getter method lazily delegating to the entity's map.

## Entity Mutability

If an entity has a respective "Modify" endpoint, it will be set to inherit `EntityManager<T>` where T is itself. This will save a mutable map of `changes` done to the entity.

The mutable (in the API sense) fields in the entity will have a `var` assignment instead of `val`, and a serialization parameter. This won't actually make it a mutable field (there is no underlying field), it merely makes a setter function that alters the changes map.

When all the changes are applied, the `EntityManager#edit` function is called returning an action that updates the entity. Once Discord returns the edited entity map, the delegated map is changed to match it.

## Fetching and Partial Entities

Fetch methods should provide an immediate partial entity, that may be used to call the API with functions that only use the entity's ID (and other immediate data) to perform actions.

The partial entities can be upgraded through an API request for the final entity using `PartialEntity#upgrade`.

## Commenting

The code should be able to explain itself. The function of comments should be to specify conditions, best practices and provide an easy link to Discord's documentation.

# Contributing

Feel free to contribute! We don't have any restrictions as of yet, just make sure your contribution falls under the scope of the project.